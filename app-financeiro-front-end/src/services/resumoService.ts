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

export const buscarResumoPorPagamento = async (
  periodo?: PeriodoResumo,
) => {
  const configuracao = periodo
    ? {
        params: periodo,
        ignorarLogoutAutomatico: true,
      }
    : {
        ignorarLogoutAutomatico: true,
      };

  const { data } =
    await httpClient.get<
      ResumoPagamentoResponse[]
    >(
      '/resumo/pagamentos',
      configuracao,
    );

  return data;
};

export const buscarResumoMensal = async ({
  ano,
  mes,
}: PeriodoResumo) => {
  const { data } =
    await httpClient.get<ResumoMensalResponse>(
      '/resumo',
      {
        params: {
          ano,
          mes,
        },
      },
    );

  return data;
};

export const buscarResumoPorCategorias = async ({
  ano,
  mes,
}: PeriodoResumo) => {
  const { data } =
    await httpClient.get<
      ResumoCategoriaResponse[]
    >(
      '/resumo/categorias',
      {
        params: {
          ano,
          mes,
        },
      },
    );

  return data;
};