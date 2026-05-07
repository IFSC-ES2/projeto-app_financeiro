import React from 'react';

interface PropsMensagemAlerta {
  mensagem: string;
  tipo?: 'danger' | 'success' | 'warning' | 'info';
}

const MensagemAlerta: React.FC<PropsMensagemAlerta> = ({ mensagem, tipo = 'danger' }) => {
  if (!mensagem) return null;

  const icones: Record<string, string> = {
    danger: '⚠️',
    success: '✅',
    warning: '⚠️',
    info: 'ℹ️',
  };

  return (
    <div className={`alert alert-${tipo} py-2 px-3 d-flex align-items-center gap-2 small`} role="alert">
      <span>{icones[tipo]}</span>
      <span>{mensagem}</span>
    </div>
  );
};

export default MensagemAlerta;
