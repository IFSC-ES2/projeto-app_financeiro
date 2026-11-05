import type {
  TipoPagamento,
  TransacaoResponse,
} from '../../types/transacao';
import {
  formatarData,
  formatarMoeda,
} from '../../utils/formatacao';

interface PropsListaTransacoesFuturas {
  transacoes: TransacaoResponse[];
}

const ROTULOS_FORMA_PAGAMENTO: Record<
  TipoPagamento,
  string
> = {
  PIX: 'PIX',
  CARTAO_DEBITO: 'Cartão de débito',
  CARTAO_CREDITO: 'Cartão de crédito',
  DINHEIRO: 'Dinheiro',
  BOLETO: 'Boleto',
  TED_DOC: 'TED/DOC',
};

const ListaTransacoesFuturas = ({
  transacoes,
}: PropsListaTransacoesFuturas) => (
  <section className="mt-4">
    <div className="d-flex align-items-center justify-content-between gap-2 mb-3">
      <h3 className="h6 fw-bold mb-0">
        Transações futuras
      </h3>

      <span className="badge text-bg-light">
        {transacoes.length}
      </span>
    </div>

    {transacoes.length === 0 ? (
      <p className="text-muted small mb-0">
        Nenhuma transação futura prevista para este mês.
      </p>
    ) : (
      <div className="d-flex flex-column gap-2">
        {transacoes.map((transacao) => {
          const ehDebito =
            transacao.tipoTransacao ===
            'DEBITO';

          /*
           * O backend não inclui novamente no total de débitos
           * as compras que já pertencem a uma fatura.
           *
           * Como TransacaoResponse ainda não expõe faturaId,
           * a forma de pagamento é utilizada como aproximação
           * para indicar visualmente que a compra será
           * representada pelo valor da fatura.
           */
          const consideradaNaFatura =
            transacao.formaPagamento ===
            'CARTAO_CREDITO';

          const classeValor =
            consideradaNaFatura
              ? 'text-body'
              : ehDebito
                ? 'text-danger'
                : 'text-success';

          const prefixoValor =
            consideradaNaFatura
              ? ''
              : ehDebito
                ? '- '
                : '+ ';

          return (
            <article
              key={transacao.transacaoId}
              className="border rounded-3 p-3"
            >
              <div className="d-flex flex-column flex-sm-row justify-content-between gap-3">
                <div>
                  <strong className="d-block mb-1">
                    {transacao.descricao?.trim() ||
                      'Transação futura'}
                  </strong>

                  <div className="d-flex flex-wrap align-items-center gap-2">
                    <span className="text-muted small">
                      {formatarData(
                        transacao.data,
                      )}
                    </span>

                    {transacao.formaPagamento && (
                      <span className="badge text-bg-light">
                        {
                          ROTULOS_FORMA_PAGAMENTO[
                            transacao.formaPagamento
                          ]
                        }
                      </span>
                    )}

                    {consideradaNaFatura && (
                      <span
                        className="badge bg-info-subtle text-info-emphasis"
                        title="Este valor será considerado no total da fatura."
                      >
                        Na fatura
                      </span>
                    )}

                    <span
                      className={`badge ${
                        ehDebito
                          ? 'bg-danger-subtle text-danger-emphasis'
                          : 'bg-success-subtle text-success-emphasis'
                      }`}
                    >
                      {ehDebito
                        ? 'Débito'
                        : 'Crédito'}
                    </span>
                  </div>
                </div>

                <strong
                  className={classeValor}
                  title={
                    consideradaNaFatura
                      ? 'Valor já representado pela fatura e não somado novamente aos débitos.'
                      : undefined
                  }
                >
                  {prefixoValor}
                  {formatarMoeda(
                    transacao.valor,
                  )}
                </strong>
              </div>
            </article>
          );
        })}
      </div>
    )}
  </section>
);

export default ListaTransacoesFuturas;