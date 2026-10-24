import type { ResumoMensalResponse } from '../../types/resumo';
import { formatarMoeda } from '../../utils/formatacao';

interface PropsCardsResumoMensal {
  resumo: ResumoMensalResponse;
}

const CardsResumoMensal = ({ resumo }: PropsCardsResumoMensal) => {
  const saldoPositivo = Number(resumo.saldo) >= 0;

  return (
    <section className="dashboard-summary-grid" aria-label="Totais do mês">
      <article className="summary-card summary-card-positive">
        <span>Total recebido</span>
        <strong>{formatarMoeda(resumo.totalRecebido)}</strong>
      </article>

      <article className="summary-card summary-card-negative">
        <span>Total gasto</span>
        <strong>{formatarMoeda(resumo.totalGasto)}</strong>
      </article>

      <article
        className={
          saldoPositivo
            ? 'summary-card summary-card-positive'
            : 'summary-card summary-card-negative'
        }
      >
        <span>Saldo do mês</span>
        <strong>{formatarMoeda(resumo.saldo)}</strong>
      </article>
    </section>
  );
};

export default CardsResumoMensal;
