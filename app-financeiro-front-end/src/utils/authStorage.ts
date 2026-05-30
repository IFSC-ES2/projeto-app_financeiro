import type { TokenDTO } from '../services/api';

const CHAVE_SESSAO = 'smartbudget.auth';
const CHAVE_LEGADA_TOKEN = 'token';

export interface SessaoAutenticacao {
  accessToken: string;
  tipo: string;
  expiracao?: string;
}

const normalizarSessao = (token: TokenDTO): SessaoAutenticacao => ({
  accessToken: token.accessToken,
  tipo: token.tipo || 'Bearer',
  expiracao: token.expiracao,
});

const expirou = (expiracao?: string) => {
  if (!expiracao) return false;

  const dataExpiracao = new Date(expiracao).getTime();
  if (Number.isNaN(dataExpiracao)) return false;

  return dataExpiracao <= Date.now();
};

const lerSessao = (): SessaoAutenticacao | null => {
  const bruto = sessionStorage.getItem(CHAVE_SESSAO);
  if (!bruto) return null;

  try {
    const sessao = JSON.parse(bruto) as Partial<SessaoAutenticacao>;
    if (!sessao.accessToken) return null;

    return {
      accessToken: sessao.accessToken,
      tipo: sessao.tipo || 'Bearer',
      expiracao: sessao.expiracao,
    };
  } catch {
    return null;
  }
};

export const salvarSessao = (token: TokenDTO) => {
  const sessao = normalizarSessao(token);
  sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
  localStorage.removeItem(CHAVE_LEGADA_TOKEN);
  return sessao;
};

export const limparSessao = () => {
  sessionStorage.removeItem(CHAVE_SESSAO);
  localStorage.removeItem(CHAVE_LEGADA_TOKEN);
};

export const descartarSessaoPersistida = () => {
  limparSessao();
};

export const recuperarSessaoValida = (): SessaoAutenticacao | null => {
  localStorage.removeItem(CHAVE_LEGADA_TOKEN);

  const sessao = lerSessao();
  if (!sessao) return null;

  if (expirou(sessao.expiracao)) {
    limparSessao();
    return null;
  }

  return sessao;
};

export const obterAccessToken = () => recuperarSessaoValida()?.accessToken ?? null;
