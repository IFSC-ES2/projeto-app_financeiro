import { useContext } from 'react';
import { cadastrarUsuario, loginUsuario } from '../services/api';
import { limparSessao, recuperarSessaoValida, salvarSessao } from '../utils/authStorage';
import { ContextoAutenticacao } from '../contexts/contextoAutenticacaoBase';
import type { DadosContextoAutenticacao } from '../contexts/tiposAutenticacao';

const contextoAutenticacaoSemProvider: DadosContextoAutenticacao = {
  get token() {
    return recuperarSessaoValida()?.accessToken ?? null;
  },
  get estaAutenticado() {
    return Boolean(recuperarSessaoValida()?.accessToken);
  },
  autenticacaoPronta: true,
  login: async (email: string, senha: string) => {
    const token = await loginUsuario({ email, senha });
    if (!token?.accessToken) throw new Error('Resposta inesperada do servidor.');

    salvarSessao(token);
    window.dispatchEvent(new Event('smartbudget:authenticated'));
  },
  cadastrar: async (nome: string, email: string, senha: string, cpf: string) => {
    const token = await cadastrarUsuario({ nome, email, senha, cpf });
    if (!token?.accessToken) throw new Error('Resposta inesperada do servidor.');

    salvarSessao(token);
    window.dispatchEvent(new Event('smartbudget:authenticated'));
  },
  sair: () => {
    limparSessao();
    window.dispatchEvent(new Event('smartbudget:unauthorized'));
  },
};

export const useAutenticacao = () => {
  const contexto = useContext(ContextoAutenticacao);

  return contexto ?? contextoAutenticacaoSemProvider;
};
