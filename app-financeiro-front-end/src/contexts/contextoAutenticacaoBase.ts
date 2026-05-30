import { createContext } from 'react';
import type { DadosContextoAutenticacao } from './tiposAutenticacao';

export const ContextoAutenticacao = createContext<DadosContextoAutenticacao | null>(null);
