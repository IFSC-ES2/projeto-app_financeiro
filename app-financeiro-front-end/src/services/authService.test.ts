import { beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import { cadastrarUsuario, login, loginUsuario } from './authService';
import type { CadastroRequest, TokenDTO } from '../types/auth';

vi.mock('./httpClient', () => ({
  default: { post: vi.fn() },
}));

const token: TokenDTO = { accessToken: 'token-123', tipo: 'Bearer' };

describe('authService', () => {
  beforeEach(() => {
    vi.mocked(httpClient.post).mockReset();
  });

  it('faz login em POST /auth/login com as credenciais e retorna o data', async () => {
    vi.mocked(httpClient.post).mockResolvedValue({ data: token });
    const credenciais = { email: 'joao@email.com', senha: '123456' };

    await expect(loginUsuario(credenciais)).resolves.toEqual(token);
    expect(httpClient.post).toHaveBeenCalledWith('/auth/login', credenciais);
  });

  it('login monta o payload { email, senha } a partir dos argumentos', async () => {
    vi.mocked(httpClient.post).mockResolvedValue({ data: token });

    await expect(login('joao@email.com', '123456')).resolves.toEqual(token);
    expect(httpClient.post).toHaveBeenCalledWith('/auth/login', {
      email: 'joao@email.com',
      senha: '123456',
    });
  });

  it('cadastra em POST /auth/register com o payload enviado', async () => {
    const cadastro: CadastroRequest = {
      nome: 'Joao',
      email: 'joao@email.com',
      senha: '123456',
      cpf: '12345678900',
    };
    vi.mocked(httpClient.post).mockResolvedValue({ data: token });

    await expect(cadastrarUsuario(cadastro)).resolves.toEqual(token);
    expect(httpClient.post).toHaveBeenCalledWith('/auth/register', cadastro);
  });

  it('propaga o erro da API para quem chama', async () => {
    const erro = new Error('Credenciais invalidas');
    vi.mocked(httpClient.post).mockRejectedValue(erro);

    await expect(login('joao@email.com', 'errada')).rejects.toBe(erro);
  });
});
