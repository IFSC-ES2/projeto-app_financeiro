import { beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import { listarCategorias } from './categoriaService';
import type { CategoriaResponse } from '../types/categoria';

vi.mock('./httpClient', () => ({
  default: { get: vi.fn() },
}));

const categorias: CategoriaResponse[] = [
  { categoriaId: 'cat-1', nome: 'Alimentação', padrao: true },
  { categoriaId: 'cat-2', nome: 'Transporte', padrao: false },
];

describe('categoriaService', () => {
  beforeEach(() => {
    vi.mocked(httpClient.get).mockReset();
  });

  it('busca as categorias em GET /categorias e retorna o data', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: categorias });

    await expect(listarCategorias()).resolves.toEqual(categorias);
    expect(httpClient.get).toHaveBeenCalledWith('/categorias');
  });

  it('propaga o erro da API para quem chama', async () => {
    const erro = new Error('Falha na API');
    vi.mocked(httpClient.get).mockRejectedValue(erro);

    await expect(listarCategorias()).rejects.toBe(erro);
  });
});
