export type TipoConta = 'CORRENTE' | 'POUPANCA' | 'CARTAO_CREDITO' | 'CARTEIRA';

export interface ContaRequest {
  nome: string;
  tipoConta: TipoConta;
  banco?: string;
  descricao?: string;
}

export interface ContaEdicaoRequest {
  nome: string;
  descricao?: string;
}

export interface ContaResponse {
  contaId: string;
  nome: string;
  tipoConta: TipoConta;
  banco?: string;
  descricao?: string;
}
