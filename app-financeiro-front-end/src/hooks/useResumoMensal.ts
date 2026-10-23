import { useCallback } from 'react';

import { buscarResumoMensal } from '../services/resumoService';
import type { ResumoMensalResponse } from '../types/resumo';
import { converterAnoMesParaPeriodo } from '../utils/dashboard';

import { useAsync } from './useAsync';

export const useResumoMensal = (anoMes: string) => {
  const carregarResumo = useCallback(
    () => buscarResumoMensal(converterAnoMesParaPeriodo(anoMes)),
    [anoMes],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
  } = useAsync<ResumoMensalResponse | null>(
    carregarResumo,
    {
      valorInicial: null,
      mensagemErro:
        'Não foi possível carregar o resumo do mês.',
    },
  );

  return {
    resumo: dados,
    carregando,
    erro,
    recarregar,
  };
};
