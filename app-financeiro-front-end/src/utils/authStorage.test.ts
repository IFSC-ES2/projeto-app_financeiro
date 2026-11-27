import { beforeEach, describe, expect, it } from 'vitest';
import {
  descartarSessaoPersistida,
  limparSessao,
  obterAccessToken,
  obterPrimeiroNome,
  recuperarSessaoValida,
  salvarSessao,
} from './authStorage';

describe('authStorage', () => {
  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  it('salva a sessao com Bearer como tipo padrao e remove a chave legada', () => {
    localStorage.setItem('token', 'legado');

    const sessao = salvarSessao({ accessToken: 'token-123', tipo: '' });

    expect(sessao).toEqual({ accessToken: 'token-123', tipo: 'Bearer', expiracao: undefined });
    expect(sessionStorage.getItem('smartbudget.auth')).toContain('token-123');
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('recupera a sessao valida salva e expoe o access token', () => {
    salvarSessao({ accessToken: 'token-123', tipo: 'Bearer' });

    expect(recuperarSessaoValida()?.accessToken).toBe('token-123');
    expect(obterAccessToken()).toBe('token-123');
  });

  it('retorna null quando nao ha sessao salva', () => {
    expect(recuperarSessaoValida()).toBeNull();
    expect(obterAccessToken()).toBeNull();
  });

  it('retorna null quando o JSON salvo e invalido', () => {
    sessionStorage.setItem('smartbudget.auth', '{json invalido');

    expect(recuperarSessaoValida()).toBeNull();
  });

  it('retorna null quando a sessao salva nao tem accessToken', () => {
    sessionStorage.setItem('smartbudget.auth', JSON.stringify({ tipo: 'Bearer' }));

    expect(recuperarSessaoValida()).toBeNull();
  });

  it('usa Bearer quando a sessao salva nao tem tipo', () => {
    sessionStorage.setItem('smartbudget.auth', JSON.stringify({ accessToken: 'token-123' }));

    expect(recuperarSessaoValida()?.tipo).toBe('Bearer');
  });

  it('retorna null e limpa a sessao quando ela esta expirada', () => {
    salvarSessao({
      accessToken: 'token-123',
      tipo: 'Bearer',
      expiracao: new Date(Date.now() - 1000).toISOString(),
    });

    expect(recuperarSessaoValida()).toBeNull();
    expect(sessionStorage.getItem('smartbudget.auth')).toBeNull();
  });

  it('mantem a sessao quando a expiracao e futura ou invalida', () => {
    salvarSessao({
      accessToken: 'token-futuro',
      tipo: 'Bearer',
      expiracao: new Date(Date.now() + 60_000).toISOString(),
    });
    expect(recuperarSessaoValida()?.accessToken).toBe('token-futuro');

    salvarSessao({ accessToken: 'token-invalido', tipo: 'Bearer', expiracao: 'nao-e-data' });
    expect(recuperarSessaoValida()?.accessToken).toBe('token-invalido');
  });

  it('limpa a sessao e a chave legada ao limpar/descartar', () => {
    salvarSessao({ accessToken: 'token-123', tipo: 'Bearer' });
    localStorage.setItem('token', 'legado');

    limparSessao();

    expect(sessionStorage.getItem('smartbudget.auth')).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();

    salvarSessao({ accessToken: 'token-123', tipo: 'Bearer' });
    localStorage.setItem('token', 'legado');

    descartarSessaoPersistida();

    expect(sessionStorage.getItem('smartbudget.auth')).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
    expect(recuperarSessaoValida()).toBeNull();
  });
});

describe('obterPrimeiroNome', () => {
  const criarToken = (payload: object) => {
    const bytes = new TextEncoder().encode(JSON.stringify(payload));
    const base64Url = btoa(String.fromCharCode(...bytes))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
    return `cabecalho.${base64Url}.assinatura`;
  };

  it('retorna o primeiro nome da claim nome, inclusive com acentos', () => {
    expect(obterPrimeiroNome(criarToken({ nome: '  Joao Lacerda ' }))).toBe('Joao');
    expect(obterPrimeiroNome(criarToken({ nome: 'Ângela Sá' }))).toBe('Ângela');
  });

  it('retorna null sem token, sem claim nome ou com token malformado', () => {
    expect(obterPrimeiroNome(null)).toBeNull();
    expect(obterPrimeiroNome(criarToken({ sub: 'a@b.com' }))).toBeNull();
    expect(obterPrimeiroNome(criarToken({ nome: '   ' }))).toBeNull();
    expect(obterPrimeiroNome('token-invalido')).toBeNull();
  });
});
