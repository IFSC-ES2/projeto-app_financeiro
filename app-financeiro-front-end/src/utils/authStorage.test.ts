import { beforeEach, describe, expect, it } from 'vitest';
import { limparSessao, obterAccessToken, recuperarSessaoValida, salvarSessao } from './authStorage';

const tokenValido = {
  accessToken: 'jwt-valido',
  tipo: 'Bearer',
  expiracao: '2999-01-01T00:00:00',
};

describe('armazenamento de autenticação', () => {
  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  it('não reutiliza token legado salvo em localStorage', () => {
    localStorage.setItem('token', 'token-antigo');

    expect(recuperarSessaoValida()).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('usa somente sessão salva pelo fluxo atual', () => {
    salvarSessao(tokenValido);

    expect(obterAccessToken()).toBe('jwt-valido');
  });



  it('mantém sessão válida após recarregar a página na mesma aba', () => {
    salvarSessao(tokenValido);

    expect(recuperarSessaoValida()?.accessToken).toBe('jwt-valido');
  });

  it('remove sessão local ao sair', () => {
    salvarSessao(tokenValido);

    limparSessao();

    expect(obterAccessToken()).toBeNull();
  });
});
