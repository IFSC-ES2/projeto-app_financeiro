import React from 'react';

interface PropsCampoFormulario extends React.InputHTMLAttributes<HTMLInputElement> {
  rotulo: string;
  id: string;
  erro?: string;
  tocado?: boolean;
  icone?: React.ReactNode;
}

const CampoFormulario: React.FC<PropsCampoFormulario> = ({
  rotulo,
  id,
  erro,
  tocado,
  icone,
  className = '',
  ...rest
}) => {
  const mostrarErro = tocado && erro;

  return (
    <div className="mb-3">
      <label htmlFor={id} className="form-label fw-semibold text-secondary small mb-1">
        {rotulo}
      </label>
      <div className="input-group">
        {icone && (
          <span className="input-group-text bg-light border-end-0 text-muted">
            {icone}
          </span>
        )}
        <input
          id={id}
          className={`form-control ${icone ? 'border-start-0' : ''} ${
            mostrarErro ? 'is-invalid' : tocado ? 'is-valid' : ''
          } ${className}`}
          style={{ boxShadow: 'none' }}
          {...rest}
        />
        {mostrarErro && (
          <div className="invalid-feedback">{erro}</div>
        )}
      </div>
    </div>
  );
};

export default CampoFormulario;
