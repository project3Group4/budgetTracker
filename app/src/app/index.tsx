import { useEffect, useState } from "react";
import {
  Button,
  Platform,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useRouter } from "expo-router";
import * as WebBrowser from "expo-web-browser";
import {
  makeRedirectUri,
  useAuthRequest,
  useAutoDiscovery,
} from "expo-auth-session";
import * as SecureStore from "expo-secure-store";
import { GoogleSignin } from "@react-native-google-signin/google-signin";

WebBrowser.maybeCompleteAuthSession();

const redirectUri = makeRedirectUri({
  path: "auth",
});

const apiUrl =
  Platform.OS === "android"
    ? process.env.EXPO_PUBLIC_ANDROID_API_URL ??
      "http://10.0.2.2:8080"
    : process.env.EXPO_PUBLIC_API_URL ??
      "http://localhost:8080";

const githubDiscovery = {
  authorizationEndpoint:
    "https://github.com/login/oauth/authorize",
  tokenEndpoint:
    "https://github.com/login/oauth/access_token",
};

const githubClientId =
  process.env.EXPO_PUBLIC_GITHUB_CLIENT_ID;

const googleClientId =
  process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID;

if (Platform.OS === "android" && googleClientId) {
  GoogleSignin.configure({
    webClientId: googleClientId,
  });
}

type SavedUser = {
  id: number;
  email: string;
  displayName: string;
  role: string;
};

async function saveCodeVerifier(verifier: string) {
  if (Platform.OS === "web") {
    sessionStorage.setItem(
      "oauth_code_verifier",
      verifier
    );
  } else {
    await SecureStore.setItemAsync(
      "oauth_code_verifier",
      verifier
    );
  }
}

async function saveOAuthProvider(provider: string) {
  if (Platform.OS === "web") {
    sessionStorage.setItem(
      "oauth_provider",
      provider
    );
  } else {
    await SecureStore.setItemAsync(
      "oauth_provider",
      provider
    );
  }
}

async function saveToken(token: string) {
  if (Platform.OS === "web") {
    localStorage.setItem("app_jwt", token);
  } else {
    await SecureStore.setItemAsync("app_jwt", token);
  }
}

async function saveUser(user: SavedUser) {
  const userJson = JSON.stringify(user);

  if (Platform.OS === "web") {
    localStorage.setItem("app_user", userJson);
  } else {
    await SecureStore.setItemAsync("app_user", userJson);
  }
}

async function getSavedUser(): Promise<SavedUser | null> {
  const userJson =
    Platform.OS === "web"
      ? localStorage.getItem("app_user")
      : await SecureStore.getItemAsync("app_user");

  return userJson ? JSON.parse(userJson) : null;
}

async function clearSession() {
  if (Platform.OS === "web") {
    localStorage.removeItem("app_jwt");
    localStorage.removeItem("app_user");
    sessionStorage.removeItem("oauth_code_verifier");
    sessionStorage.removeItem("oauth_provider");
  } else {
    await SecureStore.deleteItemAsync("app_jwt");
    await SecureStore.deleteItemAsync("app_user");
    await SecureStore.deleteItemAsync("oauth_code_verifier");
    await SecureStore.deleteItemAsync("oauth_provider");
  }
}

function getApiError(responseText: string) {
  try {
    const errorData = JSON.parse(responseText);

    return (
      errorData.detail ??
      errorData.message ??
      "Google sign-in failed."
    );
  } catch {
    return responseText || "Google sign-in failed.";
  }
}

export default function HomeScreen() {
  const router = useRouter();

  const [user, setUser] =
    useState<SavedUser | null>(null);

  const [message, setMessage] = useState("");

  useEffect(() => {
    getSavedUser().then(setUser);
  }, []);

  const [
    githubRequest,
    githubResponse,
    githubPromptAsync,
  ] = useAuthRequest(
    {
      clientId: githubClientId ?? "",
      scopes: ["read:user", "user:email"],
      redirectUri,
      usePKCE: true,
    },
    githubDiscovery
  );

  const googleDiscovery = useAutoDiscovery(
    "https://accounts.google.com"
  );

  const [
    googleRequest,
    googleResponse,
    googlePromptAsync,
  ] = useAuthRequest(
    {
      clientId: googleClientId ?? "",
      scopes: ["openid", "email", "profile"],
      redirectUri,
      usePKCE: true,
    },
    googleDiscovery
  );

  useEffect(() => {
    if (
      githubResponse?.type === "success" &&
      githubResponse.params.code
    ) {
      router.replace(
        `/auth?code=${encodeURIComponent(
          githubResponse.params.code
        )}`
      );
    }
  }, [githubResponse, router]);

  useEffect(() => {
    if (
      googleResponse?.type === "success" &&
      googleResponse.params.code
    ) {
      router.replace(
        `/auth?code=${encodeURIComponent(
          googleResponse.params.code
        )}`
      );
    }
  }, [googleResponse, router]);

  const signInWithGitHub = async () => {
    if (!githubRequest) {
      setMessage("GitHub login is not ready yet.");
      return;
    }

    if (githubRequest.codeVerifier) {
      await saveCodeVerifier(
        githubRequest.codeVerifier
      );
    }

    await saveOAuthProvider("github");

    const result = await githubPromptAsync();

    if (result.type !== "success") {
      setMessage(
        "GitHub sign-in was cancelled or rejected."
      );
    }
  };

  const signInWithGoogleWeb = async () => {
    if (!googleRequest) {
      setMessage("Google login is not ready yet.");
      return;
    }

    if (googleRequest.codeVerifier) {
      await saveCodeVerifier(
        googleRequest.codeVerifier
      );
    }

    await saveOAuthProvider("google");

    const result = await googlePromptAsync();

    if (result.type !== "success") {
      setMessage(
        "Google sign-in was cancelled or rejected."
      );
    }
  };

  const signInWithGoogleAndroid = async () => {
    if (Platform.OS !== "android") {
      return;
    }

    if (!googleClientId) {
      setMessage("Google client ID is missing.");
      return;
    }

    try {
      await GoogleSignin.hasPlayServices({
        showPlayServicesUpdateDialog: true,
      });

      const result = await GoogleSignin.signIn();

      if (result.type !== "success") {
        setMessage("Google sign-in was cancelled.");
        return;
      }

      const idToken = result.data.idToken;

      if (!idToken) {
        setMessage(
          "Google did not return an ID token."
        );
        return;
      }

      const response = await fetch(
        `${apiUrl}/auth/google`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            idToken,
          }),
        }
      );

      const responseText = await response.text();

      if (!response.ok) {
        setMessage(getApiError(responseText));
        return;
      }

      const data = JSON.parse(responseText);

      await saveToken(data.token);
      await saveUser(data.user);

      setUser(data.user);
      setMessage("Signed in with Google.");
    } catch {
      setMessage(
        "Could not complete Google sign-in."
      );
    }
  };

  const signOut = async () => {
    await clearSession();

    if (Platform.OS === "android") {
      await GoogleSignin.signOut();
    }

    setUser(null);
    setMessage("You have been signed out.");
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>
        Budget Tracker
      </Text>

      {user ? (
        <>
          <Text style={styles.message}>
            Signed in as {user.displayName}
          </Text>

          <Button
            title="Sign out"
            onPress={signOut}
          />
        </>
      ) : (
        <>
          <Button
            title="Sign in with GitHub"
            disabled={!githubRequest}
            onPress={signInWithGitHub}
          />

          {Platform.OS === "web" ? (
            <Button
              title="Sign in with Google"
              disabled={!googleRequest}
              onPress={signInWithGoogleWeb}
            />
          ) : (
            <Button
              title="Sign in with Google"
              onPress={signInWithGoogleAndroid}
            />
          )}
        </>
      )}

      {message !== "" && (
        <Text style={styles.message}>
          {message}
        </Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    gap: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: "bold",
  },
  message: {
    marginTop: 20,
  },
});