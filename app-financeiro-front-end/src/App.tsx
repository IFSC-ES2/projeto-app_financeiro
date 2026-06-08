import { BrowserRouter as Router, Navigate, Route, Routes } from 'react-router-dom';
import { ProvedorAutenticacao } from './contexts/ContextoAutenticacao';
import RotaPrivada from './routes/RotaPrivada';
import RotaPublica from './routes/RotaPublica';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import NovaConta from './pages/NovaConta';
import ImportarExtrato from './pages/ImportarExtrato';
import Dashboard from './pages/Dashboard';
import Transacoes from './pages/Transacoes';
import NovaTransacao from './pages/NovaTransacao';
import EditarTransacao from './pages/EditarTransacao';
import Categorias from './pages/Categorias';
import Parcelamentos from './pages/Parcelamentos';
import PrimeiraConta from './pages/PrimeiraConta';

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
            <Route path="/contas" element={<NovaConta />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/transacoes" element={<Transacoes />} />
            <Route path="/transacoes/nova" element={<NovaTransacao />} />
            <Route path="/transacoes/:transacaoId/editar" element={<EditarTransacao />} />
            <Route path="/categorias" element={<Categorias />} />
            <Route path="/parcelamentos" element={<Parcelamentos />} />
            <Route path="/importacoes/nova" element={<ImportarExtrato />} />
            <Route path="/contas/primeira" element={<PrimeiraConta />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </ProvedorAutenticacao>
  );
}

export default App;
