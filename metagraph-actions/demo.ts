import { Command } from 'commander';
import boxen from 'boxen';
import fetch from 'node-fetch';

import 'dotenv/config';
import { dag4 } from '@stardust-collective/dag4';

import { sendActionMessage } from './utils/messages.ts';

type VariableDetails = {
  uniqueName: string;
  authority: string;
};

type VariableData = [string, VariableDetails][];

const runDemo = async (options: {
  
}): Promise<void> => {
  console.log(
    boxen('DeSciNet Automated Demo', {
      padding: 1,
      borderStyle: 'double'
    })
  );

  const walletPrivateKey = dag4.keyStore.generatePrivateKey();
  const walletAddress = dag4.keyStore.getDagAddressFromPrivateKey(walletPrivateKey);

  const account = dag4.createAccount(walletPrivateKey);

  console.log('\x1b[38;5;214m%s\x1b[0m', 'Account Details:'); // Colored orange
  console.dir(account.keyTrio, {});

  const externalVariables = [
    { uniqueName: 'Human_Position_lon' },
    { uniqueName: 'Human_Position_lat' }
  ];

  for (const variable of externalVariables) {
    await sendActionMessage(
      {
        NewExternalVariable: {
          authority: walletAddress,
          uniqueName: variable.uniqueName,
        }
      },
      account
    );
  }

  console.log('Waiting for 3 seconds...');
  await new Promise(resolve => setTimeout(resolve, 3000));

  let variableIDs: { M_longitude?: string; M_latitude?: string } = {};
  while (Object.keys(variableIDs).length < 2) {
    console.log('Fetching variable IDs...');
    const response = await fetch('http://localhost:9200/data-application/variables');
    const data: VariableData = (await response.json()) as VariableData;

    for (const [id, details] of data) {
      if (details.uniqueName === 'Human_Position_lon') {
        variableIDs['M_longitude'] = id;
      }
      if (details.uniqueName === 'Human_Position_lat') {
        variableIDs['M_latitude'] = id;
      }
    }

    if (Object.keys(variableIDs).length < 2) {
      console.log('Not all variables found, waiting for 2 more seconds...');
      await new Promise(resolve => setTimeout(resolve, 2000));
    } else {
      console.log('\x1b[38;5;214m%s\x1b[0m', 'Variable IDs:'); // Colored orange
      console.dir(variableIDs, {});
    }
  }

  const modelData = {
    externalParameterLabels: variableIDs,
    internalParameterLabels: {
      "H_longitude": 0,
      "H_latitude": 1,
      "epsilon": 2
    },
    internalVariables: [
      {
        "equation": "latest(M_longitude, t) + randomGaussian() * epsilon * sqrt(t - latestTime(M_longitude, t))"
      },
      {
        "equation": "latest(M_latitude, t) + randomGaussian() * epsilon * sqrt(t - latestTime(M_latitude, t))"
      },
      {
        "equation": "1.0"
      }
    ]
  };

  await sendActionMessage(
    {
      NewModel: {
        model: {
          author: walletAddress,
          externalParameterLabels: modelData.externalParameterLabels,
          internalParameterLabels: modelData.internalParameterLabels,
          internalVariables: modelData.internalVariables
        }
      }
    },
    account
  );

  // New fetching loop for model ID
  let modelID: string | undefined;
  while (!modelID) {
    console.log('Fetching model ID...');
    const response = await fetch('http://localhost:9200/data-application/models');
    const data: [string, any][] = (await response.json()) as [string, any][];

    if (data.length > 0) {
      modelID = data[0][0];
      console.log('\x1b[38;5;214m%s\x1b[0m', 'Model ID:', modelID); // Colored orange
    } else {
      console.log('No models found, waiting for 2 more seconds...');
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
  }

  // Fetch environment data
  const timestamp = Date.now();
  const envResponse = await fetch(`http://localhost:9200/data-application/environment/${modelID}/${timestamp}`);
  const envData = await envResponse.json();
  console.log('\x1b[38;5;214m%s\x1b[0m', 'Environment Data for model', modelID); // Colored orange
  console.dir(envData, {});
};

const program = new Command();

program.action(runDemo);

program.parseAsync();
