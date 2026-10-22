import httpClient from './httpClient';
import type { ProjecaoMensal } from '../types/extratoFuturo';

export const buscarExtratoFuturo = async (meses = 3) => {
  const { data } = await httpClient.get<ProjecaoMensal[]>('/extrato-futuro', {
    params: { meses },
    ignorarLogoutAutomatico: true,
  });

  return data;
};
