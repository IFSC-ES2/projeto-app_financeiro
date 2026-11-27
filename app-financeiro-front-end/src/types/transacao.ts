export type TipoTransacao = 'DEBITO' | 'CREDITO';
export type TipoPagamento = 'PIX' | 'CARTAO_DEBITO' | 'CARTAO_CREDITO' | 'DINHEIRO' | 'BOLETO' | 'TED_DOC';

export interface TransacaoRequest {
  valor: number;
  data: string;
  descricao?: string;
  tipoTransacao: TipoTransacao;
  formaPagamento?: TipoPagamento;
  categoriaId?: string | null;
  contaId: string | null;
}

export interface TransacaoResponse {
  transacaoId: string;
  valor: number;
  data: string;
  descricao?: string;
  tipoTransacao: TipoTransacao;
  formaPagamento?: TipoPagamento;
  importacaoId?: string | null;
  categoriaId?: string | null;
  contaId: string | null;
  categorizada: boolean;
}

export interface CategorizarTransacaoResponse {
  categoriaId: string;
}

export interface PaginaResponse<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
  primeira: boolean;
  ultima: boolean;
}

export interface FiltroTransacoesParams {
  page?: number;
  size?: number;
  dataInicio?: string;
  dataFim?: string;
  categoriaId?: string;
  contaId?: string;
  tipo?: TipoTransacao | '';
}
