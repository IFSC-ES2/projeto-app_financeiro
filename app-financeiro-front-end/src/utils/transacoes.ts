import type { TransacaoResponse } from '../services/api';

export type FiltroPeriodo = 'MES_ATUAL' | 'TODAS';
export type FiltroTipo = 'TODAS' | 'RECEITAS' | 'DESPESAS';

export interface FiltrosTransacao {
  periodo: FiltroPeriodo;
  tipo: FiltroTipo;
  categoriaId: string;
  contaId: string;
}

export interface ResumoTransacoes {
  total: number;
  receitas: number;
  despesas: number;
  saldo: number;
}

export const ehReceita = (transacao: TransacaoResponse) => transacao.tipoTransacao === 'CREDITO';

export const normalizarValor = (transacao: TransacaoResponse) => Number(transacao.valor || 0);

const dataNoMesAtual = (data: string, referencia: Date) => {
  const [ano, mes] = data.split('-').map(Number);

  return ano === referencia.getFullYear() && mes === referencia.getMonth() + 1;
};

export const filtrarTransacoes = (
  transacoes: TransacaoResponse[],
  filtros: FiltrosTransacao,
  referencia: Date = new Date()
) =>
  transacoes.filter((transacao) => {
    if (filtros.periodo === 'MES_ATUAL' && !dataNoMesAtual(transacao.data, referencia)) return false;
    if (filtros.tipo === 'RECEITAS' && !ehReceita(transacao)) return false;
    if (filtros.tipo === 'DESPESAS' && ehReceita(transacao)) return false;
    if (filtros.categoriaId && transacao.categoriaId !== filtros.categoriaId) return false;
    if (filtros.contaId && transacao.contaId !== filtros.contaId) return false;

    return true;
  });

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

export const ordenarTransacoesPorDataDesc = (transacoes: TransacaoResponse[]) =>
  [...transacoes].sort((a, b) => b.data.localeCompare(a.data));
