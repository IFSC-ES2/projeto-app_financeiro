import httpClient from './httpClient';
import type { ProjecaoMensalResponse } from '../types/extratoFuturo';

export const buscarExtratoFuturo = async (
  meses = 3,
): Promise<ProjecaoMensalResponse[]> => {
  const { data } = await httpClient.get<ProjecaoMensalResponse[]>(
    '/extrato-futuro',
    {
      params: {
        meses,
      },
    },
  );

  return data;
};