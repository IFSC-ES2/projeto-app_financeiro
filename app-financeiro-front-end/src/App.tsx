import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import { ProvedorAutenticacao } from './contexts/ContextoAutenticacao';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import NovaTransacao from './pages/NovaTransacao';
import NovaConta from './pages/NovaConta';
import ImportarExtrato from './pages/ImportarExtrato';

// Dashboard provisório — substituir pela tela oficial
const PainelProvisorio = () => (
  <div style={{ textAlign: 'center', marginTop: '50px', fontFamily: 'system-ui, sans-serif' }}>
    <h2>Painel</h2>
    <p>Login realizado com sucesso! (Tela oficial em construção)</p>
    <Link
      to="/importacoes/nova"
      className="btn fw-semibold mt-3"
      style={{
        background: 'var(--sb-gradient)',
        color: '#fff',
        borderRadius: 8,
        padding: '0.6rem 1.4rem',
      }}
    >
      Importar extrato ou NF-e
    </Link>
  </div>
);

function App() {
  return (
    <ProvedorAutenticacao>
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route path="/dashboard" element={<PainelProvisorio />} />
          <Route path="/transacoes/nova" element={<NovaTransacao />} />
          <Route path="/contas/nova" element={<NovaConta />} />
          <Route path="/importacoes/nova" element={<ImportarExtrato />} />
        </Routes>
      </Router>
    </ProvedorAutenticacao>
  );
}

export default App;
