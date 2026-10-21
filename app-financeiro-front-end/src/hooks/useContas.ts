import { useCallback } from 'react';

import { listarContas } from '../services/contaService';
import type { ContaResponse } from '../types/conta';

import { useAsync } from './useAsync';

interface UseContasOpcoes {
  habilitado?: boolean;
  mensagemErro?: string;
  aoErro?: (erro: unknown) => boolean | void;
}

export const useContas = ({
  habilitado = true,
  mensagemErro = 'Não foi possível carregar as contas.',
  aoErro,
}: UseContasOpcoes = {}) => {
  const carregarContas = useCallback(
    () => listarContas(),
    [],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
    limparErro,
    setDados,
  } = useAsync<ContaResponse[]>(
    carregarContas,
    {
      valorInicial: [],
      mensagemErro,
      habilitado,
      aoErro,
    },
  );

  return {
    contas: dados,
    carregando,
    erro,
    recarregar,
    limparErro,
    setContas: setDados,
  };
};