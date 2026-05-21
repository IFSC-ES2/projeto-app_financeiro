import React from 'react';
import EmConstrucao from '../components/ui/EmConstrucao';

const Categorias: React.FC = () => {
  return (
    <div style={{ padding: 32, minHeight: '100%' }}>
      <div style={estilos.header}>
        <div>
          <h1 style={estilos.titulo}>Categorias</h1>
          <p style={estilos.subtitulo}>Organize seus gastos por categoria</p>
        </div>
      </div>
      <EmConstrucao
        titulo="Categorias em construção"
        descricao="Em breve você poderá criar, editar e organizar categorias personalizadas para classificar suas transações."
      />
    </div>
  );
};

const estilos: Record<string, React.CSSProperties> = {
  header: {
    marginBottom: 8,
  },
  titulo: {
    color: '#E8F4F1',
    fontSize: '1.5rem',
    fontWeight: 700,
    margin: '0 0 4px',
    letterSpacing: '-0.03em',
  },
  subtitulo: {
    color: '#4A7A72',
    fontSize: '0.875rem',
    margin: 0,
  },
};

export default Categorias;
