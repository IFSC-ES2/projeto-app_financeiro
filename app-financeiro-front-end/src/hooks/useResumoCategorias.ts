import { useCallback } from 'react';

import { buscarResumoPorCategorias } from '../services/resumoService';
import type { ResumoCategoriaResponse } from '../types/resumo';
import { converterAnoMesParaPeriodo } from '../utils/dashboard';

import { useAsync } from './useAsync';

interface ResumoCategoriasDoMes {
  anoMes: string | null;
  categorias: ResumoCategoriaResponse[];
}

export const useResumoCategorias = (anoMes: string) => {
  /*
   * A resposta não informa o período, então o resultado
   * guarda o mês consultado para ser validado abaixo.
   */
  const carregarCategorias = useCallback(
    async (): Promise<ResumoCategoriasDoMes> => ({
      anoMes,
      categorias: await buscarResumoPorCategorias(
        converterAnoMesParaPeriodo(anoMes),
      ),
    }),
    [anoMes],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
  } = useAsync<ResumoCategoriasDoMes>(
    carregarCategorias,
    {
      valorInicial: { anoMes: null, categorias: [] },
      mensagemErro:
        'Não foi possível carregar os gastos por categoria.',
    },
  );

  /*
   * Assim como no resumo mensal, as categorias de outro
   * mês são descartadas enquanto a nova busca não chega,
   * evitando misturar os totais do mês atual com as
   * categorias do mês anterior.
   */
  const ehDoMesSelecionado = dados.anoMes === anoMes;

  return {
    categorias: ehDoMesSelecionado ? dados.categorias : [],
    carregando: carregando || (!ehDoMesSelecionado && !erro),
    erro,
    recarregar,
  };
};
