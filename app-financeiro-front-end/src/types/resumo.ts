import type { TipoPagamento } from './transacao';

export interface ResumoPagamentoResponse {
  formaPagamento: TipoPagamento | null;
  rotulo: string;
  total: number;
  quantidade: number;
  percentual: number;
}
