import { DeSciNetDataUpdateAction } from '../types/descinet_actions.ts';
import axios, { AxiosError } from 'axios';

import { dag4 } from '@stardust-collective/dag4';

const metagraphL1DataUrl = "http://localhost:9400"

const isAxiosError = (value: any): value is AxiosError => {
  return value?.isAxiosError === true;
};

const generateActionMessageProof = async (
  actionMessage: DeSciNetDataUpdateAction,
  signingAccount: typeof dag4.account
) => {
  const encodedMessage = Buffer.from(JSON.stringify(actionMessage)).toString('base64')
  const signature = await dag4.keyStore.dataSign(
    signingAccount.keyTrio.privateKey,
    encodedMessage
  );

  const publicKey = signingAccount.keyTrio.publicKey;
  const uncompressedPublicKey =
    publicKey.length === 128 ? '04' + publicKey : publicKey;

  return {
    id: uncompressedPublicKey.substring(2),
    signature
  };
};

const generateActionMessageBody = async (
  actionMessage: DeSciNetDataUpdateAction,
  signingAccount: typeof dag4.account
) => {
  const proof = await generateActionMessageProof(actionMessage, signingAccount);

  const body = { value: actionMessage, proofs: [proof] };

  return body;
};

const sendActionMessage = async (
  actionMessage: DeSciNetDataUpdateAction,
  signingAccount: typeof dag4.account
) => {
  const body = await generateActionMessageBody(actionMessage, signingAccount);

  let response;
  try {
    console.log('\x1b[32m%s\x1b[0m', 'Sending Action Message:'); // Dark green text
    console.dir(body, { depth: null, colors: true });

    response = await axios.post(
      metagraphL1DataUrl + '/data',
      body
    );

    console.log('Response Data');
    console.dir(response.data, { depth: null, colors: true });
  } catch (e) {
    if (isAxiosError(e)) {
      console.log(`Status: ${e.status}`);
      console.dir(e.response?.data, { depth: null, colors: true });
      throw new Error('Send Action Message Error: See above for details');
    }
    throw e;
  }

  return response;
};

export {
  generateActionMessageProof,
  generateActionMessageBody,
  sendActionMessage
};
