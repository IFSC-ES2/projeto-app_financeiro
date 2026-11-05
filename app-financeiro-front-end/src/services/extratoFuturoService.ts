import httpClient from './httpClient';
import type { ProjecaoMensalResponse } from '../types/extratoFuturo';

export const buscarExtratoFuturo = async (
  meses: number,
): Promise<ProjecaoMensalResponse[]> => {
  const { data } =
    await httpClient.get<
      ProjecaoMensalResponse[]
    >(
      '/extrato-futuro',
      {
        params: {
          meses,
        },
      },
    );

  return data;
};