import { useState, useEffect } from 'react';
import { View, Text, TextInput, StyleSheet, ScrollView, Pressable, Alert, Platform, useWindowDimensions} from 'react-native';
import * as SecureStore from 'expo-secure-store';
import { useRouter } from 'expo-router';

const API_URL = process.env.EXPO_PUBLIC_API_URL ?? (Platform.OS === 'android' ? 'http://10.0.2.2:8080': 'http://localhost:8080');
//the promise<string | null> means the function asynchronously returns either token or null
async function getToken(): Promise<string | null> {
    if(Platform.OS === 'web'){
        return localStorage.getItem('app_jwt');
    }
    return SecureStore.getItemAsync('app_jwt');
}

type Category = {
    id: number;
    name: string;
    is_active: boolean;
    createdByUserId: number | null;
};

export default function LandingScreen() {
    const router = useRouter();
//  useState stores values entered by user
    //allows layout to adjust automatically based on screen width
    const {width} = useWindowDimensions();
    const isSmallScreen = width < 600;
    //stores each category's ID with amount entered
    const [paycheckAmount, setPaycheckAmount] = useState('');
    const [savingsPercent, setSavingsPercent] = useState('');
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [allocations, setAllocations] = useState<Record<number, string>>({});
    const [saving, setSaving] = useState(false);
    const [checkingPaycheck, setCheckingPaycheck] = useState(false);

    //retrieves active categories from backend
    async function fetchCategories() {
        try {
            //retrieves users authentication token
            const token = await getToken();

            if (!token) {
                setError('You must be signed in.');
                return;
            }
            //fetch sends request to backend
            const response = await fetch(
                `${API_URL}/api/v1/categories?activeOnly=true&size=100`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error('Failed to retrieve categories.');
            }

            const data = await response.json();
            //stores categories retrieved from data.content which accesses categories inside paginated response
            setCategories(data.content);

        } catch (err) {
            setError('Unable to load categories.');
        } finally {
            setLoading(false);
        }
    }

    //useEffect runs the request when the screen loads
    useEffect(() => {
        fetchCategories();
    }, []);

    //updates amount for specific category w/out overwriting amounts entered for other ones
    function updateAllocation(categoryId: number, amount: string){
        setAllocations(previous => ({
            ...previous, [categoryId]: amount
        }));
    }

    function validateInputs(): boolean {
        const paycheck = Number(paycheckAmount);
        const savings = Number(savingsPercent);

        const savingsAmount = Math.round(paycheck * savings) / 100;
        const availableAmount = paycheck - savingsAmount;

        if (paycheckAmount.trim() === '') {
            setError('Please enter your paycheck amount.');
            return false;
        }

        //only allows numbers and optional decimal point with up to 2 decimal points
        if (!/^\d+(\.\d{1,2})?$/.test(paycheckAmount.trim())) {
            setError('Please enter a valid paycheck amount.');
            return false;
        }

        if (Number(paycheckAmount) > 9999999999.99) {
            setError('Paycheck amount is too large.');
            return false;
        }

        if (savingsPercent.trim() === '') {
            setError('Please enter your savings percentage.');
            return false;
        }

        if (
            !/^\d+(\.\d+)?$/.test(savingsPercent.trim()) ||
            Number(savingsPercent) > 100
        ) {
            setError('Savings percentage must be between 0 and 100.');
            return false;
        }

        const budgetCategories = categories.filter(
            category => category.name.toLowerCase() !== 'savings'
        );

        for (const category of budgetCategories) {
            const amount = allocations[category.id]?.trim() ?? '';

            if (amount === '') {
                setError(`Please enter an amount for ${category.name}.`);
                return false;
            }

            if (
                !/^\d+(\.\d{1,2})?$/.test(amount) ||
                Number(amount) > 9999999999.99
            ) {
                setError(`Please enter a valid amount for ${category.name}.`);
                return false;
            }
        }

        const totalAllocations = budgetCategories.reduce(
            (total, category) =>
                total + Math.round(Number(allocations[category.id]) * 100),
            0
        ) / 100;

        if (Math.round(totalAllocations * 100) > Math.round(availableAmount * 100)) {
            setError(
                `Your total budget allocations cannot exceed ` +
                `$${availableAmount.toFixed(2)} after savings.`
            );
            return false;
        }
        return true;
    }

    async function handleSeeFinancials() {
        setError('');

        if (saving || checkingPaycheck || loading || categories.length === 0){
            return;
        }
        if (!validateInputs()) {
            return;
        }
        setSaving(true);

        try{
            const token = await getToken();
            if (!token) {
                setError('You must be signed in.');
                return;
            }
            //this excludes savings from the allocations
            const budgetCategories = categories.filter(
                category => category.name.toLowerCase() !== 'savings'
            );

            const requestBody = {
                paycheck: {
                    paycheckAmount: Number(paycheckAmount),
                    savingsPercent: Number(savingsPercent)
                },
                financials: budgetCategories.map(category => ({
                    categoryId: category.id,
                    setAmount: Number(allocations[category.id])
                }))
            };

            const response = await fetch(
                `${API_URL}/api/v1/user/me/budget`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${token}`
                    },
                    body: JSON.stringify(requestBody)
                }
            );

            if (response.status === 401) {
                setError('Your session has expired. Please sign in again.');
                return;
            }
            if (response.status === 400) {
                setError('Invalid budget information. Please check your inputs.');
                return;
            }

            if (response.status === 404) {
                setError('A required record could not be found.');
                return;
            }
            if (!response.ok) {
                setError('Unable to save budget. Please try again.');
                return;
            }
            console.log('Budget saved successfully.');
            router.replace('/home');

        }catch(err){
            setError('Unable to connect to server. Please try again.');
        } finally{
            setSaving(false);
        }

    }

    async function handleUseLastPaycheck() {
        setError('');

        if (saving || checkingPaycheck) {
            return;
        }

        setCheckingPaycheck(true);

        try {
            const token = await getToken();

            if (!token) {
                setError('You must be signed in.');
                return;
            }

            const response = await fetch(
                `${API_URL}/api/v1/user/me/paycheck`,
                {
                    method: 'GET',
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            if (response.status === 401) {
                setError('Your session has expired. Please sign in again.');
                return;
            }

            if (response.status === 404) {
                const message = "You don't have a saved paycheck yet. " +
                                "Please enter your paycheck information and " +
                                "click See Financials to continue.";

                if (Platform.OS === 'web') {
                    window.alert(message);
                } else {
                    Alert.alert('No Previous Paycheck', message);
                }
                return;
            }

            if (!response.ok) {
                setError('Unable to retrieve previous paycheck.');
                return;
            }
            console.log('Previous paycheck found.');
            router.replace('/home');

        } catch (err) {
            setError('Unable to connect to server. Please try again.');
        } finally {
            setCheckingPaycheck(false);
        }
    }

    return (
        <ScrollView
            style={styles.screen}
            contentContainerStyle={[styles.container, isSmallScreen && styles.mobileContainer]}
            keyboardShouldPersistTaps="handled"
        >
            <View style={[styles.card, isSmallScreen && styles.mobileCard]}>
            <View style={styles.header}>
                <Text style={styles.title}>Budget Tracker</Text>
                <Text style={styles.subtitle}>Let's plan your budget.</Text>
                <Text style={styles.description}>
                    Enter your paycheck information and allocate your monthly expenses.
                </Text>
            </View>

            <View style={styles.section}>
                <Text style={styles.sectionTitle}>Paycheck Information</Text>
                <Text style={styles.label}>Paycheck Amount ($)</Text>

                <TextInput
                    style={styles.input}
                    placeholder="Enter paycheck amount"
                    placeholderTextColor="#64748B"
                    keyboardType="decimal-pad"
                    value={paycheckAmount}
                    onChangeText={setPaycheckAmount}
                />

                <Text style={styles.label}>Savings Percentage (%)</Text>

                <TextInput
                    style={styles.input}
                    placeholder="Enter savings percentage"
                    placeholderTextColor="#64748B"
                    keyboardType="decimal-pad"
                    value={savingsPercent}
                    onChangeText={setSavingsPercent}
                />
            </View>

            <View style={styles.section}>
                <Text style={styles.sectionTitle}>
                    Monthly Budget Allocation
                </Text>

                <Text style={styles.sectionDescription}>
                    Allocate your available funds to each spending category.
                </Text>

                {loading && <Text>Loading categories...</Text>}
                {categories
                    .filter(category => category.name.toLowerCase() !== 'savings')
                    .map(category => (
                        <View key={category.id}>
                            <Text style={styles.label}>{category.name} ($)</Text>
                            <TextInput
                                style={styles.input}
                                placeholder={`Enter ${category.name.toLowerCase()} amount`}
                                placeholderTextColor="#64748B"
                                keyboardType="decimal-pad"
                                value={allocations[category.id] ?? ''}
                                onChangeText={amount => updateAllocation(category.id, amount)}
                            />
                        </View>
                    ))
                }
            </View>

            {error !== '' && <Text style={styles.error}>{error}</Text>}
            <View style={styles.buttonContainer}>
                <Pressable
                    style={[styles.primaryButton, (loading || saving || checkingPaycheck || categories.length === 0)
                        && styles.disabledButton]}
                    onPress={handleSeeFinancials}
                    disabled={loading || saving || checkingPaycheck || categories.length === 0}
                    accessibilityRole="button"
                >
                    <Text style={styles.primaryButtonText}>
                        {saving ? 'Saving...': 'See Financials'}
                    </Text>
                </Pressable>

                <Pressable
                    style={[styles.secondaryButton, (saving || checkingPaycheck) && styles.disabledButton]}
                    onPress={handleUseLastPaycheck}
                    disabled={saving || checkingPaycheck}
                    accessibilityRole="button"
                >
                    <Text style={styles.secondaryButtonText}>
                        {checkingPaycheck ? 'Checking...': 'Use Last Paycheck'}
                    </Text>
                </Pressable>
            </View>
        </View>
    </ScrollView>
    );
}

const styles = StyleSheet.create({
    screen:{
        flex: 1,
        backgroundColor: '#F1F5F9',
    },
    container: {
        flexGrow: 1,
        padding: 20,
        justifyContent: 'center',
        alignItems: 'center',
    },
    card: {
        width: '100%',
        maxWidth: 550,
        backgroundColor: '#FFFFFF',
        borderRadius: 20,
        padding: 25,
        borderWidth: 1,
        borderColor: '#E2E8F0'
    },
    header: {
        marginBottom: 25,
    },
    title: {
        fontSize: 30,
        fontWeight: 'bold',
        color: '#172E29',
        marginBottom: 10,
    },
    subtitle: {
        fontSize: 20,
        fontWeight: '600',
        color: '#256B53',
        marginBottom: 10,
    },
    description: {
        fontSize: 15,
        color: '#64748B',
        lineHeight: 20,
    },
    label: {
        fontSize: 15,
        fontWeight: '500',
        color: '#334155',
        marginTop: 15,
        marginBottom: 10,
    },
    input: {
        backgroundColor: '#F8FAFC',
        borderWidth: 1,
        borderColor: '#CBD5E1',
        borderRadius: 10,
        paddingHorizontal: 15,
        paddingVertical: 10,
        fontSize: 15,
        color: '#172E29',
        minHeight: 48,
    },
    section: {
        borderTopWidth: 1,
        borderTopColor: '#E2E8F0',
        paddingTop: 20,
        marginBottom: 25,
    },
    sectionTitle: {
        fontSize: 20,
        fontWeight: '600',
        color: '#172E29',
        marginBottom: 10,
    },
    sectionDescription: {
        fontSize: 15,
        color: '#64748B',
        lineHeight: 20,
        marginBottom: 10,
    },
    error: {
        color: 'red',
        marginTop: 10,
    },
    buttonContainer: {
        gap: 10,
        marginTop: 15,
    },
    primaryButton: {
        backgroundColor: '#256B53',
        borderRadius: 10,
        paddingVertical: 15,
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: 50,
    },
    primaryButtonText: {
        color: '#FFFFFF',
        fontSize: 16,
        fontWeight: '600',
    },
    secondaryButton: {
        backgroundColor: '#FFFFFF',
        borderWidth: 1,
        borderColor: '#256B53',
        borderRadius: 10,
        paddingVertical: 15,
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: 50,
    },
    secondaryButtonText: {
        color: '#256B53',
        fontSize: 15,
        fontWeight: '600',
    },
    disabledButton: {
        opacity: 0.5,
    },
    mobileContainer: {
        padding: 10,
        justifyContent: 'flex-start',
    },
    mobileCard: {
        padding: 15,
        borderRadius: 15,
    },
});