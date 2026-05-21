import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ProvedorAutenticacao } from './contexts/ContextoAutenticacao';
import RotaProtegida from './components/layout/RotaProtegida';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import Painel from './pages/Painel';

function App() {
  return (
    <ProvedorAutenticacao>
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route
            path="/painel"
            element={
              <RotaProtegida>
                <Painel />
              </RotaProtegida>
            }
          />
          <Route path="/dashboard" element={<Navigate to="/painel" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </ProvedorAutenticacao>
  );
}

export default App;
