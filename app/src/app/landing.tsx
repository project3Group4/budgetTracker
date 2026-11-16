import { useState, useEffect } from 'react';
import { View, Text, TextInput, StyleSheet, ScrollView, Button, Alert, Platform } from 'react-native';

const API_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8080';

type Category = {
    id: number;
    name: string;
    is_active: boolean;
    createdByUserId: number | null;
};

export default function LandingScreen() {
//  useState stores values entered by user
    //stores each category's ID with amount entered
    const [paycheckAmount, setPaycheckAmount] = useState('');
    const [savingsPercent, setSavingsPercent] = useState('');
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [allocations, setAllocations] = useState<Record<number, string>>({});
    const [savings, setSaving] = useState(false);
    const [checkingPaycheck, setCheckingPaycheck] = useState(false);


    async function fetchCategories() {
        try {
            //retrieves users authentication token
            const token = localStorage.getItem('app_jwt');

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
            const token = localStorage.getItem('app_jwt');
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
            const token = localStorage.getItem('app_jwt');

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

        } catch (err) {
            setError('Unable to connect to server. Please try again.');
        } finally {
            setCheckingPaycheck(false);
        }
    }

    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.title}>Budget Tracker</Text>
            <Text>Enter your paycheck information</Text>

            <Text style={styles.label}>Paycheck Amount ($)</Text>
            <TextInput
                style={styles.input}
                placeholder="Enter paycheck amount"
                keyboardType="decimal-pad"
                value={paycheckAmount}
                onChangeText={setPaycheckAmount}
            />

            <Text style={styles.label}>Savings Percentage (%)</Text>
            <TextInput
                style={styles.input}
                placeholder="Enter savings percentage"
                keyboardType="decimal-pad"
                value={savingsPercent}
                onChangeText={setSavingsPercent}
            />

            <Text style={styles.sectionTitle}>Monthly Budget Allocation</Text>

            {loading && <Text>Loading categories...</Text>}
            {categories
                .filter(category => category.name.toLowerCase() !== 'savings')
                .map(category => (
                    <View key={category.id}>
                        <Text style={styles.label}>{category.name} ($)</Text>
                        <TextInput
                            style={styles.input}
                            placeholder={`Enter ${category.name.toLowerCase()} amount`}
                            keyboardType="decimal-pad"
                            value={allocations[category.id] ?? ''}
                            onChangeText={amount => updateAllocation(category.id, amount)}
                        />
                    </View>
                ))
            }

        {error !== '' && <Text style={styles.error}>{error}</Text>}
        <Button
            title={saving ? 'Saving...' : 'See Financials'}
            onPress={handleSeeFinancials}
            disabled={loading || saving || categories.length === 0}
        />
        <Button
            title={checkingPaycheck ? 'Checking...': 'Use Last Paycheck'}
            onPress={handleUseLastPaycheck}
            disabled={saving || checkingPaycheck}
        />
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flexGrow: 1,
        padding: 20,
        justifyContent: 'center',
    },
    title: {
        fontSize: 30,
        fontWeight: 'bold',
        marginBottom: 20,
    },
    label: {
        fontSize: 15,
        marginTop: 20,
        marginBottom: 5,
    },
    input: {
        borderWidth: 1,
        borderColor: 'gray',
        borderRadius: 10,
        padding: 10,
        fontSize: 15,
    },
    sectionTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        marginTop: 30,
        marginBottom: 10,
    },
    error: {
        color: 'red',
        marginTop: 10,
    },
});