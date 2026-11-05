import type {
  FaturaResumoResponse,
  StatusFatura,
} from '../../types/extratoFuturo';
import {
  formatarData,
  formatarMoeda,
} from '../../utils/formatacao';

interface PropsListaFaturas {
  faturas: FaturaResumoResponse[];
}

const ROTULOS_STATUS: Record<
  StatusFatura,
  string
> = {
  ABERTA: 'Aberta',
  FECHADA: 'Fechada',
  PAGA: 'Paga',
};

const classesStatus: Record<
  StatusFatura,
  string
> = {
  ABERTA:
    'bg-warning-subtle text-warning-emphasis',
  FECHADA:
    'bg-danger-subtle text-danger-emphasis',
  PAGA:
    'bg-success-subtle text-success-emphasis',
};

const ListaFaturas = ({
  faturas,
}: PropsListaFaturas) => (
  <section className="mt-4">
    <div className="d-flex align-items-center justify-content-between gap-2 mb-3">
      <h3 className="h6 fw-bold mb-0">
        Faturas com vencimento
      </h3>

      <span className="badge text-bg-light">
        {faturas.length}
      </span>
    </div>

    {faturas.length === 0 ? (
      <p className="text-muted small mb-0">
        Nenhuma fatura com vencimento neste mês.
      </p>
    ) : (
      <div className="d-flex flex-column gap-2">
        {faturas.map((fatura) => (
          <article
            key={fatura.faturaId}
            className="border rounded-3 p-3"
          >
            <div className="d-flex flex-column flex-sm-row justify-content-between gap-3">
              <div>
                <div className="d-flex align-items-center flex-wrap gap-2 mb-1">
                  <strong>
                    {fatura.nomeConta}
                  </strong>

                  <span
                    className={`badge ${classesStatus[fatura.status]}`}
                  >
                    {ROTULOS_STATUS[fatura.status]}
                  </span>
                </div>

                <span className="text-muted small">
                  Vencimento em{' '}
                  {formatarData(
                    fatura.dataVencimento,
                  )}
                </span>
              </div>

              <strong className="text-danger fs-5">
                {formatarMoeda(
                  fatura.valorTotal,
                )}
              </strong>
            </div>
          </article>
        ))}
      </div>
    )}
  </section>
);

export default ListaFaturas;