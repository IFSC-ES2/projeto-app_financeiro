import { act, renderHook } from '@testing-library/react';
import type { ChangeEvent, FocusEvent } from 'react';
import { describe, expect, it } from 'vitest';
import { useFormulario } from './useFormulario';

type Valores = Record<'nome' | 'email', string>;

const valoresIniciais: Valores = { nome: '', email: '' };

const alterar = (nomeCampo: string, valor: string) =>
  ({ target: { name: nomeCampo, value: valor } }) as ChangeEvent<HTMLInputElement>;

const sairDoCampo = (nomeCampo: string) =>
  ({ target: { name: nomeCampo } }) as FocusEvent<HTMLInputElement>;

describe('useFormulario', () => {
  it('inicia com os valores informados e sem erros ou tocados', () => {
    const { result } = renderHook(() => useFormulario({ valoresIniciais }));

    expect(result.current.valores).toEqual(valoresIniciais);
    expect(result.current.erros).toEqual({});
    expect(result.current.tocados).toEqual({});
  });

  it('atualiza o valor do campo ao alterar', () => {
    const { result } = renderHook(() => useFormulario({ valoresIniciais }));

    act(() => result.current.aoAlterar(alterar('nome', 'Joao')));

    expect(result.current.valores.nome).toBe('Joao');
    expect(result.current.valores.email).toBe('');
  });

  it('limpa o erro do campo quando ele e alterado', () => {
    const validar = (valores: Valores) => (valores.nome ? {} : { nome: 'Nome obrigatorio' });
    const { result } = renderHook(() => useFormulario({ valoresIniciais, validar }));

    act(() => result.current.eValido());
    expect(result.current.erros.nome).toBe('Nome obrigatorio');

    act(() => result.current.aoAlterar(alterar('nome', 'Joao')));

    expect(result.current.erros.nome).toBeUndefined();
  });

  it('mantem o erro dos outros campos ao alterar apenas um', () => {
    const validar = (valores: Valores) => {
      const encontrados: Partial<Valores> = {};
      if (!valores.nome) encontrados.nome = 'Nome obrigatorio';
      if (!valores.email) encontrados.email = 'Email obrigatorio';
      return encontrados;
    };
    const { result } = renderHook(() => useFormulario({ valoresIniciais, validar }));

    act(() => result.current.eValido());
    expect(result.current.erros).toEqual({
      nome: 'Nome obrigatorio',
      email: 'Email obrigatorio',
    });

    act(() => result.current.aoAlterar(alterar('nome', 'Joao')));

    expect(result.current.erros.nome).toBeUndefined();
    expect(result.current.erros.email).toBe('Email obrigatorio');
  });

  it('marca o campo como tocado ao sair dele', () => {
    const { result } = renderHook(() => useFormulario({ valoresIniciais }));

    act(() => result.current.aoSair(sairDoCampo('email')));

    expect(result.current.tocados).toEqual({ email: true });
  });

  it('eValido retorna true quando nao ha funcao de validacao', () => {
    const { result } = renderHook(() => useFormulario({ valoresIniciais }));

    let valido = false;
    act(() => {
      valido = result.current.eValido();
    });

    expect(valido).toBe(true);
    expect(result.current.erros).toEqual({});
  });

  it('eValido preenche erros, marca todos como tocados e retorna false quando invalido', () => {
    const validar = (valores: Valores) => (valores.nome ? {} : { nome: 'Nome obrigatorio' });
    const { result } = renderHook(() => useFormulario({ valoresIniciais, validar }));

    let valido = true;
    act(() => {
      valido = result.current.eValido();
    });

    expect(valido).toBe(false);
    expect(result.current.erros).toEqual({ nome: 'Nome obrigatorio' });
    expect(result.current.tocados).toEqual({ nome: true, email: true });
  });

  it('eValido retorna true quando a validacao nao encontra erros', () => {
    const validar = (valores: Valores) => (valores.nome ? {} : { nome: 'Nome obrigatorio' });
    const { result } = renderHook(() =>
      useFormulario({ valoresIniciais: { nome: 'Joao', email: '' }, validar }),
    );

    let valido = false;
    act(() => {
      valido = result.current.eValido();
    });

    expect(valido).toBe(true);
    expect(result.current.erros).toEqual({});
  });

  it('permite substituir os valores com setValores', () => {
    const { result } = renderHook(() => useFormulario({ valoresIniciais }));

    act(() => result.current.setValores({ nome: 'Maria', email: 'maria@email.com' }));

    expect(result.current.valores).toEqual({ nome: 'Maria', email: 'maria@email.com' });
  });
});
