import React from 'react';

interface PropsBotaoCarregando extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  carregando: boolean;
  textoCarregando?: string;
  children: React.ReactNode;
  variante?: string;
}

const BotaoCarregando: React.FC<PropsBotaoCarregando> = ({
  carregando,
  textoCarregando = 'Aguarde...',
  children,
  variante = 'primary',
  className = '',
  disabled,
  ...rest
}) => {
  return (
    <button
      className={`btn btn-${variante} ${className}`}
      disabled={carregando || disabled}
      {...rest}
    >
      {carregando ? (
        <span className="d-flex align-items-center justify-content-center gap-2">
          <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true" />
          {textoCarregando}
        </span>
      ) : (
        children
      )}
    </button>
  );
};

export default BotaoCarregando;
