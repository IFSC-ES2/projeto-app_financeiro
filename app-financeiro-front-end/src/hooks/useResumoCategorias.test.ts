import { renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { buscarResumoPorCategorias } from '../services/resumoService';
import type { ResumoCategoriaResponse } from '../types/resumo';
import { useResumoCategorias } from './useResumoCategorias';

vi.mock('../services/resumoService', () => ({
  buscarResumoPorCategorias: vi.fn(),
}));

const categoria = (nome: string): ResumoCategoriaResponse => ({
  categoriaID: nome,
  nome,
  icone: null,
  cor: null,
  total: 10,
  quantidade: 1,
  percentual: 100,
});

describe('useResumoCategorias', () => {
  beforeEach(() => {
    vi.mocked(buscarResumoPorCategorias).mockReset();
  });

  it('nao exibe as categorias do mes anterior enquanto o novo mes carrega', async () => {
    vi.mocked(buscarResumoPorCategorias)
      .mockResolvedValueOnce([categoria('Mercado')])
      .mockReturnValueOnce(new Promise(() => undefined));

    const { result, rerender } = renderHook(
      ({ anoMes }: { anoMes: string }) => useResumoCategorias(anoMes),
      { initialProps: { anoMes: '2026-04' } },
    );

    await waitFor(() => expect(result.current.categorias).toHaveLength(1));

    rerender({ anoMes: '2026-05' });

    expect(result.current.categorias).toEqual([]);
    expect(result.current.carregando).toBe(true);
  });

  it('nao mantem as categorias do mes anterior quando o novo mes falha', async () => {
    vi.mocked(buscarResumoPorCategorias)
      .mockResolvedValueOnce([categoria('Mercado')])
      .mockRejectedValueOnce(new Error('falhou'));

    const { result, rerender } = renderHook(
      ({ anoMes }: { anoMes: string }) => useResumoCategorias(anoMes),
      { initialProps: { anoMes: '2026-04' } },
    );

    await waitFor(() => expect(result.current.categorias).toHaveLength(1));

    rerender({ anoMes: '2026-05' });

    await waitFor(() => expect(result.current.erro).not.toBe(''));
    expect(result.current.categorias).toEqual([]);
    expect(result.current.carregando).toBe(false);
  });
});
