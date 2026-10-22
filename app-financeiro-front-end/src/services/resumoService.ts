import httpClient from './httpClient';
import type { GrupoCategoria, ResumoMensal, ResumoPagamentoResponse } from '../types/resumo';

const opcoes = { ignorarLogoutAutomatico: true };

export const buscarResumoPorPagamento = async () => {
  const { data } = await httpClient.get<ResumoPagamentoResponse[]>('/resumo/pagamentos', opcoes);

  return data;
};

export const buscarResumoMensal = async (ano: number, mes: number) => {
  const { data } = await httpClient.get<ResumoMensal>('/resumo', {
    ...opcoes,
    params: { ano, mes },
  });

  return data;
};

export const buscarResumoPorCategoria = async (ano: number, mes: number) => {
  const { data } = await httpClient.get<GrupoCategoria[]>('/resumo/categorias', {
    ...opcoes,
    params: { ano, mes },
  });

  return data;
};
