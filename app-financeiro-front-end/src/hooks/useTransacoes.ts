import { useCallback } from 'react';

import { listarTransacoes } from '../services/transacaoService';

import type {
  FiltroTransacoesParams,
  PaginaResponse,
  TransacaoResponse,
} from '../types/transacao';

import { useAsync } from './useAsync';

interface UseTransacoesOpcoes extends FiltroTransacoesParams {
  habilitado?: boolean;
  mensagemErro?: string;
}

export const useTransacoes = ({
  page,
  size,
  dataInicio,
  dataFim,
  categoriaId,
  contaId,
  tipo,
  habilitado = true,
  mensagemErro = 'Não foi possível carregar as transações.',
}: UseTransacoesOpcoes = {}) => {
  const carregarTransacoes = useCallback(
    () =>
      listarTransacoes({
        page,
        size,
        dataInicio,
        dataFim,
        categoriaId,
        contaId,
        tipo,
      }),
    [
      page,
      size,
      dataInicio,
      dataFim,
      categoriaId,
      contaId,
      tipo,
    ],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
    limparErro,
    setDados,
  } = useAsync<PaginaResponse<TransacaoResponse> | null>(
    carregarTransacoes,
    {
      valorInicial: null,
      mensagemErro,
      habilitado,
    },
  );

  return {
    pagina: dados,
    carregando,
    erro,
    recarregar,
    limparErro,
    setPagina: setDados,
  };
};