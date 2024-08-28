import { useState, createContext } from 'react';
import { MantineProvider, Container, Grid, SimpleGrid, ScrollArea } from '@mantine/core';
import { Web3ReactProvider } from '@web3-react/core';

import { getWeb3Library } from 'src/utils';
import { BaseColor } from 'src/common/consts';
import { Header } from 'src/common/components';

import { AboutText } from 'src/components/AboutText';
import { WalletConnect } from 'src/components/WalletConnect';
import { TargetOutcomesTable } from 'src/components/TargetOutcomesTable';
import styles from './App.module.scss';
import { STARGAZER_CHAINS } from './common/consts/constants';

export const NetworkContext = createContext({
  selectedChain: STARGAZER_CHAINS.ETHEREUM,
  // eslint-disable-next-line
  setSelectedChain: (_: string) => null
});

function App() {
  const [selectedChain, setSelectedChain] = useState(STARGAZER_CHAINS.ETHEREUM);
  const initialValue = { selectedChain, setSelectedChain };

  return (
    <Web3ReactProvider getLibrary={getWeb3Library}>
      <ScrollArea className={styles.mainWrapper}>
        <MantineProvider
          theme={{
            fontFamily: "'Rubik', sans-serif",
            headings: { fontFamily: "'Rubik', sans-serif" },
            colors: { paua: new Array(10).fill(BaseColor.PAUA) as any },
            primaryColor: 'paua'
          }}
        >
          <NetworkContext.Provider value={initialValue as any}>
            <Container my="md" px="xs">
              <SimpleGrid cols={2} spacing="md" breakpoints={[
                { maxWidth: 'sm', cols: 1 },
                { maxWidth: 'xs', cols: 1 }
              ]}>
                <AboutText />
                <Grid gutter="md">
                  <Grid.Col>
                    <WalletConnect />
                  </Grid.Col>
                  <Grid.Col>
                    
                  </Grid.Col>
                </Grid>
              </SimpleGrid>
              <Grid mt="md" gutter="md">
                <Grid.Col>
                  <TargetOutcomesTable />
                </Grid.Col>
                <Grid.Col>
                  
                </Grid.Col>
              </Grid>
            </Container>
          </NetworkContext.Provider>
        </MantineProvider>
      </ScrollArea>
    </Web3ReactProvider>
  );
}

export default App;