import type { ResumoMensalResponse } from '../../types/resumo';
import { formatarMoeda, formatarVariacaoPercentual } from '../../utils/formatacao';

interface PropsVariacaoGastos {
  resumo: ResumoMensalResponse;
  resumoMesAnterior: ResumoMensalResponse | null;
  carregando?: boolean;
}

const obterClasseVariacao = (variacao: number) => {
  // Aumento de gastos é sinalizado como negativo para o usuário.
  if (variacao > 0) return 'dashboard-variation-up';
  if (variacao < 0) return 'dashboard-variation-down';
  return 'dashboard-variation-neutral';
};

const obterDescricaoVariacao = (variacao: number) => {
  if (variacao > 0) return 'a mais que no mês anterior';
  if (variacao < 0) return 'a menos que no mês anterior';
  return 'igual ao mês anterior';
};

const VariacaoGastos = ({
  resumo,
  resumoMesAnterior,
  carregando = false,
}: PropsVariacaoGastos) => {
  if (carregando) {
    return (
      <article className="dashboard-insight-card" aria-label="Variação de gastos">
        <span className="dashboard-insight-label">Variação de gastos</span>
        <strong className="dashboard-insight-value">—</strong>
        <p aria-live="polite">Carregando comparação com o mês anterior...</p>
      </article>
    );
  }

  const gastoMesAnterior = Number(resumoMesAnterior?.totalGasto || 0);
  const possuiComparacao =
    resumoMesAnterior !== null
    && gastoMesAnterior > 0
    && resumo.variacaoPercentualGastos !== null;

  if (!possuiComparacao) {
    return (
      <article className="dashboard-insight-card" aria-label="Variação de gastos">
        <span className="dashboard-insight-label">Variação de gastos</span>
        <strong className="dashboard-insight-value">—</strong>
        <p>Sem gastos no mês anterior para comparar.</p>
      </article>
    );
  }

  const variacao = Number(resumo.variacaoPercentualGastos);

  return (
    <article className="dashboard-insight-card" aria-label="Variação de gastos">
      <span className="dashboard-insight-label">Variação de gastos</span>
      <strong className={`dashboard-insight-value ${obterClasseVariacao(variacao)}`}>
        {formatarVariacaoPercentual(variacao)}
      </strong>
      <p>
        {obterDescricaoVariacao(variacao)} ({formatarMoeda(gastoMesAnterior)})
      </p>
    </article>
  );
};

export default VariacaoGastos;
