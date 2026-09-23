export type StatusImportacao = 'PENDENTE' | 'PROCESSANDO' | 'CONCLUIDO' | 'ERRO';

export interface ImportacaoResponse {
  id: string;
  status: StatusImportacao;
  sucessos: number;
  falhas: number;
  ignoradasPorDuplicidade: number;
  importadoEm: string;
  mensagemErro?: string | null;
}
