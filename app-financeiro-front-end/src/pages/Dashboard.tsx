import { useCallback, useState } from 'react';
import { Navigate } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import ResumoFormaPagamentoPizza from '../components/resumo/ResumoFormaPagamentoPizza';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useAsync } from '../hooks/useAsync';
import { useAutenticacao } from '../hooks/useAutenticacao';
import { buscarResumoMensal, buscarResumoPorCategoria } from '../services/resumoService';
import type { GrupoCategoria, ResumoMensal } from '../types/resumo';
import { formatarMoeda } from '../utils/formatacao';

interface DadosDashboard {
  resumo: ResumoMensal | null;
  categorias: GrupoCategoria[];
}

const dadosIniciais: DadosDashboard = { resumo: null, categorias: [] };

const mesAtual = () => {
  const data = new Date();
  return { ano: data.getFullYear(), mes: data.getMonth() + 1 };
};

const rotuloMes = (ano: number, mes: number) =>
  new Date(ano, mes - 1, 1).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });

const formatarVariacao = (valor: number | null) => {
  if (valor == null) return null;

  const texto = valor.toLocaleString('pt-BR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });

  return valor > 0 ? `+${texto}%` : `${texto}%`;
};

const Dashboard = () => {
  const { estaAutenticado } = useAutenticacao();
  const inicial = mesAtual();
  const [ano, setAno] = useState(inicial.ano);
  const [mes, setMes] = useState(inicial.mes);

  const carregar = useCallback(async () => {
    const [resumo, categorias] = await Promise.all([
      buscarResumoMensal(ano, mes),
      buscarResumoPorCategoria(ano, mes),
    ]);

    return { resumo, categorias };
  }, [ano, mes]);

  const { dados, carregando, erro } = useAsync(carregar, {
    valorInicial: dadosIniciais,
    mensagemErro: 'Não foi possível carregar o resumo do mês.',
    habilitado: estaAutenticado,
  });

  const mudarMes = (delta: number) => {
    const data = new Date(ano, mes - 1 + delta, 1);
    setAno(data.getFullYear());
    setMes(data.getMonth() + 1);
  };

  if (!estaAutenticado) {
    return <Navigate to="/login" replace />;
  }

  const resumo = dados.resumo;
  const variacao = resumo ? formatarVariacao(resumo.variacaoPercentualGastos) : null;

  return (
    <LayoutPrivado titulo="Dashboard" subtitulo="Veja aqui o seu resumo financeiro.">
      <section className="transactions-header" aria-label="Navegação do mês">
        <div>
          <h2>{rotuloMes(ano, mes)}</h2>
        </div>
        <div className="transactions-actions">
          <button type="button" className="sb-button sb-button-secondary sb-button-sm" onClick={() => mudarMes(-1)}>
            Mês anterior
          </button>
          <button type="button" className="sb-button sb-button-secondary sb-button-sm" onClick={() => mudarMes(1)}>
            Próximo mês
          </button>
        </div>
      </section>

      {carregando ? (
        <p>Carregando resumo mensal</p>
      ) : erro ? (
        <MensagemAlerta mensagem={erro} />
      ) : !resumo?.possuiTransacoes ? (
        <EstadoVazio
          titulo="Nenhuma transação neste mês"
          descricao="Quando você registrar receitas ou despesas, o resumo aparece aqui."
        />
      ) : (
        <>
          <section className="summary-grid" aria-label="Resumo do mês">
            <article className="summary-card summary-card-positive">
              <span>Total recebido</span>
              <strong>{formatarMoeda(resumo.totalRecebido)}</strong>
            </article>
            <article className="summary-card summary-card-negative">
              <span>Total gasto</span>
              <strong>{formatarMoeda(resumo.totalGasto)}</strong>
            </article>
            <article className={resumo.saldo >= 0 ? 'summary-card summary-card-positive' : 'summary-card summary-card-negative'}>
              <span>Saldo do mês</span>
              <strong>{formatarMoeda(resumo.saldo)}</strong>
            </article>
          </section>

          <section className="transactions-panel" aria-label="Maior gasto">
            <h2>Maior gasto</h2>
            <p>{resumo.categoriaMaiorGastoNome ?? 'Não informado'}</p>
            {resumo.categoriaMaiorGastoTotal != null && <p>{formatarMoeda(resumo.categoriaMaiorGastoTotal)}</p>}
          </section>

          <section className="transactions-panel" aria-label="Variação em relação ao mês anterior">
            <h2>Variação em relação ao mês anterior</h2>
            <p>{variacao ?? 'Sem comparação com o mês anterior'}</p>
          </section>

          <section className="transactions-panel" aria-label="Gastos por categoria">
            <h2>Gastos por categoria</h2>
            <ul>
              {dados.categorias.map((categoria) => (
                <li key={categoria.categoriaID}>
                  <span>{categoria.nome}</span>
                  <span>{formatarMoeda(categoria.total)}</span>
                </li>
              ))}
            </ul>
          </section>
        </>
      )}

      <ResumoFormaPagamentoPizza />
    </LayoutPrivado>
  );
};

export default Dashboard;
