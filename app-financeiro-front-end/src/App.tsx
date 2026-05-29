import { BrowserRouter as Router, Navigate, Route, Routes } from 'react-router-dom';
import { ProvedorAutenticacao } from './contexts/ContextoAutenticacao';
import RotaPrivada from './routes/RotaPrivada';
import RotaPublica from './routes/RotaPublica';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import NovaConta from './pages/NovaConta';
import Dashboard from './pages/Dashboard';
import Transacoes from './pages/Transacoes';
import NovaTransacao from './pages/NovaTransacao';
import Categorias from './pages/Categorias';
import Parcelamentos from './pages/Parcelamentos';

function App() {
  return (
    <ProvedorAutenticacao>
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />

          <Route element={<RotaPublica />}>
            <Route path="/login" element={<Login />} />
            <Route path="/cadastro" element={<Cadastro />} />
          </Route>

          <Route element={<RotaPrivada />}>
            <Route path="/contas/nova" element={<NovaConta />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/transacoes" element={<Transacoes />} />
            <Route path="/transacoes/nova" element={<NovaTransacao />} />
            <Route path="/categorias" element={<Categorias />} />
            <Route path="/parcelamentos" element={<Parcelamentos />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </ProvedorAutenticacao>
  );
}

export default App;
