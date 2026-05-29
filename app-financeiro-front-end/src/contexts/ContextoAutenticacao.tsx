/* eslint-disable react-refresh/only-export-components */
import { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { cadastrarUsuario, loginUsuario } from '../services/api';
import { ContextoAutenticacao } from './contextoAutenticacaoBase';
import type { DadosContextoAutenticacao } from './tiposAutenticacao';
import { limparSessao, recuperarSessaoValida, salvarSessao } from '../utils/authStorage';
import type { SessaoAutenticacao } from '../utils/authStorage';

interface PropsProvedorAutenticacao {
  children: ReactNode;
}

export const ProvedorAutenticacao = ({ children }: PropsProvedorAutenticacao) => {
  const [sessao, setSessao] = useState<SessaoAutenticacao | null>(() => recuperarSessaoValida());
  const [autenticacaoPronta] = useState(true);

  const sair = useCallback(() => {
    limparSessao();
    setSessao(null);
  }, []);

  useEffect(() => {
    const aoPerderAutorizacao = () => setSessao(null);
    const aoAutenticar = () => setSessao(recuperarSessaoValida());

    window.addEventListener('smartbudget:unauthorized', aoPerderAutorizacao);
    window.addEventListener('smartbudget:authenticated', aoAutenticar);

    return () => {
      window.removeEventListener('smartbudget:unauthorized', aoPerderAutorizacao);
      window.removeEventListener('smartbudget:authenticated', aoAutenticar);
    };
  }, []);

  const login = useCallback(async (email: string, senha: string) => {
    const token = await loginUsuario({ email, senha });
    if (!token?.accessToken) throw new Error('Resposta inesperada do servidor.');

    setSessao(salvarSessao(token));
  }, []);

  const cadastrar = useCallback(async (nome: string, email: string, senha: string, cpf: string) => {
    const token = await cadastrarUsuario({ nome, email, senha, cpf });
    if (!token?.accessToken) throw new Error('Resposta inesperada do servidor.');

    setSessao(salvarSessao(token));
  }, []);

  const valor = useMemo<DadosContextoAutenticacao>(
    () => ({
      token: sessao?.accessToken ?? null,
      estaAutenticado: Boolean(sessao?.accessToken),
      autenticacaoPronta,
      login,
      cadastrar,
      sair,
    }),
    [autenticacaoPronta, cadastrar, login, sair, sessao?.accessToken]
  );

  return <ContextoAutenticacao.Provider value={valor}>{children}</ContextoAutenticacao.Provider>;
};

export const useAutenticacao = () => {
  const contexto = useContext(ContextoAutenticacao);

  if (!contexto) {
    throw new Error('useAutenticacao deve ser usado dentro de ProvedorAutenticacao.');
  }

  return contexto;
};
