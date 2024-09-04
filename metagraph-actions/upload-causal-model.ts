import { Command } from 'commander';
import boxen from 'boxen';
import * as fs from 'node:fs/promises';

import 'dotenv/config';
import { dag4 } from '@stardust-collective/dag4';

import { sendActionMessage } from './utils/messages.ts';

const uploadCausalModel = async (options: {
  file: string;
}): Promise<void> => {
  console.log(
    boxen('Uploading causal model', {
      padding: 1,
      borderStyle: 'double'
    })
  );

  const walletPrivateKey = dag4.keyStore.generatePrivateKey();
  const walletAddress = dag4.keyStore.getDagAddressFromPrivateKey(walletPrivateKey);

  const account = dag4.createAccount(walletPrivateKey);

  console.log('\x1b[38;5;214m%s\x1b[0m', 'Account Details:'); // Colored orange
  console.dir(account.keyTrio, {});

  // Read and parse the JSON file
  const modelData = JSON.parse(await fs.readFile(options.file, 'utf-8'));

  console.log('\x1b[34m%s\x1b[0m', 'Loaded Model Details:'); // Blue text
  console.dir(modelData, {});
  
  if (!modelData.externalParameterLabels) {
    throw new Error('Invalid model data: missing externalParameterLabels');
  }
  if (!modelData.internalParameterLabels) {
    throw new Error('Invalid model data: missing internalParameterLabels');
  }
  if (!modelData.internalVariables) {
    throw new Error('Invalid model data: missing internalVariables');
  }

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
};

const program = new Command();

program.requiredOption('-f, --file <file>', 'File path to the JSON model specification');
program.action(uploadCausalModel);

program.parseAsync();
