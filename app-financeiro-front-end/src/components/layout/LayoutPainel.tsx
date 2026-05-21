import React from 'react';
import { useAutenticacao } from '../../contexts/ContextoAutenticacao';

interface Props {
  children: React.ReactNode;
  abaAtiva: 'dashboard' | 'transacoes' | 'categorias';
  onMudarAba: (aba: 'dashboard' | 'transacoes' | 'categorias') => void;
}

const NAVBAR_HEIGHT = 62;

const abas = [
  {
    id: 'dashboard' as const,
    label: 'Visão geral',
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" />
        <rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" />
      </svg>
    ),
  },
  {
    id: 'transacoes' as const,
    label: 'Transações',
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M7 16V4m0 0L3 8m4-4l4 4" /><path d="M17 8v12m0 0l4-4m-4 4l-4-4" />
      </svg>
    ),
  },
  {
    id: 'categorias' as const,
    label: 'Categorias',
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" />
        <line x1="7" y1="7" x2="7.01" y2="7" />
      </svg>
    ),
  },
];

const LayoutPainel: React.FC<Props> = ({ children, abaAtiva, onMudarAba }) => {
  const { sair } = useAutenticacao();

  return (
    <div style={s.root}>

      {/* ── Navbar fixa no topo ── */}
      <header style={s.navbar}>
        <div style={s.logo}>
          <img src="/smartbudget-logo.png" alt="SmartBudget" width="30" height="30" style={{ objectFit: "contain" }} />
          {/* "SmartBudget" — #F0FAF8 sobre #0E1624 → contraste ~12:1 ✓ */}
          <span style={s.logoText}>SmartBudget</span>
        </div>

        <nav style={s.tabs} role="navigation" aria-label="Navegação principal">
          {abas.map((aba) => {
            const ativo = abaAtiva === aba.id;
            return (
              <button
                key={aba.id}
                onClick={() => onMudarAba(aba.id)}
                aria-current={ativo ? 'page' : undefined}
                style={{
                  ...s.tab,
                  ...(ativo ? s.tabAtiva : s.tabInativa),
                }}
              >
                {aba.icon}
                {aba.label}
              </button>
            );
          })}
        </nav>

        {/* "Sair" — #C8DDD9 sobre #0E1624 → contraste ~8.5:1 ✓ */}
        <button onClick={sair} style={s.sairBtn} aria-label="Sair da conta">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          Sair
        </button>
      </header>

      {/* ── Conteúdo — começa abaixo da navbar ── */}
      <main
        style={{ ...s.main, paddingTop: NAVBAR_HEIGHT }}
        id="main-content"
        tabIndex={-1}
      >
        {children}
      </main>

    </div>
  );
};

const s: Record<string, React.CSSProperties> = {
  root: {
    minHeight: '100vh',
    background: '#0B1520',
    fontFamily: "'DM Sans', system-ui, sans-serif",
    display: 'flex',
    flexDirection: 'column',
  },

  /* Navbar */
  navbar: {
    display: 'flex',
    alignItems: 'center',
    gap: 20,
    padding: '0 28px',
    height: 62,
    background: '#0E1624',
    /* borda inferior sutil mas visível: contraste suficiente sem poluir */
    borderBottom: '1px solid rgba(255,255,255,0.08)',
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    zIndex: 100,
  },

  /* Logo */
  logo: {
    display: 'flex',
    alignItems: 'center',
    gap: 9,
    flexShrink: 0,
    textDecoration: 'none',
  },
  logoText: {
    /* #F0FAF8 sobre #0E1624 — ratio ~12.4:1, WCAG AAA ✓ */
    color: '#F0FAF8',
    fontWeight: 700,
    fontSize: '0.95rem',
    letterSpacing: '-0.02em',
  },

  /* Tabs */
  tabs: {
    display: 'flex',
    alignItems: 'center',
    gap: 2,
    flex: 1,
    justifyContent: 'center',
  },
  tab: {
    display: 'flex',
    alignItems: 'center',
    gap: 7,
    padding: '7px 16px',
    borderRadius: 100,
    border: '1px solid transparent',
    background: 'transparent',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
    transition: 'all 0.15s ease',
    fontFamily: "'DM Sans', system-ui, sans-serif",
    whiteSpace: 'nowrap',
  },
  /* Inativa: #A8C8C2 sobre #0E1624 — ratio ~6.8:1, WCAG AA ✓ */
  tabInativa: {
    color: '#A8C8C2',
  },
  /* Ativa: #2FA98F sobre fundo com leve overlay — pill visível */
  tabAtiva: {
    background: 'rgba(47,169,143,0.15)',
    border: '1px solid rgba(47,169,143,0.35)',
    /* #5DCFB8 sobre #0F2519 escuro → ratio ~5.2:1, WCAG AA ✓ */
    color: '#5DCFB8',
  },

  /* Botão Sair */
  sairBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    padding: '7px 14px',
    background: 'transparent',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: 8,
    /* #C8DDD9 sobre #0E1624 — ratio ~8.5:1, WCAG AAA ✓ */
    color: '#C8DDD9',
    fontSize: '0.8rem',
    fontWeight: 500,
    cursor: 'pointer',
    fontFamily: "'DM Sans', system-ui, sans-serif",
    flexShrink: 0,
    transition: 'color 0.15s, border-color 0.15s',
  },

  /* Main — paddingTop empurra conteúdo abaixo da navbar fixa */
  main: {
    flex: 1,
    minHeight: '100vh',
  },
};

export default LayoutPainel;
