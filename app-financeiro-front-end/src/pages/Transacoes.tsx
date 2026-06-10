import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import ResumoFormaPagamento from '../components/resumo/ResumoFormaPagamento';
import {
  categorizarTransacao,
  excluirTransacao,
  listarCategorias,
  listarContas,
  listarTransacoes,
  obterMensagemErroApi,
} from '../services/api';
import type {
  CategoriaResponse,
  ContaResponse,
  PaginaResponse,
  TransacaoResponse,
} from '../services/api';
import { formatarData, formatarMoeda } from '../utils/formatacao';
import { calcularResumoTransacoes } from '../utils/transacoes';

interface EstadoNavegacaoTransacoes {
  mensagem?: string;
}

type TipoTransacao = TransacaoResponse['tipoTransacao'];

interface FiltrosTransacao {
  dataInicio: string;
  dataFim: string;
  contaId: string;
  categoriaId: string;
  tipo: TipoTransacao | '';
}

const filtrosIniciais: FiltrosTransacao = {
  dataInicio: '',
  dataFim: '',
  contaId: '',
  categoriaId: '',
  tipo: '',
};

const TAMANHOS_PAGINA = [10, 20, 50];

const obterRotuloTipo = (tipo: TipoTransacao) => {
  const rotulos: Record<TipoTransacao, string> = {
    CREDITO: 'Receita',
    DEBITO: 'Despesa',
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

  const [pagina, setPagina] = useState<PaginaResponse<TransacaoResponse> | null>(null);
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [filtros, setFiltros] = useState<FiltrosTransacao>(filtrosIniciais);
  const [paginaAtual, setPaginaAtual] = useState(0);
  const [tamanho, setTamanho] = useState(20);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [mensagemSucesso, setMensagemSucesso] = useState(estado?.mensagem ?? '');
  const [transacaoAtualizandoId, setTransacaoAtualizandoId] = useState<string | null>(null);
  const [transacaoExcluindoId, setTransacaoExcluindoId] = useState<string | null>(null);
  const [mensagemCategoria, setMensagemCategoria] = useState('');
  const [atualizacaoLista, setAtualizacaoLista] = useState(0);

  useEffect(() => {
    if (estado?.mensagem) {
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [estado?.mensagem, location.pathname, navigate]);

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
        const [contasCarregadas, categoriasCarregadas] = await Promise.all([listarContas(), listarCategorias()]);

        if (!ativo) return;
        setContas(contasCarregadas);
        setCategorias(categoriasCarregadas);
      } catch (erroCapturado) {
        if (!ativo) return;
        setErro(obterMensagemErroApi(erroCapturado, 'Não foi possível carregar contas e categorias.'));
      }
    };

    carregarApoio();

    return () => {
      ativo = false;
    };
  }, []);

  useEffect(() => {
    let ativo = true;

    const carregarTransacoes = async () => {
      setCarregando(true);
      setErro('');

      try {
        const resultado = await listarTransacoes({
          page: paginaAtual,
          size: tamanho,
          dataInicio: filtros.dataInicio || undefined,
          dataFim: filtros.dataFim || undefined,
          categoriaId: filtros.categoriaId || undefined,
          contaId: filtros.contaId || undefined,
          tipo: filtros.tipo || undefined,
        });
        if (!ativo) return;
        setPagina(resultado);
      } catch (erroCapturado) {
        if (!ativo) return;
        setErro(obterMensagemErroApi(erroCapturado, 'Não foi possível carregar as transações.'));
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    carregarTransacoes();

    return () => {
      ativo = false;
    };
  }, [filtros, paginaAtual, tamanho, atualizacaoLista]);

  const contaPorId = useMemo(() => new Map(contas.map((conta) => [conta.contaId, conta])), [contas]);

  const transacoes = useMemo(() => pagina?.conteudo ?? [], [pagina]);
  const totalElementos = pagina?.totalElementos ?? 0;
  const totalPaginas = Math.max(pagina?.totalPaginas ?? 0, 1);
  const resumo = useMemo(() => calcularResumoTransacoes(transacoes), [transacoes]);

  const alterarFiltro = (campo: keyof FiltrosTransacao, valor: string) => {
    setMensagemCategoria('');
    setMensagemSucesso('');
    setFiltros((atuais) => ({ ...atuais, [campo]: valor }));
    setPaginaAtual(0);
  };

  const alterarTamanho = (novoTamanho: number) => {
    setTamanho(novoTamanho);
    setPaginaAtual(0);
  };

  const atualizarCategoriaTransacao = async (transacaoId: string, categoriaId: string) => {
    if (!categoriaId) return;

    setTransacaoAtualizandoId(transacaoId);
    setErro('');
    setMensagemCategoria('');

    try {
      const transacaoAtualizada = await categorizarTransacao(transacaoId, categoriaId);

      setPagina((atual) =>
        atual
          ? {
            ...atual,
            conteudo: atual.conteudo.map((transacao) =>
              transacao.transacaoId === transacaoId ? transacaoAtualizada : transacao
            ),
          }
          : atual
      );

      setMensagemCategoria('Categoria atualizada com sucesso.');
    } catch (erroCapturado) {
      setErro(obterMensagemErroApi(erroCapturado, 'Não foi possível atualizar a categoria da transação.'));
    } finally {
      setTransacaoAtualizandoId(null);
    }
  };

  const excluirTransacaoSelecionada = async (transacao: TransacaoResponse) => {
    const confirmou = window.confirm(
      `Deseja excluir a transação "${transacao.descricao || 'Transação manual'}"? Esta ação não pode ser desfeita.`
    );

    if (!confirmou) return;

    setTransacaoExcluindoId(transacao.transacaoId);
    setErro('');
    setMensagemCategoria('');
    setMensagemSucesso('');

    try {
      await excluirTransacao(transacao.transacaoId);
      setMensagemSucesso('Transação excluída com sucesso.');

      if (transacoes.length === 1 && paginaAtual > 0) {
        setPaginaAtual((atual) => Math.max(atual - 1, 0));
      } else {
        setAtualizacaoLista((atual) => atual + 1);
      }
    } catch (erroCapturado) {
      setErro(obterMensagemErroApi(erroCapturado, 'Não foi possível excluir a transação.'));
    } finally {
      setTransacaoExcluindoId(null);
    }
  };

  return (
    <LayoutPrivado titulo="Transações" subtitulo="Visualize e registre movimentações financeiras da sua conta.">
      <MensagemAlerta mensagem={mensagemSucesso} tipo="success" />
      <MensagemAlerta mensagem={mensagemCategoria} tipo="success" />
      <MensagemAlerta mensagem={erro} tipo="danger" />
      <section className="filters-panel" aria-label="Filtros de transações">
        <label>
          <span>Início</span>
          <input
            type="date"
            value={filtros.dataInicio}
            max={filtros.dataFim || undefined}
            onChange={(evento) => alterarFiltro('dataInicio', evento.target.value)}
          />
        </label>

        <label>
          <span>Fim</span>
          <input
            type="date"
            value={filtros.dataFim}
            min={filtros.dataInicio || undefined}
            onChange={(evento) => alterarFiltro('dataFim', evento.target.value)}
          />
        </label>

        <label>
          <span>Tipo</span>
          <select
            value={filtros.tipo}
            onChange={(evento) => alterarFiltro('tipo', evento.target.value as TipoTransacao | '')}
          >
            <option value="">Todos os tipos</option>
            <option value="CREDITO">Receita</option>
            <option value="DEBITO">Despesa</option>
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

      <section className="summary-grid" aria-label="Resumo das transações (página atual)">
        <article className="summary-card">
          <span>Total</span>
          <strong>{totalElementos}</strong>
        </article>
        <article className="summary-card summary-card-positive">
          <span>Receitas (página)</span>
          <strong>{formatarMoeda(resumo.receitas)}</strong>
        </article>
        <article className="summary-card summary-card-negative">
          <span>Despesas (página)</span>
          <strong>{formatarMoeda(resumo.despesas)}</strong>
        </article>
        <article className={resumo.saldo >= 0 ? 'summary-card summary-card-positive' : 'summary-card summary-card-negative'}>
          <span>Saldo (página)</span>
          <strong>{formatarMoeda(resumo.saldo)}</strong>
        </article>
      </section>

      <ResumoFormaPagamento />

      <section className="transactions-panel">
        <div className="transactions-header">
          <div>
            <h2>Movimentações</h2>
            <p>Filtros e paginação aplicados no servidor. Receitas, despesas e saldo referem-se à página atual.</p>
          </div>

          <div className="transactions-actions">
            <Link to="/importacoes/nova" className="sb-button sb-button-secondary sb-button-sm">
              Importar extrato
            </Link>

            <Link to="/transacoes/nova" className="sb-button sb-button-primary sb-button-sm">
              <span aria-hidden="true">+</span>
              Nova transação
            </Link>
          </div>
        </div>

        {carregando ? (
          <div className="loading-inline" aria-live="polite">
            Carregando transações...
          </div>
        ) : transacoes.length === 0 ? (
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
          <>
            <div className="transactions-table-wrapper">
              <table className="transactions-table">
                <thead>
                  <tr>
                    <th>Descrição</th>
                    <th>Categoria</th>
                    <th>Conta</th>
                    <th>Data</th>
                    <th className="text-end">Valor</th>
                    <th className="text-end">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {transacoes.map((transacao) => {
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
                              <span className="category-pending-badge">Pendente</span>
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
                              <small className="category-updating">Atualizando...</small>
                            )}
                          </div>
                        </td>
                        <td>{conta?.nome || 'Dinheiro'}</td>
                        <td>{formatarData(transacao.data)}</td>
                        <td className={receita ? 'text-end amount-positive' : 'text-end amount-negative'}>
                          {formatarMoeda(transacao.valor)}
                        </td>
                        <td className="text-end">
                          <div className="transaction-actions">
                            <Link
                              to={`/transacoes/${transacao.transacaoId}/editar`}
                              state={{ transacao }}
                              className="sb-button sb-button-secondary sb-button-xs"
                            >
                              Editar
                            </Link>
                            <button
                              type="button"
                              className="sb-button sb-button-danger sb-button-xs"
                              disabled={transacaoExcluindoId === transacao.transacaoId}
                              onClick={() => excluirTransacaoSelecionada(transacao)}
                            >
                              {transacaoExcluindoId === transacao.transacaoId ? 'Excluindo...' : 'Excluir'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="pagination-bar">
              <div className="pagination-info">
                <label className="page-size">
                  <span>Por página</span>
                  <select value={tamanho} onChange={(evento) => alterarTamanho(Number(evento.target.value))}>
                    {TAMANHOS_PAGINA.map((opcao) => (
                      <option key={opcao} value={opcao}>
                        {opcao}
                      </option>
                    ))}
                  </select>
                </label>
                <span className="pagination-count">
                  {totalElementos} {totalElementos === 1 ? 'transação' : 'transações'}
                </span>
              </div>

              <div className="pagination-controls">
                <button
                  type="button"
                  className="sb-button sb-button-secondary sb-button-sm"
                  disabled={pagina?.primeira ?? true}
                  onClick={() => setPaginaAtual((atual) => Math.max(atual - 1, 0))}
                >
                  <span aria-hidden="true">‹</span> Anterior
                </button>
                <span className="pagination-status">
                  Página {paginaAtual + 1} de {totalPaginas}
                </span>
                <button
                  type="button"
                  className="sb-button sb-button-secondary sb-button-sm"
                  disabled={pagina?.ultima ?? true}
                  onClick={() => setPaginaAtual((atual) => atual + 1)}
                >
                  Próxima <span aria-hidden="true">›</span>
                </button>
              </div>
            </div>
          </>
        )}
      </section>
    </LayoutPrivado>
  );
};

export default Transacoes;
