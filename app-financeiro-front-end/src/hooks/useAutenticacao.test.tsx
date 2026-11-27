import { renderHook } from '@testing-library/react';
import type { ReactNode } from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { useAutenticacao } from './useAutenticacao';
import { ContextoAutenticacao } from '../contexts/contextoAutenticacaoBase';
import type { DadosContextoAutenticacao } from '../contexts/tiposAutenticacao';

const contextoFake: DadosContextoAutenticacao = {
  token: 'token-123',
  estaAutenticado: true,
  autenticacaoPronta: true,
  login: vi.fn(),
  cadastrar: vi.fn(),
  sair: vi.fn(),
};

describe('useAutenticacao', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('retorna o contexto quando usado dentro do provedor', () => {
    const wrapper = ({ children }: { children: ReactNode }) => (
      <ContextoAutenticacao.Provider value={contextoFake}>{children}</ContextoAutenticacao.Provider>
    );

    const { result } = renderHook(() => useAutenticacao(), { wrapper });

    expect(result.current).toBe(contextoFake);
    expect(result.current.estaAutenticado).toBe(true);
  });

  it('lanca erro quando usado fora do provedor', () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined);

    expect(() => renderHook(() => useAutenticacao())).toThrow(
      'useAutenticacao deve ser usado dentro de ProvedorAutenticacao.',
    );
  });
});
