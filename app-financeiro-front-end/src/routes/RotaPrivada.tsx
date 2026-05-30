import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAutenticacao } from '../hooks/useAutenticacao';

const RotaPrivada = () => {
  const { autenticacaoPronta, estaAutenticado } = useAutenticacao();
  const location = useLocation();

  if (!autenticacaoPronta) {
    return (
      <main className="app-loading" aria-live="polite">
        Validando sessão...
      </main>
    );
  }

  if (!estaAutenticado) {
    return <Navigate to="/login" replace state={{ origem: location.pathname }} />;
  }

  return <Outlet />;
};

export default RotaPrivada;
