import httpClient from './httpClient';
import type { ImportacaoResponse, StatusImportacao } from '../types/importacao';

export const criarImportacao = async (arquivo: File, contaId: string) => {
  const formData = new FormData();
  formData.append('arquivo', arquivo);
  formData.append('contaId', contaId);

  const { data } = await httpClient.post<ImportacaoResponse>('/importacoes', formData);
  return data;
};

export const consultarStatusImportacao = async (importacaoId: string) => {
  const { data } = await httpClient.get<StatusImportacao>(`/importacoes/${importacaoId}/status`);
  return data;
};
