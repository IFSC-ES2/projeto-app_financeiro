import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAutenticacao } from '../../contexts/ContextoAutenticacao';

interface Props {
  children: React.ReactNode;
}

const RotaProtegida: React.FC<Props> = ({ children }) => {
  const { estaAutenticado } = useAutenticacao();
  if (!estaAutenticado) return <Navigate to="/login" replace />;
  return <>{children}</>;
};

export default RotaProtegida;
