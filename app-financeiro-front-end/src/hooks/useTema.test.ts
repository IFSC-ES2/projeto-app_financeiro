import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { CHAVE_TEMA, useTema } from './useTema';

describe('useTema', () => {
  beforeEach(() => {
    localStorage.clear();
    delete document.documentElement.dataset.tema;
    delete document.documentElement.dataset.bsTheme;
  });

  it('parte do tema já aplicado no documento', () => {
    document.documentElement.dataset.tema = 'escuro';
    const { result } = renderHook(() => useTema());
    expect(result.current.tema).toBe('escuro');
  });

  it('alterna o tema, aplica no documento e salva a escolha', () => {
    const { result } = renderHook(() => useTema());
    expect(result.current.tema).toBe('claro');

    act(() => result.current.alternarTema());

    expect(result.current.tema).toBe('escuro');
    expect(document.documentElement.dataset.tema).toBe('escuro');
    expect(document.documentElement.dataset.bsTheme).toBe('dark');
    expect(localStorage.getItem(CHAVE_TEMA)).toBe('escuro');

    act(() => result.current.alternarTema());

    expect(document.documentElement.dataset.tema).toBe('claro');
    expect(localStorage.getItem(CHAVE_TEMA)).toBe('claro');
  });
});
