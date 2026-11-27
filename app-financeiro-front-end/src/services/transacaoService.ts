import httpClient from './httpClient';
import type {
  FiltroTransacoesParams,
  PaginaResponse,
  TransacaoRequest,
  TransacaoResponse,
} from '../types/transacao';

export const registrarTransacaoManual = async (transacao: TransacaoRequest) => {
  const { data } = await httpClient.post<TransacaoResponse>('/transacoes/manual', transacao);
  return data;
};

export const editarTransacao = async (transacaoId: string, transacao: TransacaoRequest) => {
  const { data } = await httpClient.put<TransacaoResponse>(`/transacoes/${transacaoId}`, transacao);
  return data;
};

export const excluirTransacao = async (transacaoId: string) => {
  await httpClient.delete(`/transacoes/${transacaoId}`);
};

export const listarTransacoes = async (params: FiltroTransacoesParams = {}) => {
  const query: Record<string, string> = {};
  if (params.page != null) query.page = String(params.page);
  if (params.size != null) query.size = String(params.size);
  if (params.dataInicio) query.dataInicio = params.dataInicio;
  if (params.dataFim) query.dataFim = params.dataFim;
  if (params.categoriaId) query.categoriaId = params.categoriaId;
  if (params.contaId) query.contaId = params.contaId;
  if (params.tipo) query.tipo = params.tipo;

  const { data } = await httpClient.get<PaginaResponse<TransacaoResponse>>('/transacoes', { params: query });
  return data;
};

export const categorizarTransacao = async (transacaoId: string, categoriaId: string) => {
  const { data } = await httpClient.patch<TransacaoResponse>(`/transacoes/${transacaoId}/categoria`, {
    categoriaId,
  });

  return data;
};
