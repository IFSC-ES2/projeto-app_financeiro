import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import {
  listarTransacoes,
  listarContas,
  listarCategorias,
  type TransacaoResponse,
  type ContaResponse,
  type CategoriaResponse,
} from '../services/api';
import { useAutenticacao } from './ContextoAutenticacao';

interface DadosContextoTransacoes {
  transacoes: TransacaoResponse[];
  contas: ContaResponse[];
  categorias: CategoriaResponse[];
  carregando: boolean;
  erro: string | null;
  recarregar: () => Promise<void>;
}

const ContextoTransacoes = createContext<DadosContextoTransacoes>({} as DadosContextoTransacoes);

export const ProvedorTransacoes: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { estaAutenticado } = useAutenticacao();
  const [transacoes, setTransacoes] = useState<TransacaoResponse[]>([]);
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const recarregar = useCallback(async () => {
    if (!estaAutenticado) return;
    setCarregando(true);
    setErro(null);
    try {
      const [transacoesData, contasData, categoriasData] = await Promise.all([
        listarTransacoes().catch(() => []),
        listarContas().catch(() => []),
        listarCategorias().catch(() => []),
      ]);
      setTransacoes(transacoesData);
      setContas(contasData);
      setCategorias(categoriasData);
    } catch (e) {
      setErro('Erro ao carregar dados.');
    } finally {
      setCarregando(false);
    }
  }, [estaAutenticado]);

  useEffect(() => {
    if (estaAutenticado) {
      recarregar();
    }
  }, [estaAutenticado, recarregar]);

  return (
    <ContextoTransacoes.Provider value={{ transacoes, contas, categorias, carregando, erro, recarregar }}>
      {children}
    </ContextoTransacoes.Provider>
  );
};

export const useTransacoes = () => useContext(ContextoTransacoes);
