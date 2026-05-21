import React, { useState } from 'react';
import LayoutPainel from '../components/layout/LayoutPainel';
import Dashboard from './Dashboard';
import Transacoes from './Transacoes';
import Categorias from './Categorias';
import { ProvedorTransacoes } from '../contexts/ContextoTransacoes';

type Aba = 'dashboard' | 'transacoes' | 'categorias';

const Painel: React.FC = () => {
  const [abaAtiva, setAbaAtiva] = useState<Aba>('dashboard');

  const renderConteudo = () => {
    switch (abaAtiva) {
      case 'dashboard':
        return <Dashboard />;
      case 'transacoes':
        return <Transacoes />;
      case 'categorias':
        return <Categorias />;
    }
  };

  return (
    <ProvedorTransacoes>
      <LayoutPainel abaAtiva={abaAtiva} onMudarAba={setAbaAtiva}>
        {renderConteudo()}
      </LayoutPainel>
    </ProvedorTransacoes>
  );
};

export default Painel;
