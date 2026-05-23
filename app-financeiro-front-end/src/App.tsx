import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ProvedorAutenticacao } from './contexts/ContextoAutenticacao';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import NovaTransacao from './pages/NovaTransacao';
import { NovaConta } from './pages/NovaConta';

// Dashboard provisório — substituir pela tela oficial
const PainelProvisorio = () => (
  <div style={{ textAlign: 'center', marginTop: '50px', fontFamily: 'system-ui, sans-serif' }}>
    <h2>Painel</h2>
    <p>Login realizado com sucesso! (Tela oficial em construção)</p>
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
        </Routes>
      </Router>
    </ProvedorAutenticacao>
  );
}

export default App;
