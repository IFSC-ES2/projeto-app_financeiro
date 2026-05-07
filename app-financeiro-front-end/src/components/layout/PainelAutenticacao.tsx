import React from 'react';

interface PropsPainelAutenticacao {
  titulo: string;
  subtitulo: string;
}

const PainelAutenticacao: React.FC<PropsPainelAutenticacao> = ({ titulo, subtitulo }) => {
  return (
    <div
      className="d-none d-lg-flex flex-column justify-content-center align-items-start p-5 position-relative overflow-hidden"
      style={{
        background: 'linear-gradient(145deg, #5b2d8e 0%, #7c3fbd 40%, #9b5fd4 70%, #b57fe8 100%)',
        minHeight: '100%',
        flex: '0 0 45%',
      }}
    >
      {/* Formas decorativas topográficas */}
      <svg
        className="position-absolute top-0 start-0 w-100 h-100"
        viewBox="0 0 500 600"
        xmlns="http://www.w3.org/2000/svg"
        style={{ opacity: 0.18, pointerEvents: 'none' }}
        preserveAspectRatio="xMidYMid slice"
      >
        <ellipse cx="80" cy="500" rx="280" ry="280" fill="none" stroke="white" strokeWidth="1.2" />
        <ellipse cx="80" cy="500" rx="240" ry="240" fill="none" stroke="white" strokeWidth="1" />
        <ellipse cx="80" cy="500" rx="200" ry="200" fill="none" stroke="white" strokeWidth="0.8" />
        <ellipse cx="80" cy="500" rx="160" ry="160" fill="none" stroke="white" strokeWidth="0.8" />
        <ellipse cx="80" cy="500" rx="120" ry="120" fill="none" stroke="white" strokeWidth="0.8" />
        <ellipse cx="420" cy="80" rx="200" ry="200" fill="none" stroke="white" strokeWidth="1" />
        <ellipse cx="420" cy="80" rx="160" ry="160" fill="none" stroke="white" strokeWidth="0.8" />
        <ellipse cx="420" cy="80" rx="120" ry="120" fill="none" stroke="white" strokeWidth="0.8" />
        <path d="M0,300 Q100,250 200,300 T400,300 T600,300" fill="none" stroke="white" strokeWidth="1" />
        <path d="M0,330 Q100,280 200,330 T400,330 T600,330" fill="none" stroke="white" strokeWidth="0.7" />
        <path d="M0,360 Q100,310 200,360 T400,360 T600,360" fill="none" stroke="white" strokeWidth="0.5" />
      </svg>

      {/* Círculos flutuantes */}
      <div className="position-absolute" style={{ top: '12%', right: '15%', opacity: 0.25 }}>
        <svg width="40" height="40" viewBox="0 0 40 40">
          <circle cx="20" cy="20" r="18" fill="none" stroke="white" strokeWidth="1.5" />
          <circle cx="20" cy="20" r="10" fill="none" stroke="white" strokeWidth="1" />
        </svg>
      </div>
      <div className="position-absolute" style={{ bottom: '18%', right: '10%', opacity: 0.2 }}>
        <svg width="28" height="28" viewBox="0 0 28 28">
          <circle cx="14" cy="14" r="12" fill="none" stroke="white" strokeWidth="1.5" />
        </svg>
      </div>
      <div className="position-absolute" style={{ top: '35%', left: '8%', opacity: 0.18 }}>
        <svg width="12" height="12" viewBox="0 0 12 12">
          <path d="M6 0 L12 6 L6 12 L0 6 Z" fill="white" />
        </svg>
      </div>
      <div className="position-absolute" style={{ top: '15%', left: '30%', opacity: 0.2 }}>
        <svg width="8" height="8" viewBox="0 0 8 8">
          <circle cx="4" cy="4" r="3" fill="white" />
        </svg>
      </div>
      <div className="position-absolute" style={{ bottom: '30%', left: '20%', opacity: 0.15 }}>
        <svg width="16" height="16" viewBox="0 0 16 16">
          <rect x="2" y="2" width="12" height="12" rx="2" fill="none" stroke="white" strokeWidth="1.5" />
        </svg>
      </div>
      {/* Grade de pontos */}
      <div className="position-absolute" style={{ top: '10%', right: '8%', opacity: 0.3 }}>
        <svg width="60" height="60" viewBox="0 0 60 60">
          {[0, 1, 2, 3].map((linha) =>
            [0, 1, 2, 3].map((coluna) => (
              <circle
                key={`${linha}-${coluna}`}
                cx={coluna * 18 + 6}
                cy={linha * 18 + 6}
                r="2"
                fill="white"
              />
            ))
          )}
        </svg>
      </div>

      {/* Logo da marca */}
      <div className="position-relative mb-auto" style={{ zIndex: 1, paddingTop: '1rem' }}>
        <div className="d-flex align-items-center gap-2 mb-2">
          <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
            <rect width="36" height="36" rx="10" fill="rgba(255,255,255,0.2)" />
            <path
              d="M10 24 L14 16 L18 20 L22 12 L26 18"
              stroke="white"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              fill="none"
            />
            <circle cx="26" cy="18" r="2" fill="white" />
          </svg>
          <span
            className="fw-bold text-white"
            style={{ fontSize: '1.3rem', letterSpacing: '-0.02em', fontFamily: 'system-ui, sans-serif' }}
          >
            SmartBudget
          </span>
        </div>
      </div>

      {/* Mensagem principal */}
      <div className="position-relative" style={{ zIndex: 1, marginTop: 'auto', marginBottom: 'auto' }}>
        <h1
          className="fw-bold text-white mb-3"
          style={{ fontSize: 'clamp(1.8rem, 3vw, 2.4rem)', lineHeight: 1.2, letterSpacing: '-0.02em' }}
        >
          {titulo}
        </h1>
        <p
          className="text-white mb-0"
          style={{ opacity: 0.85, fontSize: '1rem', maxWidth: '280px', lineHeight: 1.6 }}
        >
          {subtitulo}
        </p>
      </div>

      <div style={{ marginTop: 'auto' }} />
    </div>
  );
};

export default PainelAutenticacao;
