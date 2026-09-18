import httpClient from './httpClient';
import type { CategoriaResponse } from '../types/categoria';

export const listarCategorias = async () => {
  const { data } = await httpClient.get<CategoriaResponse[]>('/categorias');
  return data;
};
