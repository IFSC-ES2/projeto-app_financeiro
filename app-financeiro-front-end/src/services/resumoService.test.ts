import { beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import { buscarResumoMensal, buscarResumoPorCategoria, buscarResumoPorPagamento } from './resumoService';
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

  it('busca o resumo em GET /resumo/pagamentos ignorando o logout automatico', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: resumo });

    await expect(buscarResumoPorPagamento()).resolves.toEqual(resumo);
    expect(httpClient.get).toHaveBeenCalledWith('/resumo/pagamentos', {
      ignorarLogoutAutomatico: true,
    });
  });

  it('propaga o erro da API para quem chama', async () => {
    const erro = new Error('Falha na API');
    vi.mocked(httpClient.get).mockRejectedValue(erro);

    await expect(buscarResumoPorPagamento()).rejects.toBe(erro);
  });

  it('busca o resumo mensal em GET /resumo com ano e mes', async () => {
    const resumoMensal = { ano: 2026, mes: 10, possuiTransacoes: true };
    vi.mocked(httpClient.get).mockResolvedValue({ data: resumoMensal });

    await expect(buscarResumoMensal(2026, 10)).resolves.toEqual(resumoMensal);
    expect(httpClient.get).toHaveBeenCalledWith('/resumo', {
      ignorarLogoutAutomatico: true,
      params: { ano: 2026, mes: 10 },
    });
  });

  it('busca os gastos por categoria em GET /resumo/categorias', async () => {
    const categorias = [{ categoriaID: 'cat-1', nome: 'Moradia', total: 80 }];
    vi.mocked(httpClient.get).mockResolvedValue({ data: categorias });

    await expect(buscarResumoPorCategoria(2026, 10)).resolves.toEqual(categorias);
    expect(httpClient.get).toHaveBeenCalledWith('/resumo/categorias', {
      ignorarLogoutAutomatico: true,
      params: { ano: 2026, mes: 10 },
    });
  });
});
