import { act, renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { useAsync } from './useAsync';

describe('useAsync', () => {
  it('retorna os dados da operacao e finaliza o carregamento', async () => {
    const operacao = vi.fn().mockResolvedValue('ok');

    const { result } = renderHook(() =>
      useAsync(operacao, { valorInicial: 'inicial', mensagemErro: 'Erro ao carregar' }),
    );

    await waitFor(() => expect(result.current.dados).toBe('ok'));
    expect(result.current.carregando).toBe(false);
    expect(result.current.erro).toBe('');
    expect(operacao).toHaveBeenCalledTimes(1);
  });

  it('preenche o erro com a mensagem tratada quando a operacao falha', async () => {
    const operacao = vi.fn().mockRejectedValue(new Error('falhou'));

    const { result } = renderHook(() =>
      useAsync(operacao, { valorInicial: 'inicial', mensagemErro: 'Erro ao carregar' }),
    );

    await waitFor(() => expect(result.current.erro).toBe('Erro ao carregar'));
    expect(result.current.dados).toBe('inicial');
    expect(result.current.carregando).toBe(false);
  });

  it('nao preenche o erro quando aoErro trata a falha', async () => {
    const operacao = vi.fn().mockRejectedValue(new Error('falhou'));
    const aoErro = vi.fn().mockReturnValue(true);

    const { result } = renderHook(() =>
      useAsync(operacao, { valorInicial: 'inicial', mensagemErro: 'Erro ao carregar', aoErro }),
    );

    await waitFor(() => expect(aoErro).toHaveBeenCalled());
    expect(result.current.erro).toBe('');
  });

  it('nao executa a operacao quando habilitado e false', async () => {
    const operacao = vi.fn().mockResolvedValue('ok');

    const { result } = renderHook(() =>
      useAsync(operacao, {
        valorInicial: 'inicial',
        mensagemErro: 'Erro ao carregar',
        habilitado: false,
      }),
    );

    expect(operacao).not.toHaveBeenCalled();
    expect(result.current.dados).toBe('inicial');
    expect(result.current.carregando).toBe(false);
  });

  it('executa a operacao novamente ao recarregar', async () => {
    const operacao = vi.fn().mockResolvedValueOnce('primeiro').mockResolvedValueOnce('segundo');

    const { result } = renderHook(() =>
      useAsync(operacao, { valorInicial: 'inicial', mensagemErro: 'Erro ao carregar' }),
    );

    await waitFor(() => expect(result.current.dados).toBe('primeiro'));

    act(() => result.current.recarregar());

    await waitFor(() => expect(result.current.dados).toBe('segundo'));
    expect(operacao).toHaveBeenCalledTimes(2);
  });

  it('limpa o erro e permite atualizar os dados manualmente', async () => {
    const operacao = vi.fn().mockRejectedValue(new Error('falhou'));

    const { result } = renderHook(() =>
      useAsync<string>(operacao, { valorInicial: 'inicial', mensagemErro: 'Erro ao carregar' }),
    );

    await waitFor(() => expect(result.current.erro).toBe('Erro ao carregar'));

    act(() => result.current.limparErro());
    expect(result.current.erro).toBe('');

    act(() => result.current.setDados('manual'));
    expect(result.current.dados).toBe('manual');
  });

  it('ignora a resposta quando o componente ja desmontou', async () => {
    let resolver: (valor: string) => void = () => undefined;
    const operacao = vi.fn(
      () =>
        new Promise<string>((resolve) => {
          resolver = resolve;
        }),
    );

    const { result, unmount } = renderHook(() =>
      useAsync(operacao, { valorInicial: 'inicial', mensagemErro: 'Erro ao carregar' }),
    );

    unmount();

    await act(async () => resolver('ok'));

    expect(result.current.dados).toBe('inicial');
  });

  it('ignora a resposta obsoleta quando a operacao e substituida', async () => {
    let resolverPrimeira: (valor: string) => void = () => undefined;
    let resolverSegunda: (valor: string) => void = () => undefined;

    const primeira = vi.fn(
      () =>
        new Promise<string>((resolve) => {
          resolverPrimeira = resolve;
        }),
    );
    const segunda = vi.fn(
      () =>
        new Promise<string>((resolve) => {
          resolverSegunda = resolve;
        }),
    );

    const { result, rerender } = renderHook(
      ({ operacao }: { operacao: () => Promise<string> }) =>
        useAsync(operacao, { valorInicial: 'inicial', mensagemErro: 'Erro ao carregar' }),
      { initialProps: { operacao: primeira } },
    );

    rerender({ operacao: segunda });

    await act(async () => resolverSegunda('segunda'));

    await waitFor(() => expect(result.current.dados).toBe('segunda'));

    await act(async () => resolverPrimeira('primeira'));

    expect(result.current.dados).toBe('segunda');
  });
});
