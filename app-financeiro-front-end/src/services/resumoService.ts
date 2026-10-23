import httpClient from './httpClient';
import type {
  ResumoCategoriaResponse,
  ResumoMensalResponse,
  ResumoPagamentoResponse,
} from '../types/resumo';

export interface PeriodoResumo {
  ano: number;
  mes: number;
}

export const buscarResumoPorPagamento = async () => {
  const { data } = await httpClient.get<ResumoPagamentoResponse[]>('/resumo/pagamentos', {
    ignorarLogoutAutomatico: true,
  });

  return data;
};

export const buscarResumoMensal = async ({ ano, mes }: PeriodoResumo) => {
  const { data } = await httpClient.get<ResumoMensalResponse>('/resumo', {
    params: { ano, mes },
  });

  return data;
};
