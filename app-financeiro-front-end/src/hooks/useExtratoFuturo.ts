import { useCallback } from 'react';

import { buscarExtratoFuturo } from '../services/extratoFuturoService';
import type { ProjecaoMensalResponse } from '../types/extratoFuturo';

import { useAsync } from './useAsync';

export const useExtratoFuturo = (
  meses = 3,
) => {
  const carregarExtratoFuturo = useCallback(
    () => buscarExtratoFuturo(meses),
    [meses],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
  } = useAsync<ProjecaoMensalResponse[]>(
    carregarExtratoFuturo,
    {
      valorInicial: [],
      mensagemErro:
        'Não foi possível carregar o extrato futuro.',
    },
  );

  return {
    projecoes: dados,
    carregando,
    erro,
    recarregar,
  };
};