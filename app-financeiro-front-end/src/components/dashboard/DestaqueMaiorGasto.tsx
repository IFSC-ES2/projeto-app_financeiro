import type { ResumoMensalResponse } from '../../types/resumo';
import { formatarMoeda, formatarPercentual } from '../../utils/formatacao';

interface PropsDestaqueMaiorGasto {
  resumo: ResumoMensalResponse;
}

const DestaqueMaiorGasto = ({ resumo }: PropsDestaqueMaiorGasto) => {
  const totalCategoria = Number(resumo.categoriaMaiorGastoTotal || 0);
  const totalGasto = Number(resumo.totalGasto || 0);
  const participacao = totalGasto > 0 ? (totalCategoria / totalGasto) * 100 : 0;

  return (
    <article className="dashboard-insight-card" aria-label="Categoria com maior gasto">
      <span className="dashboard-insight-label">Categoria com maior gasto</span>

      {resumo.categoriaMaiorGastoNome && totalCategoria > 0 ? (
        <>
          <strong className="dashboard-insight-value">
            {resumo.categoriaMaiorGastoNome}
          </strong>
          <p>
            {formatarMoeda(totalCategoria)} ·{' '}
            {formatarPercentual(participacao)} dos gastos do mês
          </p>
        </>
      ) : (
        <>
          <strong className="dashboard-insight-value">—</strong>
          <p>Nenhum gasto registrado neste mês.</p>
        </>
      )}
    </article>
  );
};

export default DestaqueMaiorGasto;
