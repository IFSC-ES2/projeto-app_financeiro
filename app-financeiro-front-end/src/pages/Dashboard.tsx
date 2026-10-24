import { useState } from 'react';

import CardsResumoMensal from '../components/dashboard/CardsResumoMensal';
import DestaqueMaiorGasto from '../components/dashboard/DestaqueMaiorGasto';
import GastosPorCategoria from '../components/dashboard/GastosPorCategoria';
import SeletorMes from '../components/dashboard/SeletorMes';
import VariacaoGastos from '../components/dashboard/VariacaoGastos';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import ResumoFormaPagamentoPizza from '../components/resumo/ResumoFormaPagamentoPizza';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useResumoCategorias } from '../hooks/useResumoCategorias';
import { useResumoMensal } from '../hooks/useResumoMensal';
import { alterarAnoMes, formatarRotuloMes, obterMesAtual } from '../utils/categorias';
import { formatarMoeda } from '../utils/formatacao';

const Dashboard = () => {
  const [mesSelecionado, setMesSelecionado] = useState(obterMesAtual);

  const {
    resumo,
    resumoMesAnterior,
    carregandoMesAnterior,
    carregando,
    erro,
    recarregar,
  } = useResumoMensal(mesSelecionado);

  const {
    categorias,
    carregando: carregandoCategorias,
    erro: erroCategorias,
    recarregar: recarregarCategorias,
  } = useResumoCategorias(mesSelecionado);

  // Formato AAAA-MM permite comparar os meses como texto.
  const mesAtual = obterMesAtual();
  const ehMesAtualOuFuturo = mesSelecionado >= mesAtual;
  const rotuloMes = formatarRotuloMes(mesSelecionado);
  const temResumo = Boolean(resumo?.possuiTransacoes);

  /*
   * Enquanto o resumo do mês pedido não chega não há dados
   * para exibir, e a ausência deles não é um erro.
   */
  const aguardandoResumo = carregando || (!resumo && !erro);

  const renderizarConteudo = () => {
    if (aguardandoResumo) {
      return (
        <div className="dashboard-state-card loading-inline" aria-live="polite">
          Carregando resumo do mês...
        </div>
      );
    }

    if (erro || !resumo) {
      return (
        <div className="dashboard-state-card dashboard-error">
          <MensagemAlerta
            mensagem={erro || 'Não foi possível carregar o resumo do mês.'}
            tipo="danger"
          />
          <button
            type="button"
            className="btn btn-outline-secondary dashboard-retry-button"
            onClick={recarregar}
          >
            Tentar novamente
          </button>
        </div>
      );
    }

    if (!resumo.possuiTransacoes) {
      return (
        <EstadoVazio
          titulo="Nenhuma transação neste mês"
          descricao="Registre ou importe transações para acompanhar o seu resumo financeiro."
        />
      );
    }

    return (
      <>
        <CardsResumoMensal resumo={resumo} />

        <div className="dashboard-insights-grid">
          <DestaqueMaiorGasto resumo={resumo} />
          <VariacaoGastos
            resumo={resumo}
            resumoMesAnterior={resumoMesAnterior}
            carregando={carregandoMesAnterior}
          />
        </div>

        <GastosPorCategoria
          categorias={categorias}
          carregando={carregandoCategorias}
          erro={erroCategorias}
          onTentarNovamente={recarregarCategorias}
        />
      </>
    );
  };

  return (
    <LayoutPrivado
      titulo="Dashboard"
      subtitulo="Veja aqui o seu resumo financeiro."
    >
      <section className="dashboard-header-card" aria-label="Saldo do mês selecionado">
        <div className="dashboard-header-hero">
          {temResumo && resumo ? (
            <>
              <strong>{formatarMoeda(resumo.saldo)}</strong>
              <span>saldo em {rotuloMes}</span>
            </>
          ) : (
            <>
              <strong>{rotuloMes}</strong>
              <span>
                {aguardandoResumo ? 'Carregando resumo...' : 'Resumo financeiro do mês'}
              </span>
            </>
          )}
        </div>

        <SeletorMes
          mesSelecionado={mesSelecionado}
          mesMaximo={mesAtual}
          desabilitarProximo={ehMesAtualOuFuturo}
          onAlterarMes={(deslocamento) =>
            setMesSelecionado((mes) => alterarAnoMes(mes, deslocamento))}
          onSelecionarMes={(mes) => setMesSelecionado(mes > mesAtual ? mesAtual : mes)}
        />
      </section>

      {renderizarConteudo()}

      <ResumoFormaPagamentoPizza />
    </LayoutPrivado>
  );
};

export default Dashboard;
