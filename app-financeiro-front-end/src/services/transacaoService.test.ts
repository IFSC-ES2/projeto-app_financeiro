import { beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import { buscarResumoPorPagamento } from './resumoService';
import type { ResumoPagamentoResponse } from '../types/resumo';

vi.mock('./httpClient', () => ({
  default: { get: vi.fn() },
}));

const resumo: ResumoPagamentoResponse[] = [
  { formaPagamento: 'PIX', rotulo: 'Pix', total: 500, quantidade: 3, percentual: 100 },
];

describe('resumoService', () => {
  beforeEach(() => {
    vi.mocked(httpClient.get).mockReset();
  });

  it('busca o resumo em GET /resumo/pagamentos sem período explícito', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: resumo });

    await expect(buscarResumoPorPagamento()).resolves.toEqual(resumo);

    expect(httpClient.get).toHaveBeenCalledWith('/resumo/pagamentos', {
      params: undefined,
      ignorarLogoutAutomatico: true,
    });
  });

  it('envia ano e mês ao buscar o resumo por forma de pagamento', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: resumo });

    await expect(
      buscarResumoPorPagamento({
        ano: 2026,
        mes: 4,
      }),
    ).resolves.toEqual(resumo);

    expect(httpClient.get).toHaveBeenCalledWith('/resumo/pagamentos', {
      params: {
        ano: 2026,
        mes: 4,
      },
      ignorarLogoutAutomatico: true,
    });
  });

  it('propaga o erro da API para quem chama', async () => {
    const erro = new Error('Falha na API');
    vi.mocked(httpClient.get).mockRejectedValue(erro);

    await expect(buscarResumoPorPagamento()).rejects.toBe(erro);
  });
});