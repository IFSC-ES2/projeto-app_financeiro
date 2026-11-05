import ProjecaoMensal from '../components/extratoFuturo/ProjecaoMensal';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useExtratoFuturo } from '../hooks/useExtratoFuturo';

const MESES_PADRAO = 3;

const obterPeriodoDescricao = (
  meses: number,
) =>
  meses === 1
    ? 'no próximo mês'
    : `nos próximos ${meses} meses`;

const obterTituloPeriodo = (
  meses: number,
) =>
  meses === 1
    ? 'Próximo mês'
    : `Próximos ${meses} meses`;

const Parcelamentos = () => {
  const {
    projecoes,
    carregando,
    erro,
    recarregar,
  } = useExtratoFuturo(
    MESES_PADRAO,
  );

  const possuiDadosFuturos =
    projecoes.some(
      (projecao) =>
        projecao.transacoes.length >
          0 ||
        projecao.faturas.length >
          0,
    );

  const periodoDescricao =
    obterPeriodoDescricao(
      MESES_PADRAO,
    );

  const tituloPeriodo =
    obterTituloPeriodo(
      MESES_PADRAO,
    );

  const renderizarConteudo =
    () => {
      if (carregando) {
        return (
          <section
            className="card border-0 shadow-sm"
            style={{
              borderRadius: 18,
            }}
            aria-live="polite"
          >
            <div className="card-body p-5 text-center">
              <div
                className="spinner-border mb-3"
                style={{
                  color:
                    'var(--sb-primary)',
                }}
                role="status"
                aria-hidden="true"
              />

              <p className="text-muted mb-0">
                Carregando sua
                projeção financeira...
              </p>
            </div>
          </section>
        );
      }

      if (erro) {
        return (
          <section
            className="card border-0 shadow-sm"
            style={{
              borderRadius: 18,
            }}
          >
            <div className="card-body p-4">
              <MensagemAlerta
                mensagem={erro}
                tipo="danger"
              />

              <button
                type="button"
                className="sb-button sb-button-secondary"
                onClick={
                  recarregar
                }
              >
                Tentar novamente
              </button>
            </div>
          </section>
        );
      }

      if (
        projecoes.length ===
          0 ||
        !possuiDadosFuturos
      ) {
        return (
          <EstadoVazio
            titulo="Nenhum compromisso futuro"
            descricao={`Você não possui transações futuras ou faturas abertas previstas ${periodoDescricao}.`}
          />
        );
      }

      return (
        <>
          <section
            className="card border-0 shadow-sm mb-4"
            style={{
              borderRadius: 18,
            }}
          >
            <div className="card-body p-4">
              <p className="text-muted small text-uppercase fw-semibold mb-1">
                {tituloPeriodo}
              </p>

              <h2 className="h5 fw-bold mb-2">
                Planejamento financeiro
              </h2>

              <p className="text-muted mb-0">
                Acompanhe seus créditos,
                débitos, boletos e faturas
                previstos{' '}
                {periodoDescricao}.
              </p>
            </div>
          </section>

          {projecoes.map(
            (projecao) => (
              <ProjecaoMensal
                key={
                  projecao.mes
                }
                projecao={
                  projecao
                }
              />
            ),
          )}
        </>
      );
    };

  return (
    <LayoutPrivado
      titulo="Extrato futuro"
      subtitulo={`Visualize seus compromissos e resultados previstos ${periodoDescricao}.`}
    >
      {renderizarConteudo()}
    </LayoutPrivado>
  );
};

export default Parcelamentos;