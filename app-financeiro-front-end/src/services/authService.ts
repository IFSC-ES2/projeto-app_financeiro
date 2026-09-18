import httpClient from './httpClient';
import type { CadastroRequest, LoginRequest, TokenDTO } from '../types/auth';

export const loginUsuario = async (credenciais: LoginRequest) => {
  const { data } = await httpClient.post<TokenDTO>('/auth/login', credenciais);
  return data;
};

export const login = async (email: string, senha: string) => loginUsuario({ email, senha });

export const cadastrarUsuario = async (cadastro: CadastroRequest) => {
  const { data } = await httpClient.post<TokenDTO>('/auth/register', cadastro);
  return data;
};
