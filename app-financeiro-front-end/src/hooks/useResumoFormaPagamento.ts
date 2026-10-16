import { useCallback } from 'react';

import { buscarResumoPorPagamento } from '../services/resumoService';
import type { ResumoPagamentoResponse } from '../types/resumo';

import { useAsync } from './useAsync';

export const useResumoFormaPagamento = () => {
  const carregarResumo = useCallback(
    () => buscarResumoPorPagamento(),
    [],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
  } = useAsync<ResumoPagamentoResponse[]>(
    carregarResumo,
    {
      valorInicial: [],
      mensagemErro:
        'Não foi possível carregar o resumo por forma de pagamento.',
    },
  );

  return {
    resumo: dados,
    carregando,
    erro,
    recarregar,
  };
};