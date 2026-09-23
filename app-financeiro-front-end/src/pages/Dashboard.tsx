import { useEffect, useState } from 'react';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import ResumoFormaPagamentoPizza from '../components/resumo/ResumoFormaPagamentoPizza';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import {
  buscarResumoMensal,
  buscarResumoPorCategorias,
  obterMensagemErroApi,
} from '../services/api';
import type { GrupoCategoriaResponse, ResumoMensalResponse } from '../services/api';
import { formatarMoeda } from '../utils/formatacao';

const NOMES_MESES = [
  'Janeiro',
  'Fevereiro',
  'Março',
  'Abril',
  'Maio',
  'Junho',
  'Julho',
  'Agosto',
  'Setembro',
  'Outubro',
  'Novembro',
  'Dezembro',
];

const formatarPeriodo = (ano: number, mes: number) => `${NOMES_MESES[mes - 1]} de ${ano}`;

const formatarVariacao = (variacao: number) => {
  const valorAbsoluto = Math.abs(variacao);
  const sinal = variacao > 0 ? '+' : variacao < 0 ? '−' : '';
  return `${sinal}${valorAbsoluto.toLocaleString('pt-BR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}%`;
};

const Dashboard = () => {
  const hoje = new Date();
  const [ano, setAno] = useState(hoje.getFullYear());
  const [mes, setMes] = useState(hoje.getMonth() + 1);
  const [resumo, setResumo] = useState<ResumoMensalResponse | null>(null);
  const [categorias, setCategorias] = useState<GrupoCategoriaResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    let cancelado = false;

    const carregar = async () => {
      setCarregando(true);
      setErro('');

      try {
        const [resumoCarregado, categoriasCarregadas] = await Promise.all([
          buscarResumoMensal(ano, mes),
          buscarResumoPorCategorias(ano, mes),
        ]);

        if (cancelado) return;

        setResumo(resumoCarregado);
        setCategorias(categoriasCarregadas);
      } catch (err) {
        if (cancelado) return;
        setResumo(null);
        setCategorias([]);
        setErro(
          obterMensagemErroApi(err, 'Não foi possível carregar o resumo mensal. Tente novamente.'),
        );
      } finally {
        if (!cancelado) setCarregando(false);
      }
    };

    carregar();

    return () => {
      cancelado = true;
    };
  }, [ano, mes]);

  const irParaMesAnterior = () => {
    if (mes === 1) {
      setAno((valor) => valor - 1);
      setMes(12);
      return;
    }
    setMes((valor) => valor - 1);
  };

  const irParaProximoMes = () => {
    if (mes === 12) {
      setAno((valor) => valor + 1);
      setMes(1);
      return;
    }
    setMes((valor) => valor + 1);
  };

  const possuiDados = Boolean(resumo?.possuiTransacoes);
  const saldo = resumo?.saldo ?? 0;

  return (
    <LayoutPrivado
      titulo="Dashboard"
      subtitulo="Veja aqui o seu resumo financeiro."
    >
      <section className="dashboard-month-nav" aria-label="Navegação entre meses">
        <button
          type="button"
          className="sb-button sb-button-secondary sb-button-sm"
          onClick={irParaMesAnterior}
          aria-label="Mês anterior"
        >
          Anterior
        </button>
        <h2 className="dashboard-month-label">{formatarPeriodo(ano, mes)}</h2>
        <button
          type="button"
          className="sb-button sb-button-secondary sb-button-sm"
          onClick={irParaProximoMes}
          aria-label="Próximo mês"
        >
          Próximo
        </button>
      </section>

      {carregando ? (
        <div className="loading-inline" aria-live="polite">
          Carregando resumo mensal...
        </div>
      ) : erro ? (
        <MensagemAlerta mensagem={erro} tipo="danger" />
      ) : !possuiDados ? (
        <EstadoVazio
          titulo="Nenhuma transação neste mês"
          descricao="Importe um extrato ou cadastre uma transação manual para ver o resumo mensal."
        />
      ) : (
        <>
          <section className="summary-grid" aria-label="Resumo mensal">
            <article className="summary-card">
              <span>Total recebido</span>
              <strong>{formatarMoeda(resumo!.totalRecebido)}</strong>
            </article>
            <article className="summary-card">
              <span>Total gasto</span>
              <strong>{formatarMoeda(resumo!.totalGasto)}</strong>
            </article>
            <article
              className={
                saldo >= 0
                  ? 'summary-card summary-card-positive'
                  : 'summary-card summary-card-negative'
              }
            >
              <span>Saldo do mês</span>
              <strong>{formatarMoeda(saldo)}</strong>
            </article>
            <article className="summary-card">
              <span>Variação de gastos</span>
              <strong>{formatarVariacao(resumo!.variacaoPercentualGastos)}</strong>
            </article>
          </section>

          {resumo?.categoriaMaiorGastoNome ? (
            <section className="dashboard-highlight-card" aria-label="Categoria com maior gasto">
              <h2>Categoria com maior gasto</h2>
              <p>
                <strong>{resumo.categoriaMaiorGastoNome}</strong>
                {' — '}
                {formatarMoeda(resumo.categoriaMaiorGastoTotal ?? 0)}
              </p>
            </section>
          ) : null}

          <section className="dashboard-categories-card" aria-label="Gastos por categoria">
            <div className="dashboard-categories-header">
              <h2>Gastos por categoria</h2>
              <p>Distribuição das despesas do mês selecionado.</p>
            </div>

            {categorias.length === 0 ? (
              <EstadoVazio
                titulo="Sem gastos categorizados"
                descricao="Não há despesas agrupadas por categoria neste período."
              />
            ) : (
              <ul className="dashboard-categories-list">
                {categorias.map((grupo) => (
                  <li key={`${grupo.categoriaID ?? 'sem-categoria'}-${grupo.nome}`}>
                    <div>
                      <strong>{grupo.nome}</strong>
                      <small>
                        {grupo.quantidade} {grupo.quantidade === 1 ? 'lançamento' : 'lançamentos'}
                      </small>
                    </div>
                    <div>
                      <strong>{formatarMoeda(grupo.total)}</strong>
                      <small>{grupo.percentual.toLocaleString('pt-BR')}%</small>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}

      <ResumoFormaPagamentoPizza />
    </LayoutPrivado>
  );
};

export default Dashboard;
