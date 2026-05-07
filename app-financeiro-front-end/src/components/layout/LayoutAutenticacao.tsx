import React from 'react';
import PainelAutenticacao from './PainelAutenticacao';

interface PropsLayoutAutenticacao {
  tituloPainel: string;
  subtituloPainel: string;
  children: React.ReactNode;
}

const LayoutAutenticacao: React.FC<PropsLayoutAutenticacao> = ({ tituloPainel, subtituloPainel, children }) => {
  return (
    <div
      className="d-flex align-items-stretch"
      style={{ minHeight: '100vh', background: 'var(--sb-bg)' }}
    >
      {/* Painel decorativo esquerdo */}
      <PainelAutenticacao titulo={tituloPainel} subtitulo={subtituloPainel} />

      {/* Área do formulário direito */}
      <div
        className="flex-grow-1 d-flex align-items-center justify-content-center p-4"
        style={{ background: 'var(--sb-surface)' }}
      >
        <div style={{ width: '100%', maxWidth: '420px' }}>
          {children}
        </div>
      </div>
    </div>
  );
};

export default LayoutAutenticacao;
