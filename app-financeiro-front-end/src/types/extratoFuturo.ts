import type { TransacaoResponse } from './transacao';

export type StatusFatura = 'ABERTA' | 'FECHADA' | 'PAGA';

export interface FaturaResumoResponse {
  faturaId: string;
  nomeConta: string;
  mesReferencia: string;
  dataVencimento: string;
  valorTotal: number;
  status: StatusFatura;
}

export interface ProjecaoMensalResponse {
  mes: string;
  saldoPrevisto: number;
  totalDebitos: number;
  totalCreditos: number;
  quantidadeVencimentos: number;
  transacoes: TransacaoResponse[];
  faturas: FaturaResumoResponse[];
}