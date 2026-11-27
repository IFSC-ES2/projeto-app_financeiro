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
type TratadorErro = (
  erro: unknown,
) => boolean | void;
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
  setDados: Dispatch<
    SetStateAction<T>
  >;
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
  const [
    carregando,
    setCarregando,
  ] = useState(habilitado);
  const [erro, setErro] =
    useState('');
  const [versao, setVersao] =
    useState(0);
  const recarregar =
    useCallback(() => {
      setVersao(
        (atual) => atual + 1,
      );
    }, []);
  const limparErro =
    useCallback(() => {
      setErro('');
    }, []);
  useEffect(() => {
    if (!habilitado) {
      return;
    }

    let ativo = true;

    const executar = async () => {
      let finalizado = false;

      /*
       * Loading e limpeza do erro são
       * atualizados fora da execução
       * síncrona do effect.
       *
       * Isso evita a regra
       * react-hooks/set-state-in-effect,
       * sem atrasar o início da operação.
       */
      queueMicrotask(() => {
        if (
          !ativo ||
          finalizado
        ) {
          return;
        }

        setCarregando(true);
        setErro('');
      });

      try {
        /*
         * A operação precisa estar dentro
         * do try.
         *
         * Assim são tratadas tanto:
         *
         * - rejeições da Promise;
         * - exceções síncronas lançadas
         *   antes de a Promise ser
         *   retornada.
         *
         * A chamada continua acontecendo
         * imediatamente, sem um await
         * anterior.
         */
        const resultado =
          await operacao();

        if (!ativo) {
          return;
        }

        setDados(resultado);
      } catch (
        erroCapturado
      ) {
        if (!ativo) {
          return;
        }

        const tratado =
          aoErro?.(
            erroCapturado,
          );

        if (
          tratado === true
        ) {
          return;
        }

        setErro(
          obterMensagemErroApi(
            erroCapturado,
            mensagemErro,
          ),
        );
      } finally {
        /*
         * Impede que o microtask de
         * loading execute depois de uma
         * exceção síncrona.
         */
        finalizado = true;

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
      habilitado &&
      carregando,
    erro,
    recarregar,
    limparErro,
    setDados,
  };
};