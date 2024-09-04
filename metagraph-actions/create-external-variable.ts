import { Command } from 'commander';
import boxen from 'boxen';

import 'dotenv/config';
import { dag4 } from '@stardust-collective/dag4';

import { sendActionMessage } from './utils/messages.ts';

const createExternalVariable = async (options: {
  uniqueName: string;
}): Promise<void> => {
  console.log(
    boxen('Creating external variable', {
      padding: 1,
      borderStyle: 'double'
    })
  );

  const walletPrivateKey = dag4.keyStore.generatePrivateKey();
  const walletAddress = dag4.keyStore.getDagAddressFromPrivateKey(walletPrivateKey);

  const account = dag4.createAccount(walletPrivateKey);

  console.log(`Account Details`);
  console.dir(account.keyTrio, {});

  await sendActionMessage(
    {
      NewExternalVariable: {
        authority: walletAddress,
        uniqueName: options.uniqueName,
      }
    },
    account
  );
};

const program = new Command();

program.requiredOption('-u, --unique-name <unique-name>', 'Unique Name');
program.action(createExternalVariable);

program.parseAsync();
