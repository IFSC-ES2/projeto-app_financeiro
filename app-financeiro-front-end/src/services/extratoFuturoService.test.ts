import { beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import { buscarExtratoFuturo } from './extratoFuturoService';

vi.mock('./httpClient', () => ({
  default: { get: vi.fn() },
}));

describe('extratoFuturoService', () => {
  beforeEach(() => {
    vi.mocked(httpClient.get).mockReset();
  });

  it('busca a projecao em GET /extrato-futuro com 3 meses por padrao', async () => {
    const projecao = [{ mes: '2026-11' }];
    vi.mocked(httpClient.get).mockResolvedValue({ data: projecao });

    await expect(buscarExtratoFuturo()).resolves.toEqual(projecao);
    expect(httpClient.get).toHaveBeenCalledWith('/extrato-futuro', {
      params: { meses: 3 },
      ignorarLogoutAutomatico: true,
    });
  });

  it('propaga o erro da API para quem chama', async () => {
    const erro = new Error('Falha na API');
    vi.mocked(httpClient.get).mockRejectedValue(erro);

    await expect(buscarExtratoFuturo(3)).rejects.toBe(erro);
  });
});
