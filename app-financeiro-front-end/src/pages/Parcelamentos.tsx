import ProjecaoMensal from '../components/extratoFuturo/ProjecaoMensal';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useExtratoFuturo } from '../hooks/useExtratoFuturo';

const MESES_PADRAO = 3;

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
                className="btn btn-outline-secondary"
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
            descricao="Você não possui transações futuras ou faturas abertas previstas para os próximos três meses."
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
                Próximos meses
              </p>

              <h2 className="h5 fw-bold mb-2">
                Planejamento financeiro
              </h2>

              <p className="text-muted mb-0">
                Acompanhe seus
                créditos, débitos,
                boletos e faturas
                previstos para os
                próximos três meses.
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
      subtitulo="Visualize seus compromissos e saldos previstos para os próximos meses."
    >
      {renderizarConteudo()}
    </LayoutPrivado>
  );
};

export default Parcelamentos;