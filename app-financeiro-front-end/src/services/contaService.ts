import httpClient from './httpClient';
import type { ContaEdicaoRequest, ContaRequest, ContaResponse } from '../types/conta';

export const listarContas = async () => {
  const { data } = await httpClient.get<ContaResponse[]>('/contas');
  return data;
};

export const registrarConta = async (conta: ContaRequest) => {
  const { data } = await httpClient.post<ContaResponse>('/contas/registrar', conta);
  return data;
};

export const editarConta = async (contaId: string, conta: ContaEdicaoRequest) => {
  const { data } = await httpClient.put<ContaResponse>(`/contas/${contaId}`, conta);
  return data;
};

export const excluirConta = async (contaId: string) => {
  await httpClient.delete(`/contas/${contaId}`);
};
