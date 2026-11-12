import { useEffect, useState } from "react";
import {
  Platform,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";
import { makeRedirectUri } from "expo-auth-session";
import * as SecureStore from "expo-secure-store";

const apiUrl =
  process.env.EXPO_PUBLIC_API_URL ??
  "http://localhost:8080";

async function getCodeVerifier() {
  if (Platform.OS === "web") {
    return sessionStorage.getItem(
      "github_code_verifier"
    );
  }

  return SecureStore.getItemAsync(
    "github_code_verifier"
  );
}

async function saveToken(token: string) {
  if (Platform.OS === "web") {
    localStorage.setItem("app_jwt", token);
  } else {
    await SecureStore.setItemAsync("app_jwt", token);
  }
}

async function saveUser(user: object) {
  const userJson = JSON.stringify(user);

  if (Platform.OS === "web") {
    localStorage.setItem("app_user", userJson);
  } else {
    await SecureStore.setItemAsync("app_user", userJson);
  }
}

async function removeCodeVerifier() {
  if (Platform.OS === "web") {
    sessionStorage.removeItem(
      "github_code_verifier"
    );
  } else {
    await SecureStore.deleteItemAsync(
      "github_code_verifier"
    );
  }
}

export default function AuthRedirectScreen() {
  const params = useLocalSearchParams();
  const router = useRouter();

  const [message, setMessage] =
    useState("Signing you in...");

  useEffect(() => {
    const finishLogin = async () => {
      const code = Array.isArray(params.code)
        ? params.code[0]
        : params.code;

      if (!code) {
        setMessage(
          "No GitHub authorization code was received."
        );
        return;
      }

      const codeVerifier = await getCodeVerifier();

      if (!codeVerifier) {
        setMessage(
          "The login verifier is missing. Please try again."
        );
        return;
      }

      const redirectUri = makeRedirectUri({
        path: "auth",
      });

      try {
        const result = await fetch(
          `${apiUrl}/auth/github`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              code,
              codeVerifier,
              redirectUri,
            }),
          }
        );

        if (!result.ok) {
          const errorText = await result.text();
          throw new Error(
            errorText || "The API rejected the login."
          );
        }

        const data = await result.json();

        await saveToken(data.token);
        await saveUser(data.user);
        await removeCodeVerifier();

        router.replace("/");
      } catch (error) {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "Sign-in failed.";

        setMessage(`Sign-in failed: ${errorMessage}`);
      }
    };

    finishLogin();
  }, [params.code, router]);

  return (
    <View style={styles.container}>
      <Text>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
});