import { useCallback } from 'react';

import { buscarResumoPorCategorias } from '../services/resumoService';
import type { ResumoCategoriaResponse } from '../types/resumo';
import { converterAnoMesParaPeriodo } from '../utils/dashboard';

import { useAsync } from './useAsync';

export const useResumoCategorias = (anoMes: string) => {
  const carregarCategorias = useCallback(
    () => buscarResumoPorCategorias(converterAnoMesParaPeriodo(anoMes)),
    [anoMes],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
  } = useAsync<ResumoCategoriaResponse[]>(
    carregarCategorias,
    {
      valorInicial: [],
      mensagemErro:
        'Não foi possível carregar os gastos por categoria.',
    },
  );

  return {
    categorias: dados,
    carregando,
    erro,
    recarregar,
  };
};
