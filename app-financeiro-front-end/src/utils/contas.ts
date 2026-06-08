import type { ContaResponse } from '../services/api';

export const ehCarteiraAutomaticaDinheiro = (conta: ContaResponse) => {
  const nome = conta.nome.trim().toLowerCase();
  const banco = (conta.banco || '').trim().toLowerCase();
  const descricao = (conta.descricao || '').trim().toLowerCase();

  return (
    conta.tipoConta === 'CARTEIRA' &&
    banco === 'dinheiro' &&
    nome.startsWith('dinheiro / carteira') &&
    descricao.includes('transações em dinheiro')
  );
};
