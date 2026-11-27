import type { TransacaoResponse } from '../../types/transacao';

export interface EstadoTransacoesCategoria {
  carregando: boolean;
  carregado: boolean;
  erro: string;
  transacoes: TransacaoResponse[];
  total: number;
}
