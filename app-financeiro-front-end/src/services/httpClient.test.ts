import { AxiosError } from 'axios';
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import { obterAccessToken, salvarSessao } from '../utils/authStorage';

const salvarToken = () =>
  salvarSessao({ accessToken: 'token-teste-123', tipo: 'Bearer' });

const responderOk = (aoChamar?: (config: InternalAxiosRequestConfig) => void) =>
  async (config: InternalAxiosRequestConfig): Promise<AxiosResponse> => {
    aoChamar?.(config);
    return {
      data: { ok: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    };
  };

const falharComStatus = (status: number) =>
  async (config: InternalAxiosRequestConfig): Promise<AxiosResponse> => {
    throw new AxiosError(`Erro ${status}`, 'ERR_BAD_REQUEST', config, {}, {
      data: {},
      status,
      statusText: '',
      headers: {},
      config,
    });
  };

describe('httpClient', () => {
  const aoUnauthorized = vi.fn();
  const adapterOriginal = httpClient.defaults.adapter;

  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
    aoUnauthorized.mockClear();
    window.addEventListener('smartbudget:unauthorized', aoUnauthorized);
  });

  afterEach(() => {
    httpClient.defaults.adapter = adapterOriginal;
    window.removeEventListener('smartbudget:unauthorized', aoUnauthorized);
  });

  it('envia o token salvo no header Authorization', async () => {
    salvarToken();
    let configCapturada: InternalAxiosRequestConfig | undefined;
    httpClient.defaults.adapter = responderOk((config) => {
      configCapturada = config;
    });

    await httpClient.get('/contas');

    expect(configCapturada?.headers.get('Authorization')).toBe('Bearer token-teste-123');
  });

  it('nao envia Authorization quando nao ha token salvo', async () => {
    let configCapturada: InternalAxiosRequestConfig | undefined;
    httpClient.defaults.adapter = responderOk((config) => {
      configCapturada = config;
    });

    await httpClient.get('/contas');

    expect(configCapturada?.headers.get('Authorization')).toBeFalsy();
  });

  it('em erro 401 limpa a sessao e dispara smartbudget:unauthorized', async () => {
    salvarToken();
    httpClient.defaults.adapter = falharComStatus(401);

    await expect(httpClient.get('/contas')).rejects.toBeInstanceOf(AxiosError);

    expect(obterAccessToken()).toBeNull();
    expect(sessionStorage.getItem('smartbudget.auth')).toBeNull();
    expect(aoUnauthorized).toHaveBeenCalledTimes(1);
  });

  it('com ignorarLogoutAutomatico nao limpa a sessao nem dispara o evento em 401', async () => {
    salvarToken();
    httpClient.defaults.adapter = falharComStatus(401);

    await expect(
      httpClient.get('/resumo', { ignorarLogoutAutomatico: true }),
    ).rejects.toBeInstanceOf(AxiosError);

    expect(obterAccessToken()).toBe('token-teste-123');
    expect(aoUnauthorized).not.toHaveBeenCalled();
  });

  it('nao limpa a sessao nem dispara o evento em erros diferentes de 401', async () => {
    salvarToken();
    httpClient.defaults.adapter = falharComStatus(500);

    await expect(httpClient.get('/contas')).rejects.toBeInstanceOf(AxiosError);

    expect(obterAccessToken()).toBe('token-teste-123');
    expect(aoUnauthorized).not.toHaveBeenCalled();
  });
});
