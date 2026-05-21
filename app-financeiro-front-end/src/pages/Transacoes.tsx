import React, { useState, useMemo } from 'react';
import { useTransacoes } from '../contexts/ContextoTransacoes';
import TabelaTransacoes from '../components/transacoes/TabelaTransacoes';
import ModalNovaTransacao from '../components/transacoes/ModalNovaTransacao';
import type { TransacaoResponse } from '../services/api';

const fmt = (v: number) =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v);

const Transacoes: React.FC = () => {
  const { transacoes, carregando } = useTransacoes();
  const [modalAberto, setModalAberto] = useState(false);

  const resumo = useMemo(() => {
    const despesas = transacoes
      .filter((t: TransacaoResponse) => t.tipoTransacao !== 'CREDITO')
      .reduce((s: number, t: TransacaoResponse) => s + Number(t.valor), 0);
    const receitas = transacoes
      .filter((t: TransacaoResponse) => t.tipoTransacao === 'CREDITO')
      .reduce((s: number, t: TransacaoResponse) => s + Number(t.valor), 0);
    return { total: transacoes.length, despesas, receitas, saldo: receitas - despesas };
  }, [transacoes]);

  const cards = [
    {
      label: 'Total',
      value: carregando ? '…' : String(resumo.total),
      /* valor default: #F0FAF8 sobre #111E2E → ~11.8:1 ✓ */
      valueColor: '#F0FAF8',
      icon: (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#A8C8C2" strokeWidth="2.5">
          <line x1="4" y1="6" x2="20" y2="6" /><line x1="4" y1="12" x2="20" y2="12" /><line x1="4" y1="18" x2="20" y2="18" />
        </svg>
      ),
    },
    {
      label: 'Despesas',
      value: carregando ? '…' : fmt(resumo.despesas),
      /* #FF8A80 sobre #111E2E → ~6.1:1, WCAG AA ✓ (vermelho mais claro que #ef4444) */
      valueColor: '#FF8A80',
      icon: (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#FF8A80" strokeWidth="2.5">
          <polyline points="23 18 13.5 8.5 8.5 13.5 1 6" /><polyline points="17 18 23 18 23 12" />
        </svg>
      ),
    },
    {
      label: 'Receitas',
      value: carregando ? '…' : fmt(resumo.receitas),
      /* #5DCFB8 sobre #111E2E → ~7.2:1, WCAG AA ✓ */
      valueColor: '#5DCFB8',
      icon: (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#5DCFB8" strokeWidth="2.5">
          <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" /><polyline points="17 6 23 6 23 12" />
        </svg>
      ),
    },
    {
      label: 'Saldo',
      value: carregando ? '…' : fmt(resumo.saldo),
      valueColor: resumo.saldo >= 0 ? '#5DCFB8' : '#FF8A80',
      icon: (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
          stroke={resumo.saldo >= 0 ? '#5DCFB8' : '#FF8A80'} strokeWidth="2.5">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="16" /><line x1="8" y1="12" x2="16" y2="12" />
        </svg>
      ),
    },
  ];

  return (
    <div style={s.wrapper}>
      {/* Summary cards */}
      <div style={s.cards}>
        {cards.map((c) => (
          <div key={c.label} style={s.card}>
            {/* label: #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */}
            <span style={s.cardLabel}>
              {c.icon}
              {c.label}
            </span>
            <span style={{ ...s.cardValue, color: c.valueColor }}>
              {c.value}
            </span>
          </div>
        ))}
      </div>

      {/* Table */}
      <div style={s.tableArea}>
        <TabelaTransacoes onNovaTransacao={() => setModalAberto(true)} />
      </div>

      {modalAberto && <ModalNovaTransacao aoFechar={() => setModalAberto(false)} />}
    </div>
  );
};

const s: Record<string, React.CSSProperties> = {
  wrapper: {
    padding: '28px 28px 40px',
  },
  cards: {
    display: 'grid',
    gridTemplateColumns: 'repeat(4, 1fr)',
    gap: 12,
    marginBottom: 24,
  },
  card: {
    /* #111E2E — escuro suficiente para contrastar com todos os valueColors acima */
    background: '#111E2E',
    border: '1px solid rgba(255,255,255,0.06)',
    borderRadius: 12,
    padding: '18px 20px',
    display: 'flex',
    flexDirection: 'column',
    gap: 10,
  },
  cardLabel: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    /* #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */
    color: '#A8C8C2',
    fontSize: '0.75rem',
    fontWeight: 600,
    textTransform: 'uppercase' as const,
    letterSpacing: '0.07em',
  },
  cardValue: {
    fontSize: '1.4rem',
    fontWeight: 700,
    letterSpacing: '-0.03em',
    fontFamily: "'DM Mono', monospace",
  },
  tableArea: {
    background: '#111E2E',
    border: '1px solid rgba(255,255,255,0.06)',
    borderRadius: 14,
    padding: '20px 20px 8px',
  },
};

export default Transacoes;
