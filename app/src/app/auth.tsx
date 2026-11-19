import { useEffect, useState } from "react";
import {
  Platform,
  StyleSheet,
  Text,
  View,
} from "react-native";
import {
  useLocalSearchParams,
  useRouter,
} from "expo-router";
import { makeRedirectUri } from "expo-auth-session";
import * as SecureStore from "expo-secure-store";

const apiUrl =
  process.env.EXPO_PUBLIC_API_URL ??
  (Platform.OS === "android"
    ? "http://10.0.2.2:8080"
    : "http://localhost:8080");

type AuthProvider = "github" | "google";

function getSingleParam(
  value: string | string[] | undefined
): string | undefined {
  // return the first value when expo provides a repeated query parameter.
  if (Array.isArray(value)) {
    return value[0];
  }

  // return the query parameter when expo provides one value.
  return value;
}

async function getCodeVerifier(): Promise<string | null> {
  // read the pkce verifier saved before the provider login started.
  if (Platform.OS === "web") {
    return sessionStorage.getItem("oauth_code_verifier");
  }

  return SecureStore.getItemAsync("oauth_code_verifier");
}

async function getOAuthProvider(): Promise<AuthProvider | null> {
  // read the provider saved before the provider login started.
  const provider =
    Platform.OS === "web"
      ? sessionStorage.getItem("oauth_provider")
      : await SecureStore.getItemAsync("oauth_provider");

  // reject unknown provider values before constructing the api request.
  if (provider !== "github" && provider !== "google") {
    return null;
  }

  return provider;
}

async function saveToken(token: string) {
  // save the application's jwt for authenticated api requests.
  if (Platform.OS === "web") {
    localStorage.setItem("app_jwt", token);
  } else {
    await SecureStore.setItemAsync("app_jwt", token);
  }
}

async function saveUser(user: object) {
  // save the signed-in user's information for the home screen.
  const userJson = JSON.stringify(user);

  if (Platform.OS === "web") {
    localStorage.setItem("app_user", userJson);
  } else {
    await SecureStore.setItemAsync("app_user", userJson);
  }
}

async function removeOAuthState() {
  // remove the temporary oauth values after the api finishes the login.
  if (Platform.OS === "web") {
    sessionStorage.removeItem("oauth_code_verifier");
    sessionStorage.removeItem("oauth_provider");
  } else {
    await SecureStore.deleteItemAsync("oauth_code_verifier");
    await SecureStore.deleteItemAsync("oauth_provider");
  }
}

export default function AuthRedirectScreen() {
  const params = useLocalSearchParams<{
    code?: string | string[];
    error?: string | string[];
  }>();

  const router = useRouter();
  const [message, setMessage] = useState("Signing you in...");

  useEffect(() => {
    const finishLogin = async () => {
      // read the provider response from the redirect query parameters.
      const code = getSingleParam(params.code);
      const providerError = getSingleParam(params.error);

      // stop when the provider returned an authorization error.
      if (providerError) {
        setMessage("The sign-in was cancelled or rejected.");
        return;
      }

      // stop when the provider did not return an authorization code.
      if (!code) {
        setMessage("No authorization code was received.");
        return;
      }

      // read the provider and pkce verifier saved before redirecting.
      const provider = await getOAuthProvider();
      const codeVerifier = await getCodeVerifier();

      // stop when the temporary oauth state is missing.
      if (!provider || !codeVerifier) {
        setMessage(
          "The sign-in information is missing. Please try again."
        );
        return;
      }

      // recreate the same redirect uri used to start the oauth request.
      const redirectUri = makeRedirectUri({
        path: "auth",
      });

      // choose the backend endpoint that matches the provider.
      const endpoint =
        provider === "google"
          ? `${apiUrl}/auth/google`
          : `${apiUrl}/auth/github`;

      try {
        // send the authorization code to the api for provider verification.
        const response = await fetch(endpoint, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            code,
            codeVerifier,
            redirectUri,
          }),
        });

        // read the response text so non-json errors do not crash the screen.
        const responseText = await response.text();

        // show the api error when the provider exchange fails.
        if (!response.ok) {
          setMessage(
            responseText || "The sign-in request was rejected."
          );
          return;
        }

        // convert the successful api response into javascript data.
        const data = JSON.parse(responseText);

        // save the application's jwt and user returned by the api.
        await saveToken(data.token);
        await saveUser(data.user);

        // remove the temporary values that were only needed for this login.
        await removeOAuthState();

        // return to the application after the login is complete.
        router.replace("/landing");
      } catch {
        // show a safe message when the api cannot be reached.
        setMessage(
          "Could not contact the api. Make sure the backend is running."
        );
      }
    };

    finishLogin();
  }, [params.code, params.error, router]);

  return (
    <View style={styles.container}>
      <Text style={styles.message}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
  },
  message: {
    textAlign: "center",
  },
});