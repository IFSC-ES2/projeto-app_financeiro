import React from 'react';
import EmConstrucao from '../components/ui/EmConstrucao';

const Dashboard: React.FC = () => {
  return (
    <div style={{ padding: 32, minHeight: '100%' }}>
      <div style={estilos.header}>
        <div>
          <h1 style={estilos.titulo}>Dashboard</h1>
          <p style={estilos.subtitulo}>Visão geral das suas finanças</p>
        </div>
      </div>
      <EmConstrucao
        titulo="Dashboard em construção"
        descricao="Em breve você verá gráficos, resumos mensais, análise de gastos e muito mais sobre sua saúde financeira."
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

export default Dashboard;
