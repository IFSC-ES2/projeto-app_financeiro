import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { buscarResumoMensal } from '../services/resumoService';
import type { ResumoMensalResponse } from '../types/resumo';
import { useResumoMensal } from './useResumoMensal';

vi.mock('../services/resumoService', () => ({
  buscarResumoMensal: vi.fn(),
}));

const resumoDe = (ano: number, mes: number): ResumoMensalResponse => ({
  ano,
  mes,
  dataInicio: '',
  dataFim: '',
  totalRecebido: 0,
  totalGasto: 100,
  saldo: -100,
  categoriaMaiorGastoId: null,
  categoriaMaiorGastoNome: null,
  categoriaMaiorGastoTotal: 0,
  variacaoPercentualGastos: null,
  possuiTransacoes: true,
});

describe('useResumoMensal', () => {
  beforeEach(() => {
    vi.mocked(buscarResumoMensal).mockReset();
  });

  it('exibe o resumo do mes sem esperar a comparacao com o mes anterior', async () => {
    let resolverAnterior: (resumo: ResumoMensalResponse) => void = () => undefined;

    vi.mocked(buscarResumoMensal).mockImplementation(({ mes }) =>
      mes === 5
        ? Promise.resolve(resumoDe(2026, 5))
        : new Promise((resolve) => {
          resolverAnterior = resolve;
        }),
    );

    const { result } = renderHook(() => useResumoMensal('2026-05'));

    await waitFor(() => expect(result.current.resumo?.mes).toBe(5));
    expect(result.current.carregandoMesAnterior).toBe(true);
    expect(result.current.resumoMesAnterior).toBeNull();

    await act(async () => resolverAnterior(resumoDe(2026, 4)));

    expect(result.current.carregandoMesAnterior).toBe(false);
    expect(result.current.resumoMesAnterior?.mes).toBe(4);
  });

  it('exibe o resumo do mes quando a comparacao com o mes anterior falha', async () => {
    vi.mocked(buscarResumoMensal).mockImplementation(({ mes }) =>
      mes === 5
        ? Promise.resolve(resumoDe(2026, 5))
        : Promise.reject(new Error('falhou')),
    );

    const { result } = renderHook(() => useResumoMensal('2026-05'));

    await waitFor(() => expect(result.current.carregandoMesAnterior).toBe(false));
    expect(result.current.resumo?.mes).toBe(5);
    expect(result.current.resumoMesAnterior).toBeNull();
    expect(result.current.erro).toBe('');
  });
});
