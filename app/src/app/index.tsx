import { useEffect, useState } from "react";
import {
  Button,
  Platform,
  StyleSheet,
  Text,
  View,
} from "react-native";
import * as WebBrowser from "expo-web-browser";
import {
  makeRedirectUri,
  useAuthRequest,
} from "expo-auth-session";
import * as SecureStore from "expo-secure-store";

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

type SavedUser = {
  id: number;
  email: string;
  displayName: string;
  role: string;
};

async function saveCodeVerifier(verifier: string) {
  if (Platform.OS === "web") {
    sessionStorage.setItem(
      "github_code_verifier",
      verifier
    );
  } else {
    await SecureStore.setItemAsync(
      "github_code_verifier",
      verifier
    );
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
  } else {
    await SecureStore.deleteItemAsync("app_jwt");
    await SecureStore.deleteItemAsync("app_user");
  }
}

export default function HomeScreen() {
  const [user, setUser] =
    useState<SavedUser | null>(null);

  const [message, setMessage] = useState("");

  useEffect(() => {
    getSavedUser().then(setUser);
  }, []);

  const [request, , promptAsync] = useAuthRequest(
    {
      clientId: githubClientId ?? "",
      scopes: ["read:user", "user:email"],
      redirectUri,
      usePKCE: true,
    },
    githubDiscovery
  );

  const signInWithGitHub = async () => {
    if (!request) {
      setMessage("GitHub login is not ready yet.");
      return;
    }

    if (request.codeVerifier) {
      await saveCodeVerifier(request.codeVerifier);
    }

    await promptAsync();
  };

  const signOut = async () => {
    await clearSession();
    setUser(null);
    setMessage("You have been signed out.");
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Budget Tracker</Text>

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
        <Button
          title="Sign in with GitHub"
          disabled={!request}
          onPress={signInWithGitHub}
        />
      )}

      {message !== "" && (
        <Text style={styles.message}>{message}</Text>
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