import { formatarMoeda } from '../../utils/formatacao';

interface PropsResumoCategorias {
  gradienteGrafico: string;
  mesSelecionado: string;
  rotuloMes: string;
  totalGasto: number;
  onAlterarMes: (deslocamento: number) => void;
  onSelecionarMes: (mes: string) => void;
}

const ResumoCategorias = ({
  gradienteGrafico, mesSelecionado, rotuloMes, totalGasto, onAlterarMes, onSelecionarMes,
}: PropsResumoCategorias) => (
  <section className="categories-summary-card" aria-label="Resumo mensal por categoria">
    <div className="categories-total-block">
      <strong>{formatarMoeda(totalGasto)}</strong>
      <span>gasto em {rotuloMes}</span>
    </div>
    <div className="categories-donut-chart" style={{ background: gradienteGrafico }} aria-label={`Distribuição dos gastos de ${rotuloMes}`} role="img">
      <span />
    </div>
    <div className="categories-month-control" aria-label="Selecionar mês">
      <button type="button" className="categories-month-button" aria-label="Mês anterior" onClick={() => onAlterarMes(-1)}>‹</button>
      <label>
        <span>Mês selecionado</span>
        <input type="month" value={mesSelecionado} onChange={(evento) => onSelecionarMes(evento.target.value)} />
      </label>
      <button type="button" className="categories-month-button" aria-label="Próximo mês" onClick={() => onAlterarMes(1)}>›</button>
    </div>
  </section>
);

export default ResumoCategorias;
