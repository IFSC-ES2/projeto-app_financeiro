import { beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import { editarConta, excluirConta, listarContas, registrarConta } from './contaService';
import type { ContaEdicaoRequest, ContaRequest, ContaResponse } from '../types/conta';

vi.mock('./httpClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}));

const conta: ContaResponse = {
  contaId: 'conta-1',
  nome: 'Conta Corrente',
  banco: 'Nubank',
  tipoConta: 'CORRENTE',
};

describe('contaService', () => {
  beforeEach(() => {
    vi.mocked(httpClient.get).mockReset();
    vi.mocked(httpClient.post).mockReset();
    vi.mocked(httpClient.put).mockReset();
    vi.mocked(httpClient.delete).mockReset();
  });

  it('lista as contas em GET /contas e retorna o data', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: [conta] });

    await expect(listarContas()).resolves.toEqual([conta]);
    expect(httpClient.get).toHaveBeenCalledWith('/contas');
  });

  it('registra a conta em POST /contas/registrar com o payload enviado', async () => {
    const novaConta: ContaRequest = { nome: 'Poupança', banco: 'Inter', tipoConta: 'POUPANCA' };
    vi.mocked(httpClient.post).mockResolvedValue({ data: conta });

    await expect(registrarConta(novaConta)).resolves.toEqual(conta);
    expect(httpClient.post).toHaveBeenCalledWith('/contas/registrar', novaConta);
  });

  it('edita a conta em PUT /contas/{contaId} com id e payload corretos', async () => {
    const edicao: ContaEdicaoRequest = { nome: 'Conta Principal', descricao: 'Principal' };
    vi.mocked(httpClient.put).mockResolvedValue({ data: conta });

    await expect(editarConta('conta-1', edicao)).resolves.toEqual(conta);
    expect(httpClient.put).toHaveBeenCalledWith('/contas/conta-1', edicao);
  });

  it('exclui a conta em DELETE /contas/{contaId}', async () => {
    vi.mocked(httpClient.delete).mockResolvedValue({ data: undefined });

    await excluirConta('conta-1');

    expect(httpClient.delete).toHaveBeenCalledWith('/contas/conta-1');
  });

  it('propaga o erro da API para quem chama', async () => {
    const erro = new Error('Falha na API');
    vi.mocked(httpClient.get).mockRejectedValue(erro);

    await expect(listarContas()).rejects.toBe(erro);
  });

  it('propaga o erro da API em operacao de escrita (excluirConta)', async () => {
    const erro = new Error('Falha na API');
    vi.mocked(httpClient.delete).mockRejectedValue(erro);

    await expect(excluirConta('conta-1')).rejects.toBe(erro);
    expect(httpClient.delete).toHaveBeenCalledWith('/contas/conta-1');
  });
});
