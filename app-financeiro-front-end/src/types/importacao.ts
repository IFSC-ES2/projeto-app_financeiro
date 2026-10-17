export type StatusImportacao =
  | 'PENDENTE'
  | 'PROCESSANDO'
  | 'CONCLUIDO'
  | 'ERRO';
export interface ErroImportacao {
  numeroRegistro: number | null;
  campo: string | null;
  mensagem: string;
}

export interface ImportacaoResponse {
  id: string;
  status: StatusImportacao;
  sucessos: number;
  falhas: number;
  ignoradasPorDuplicidade: number;
  importadoEm: string;
  mensagemErro?: string | null;
  erros: ErroImportacao[];
}