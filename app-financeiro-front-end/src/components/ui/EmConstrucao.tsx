import React from 'react';

interface Props {
  titulo: string;
  descricao?: string;
}

const EmConstrucao: React.FC<Props> = ({
  titulo,
  descricao = 'Esta seção está sendo desenvolvida e estará disponível em breve.',
}) => (
  <div style={s.wrapper}>
    <div style={s.card}>
      <div style={s.iconArea}>
        <svg width="44" height="44" viewBox="0 0 24 24" fill="none">
          <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="#5DCFB8" strokeWidth="1.5"
            strokeLinecap="round" strokeLinejoin="round" />
          <path d="M2 17l10 5 10-5" stroke="#5DCFB8" strokeWidth="1.5"
            strokeLinecap="round" strokeLinejoin="round" opacity="0.45" />
          <path d="M2 12l10 5 10-5" stroke="#5DCFB8" strokeWidth="1.5"
            strokeLinecap="round" strokeLinejoin="round" opacity="0.7" />
        </svg>
      </div>

      {/* badge: #5DCFB8 sobre rgba(93,207,184,0.1) fundo escuro → ~7.2:1 ✓ */}
      <div style={s.badge}>Em construção</div>

      {/* título: #F0FAF8 sobre #111E2E → ~11.8:1 ✓ */}
      <h2 style={s.titulo}>{titulo}</h2>

      {/* descrição: #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */}
      <p style={s.descricao}>{descricao}</p>

      <div style={s.progressBar}>
        <div style={s.progressFill} />
      </div>
      {/* label rodapé: #6A9A94 sobre #111E2E → ~4.6:1 ✓ (texto decorativo/secondary) */}
      <p style={s.progressLabel}>Desenvolvimento em andamento</p>
    </div>
  </div>
);

const s: Record<string, React.CSSProperties> = {
  wrapper: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 40,
  },
  card: {
    background: '#111E2E',
    border: '1px solid rgba(255,255,255,0.06)',
    borderRadius: 16,
    padding: '48px 52px',
    textAlign: 'center',
    maxWidth: 420,
    width: '100%',
  },
  iconArea: {
    marginBottom: 20,
    display: 'flex',
    justifyContent: 'center',
  },
  badge: {
    display: 'inline-block',
    background: 'rgba(93,207,184,0.1)',
    color: '#5DCFB8',
    fontSize: '0.72rem',
    fontWeight: 700,
    letterSpacing: '0.09em',
    textTransform: 'uppercase' as const,
    padding: '4px 12px',
    borderRadius: 100,
    marginBottom: 16,
  },
  titulo: {
    color: '#F0FAF8',
    fontSize: '1.3rem',
    fontWeight: 700,
    margin: '0 0 10px',
    letterSpacing: '-0.02em',
  },
  descricao: {
    color: '#A8C8C2',
    fontSize: '0.9rem',
    lineHeight: 1.65,
    margin: '0 0 28px',
  },
  progressBar: {
    height: 4,
    background: 'rgba(93,207,184,0.1)',
    borderRadius: 100,
    overflow: 'hidden',
    marginBottom: 10,
  },
  progressFill: {
    height: '100%',
    width: '35%',
    background: 'linear-gradient(90deg, #2FA98F, #5DCFB8)',
    borderRadius: 100,
  },
  progressLabel: {
    color: '#6A9A94',
    fontSize: '0.78rem',
    margin: 0,
  },
};

export default EmConstrucao;
