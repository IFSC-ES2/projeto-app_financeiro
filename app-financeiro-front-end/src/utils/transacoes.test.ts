import { describe, expect, it } from 'vitest';
import type { TransacaoResponse } from '../services/api';
import { calcularResumoTransacoes, filtrarTransacoes } from './transacoes';

const transacoes: TransacaoResponse[] = [
  {
    transacaoId: '1',
    valor: 100,
    data: '2026-05-10',
    descricao: 'Salário',
    tipoTransacao: 'CREDITO',
    formaPagamento: 'PIX',
    categoriaId: 'cat-1',
    contaId: 'conta-1',
  },
  {
    transacaoId: '2',
    valor: 40,
    data: '2026-05-11',
    descricao: 'Mercado',
    tipoTransacao: 'DEBITO',
    formaPagamento: 'CARTAO_DEBITO',
    categoriaId: 'cat-2',
    contaId: 'conta-1',
  },
  {
    transacaoId: '3',
    valor: 20,
    data: '2026-04-15',
    descricao: 'Transporte',
    tipoTransacao: 'DEBITO',
    formaPagamento: 'PIX',
    categoriaId: 'cat-3',
    contaId: 'conta-2',
  },
];

describe('filtros de transações', () => {
  it('filtra transações do mês atual pela data de referência', () => {
    const resultado = filtrarTransacoes(
      transacoes,
      { periodo: 'MES_ATUAL', tipo: 'TODAS', categoriaId: '', contaId: '' },
      new Date('2026-05-20T00:00:00')
    );

    expect(resultado.map((transacao) => transacao.transacaoId)).toEqual(['1', '2']);
  });

  it('filtra somente despesas', () => {
    const resultado = filtrarTransacoes(
      transacoes,
      { periodo: 'TODAS', tipo: 'DESPESAS', categoriaId: '', contaId: '' },
      new Date('2026-05-20T00:00:00')
    );

    expect(resultado.map((transacao) => transacao.transacaoId)).toEqual(['2', '3']);
  });

  it('calcula totais separando receitas e despesas', () => {
    expect(calcularResumoTransacoes(transacoes)).toEqual({
      total: 3,
      receitas: 100,
      despesas: 60,
      saldo: 40,
    });
  });
});
