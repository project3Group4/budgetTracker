import { useEffect, useState } from "react";
import { Button, Platform, StyleSheet, ScrollView, Text, View } from "react-native";
import { useRouter } from "expo-router";

import * as SecureStore from "expo-secure-store";

type User = {
    id: number;
    email: string;
    displayName: string;
    role: string;
};

// This is a function from Ann's original sign out button
async function clearSession() {
    if (Platform.OS === "web") {
        localStorage.removeItem("app_jwt");
        localStorage.removeItem("app_user");
    } else {
        await SecureStore.deleteItemAsync("app_jwt");
        await SecureStore.deleteItemAsync("app_user");
    }
}

async function getUser(): Promise<User | null> {
  const userJson =
    Platform.OS === "web"
      ? localStorage.getItem("app_user")
      : await SecureStore.getItemAsync("app_user");

  return userJson ? JSON.parse(userJson) : null;
}

async function getToken(): Promise<string | null> {
    if (Platform.OS === "web") {
        return localStorage.getItem("app_jwt");
    }
    return SecureStore.getItemAsync("app_jwt");
}

export default function HomePage() {
    const router = useRouter();
    const [user, setUser] = useState<User | null>(null);
    const [checkAuthorization, setCheckAuthorization] = useState(true);

    // This should check to make sure the user is logged in and redirect them to the sign in if not
    useEffect(() => {
        async function checkAuth() {
            const token = await getToken();
            if(!token){
                router.replace('/');
                return;
            }
        const savedUser = await getUser();
        setUser(savedUser);
        setCheckAuthorization(false);
        }
        checkAuth();
    }, []);

    // This is a function from Ann's original sign out button
    const signOut = async () => {
        await clearSession();
        setUser(null);
        router.replace('/');
    };

    // This brings the user to the profile screen when one is made
    const goToProfile = async () => {
        await clearSession();
        //router.replace('/pageName');
    };

    return(
        <ScrollView style={styles.screen} contentContainerStyle={[styles.container]} keyboardShouldPersistTaps="handled">
            {user && (<Text> Welcome {user.displayName}!</Text>)}
            <Button title = "Sign Out" onPress = {signOut}/>
            <Button title = "Edit Profile" onPress = {goToProfile}/>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flexGrow: 1,
        padding: 20,
        justifyContent: 'center',
        alignItems: 'center',
    },
    screen:{
        backgroundColor: '#F1F5F9',
        flex: 1,
    },
});