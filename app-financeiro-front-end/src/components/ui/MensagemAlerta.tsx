interface PropsMensagemAlerta {
  mensagem: string;
  tipo?: 'danger' | 'success' | 'warning' | 'info';
}

const MensagemAlerta = ({ mensagem, tipo = 'danger' }: PropsMensagemAlerta) => {
  if (!mensagem) return null;

  return (
    <div className={`sb-alert sb-alert-${tipo}`} role="alert">
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M12 9v4m0 4h.01M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
      </svg>
      <span>{mensagem}</span>
    </div>
  );
};

export default MensagemAlerta;
