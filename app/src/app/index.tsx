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

// complete oauth popups when the provider redirects back to the app.
WebBrowser.maybeCompleteAuthSession();

const redirectUri = makeRedirectUri({
  path: "auth",
});

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

type SavedUser = {
  id: number;
  email: string;
  displayName: string;
  role: string;
};

async function saveCodeVerifier(verifier: string) {
  // store the pkce verifier until the provider redirects back to the app.
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
  // store the provider so the callback route knows which api endpoint to use.
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

async function getSavedUser(): Promise<SavedUser | null> {
  // retrieve the saved user from the platform's local storage.
  const userJson =
    Platform.OS === "web"
      ? localStorage.getItem("app_user")
      : await SecureStore.getItemAsync("app_user");

  // convert the stored json text back into a user object.
  return userJson ? JSON.parse(userJson) : null;
}

async function clearSession() {
  // remove the application session from the current platform.
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

export default function HomeScreen() {
  const router = useRouter();

  const [user, setUser] =
    useState<SavedUser | null>(null);

  const [message, setMessage] = useState("");

  // load the saved user when the home screen opens.
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
    // read the authorization code returned by either oauth popup.
    const githubCode =
      githubResponse?.type === "success"
        ? githubResponse.params?.code
        : undefined;

    const googleCode =
      googleResponse?.type === "success"
        ? googleResponse.params?.code
        : undefined;

    // use whichever provider returned a successful authorization code.
    const code = githubCode ?? googleCode;

    // wait until a provider returns a successful authorization code.
    if (!code) {
      return;
    }

    // send the authorization code to the callback screen.
    router.replace({
      pathname: "/auth",
      params: { code },
    });
  }, [githubResponse, googleResponse, router]);

  const signInWithGitHub = async () => {
    // wait until expo creates the github authorization request.
    if (!githubRequest) {
      setMessage("GitHub login is not ready yet.");
      return;
    }

    // save the verifier needed by the api to finish the pkce exchange.
    if (githubRequest.codeVerifier) {
      await saveCodeVerifier(
        githubRequest.codeVerifier
      );
    }

    // tell the callback route that this request belongs to github.
    await saveOAuthProvider("github");

    // open github's authorization page.
    const result = await githubPromptAsync();

    // show a message when github rejects or cancels the login.
    if (result.type === "error") {
      setMessage(
        "GitHub sign-in was cancelled or rejected."
      );
    }
  };

  const signInWithGoogleWeb = async () => {
    // only use this authorization-code flow in the web browser.
    if (Platform.OS !== "web") {
      setMessage(
        "Native Google sign-in will be added next."
      );
      return;
    }

    // wait until expo creates the google authorization request.
    if (!googleRequest) {
      setMessage("Google login is not ready yet.");
      return;
    }

    // save the verifier needed by the api to finish the pkce exchange.
    if (googleRequest.codeVerifier) {
      await saveCodeVerifier(
        googleRequest.codeVerifier
      );
    }

    // tell the callback route that this request belongs to google.
    await saveOAuthProvider("google");

    // open google's authorization page.
    const result = await googlePromptAsync();

    // show a message when google rejects or cancels the login.
    if (result.type === "error") {
      setMessage(
        "Google sign-in was cancelled or rejected."
      );
    }
  };

  const signOut = async () => {
    // remove the saved jwt and user information.
    await clearSession();

    // update the screen after signing out.
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

          {Platform.OS === "web" && (
            <Button
              title="Sign in with Google"
              disabled={!googleRequest}
              onPress={signInWithGoogleWeb}
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