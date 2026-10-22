import type { TipoPagamento } from './transacao';

export interface ResumoPagamentoResponse {
  formaPagamento: TipoPagamento | null;
  rotulo: string;
  total: number;
  quantidade: number;
  percentual: number;
}

export interface ResumoMensal {
  ano: number;
  mes: number;
  dataInicio: string;
  dataFim: string;
  totalRecebido: number;
  totalGasto: number;
  saldo: number;
  categoriaMaiorGastoId: string | null;
  categoriaMaiorGastoNome: string | null;
  categoriaMaiorGastoTotal: number | null;
  variacaoPercentualGastos: number | null;
  possuiTransacoes: boolean;
}

export interface GrupoCategoria {
  categoriaID: string;
  nome: string;
  icone: string | null;
  cor: string | null;
  total: number;
  quantidade: number;
  percentual: number;
}
