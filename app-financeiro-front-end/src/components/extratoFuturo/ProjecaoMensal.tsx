import type { ProjecaoMensalResponse } from '../../types/extratoFuturo';
import { formatarMoeda } from '../../utils/formatacao';

import ListaFaturas from './ListaFaturas';
import ListaTransacoesFuturas from './ListaTransacoesFuturas';

interface PropsProjecaoMensal {
  projecao: ProjecaoMensalResponse;
}

const formatarMes = (
  anoMes: string,
) => {
  const [anoTexto, mesTexto] =
    anoMes.split('-');

  const ano = Number(anoTexto);
  const mes = Number(mesTexto);

  if (
    !Number.isInteger(ano) ||
    !Number.isInteger(mes) ||
    mes < 1 ||
    mes > 12
  ) {
    return anoMes;
  }

  const rotulo =
    new Intl.DateTimeFormat(
      'pt-BR',
      {
        month: 'long',
        year: 'numeric',
      },
    ).format(
      new Date(
        ano,
        mes - 1,
        1,
      ),
    );

  return (
    rotulo.charAt(0).toUpperCase() +
    rotulo.slice(1)
  );
};

const ProjecaoMensal = ({
  projecao,
}: PropsProjecaoMensal) => {
  const saldoPositivo =
    Number(
      projecao.saldoPrevisto,
    ) >= 0;

  return (
    <article
      className="card border-0 shadow-sm mb-4"
      style={{
        borderRadius: 18,
      }}
    >
      <div className="card-body p-4">
        <div className="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2 mb-4">
          <div>
            <p className="text-muted small text-uppercase fw-semibold mb-1">
              Projeção mensal
            </p>

            <h2 className="h4 fw-bold mb-0">
              {formatarMes(
                projecao.mes,
              )}
            </h2>
          </div>

          {projecao.quantidadeVencimentos >
            0 && (
            <span className="badge bg-warning-subtle text-warning-emphasis align-self-start align-self-sm-center">
              {
                projecao.quantidadeVencimentos
              }{' '}
              {projecao.quantidadeVencimentos ===
              1
                ? 'vencimento'
                : 'vencimentos'}
            </span>
          )}
        </div>

        <div className="row g-3">
          <div className="col-12 col-md-4">
            <div className="border rounded-3 p-3 h-100">
              <span className="text-muted small d-block mb-1">
                Saldo previsto
              </span>

              <strong
                className={`fs-5 ${
                  saldoPositivo
                    ? 'text-success'
                    : 'text-danger'
                }`}
              >
                {formatarMoeda(
                  projecao.saldoPrevisto,
                )}
              </strong>
            </div>
          </div>

          <div className="col-12 col-md-4">
            <div className="border rounded-3 p-3 h-100">
              <span className="text-muted small d-block mb-1">
                Créditos
              </span>

              <strong className="fs-5 text-success">
                {formatarMoeda(
                  projecao.totalCreditos,
                )}
              </strong>
            </div>
          </div>

          <div className="col-12 col-md-4">
            <div className="border rounded-3 p-3 h-100">
              <span className="text-muted small d-block mb-1">
                Débitos
              </span>

              <strong className="fs-5 text-danger">
                {formatarMoeda(
                  projecao.totalDebitos,
                )}
              </strong>
            </div>
          </div>
        </div>

        <ListaFaturas
          faturas={
            projecao.faturas
          }
        />

        <ListaTransacoesFuturas
          transacoes={
            projecao.transacoes
          }
        />
      </div>
    </article>
  );
};

export default ProjecaoMensal;