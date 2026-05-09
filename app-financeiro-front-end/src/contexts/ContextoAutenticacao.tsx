import React, { createContext, useContext, useState, useCallback } from 'react';
import api from '../services/api';

interface DadosContextoAutenticacao {
  token: string | null;
  estaAutenticado: boolean;
  login: (email: string, senha: string) => Promise<void>;
  cadastrar: (nome: string, email: string, senha: string, cpf: string) => Promise<void>;
  sair: () => void;
}

const ContextoAutenticacao = createContext<DadosContextoAutenticacao>({} as DadosContextoAutenticacao);

export const ProvedorAutenticacao: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

  const login = useCallback(async (email: string, senha: string) => {
    const { data } = await api.post('/auth/login', { email, senha });
    if (!data?.accessToken) throw new Error('Resposta inesperada do servidor.');
    localStorage.setItem('token', data.accessToken);
    setToken(data.accessToken);
  }, []);

  const cadastrar = useCallback(async (nome: string, email: string, senha: string, cpf: string) => {
    const { data } = await api.post('/auth/register', { nome, email, senha, cpf });
    if (!data?.accessToken) throw new Error('Resposta inesperada do servidor.');
    localStorage.setItem('token', data.accessToken);
    setToken(data.accessToken);
  }, []);

  const sair = useCallback(() => {
    localStorage.removeItem('token');
    setToken(null);
  }, []);

  return (
    <ContextoAutenticacao.Provider value={{ token, estaAutenticado: !!token, login, cadastrar, sair }}>
      {children}
    </ContextoAutenticacao.Provider>
  );
};

export const useAutenticacao = () => useContext(ContextoAutenticacao);
