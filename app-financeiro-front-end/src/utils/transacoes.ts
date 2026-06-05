import type { TransacaoResponse } from '../services/api';

export interface ResumoTransacoes {
  total: number;
  receitas: number;
  despesas: number;
  saldo: number;
}

export const ehReceita = (transacao: TransacaoResponse) => transacao.tipoTransacao === 'CREDITO';

export const normalizarValor = (transacao: TransacaoResponse) => Number(transacao.valor || 0);

export const calcularResumoTransacoes = (transacoes: TransacaoResponse[]): ResumoTransacoes => {
  const resumo = transacoes.reduce(
    (acumulado, transacao) => {
      const valor = normalizarValor(transacao);

      if (ehReceita(transacao)) {
        return { ...acumulado, receitas: acumulado.receitas + valor };
      }

      return { ...acumulado, despesas: acumulado.despesas + valor };
    },
    { receitas: 0, despesas: 0 }
  );

  return {
    total: transacoes.length,
    receitas: resumo.receitas,
    despesas: resumo.despesas,
    saldo: resumo.receitas - resumo.despesas,
  };
};
