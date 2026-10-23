import { useCallback } from 'react';

import { buscarResumoMensal } from '../services/resumoService';
import type { ResumoMensalResponse } from '../types/resumo';
import {
  converterAnoMesParaPeriodo,
  obterPeriodoAnterior,
} from '../utils/dashboard';

import { useAsync } from './useAsync';

interface ResumoMensalComparativo {
  atual: ResumoMensalResponse | null;
  anterior: ResumoMensalResponse | null;
}

export const useResumoMensal = (anoMes: string) => {
  const carregarResumo = useCallback(
    async (): Promise<ResumoMensalComparativo> => {
      /*
       * O mês anterior serve apenas para saber se
       * existe base de comparação. Uma falha nele não
       * deve impedir a exibição do mês selecionado.
       */
      const [atual, anterior] = await Promise.all([
        buscarResumoMensal(converterAnoMesParaPeriodo(anoMes)),
        buscarResumoMensal(obterPeriodoAnterior(anoMes)).catch(() => null),
      ]);

      return { atual, anterior };
    },
    [anoMes],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
  } = useAsync<ResumoMensalComparativo>(
    carregarResumo,
    {
      valorInicial: { atual: null, anterior: null },
      mensagemErro:
        'Não foi possível carregar o resumo do mês.',
    },
  );

  return {
    resumo: dados.atual,
    resumoMesAnterior: dados.anterior,
    carregando,
    erro,
    recarregar,
  };
};
