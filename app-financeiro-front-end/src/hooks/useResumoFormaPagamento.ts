import { useCallback } from 'react';

import { buscarResumoPorPagamento } from '../services/resumoService';
import type { ResumoPagamentoResponse } from '../types/resumo';
import { converterAnoMesParaPeriodo } from '../utils/dashboard';

import { useAsync } from './useAsync';

interface ResumoPagamentoDoMes {
  anoMes: string | null;
  resumo: ResumoPagamentoResponse[];
}

export const useResumoFormaPagamento = (
  anoMes?: string,
) => {
  const carregarResumo =
    useCallback(
      async (): Promise<ResumoPagamentoDoMes> => ({
        anoMes: anoMes ?? null,
        resumo:
          await buscarResumoPorPagamento(
            anoMes
              ? converterAnoMesParaPeriodo(
                  anoMes,
                )
              : undefined,
          ),
      }),
      [anoMes],
    );

  const {
    dados,
    carregando,
    erro,
    recarregar,
  } = useAsync<ResumoPagamentoDoMes>(
    carregarResumo,
    {
      valorInicial: {
        anoMes: null,
        resumo: [],
      },
      mensagemErro:
        'Não foi possível carregar o resumo por forma de pagamento.',
    },
  );

  const periodoEsperado =
    anoMes ?? null;

  const ehDoPeriodoSelecionado =
    dados.anoMes ===
    periodoEsperado;

  return {
    resumo:
      ehDoPeriodoSelecionado
        ? dados.resumo
        : [],
    carregando:
      carregando ||
      (!ehDoPeriodoSelecionado &&
        !erro),
    erro,
    recarregar,
  };
};