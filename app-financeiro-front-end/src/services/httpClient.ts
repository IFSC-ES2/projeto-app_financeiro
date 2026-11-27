import axios from 'axios';
import { limparSessao, obterAccessToken } from '../utils/authStorage';

declare module 'axios' {
  export interface AxiosRequestConfig {
    ignorarLogoutAutomatico?: boolean;
  }
}

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
});

httpClient.interceptors.request.use((config) => {
  const token = obterAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (erro: unknown) => {
    if (
      axios.isAxiosError(erro) &&
      !erro.config?.ignorarLogoutAutomatico &&
      erro.response?.status === 401
    ) {
      limparSessao();
      window.dispatchEvent(new Event('smartbudget:unauthorized'));
    }

    return Promise.reject(erro);
  }
);

export default httpClient;
