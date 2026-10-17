import { useCallback } from 'react';

import { listarCategorias } from '../services/categoriaService';
import type { CategoriaResponse } from '../types/categoria';

import { useAsync } from './useAsync';

interface UseCategoriasOpcoes {
  habilitado?: boolean;
  mensagemErro?: string;
}

export const useCategorias = ({
  habilitado = true,
  mensagemErro = 'Não foi possível carregar as categorias.',
}: UseCategoriasOpcoes = {}) => {
  const carregarCategorias = useCallback(
    () => listarCategorias(),
    [],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
    limparErro,
  } = useAsync<CategoriaResponse[]>(
    carregarCategorias,
    {
      valorInicial: [],
      mensagemErro,
      habilitado,
    },
  );

  return {
    categorias: dados,
    carregando,
    erro,
    recarregar,
    limparErro,
  };
};