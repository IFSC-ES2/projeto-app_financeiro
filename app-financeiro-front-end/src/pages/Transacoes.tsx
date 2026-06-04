import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import {
  categorizarTransacao,
  listarCategorias,
  listarContas,
  listarTransacoes,
  obterMensagemErroApi,
} from '../services/api';
import type { CategoriaResponse, ContaResponse, TransacaoResponse } from '../services/api';
import { formatarData, formatarMoeda } from '../utils/formatacao';
import {
  calcularResumoTransacoes,
  filtrarTransacoes,
  ordenarTransacoesPorDataDesc,
} from '../utils/transacoes';
import type { FiltroPeriodo, FiltroTipo, FiltrosTransacao } from '../utils/transacoes';

interface EstadoNavegacaoTransacoes {
  mensagem?: string;
  transacaoCriada?: TransacaoResponse;
}

const filtrosIniciais: FiltrosTransacao = {
  periodo: 'MES_ATUAL',
  tipo: 'TODAS',
  categoriaId: '',
  contaId: '',
};

const obterRotuloTipo = (tipo: TransacaoResponse['tipoTransacao']) => {
  const rotulos: Record<TransacaoResponse['tipoTransacao'], string> = {
    CREDITO: 'Receita',
    DEBITO: 'Despesa',
    PARCELAMENTO: 'Parcelamento',
    BOLETO: 'Boleto',
  };

  return rotulos[tipo];
};

const obterRotuloFormaPagamento = (forma?: TransacaoResponse['formaPagamento']) => {
  if (!forma) return 'Não informado';

  const rotulos: Record<NonNullable<TransacaoResponse['formaPagamento']>, string> = {
    PIX: 'Pix',
    CARTAO_DEBITO: 'Cartão de débito',
    CARTAO_CREDITO: 'Cartão de crédito',
    DINHEIRO: 'Dinheiro',
    BOLETO: 'Boleto',
    TED_DOC: 'TED/DOC',
  };

  return rotulos[forma];
};

const Transacoes = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const estado = location.state as EstadoNavegacaoTransacoes | null;

  const [transacoes, setTransacoes] = useState<TransacaoResponse[]>(() =>
    estado?.transacaoCriada ? [estado.transacaoCriada] : []
  );
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [filtros, setFiltros] = useState<FiltrosTransacao>(filtrosIniciais);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [mensagemSucesso, setMensagemSucesso] = useState(estado?.mensagem ?? '');
  const [transacaoAtualizandoId, setTransacaoAtualizandoId] = useState<string | null>(null);
  const [mensagemCategoria, setMensagemCategoria] = useState('');

  useEffect(() => {
    if (estado?.mensagem || estado?.transacaoCriada) {
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [estado?.mensagem, estado?.transacaoCriada, location.pathname, navigate]);

 useEffect(() => {
  if (!mensagemCategoria) {
    return;
  }

  const timeoutId = window.setTimeout(() => {
    setMensagemCategoria('');
  }, 3000);

  return () => {
    window.clearTimeout(timeoutId);
  };
}, [mensagemCategoria]);

useEffect(() => {
  if (!mensagemSucesso) {
    return;
  }

  const timeoutId = window.setTimeout(() => {
    setMensagemSucesso('');
  }, 3000);

  return () => {
    window.clearTimeout(timeoutId);
  };
}, [mensagemSucesso]);

useEffect(() => {
  let ativo = true;

  const carregarApoio = async () => {
    try {
      const [contasCarregadas, categoriasCarregadas] = await Promise.all([
        listarContas(),
        listarCategorias(),
      ]);

      if (!ativo) return;

      setContas(contasCarregadas);
      setCategorias(categoriasCarregadas);
    } catch (erroCapturado) {
      if (!ativo) return;

      setErro(obterMensagemErroApi(erroCapturado, 'Não foi possível carregar contas e categorias.'));
    }
  };

  const carregarTransacoes = async () => {
    setCarregando(true);
    setErro('');

    try {
      const transacoesCarregadas = await listarTransacoes();

      if (!ativo) return;

      setTransacoes(ordenarTransacoesPorDataDesc(transacoesCarregadas));
    } catch (erroCapturado) {
      if (!ativo) return;

      setErro(obterMensagemErroApi(erroCapturado, 'Não foi possível carregar as transações.'));
    } finally {
      if (ativo) setCarregando(false);
    }
  };

  carregarApoio();
  carregarTransacoes();

  return () => {
    ativo = false;
  };
}, []);

  const contaPorId = useMemo(() => new Map(contas.map((conta) => [conta.contaId, conta])), [contas]);

  const transacoesFiltradas = useMemo(
    () => ordenarTransacoesPorDataDesc(filtrarTransacoes(transacoes, filtros)),
    [filtros, transacoes]
  );

  const resumo = useMemo(() => calcularResumoTransacoes(transacoesFiltradas), [transacoesFiltradas]);

  const alterarFiltro = (campo: keyof FiltrosTransacao, valor: string) => {
  setMensagemCategoria('');
  setMensagemSucesso('');
  setFiltros((atuais) => ({ ...atuais, [campo]: valor }));
};

  const atualizarCategoriaTransacao = async (transacaoId: string, categoriaId: string) => {
  if (!categoriaId) return;

  setTransacaoAtualizandoId(transacaoId);
  setErro('');
  setMensagemCategoria('');

  try {
    const transacaoAtualizada = await categorizarTransacao(transacaoId, categoriaId);

    setTransacoes((atuais) =>
      atuais.map((transacao) =>
        transacao.transacaoId === transacaoId ? transacaoAtualizada : transacao
      )
    );

    setMensagemCategoria('Categoria atualizada com sucesso.');
  } catch (erroCapturado) {
    setErro(obterMensagemErroApi(erroCapturado, 'Não foi possível atualizar a categoria da transação.'));
  } finally {
    setTransacaoAtualizandoId(null);
  }
};

  return (
    <LayoutPrivado titulo="Transações" subtitulo="Visualize e registre movimentações financeiras da sua conta.">
      <MensagemAlerta mensagem={mensagemSucesso} tipo="success" />
      <MensagemAlerta mensagem={mensagemCategoria} tipo="success" />
      <MensagemAlerta mensagem={erro} tipo="danger" />
      <section className="filters-panel" aria-label="Filtros de transações">
        <label>
          <span>Período</span>
          <select
            value={filtros.periodo}
            onChange={(evento) => alterarFiltro('periodo', evento.target.value as FiltroPeriodo)}
          >
            <option value="MES_ATUAL">Este mês</option>
            <option value="TODAS">Todos os períodos</option>
          </select>
        </label>

        <label>
          <span>Tipo</span>
          <select value={filtros.tipo} onChange={(evento) => alterarFiltro('tipo', evento.target.value as FiltroTipo)}>
            <option value="TODAS">Todas as transações</option>
            <option value="RECEITAS">Receitas</option>
            <option value="DESPESAS">Despesas</option>
          </select>
        </label>

        <label>
          <span>Conta</span>
          <select value={filtros.contaId} onChange={(evento) => alterarFiltro('contaId', evento.target.value)}>
            <option value="">Todas as contas</option>
            {contas.map((conta) => (
              <option key={conta.contaId} value={conta.contaId}>
                {conta.nome}
              </option>
            ))}
          </select>
        </label>

        <label>
          <span>Categoria</span>
          <select value={filtros.categoriaId} onChange={(evento) => alterarFiltro('categoriaId', evento.target.value)}>
            <option value="">Todas as categorias</option>
            {categorias.map((categoria) => (
              <option key={categoria.categoriaId} value={categoria.categoriaId}>
                {categoria.nome}
              </option>
            ))}
          </select>
        </label>
      </section>

      <section className="summary-grid" aria-label="Resumo das transações filtradas">
        <article className="summary-card">
          <span>Total</span>
          <strong>{resumo.total}</strong>
        </article>
        <article className="summary-card summary-card-positive">
          <span>Receitas</span>
          <strong>{formatarMoeda(resumo.receitas)}</strong>
        </article>
        <article className="summary-card summary-card-negative">
          <span>Despesas</span>
          <strong>{formatarMoeda(resumo.despesas)}</strong>
        </article>
        <article className={resumo.saldo >= 0 ? 'summary-card summary-card-positive' : 'summary-card summary-card-negative'}>
          <span>Saldo</span>
          <strong>{formatarMoeda(resumo.saldo)}</strong>
        </article>
      </section>

      <section className="transactions-panel">
        <div className="transactions-header">
          <div>
            <h2>Movimentações</h2>
            <p>Resultados filtrados pela seleção acima.</p>
          </div>

          <Link to="/transacoes/nova" className="sb-button sb-button-primary sb-button-sm">
            <span aria-hidden="true">+</span>
            Nova transação
          </Link>
        </div>

        {carregando ? (
          <div className="loading-inline" aria-live="polite">
            Carregando transações...
          </div>
        ) : transacoesFiltradas.length === 0 ? (
          <EstadoVazio
            titulo="Nenhuma transação encontrada"
            descricao="Nenhuma movimentação encontrada para os filtros selecionados."
            acao={
              <Link to="/transacoes/nova" className="sb-button sb-button-primary sb-button-sm">
                Criar transação
              </Link>
            }
          />
        ) : (
          <div className="transactions-table-wrapper">
            <table className="transactions-table">
              <thead>
                <tr>
                  <th>Descrição</th>
                  <th>Categoria</th>
                  <th>Conta</th>
                  <th>Data</th>
                  <th className="text-end">Valor</th>
                </tr>
              </thead>
              <tbody>
                {transacoesFiltradas.map((transacao) => {
                  const receita = transacao.tipoTransacao === 'CREDITO';
                  const conta = transacao.contaId ? contaPorId.get(transacao.contaId) : undefined;

                  return (
                    <tr key={transacao.transacaoId}>
                      <td>
                        <div className="transaction-description">
                          <span className={receita ? 'transaction-dot positive' : 'transaction-dot negative'} />
                          <div>
                            <strong>{transacao.descricao || 'Transação manual'}</strong>
                            <small>
                              {obterRotuloTipo(transacao.tipoTransacao)} • {obterRotuloFormaPagamento(transacao.formaPagamento)}
                            </small>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div className="category-cell">
                          {!transacao.categorizada && (
                            <span className="category-pending-badge">
                              Pendente
                            </span>
                          )}

                          <select
                            className="category-select"
                            value={transacao.categoriaId ?? ''}
                            disabled={transacaoAtualizandoId === transacao.transacaoId}
                            onChange={(evento) => atualizarCategoriaTransacao(transacao.transacaoId, evento.target.value)}
                            aria-label={`Categoria da transação ${transacao.descricao || transacao.transacaoId}`}
                          >
                            <option value="" disabled>Sem categoria</option>
                            {categorias.map((categoriaOpcao) => (
                              <option key={categoriaOpcao.categoriaId} value={categoriaOpcao.categoriaId}>
                                {categoriaOpcao.nome}
                                {categoriaOpcao.padrao ? ' — padrão' : ''}
                              </option>
                            ))}
                          </select>

                          {transacaoAtualizandoId === transacao.transacaoId && (
                            <small className="category-updating">
                              Atualizando...
                            </small>
                          )}
                        </div>
                      </td>
                      <td>{conta?.nome || 'Dinheiro'}</td>
                      <td>{formatarData(transacao.data)}</td>
                      <td className={receita ? 'text-end amount-positive' : 'text-end amount-negative'}>
                        {formatarMoeda(transacao.valor)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </LayoutPrivado>
  );
};

export default Transacoes;
