import { Paper, Button, Text, Title, Stack, Kbd, Alert, Center, Loader } from '@mantine/core';
import { useWeb3React } from 'src/utils/web3-react';
import { stargazerConnector } from 'src/common/consts';
import { useState, useEffect } from 'react';
import { AlertCircle } from 'tabler-icons-react';

export const WalletConnect = () => {
    const { activate, account, deactivate } = useWeb3React();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const doActivate = async () => {
        setLoading(true);
        try {
            await activate(stargazerConnector, undefined, true);
        } catch (e) {
            if (e instanceof Error && /providers are not available/i.test(e.message)) {
                setError('Seems Stargazer Wallet is not installed or available');
            } else if (e instanceof Error && /Provider is was not activated/i.test(e.message)) {
                console.error('Provider was not activated, logging out.');
                deactivate();
            } else {
                console.error(e);
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const handleProviderError = (e: ErrorEvent) => {
            if (e.error instanceof Error && /Provider is was not activated/i.test(e.error.message)) {
                console.error('Provider was not activated, logging out.');
                deactivate();
            }
        };

        window.addEventListener('error', handleProviderError);
        return () => {
            window.removeEventListener('error', handleProviderError);
        };
    }, [deactivate]);

    return (
        <Paper p="md">
            <Title order={3} mb="sm">Authenticate as Stakeholder</Title>
            <Stack spacing="md">
                <Text>Stake on outcomes to predict what you care about.</Text>
                {error && (
                    <Alert icon={<AlertCircle size={16} />} title="Ohh no!" color="red">
                        {error}
                    </Alert>
                )}
                {!account && (
                    <Button fullWidth size="lg" onClick={doActivate} disabled={loading}>
                        {loading ? 'Connecting...' : 'Connect Wallet'}
                    </Button>
                )}
                {account && (
                    <Text>
                        Connected EVM Address: <Kbd>{account}</Kbd>
                    </Text>
                )}
            </Stack>
        </Paper>
    );
};