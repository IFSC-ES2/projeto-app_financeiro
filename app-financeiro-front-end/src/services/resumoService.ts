import httpClient from './httpClient';
import type { ResumoPagamentoResponse } from '../types/resumo';

export const buscarResumoPorPagamento = async () => {
  const { data } = await httpClient.get<ResumoPagamentoResponse[]>('/resumo/pagamentos', {
    ignorarLogoutAutomatico: true,
  });

  return data;
};
