import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { listarCategorias, listarContas, listarTransacoes, obterMensagemErroApi } from '../services/api';
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

  const [pagina, setPagina] = useState<PaginaResponse<TransacaoResponse> | null>(null);
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [filtros, setFiltros] = useState<FiltrosTransacao>(filtrosIniciais);
  const [paginaAtual, setPaginaAtual] = useState(0);
  const [tamanho, setTamanho] = useState(20);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [mensagemSucesso] = useState(estado?.mensagem ?? '');

  useEffect(() => {
    if (estado?.mensagem) {
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [estado?.mensagem, location.pathname, navigate]);

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
        obterMensagemErroApi(erroCapturado, 'Não foi possível carregar contas e categorias.');
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
  }, [filtros, paginaAtual, tamanho]);

  const contaPorId = useMemo(() => new Map(contas.map((conta) => [conta.contaId, conta])), [contas]);
  const categoriaPorId = useMemo(
    () => new Map(categorias.map((categoria) => [categoria.categoriaId, categoria])),
    [categorias]
  );

  const transacoes = useMemo(() => pagina?.conteudo ?? [], [pagina]);
  const totalElementos = pagina?.totalElementos ?? 0;
  const totalPaginas = Math.max(pagina?.totalPaginas ?? 0, 1);
  const resumo = useMemo(() => calcularResumoTransacoes(transacoes), [transacoes]);

  const alterarFiltro = (campo: keyof FiltrosTransacao, valor: string) => {
    setFiltros((atuais) => ({ ...atuais, [campo]: valor }));
    setPaginaAtual(0);
  };

  const alterarTamanho = (novoTamanho: number) => {
    setTamanho(novoTamanho);
    setPaginaAtual(0);
  };

  return (
    <LayoutPrivado titulo="Transações" subtitulo="Visualize e registre movimentações financeiras da sua conta.">
      <MensagemAlerta mensagem={mensagemSucesso} tipo="success" />
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
            <option value="PARCELAMENTO">Parcelamento</option>
            <option value="BOLETO">Boleto</option>
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

      <section className="transactions-panel">
        <div className="transactions-header">
          <div>
            <h2>Movimentações</h2>
            <p>Filtros e paginação aplicados no servidor. Receitas, despesas e saldo referem-se à página atual.</p>
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
                  </tr>
                </thead>
                <tbody>
                  {transacoes.map((transacao) => {
                    const receita = transacao.tipoTransacao === 'CREDITO';
                    const categoria = transacao.categoriaId ? categoriaPorId.get(transacao.categoriaId) : undefined;
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
                        <td>{categoria?.nome || 'Sem categoria'}</td>
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
