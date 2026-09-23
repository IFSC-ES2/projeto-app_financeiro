import axios from 'axios';

interface ErroApiPayload {
  erro?: unknown;
  message?: unknown;
}

const obterTextoSeguro = (valor: unknown) => (typeof valor === 'string' && valor.trim() ? valor : undefined);

export const obterMensagemErroApi = (erro: unknown, fallback: string) => {
  if (!axios.isAxiosError(erro)) return fallback;

  const dados = erro.response?.data as ErroApiPayload | undefined;
  return obterTextoSeguro(dados?.erro) || obterTextoSeguro(dados?.message) || fallback;
};

export const obterStatusHttp = (erro: unknown) => (axios.isAxiosError(erro) ? erro.response?.status : undefined);
