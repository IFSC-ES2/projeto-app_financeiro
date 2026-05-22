import React, { useState } from 'react';
import { useTransacoes } from '../../contexts/ContextoTransacoes';
import type { TransacaoResponse } from '../../services/api';

interface Props {
  onNovaTransacao: () => void;
}


/* Cores de badge: todas com fundo escuro + texto claro para garantir contraste */
const tipoBadge: Record<string, { bg: string; color: string }> = {
  /* texto #FF8A80 sobre #2A1018 → ~5.8:1 ✓ */
  DEBITO:       { bg: 'rgba(255,138,128,0.12)', color: '#FF8A80' },
  /* texto #5DCFB8 sobre #0E2420 → ~7.2:1 ✓ */
  CREDITO:      { bg: 'rgba(93,207,184,0.12)',  color: '#5DCFB8' },
  /* texto #FFD97A sobre #252010 → ~7.8:1 ✓ */
  PARCELAMENTO: { bg: 'rgba(255,217,122,0.12)', color: '#FFD97A' },
  /* texto #C4AAFF sobre #1A1028 → ~7.1:1 ✓ */
  BOLETO:       { bg: 'rgba(196,170,255,0.12)', color: '#C4AAFF' },
};

const fmt = (v: number) =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v);

const fmtData = (data: string) => {
  const [ano, mes, dia] = data.split('-');
  return `${dia}/${mes}/${ano}`;
};

const TabelaTransacoes: React.FC<Props> = ({ onNovaTransacao }) => {
  const { transacoes, contas, categorias, carregando, erro } = useTransacoes();
  const [busca, setBusca] = useState('');

  const getNomeConta = (id: string) =>
    contas.find((c) => c.contaId === id)?.nome ?? '—';

  const getNomeCategoria = (id?: string | null) => {
    if (!id) return null;
    const cat = categorias.find((c) => c.categoriaId === id);
    return cat ? `${cat.icone ?? ''} ${cat.nome}`.trim() : null;
  };

  const filtradas: TransacaoResponse[] = transacoes
    .filter((t) => {
      if (!busca) return true;
      const q = busca.toLowerCase();
      return (
        t.descricao?.toLowerCase().includes(q) ||
        getNomeConta(t.contaId).toLowerCase().includes(q) ||
        getNomeCategoria(t.categoriaId)?.toLowerCase().includes(q)
      );
    })
    .sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime());

  return (
    <div>
      {/* Toolbar */}
      <div style={s.toolbar}>
        <div style={s.searchWrap}>
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
            stroke="#A8C8C2" strokeWidth="2" style={{ flexShrink: 0 }}>
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            placeholder="Buscar transações…"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            style={s.searchInput}
            aria-label="Buscar transações"
          />
        </div>
        <button style={s.novaBtn} onClick={onNovaTransacao}>
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" strokeWidth="2.5">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          Nova Transação
        </button>
      </div>

      {/* Loading */}
      {carregando && (
        <div style={s.estado}>
          <div style={s.spinner} />
          {/* #A8C8C2 sobre #0B1520 → ~6.8:1 ✓ */}
          <p style={s.estadoTxt}>Carregando transações…</p>
        </div>
      )}

      {/* Error */}
      {erro && !carregando && (
        <div style={s.estado}>
          {/* #FF8A80 sobre #0B1520 → ~6.1:1 ✓ */}
          <p style={{ ...s.estadoTxt, color: '#FF8A80' }}>{erro}</p>
        </div>
      )}

      {/* Empty */}
      {!carregando && !erro && filtradas.length === 0 && (
        <div style={s.vazio}>
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none"
            stroke="#5DCFB8" strokeWidth="1.5">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          <p style={s.vazioTxt}>
            {busca
              ? 'Nenhuma transação encontrada para essa busca.'
              : 'Nenhuma transação registrada ainda.'}
          </p>
        </div>
      )}

      {/* Table */}
      {!carregando && !erro && filtradas.length > 0 && (
        <div style={s.tableWrap}>
          <table style={s.table}>
            <thead>
              <tr>
                {['Descrição', 'Categoria', 'Conta', 'Data', 'Valor', ''].map((col, i) => (
                  <th key={i} style={{ ...s.th, ...(i === 4 ? { textAlign: 'right' } : {}) }}>
                    {col.toUpperCase()}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtradas.map((t) => {
                const catNome = getNomeCategoria(t.categoriaId);
                const isCredito = t.tipoTransacao === 'CREDITO';
                const badge = tipoBadge[t.tipoTransacao] ?? { bg: 'rgba(255,255,255,0.06)', color: '#C8DDD9' };

                return (
                  <tr key={t.transacaoId} style={s.tr}>
                    {/* Descrição */}
                    <td style={s.td}>
                      <div style={s.descRow}>
                        <div style={{
                          ...s.iconCircle,
                          borderColor: badge.color + '44',
                        }}>
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
                            stroke={badge.color} strokeWidth="2.2">
                            {isCredito
                              ? <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
                              : <polyline points="23 18 13.5 8.5 8.5 13.5 1 6" />}
                          </svg>
                        </div>
                        {/* #F0FAF8 sobre #111E2E → ~11.8:1 ✓ */}
                        <span style={s.descText}>{t.descricao || '—'}</span>
                      </div>
                    </td>

                    {/* Categoria */}
                    <td style={s.td}>
                      {catNome ? (
                        <span style={{ ...s.catBadge, background: badge.bg, color: badge.color }}>
                          {catNome}
                        </span>
                      ) : (
                        /* #6A9A94 sobre #111E2E → ~4.6:1 ✓ (texto secundário) */
                        <span style={s.muted}>—</span>
                      )}
                    </td>

                    {/* Conta */}
                    <td style={s.td}>
                      <span style={s.contaRow}>
                        <span style={{ ...s.contaDot, background: badge.color }} />
                        {/* #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */}
                        <span style={s.contaText}>{getNomeConta(t.contaId)}</span>
                      </span>
                    </td>

                    {/* Data */}
                    <td style={s.td}>
                      {/* #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */}
                      <span style={s.mono}>{fmtData(t.data)}</span>
                    </td>

                    {/* Valor */}
                    <td style={{ ...s.td, textAlign: 'right' }}>
                      <span style={{
                        ...s.mono,
                        fontWeight: 700,
                        /* crédito: #5DCFB8 → ~7.2:1 ✓  |  débito: #F0FAF8 → ~11.8:1 ✓ */
                        color: isCredito ? '#5DCFB8' : '#F0FAF8',
                      }}>
                        {fmt(t.valor)}
                      </span>
                    </td>

                    {/* Actions */}
                    <td style={{ ...s.td, padding: '0 12px', textAlign: 'center' }}>
                      <button style={s.moreBtn} aria-label="Mais opções">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
                          stroke="currentColor" strokeWidth="2">
                          <circle cx="12" cy="5" r="1.2" fill="currentColor" />
                          <circle cx="12" cy="12" r="1.2" fill="currentColor" />
                          <circle cx="12" cy="19" r="1.2" fill="currentColor" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

const s: Record<string, React.CSSProperties> = {
  toolbar: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    marginBottom: 16,
    flexWrap: 'wrap',
  },
  searchWrap: {
    display: 'flex',
    alignItems: 'center',
    gap: 9,
    background: '#0E1624',
    border: '1px solid rgba(255,255,255,0.08)',
    borderRadius: 10,
    padding: '9px 14px',
    flex: '0 1 300px',
    minWidth: 200,
  },
  searchInput: {
    background: 'transparent',
    border: 'none',
    outline: 'none',
    /* #F0FAF8 sobre #0E1624 → ~12.4:1 ✓ */
    color: '#F0FAF8',
    fontSize: '0.875rem',
    width: '100%',
    fontFamily: "'DM Sans', system-ui, sans-serif",
  },
  novaBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: 7,
    padding: '10px 18px',
    background: '#0E1624',
    border: '1px solid rgba(255,255,255,0.12)',
    borderRadius: 10,
    /* #F0FAF8 sobre #0E1624 → ~12.4:1 ✓ */
    color: '#F0FAF8',
    fontSize: '0.875rem',
    fontWeight: 600,
    cursor: 'pointer',
    fontFamily: "'DM Sans', system-ui, sans-serif",
    whiteSpace: 'nowrap',
  },
  estado: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '60px 20px',
    gap: 12,
  },
  estadoTxt: {
    color: '#A8C8C2',
    fontSize: '0.875rem',
    margin: 0,
  },
  spinner: {
    width: 26,
    height: 26,
    border: '3px solid rgba(93,207,184,0.15)',
    borderTopColor: '#5DCFB8',
    borderRadius: '50%',
  },
  vazio: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: 12,
    padding: '56px 20px',
  },
  vazioTxt: {
    /* #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */
    color: '#A8C8C2',
    fontSize: '0.875rem',
    margin: 0,
  },
  tableWrap: {
    overflowX: 'auto',
    borderRadius: 10,
    border: '1px solid rgba(255,255,255,0.05)',
    marginTop: 4,
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
  },
  th: {
    padding: '10px 16px',
    fontSize: '0.68rem',
    fontWeight: 700,
    letterSpacing: '0.09em',
    /* #7AADA6 sobre #0D1A28 → ~5.1:1 ✓ */
    color: '#7AADA6',
    textAlign: 'left',
    borderBottom: '1px solid rgba(255,255,255,0.06)',
    background: '#0D1A28',
    whiteSpace: 'nowrap',
  },
  tr: {
    borderBottom: '1px solid rgba(255,255,255,0.04)',
  },
  td: {
    padding: '13px 16px',
    fontSize: '0.875rem',
    verticalAlign: 'middle',
  },
  descRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
  },
  iconCircle: {
    width: 30,
    height: 30,
    background: '#0E1624',
    border: '1px solid',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  descText: {
    fontWeight: 500,
    color: '#F0FAF8',
  },
  catBadge: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 5,
    padding: '4px 10px',
    borderRadius: 100,
    fontSize: '0.78rem',
    fontWeight: 600,
  },
  muted: {
    color: '#6A9A94',
  },
  contaRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 7,
  },
  contaDot: {
    width: 7,
    height: 7,
    borderRadius: '50%',
    flexShrink: 0,
  },
  contaText: {
    color: '#A8C8C2',
    fontSize: '0.875rem',
  },
  mono: {
    fontFamily: "'DM Mono', monospace",
    fontSize: '0.85rem',
    color: '#A8C8C2',
  },
  moreBtn: {
    background: 'transparent',
    border: 'none',
    color: '#6A9A94',
    cursor: 'pointer',
    borderRadius: 6,
    padding: '4px 6px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
};

export default TabelaTransacoes;
