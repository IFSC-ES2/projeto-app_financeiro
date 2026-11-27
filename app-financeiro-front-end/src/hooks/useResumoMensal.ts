import { useCallback } from 'react';

import { buscarResumoMensal } from '../services/resumoService';
import type { ResumoMensalResponse } from '../types/resumo';
import {
  converterAnoMesParaPeriodo,
  obterPeriodoAnterior,
} from '../utils/dashboard';

import { useAsync } from './useAsync';

interface ResumoMesAnterior {
  anoMes: string | null;
  resumo: ResumoMensalResponse | null;
}

export const useResumoMensal = (anoMes: string) => {
  const carregarResumo = useCallback(
    () => buscarResumoMensal(converterAnoMesParaPeriodo(anoMes)),
    [anoMes],
  );

  /*
   * O mês anterior é buscado à parte para que o resumo do
   * mês selecionado não espere por ele. Ele serve apenas
   * para saber se existe base de comparação, então uma
   * falha nele não deve impedir a exibição do mês.
   */
  const carregarMesAnterior = useCallback(
    async (): Promise<ResumoMesAnterior> => ({
      anoMes,
      resumo: await buscarResumoMensal(obterPeriodoAnterior(anoMes))
        .catch(() => null),
    }),
    [anoMes],
  );

  const {
    dados,
    carregando,
    erro,
    recarregar,
  } = useAsync<ResumoMensalResponse | null>(
    carregarResumo,
    {
      valorInicial: null,
      mensagemErro:
        'Não foi possível carregar o resumo do mês.',
    },
  );

  const {
    dados: dadosMesAnterior,
    carregando: carregandoMesAnterior,
    recarregar: recarregarMesAnterior,
  } = useAsync<ResumoMesAnterior>(
    carregarMesAnterior,
    {
      valorInicial: { anoMes: null, resumo: null },
      mensagemErro: '',
    },
  );

  const recarregarTudo = useCallback(() => {
    recarregar();
    recarregarMesAnterior();
  }, [recarregar, recarregarMesAnterior]);

  /*
   * Entre a troca de mês e o início da nova busca existe
   * um render com os dados do mês anterior. A resposta
   * informa a que período pertence, então o resumo é
   * descartado quando não corresponde ao mês pedido.
   */
  const { ano, mes } = converterAnoMesParaPeriodo(anoMes);
  const ehResumoDoMesSelecionado =
    dados?.ano === ano && dados?.mes === mes;
  const ehComparacaoDoMesSelecionado = dadosMesAnterior.anoMes === anoMes;

  return {
    resumo: ehResumoDoMesSelecionado ? dados : null,
    resumoMesAnterior: ehComparacaoDoMesSelecionado
      ? dadosMesAnterior.resumo
      : null,
    carregandoMesAnterior:
      carregandoMesAnterior || !ehComparacaoDoMesSelecionado,
    carregando,
    erro,
    recarregar: recarregarTudo,
  };
};
