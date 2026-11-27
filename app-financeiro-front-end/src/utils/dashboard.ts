import type { PeriodoResumo } from '../services/resumoService';
import { alterarAnoMes } from './categorias';

export const converterAnoMesParaPeriodo = (anoMes: string): PeriodoResumo => {
  const [ano, mes] = anoMes.split('-').map(Number);
  return { ano, mes };
};

export const obterPeriodoAnterior = (anoMes: string) =>
  converterAnoMesParaPeriodo(alterarAnoMes(anoMes, -1));
