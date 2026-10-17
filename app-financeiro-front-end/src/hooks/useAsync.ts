import {
  useCallback,
  useEffect,
  useState,
} from 'react';

import type {
  Dispatch,
  SetStateAction,
} from 'react';

import { obterMensagemErroApi } from '../services/apiError';

type TratadorErro = (erro: unknown) => boolean | void;

interface UseAsyncOpcoes<T> {
  valorInicial: T;
  mensagemErro: string;
  habilitado?: boolean;
  aoErro?: TratadorErro;
}

interface UseAsyncRetorno<T> {
  dados: T;
  carregando: boolean;
  erro: string;
  recarregar: () => void;
  limparErro: () => void;
  setDados: Dispatch<SetStateAction<T>>;
}

export const useAsync = <T>(
  operacao: () => Promise<T>,
  {
    valorInicial,
    mensagemErro,
    habilitado = true,
    aoErro,
  }: UseAsyncOpcoes<T>,
): UseAsyncRetorno<T> => {
  const [dados, setDados] =
    useState<T>(valorInicial);

  const [carregando, setCarregando] =
    useState(habilitado);

  const [erro, setErro] =
    useState('');

  const [versao, setVersao] =
    useState(0);

  const recarregar = useCallback(() => {
    setVersao((atual) => atual + 1);
  }, []);

  const limparErro = useCallback(() => {
    setErro('');
  }, []);

  useEffect(() => {
    if (!habilitado) {
      return;
    }

    let ativo = true;

    const executar = async () => {
  const promessa = operacao();

  queueMicrotask(() => {
    if (!ativo) {
      return;
    }

    setCarregando(true);
    setErro('');
  });

  try {
    const resultado = await promessa;

    if (!ativo) {
      return;
    }

    setDados(resultado);
  } catch (erroCapturado) {
    if (!ativo) {
      return;
    }

    const tratado =
      aoErro?.(erroCapturado);

    if (tratado === true) {
      return;
    }

    setErro(
      obterMensagemErroApi(
        erroCapturado,
        mensagemErro,
      ),
    );
  } finally {
    if (ativo) {
      setCarregando(false);
    }
  }
};

    void executar();

    return () => {
      ativo = false;
    };
  }, [
    operacao,
    mensagemErro,
    habilitado,
    aoErro,
    versao,
  ]);

  return {
    dados,
    carregando:
      habilitado && carregando,

    erro,
    recarregar,
    limparErro,
    setDados,
  };
};