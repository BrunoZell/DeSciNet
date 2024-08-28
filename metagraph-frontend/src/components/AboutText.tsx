import { Paper, Text, Title, Stack, Box, Anchor } from '@mantine/core';
import styles from './AboutText.module.scss';

export const AboutText = () => (
    <Paper p="md" className={styles.aboutText}>
        <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }} p="md">
            <Title order={3} mb="md">About DeSciNet</Title>
            <Stack spacing="md" sx={{ flex: 1 }}>
                <Text>
                    DeSciNet is a collaboration model building network. An objective surprise tests the robustness of submitted causal models.
                </Text>
            </Stack>
        </Box>
    </Paper>
);