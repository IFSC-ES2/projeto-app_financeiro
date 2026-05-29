import type { ReactNode } from 'react';

interface PropsEstadoVazio {
  titulo: string;
  descricao: string;
  acao?: ReactNode;
}

const EstadoVazio = ({ titulo, descricao, acao }: PropsEstadoVazio) => (
  <section className="empty-state" aria-live="polite">
    <div className="empty-state-icon" aria-hidden="true">
      <svg viewBox="0 0 24 24">
        <path d="M4 6h16M4 12h16M4 18h10" />
      </svg>
    </div>
    <h2>{titulo}</h2>
    <p>{descricao}</p>
    {acao}
  </section>
);

export default EstadoVazio;
