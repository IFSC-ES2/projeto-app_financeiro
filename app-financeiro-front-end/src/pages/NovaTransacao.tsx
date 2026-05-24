import React, { useEffect, useMemo, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useAutenticacao } from '../contexts/ContextoAutenticacao';
import {
  listarCategorias,
  listarContas,
  registrarTransacaoManual,
} from '../services/api';
import type {
  CategoriaResponse,
  ContaResponse,
  TipoPagamento,
  TipoTransacao,
  TransacaoResponse,
} from '../services/api';

type CamposTransacao = {
  valor: string;
  data: string;
  descricao: string;
  tipoTransacao: TipoTransacao;
  formaPagamento: TipoPagamento;
  categoriaId: string;
  contaId: string;
};

const valoresIniciais: CamposTransacao = {
  valor: '',
  data: new Date().toISOString().slice(0, 10),
  descricao: '',
  tipoTransacao: 'DEBITO',
  formaPagamento: 'PIX',
  categoriaId: '',
  contaId: '',
};

const tiposTransacao: Array<{ valor: TipoTransacao; rotulo: string }> = [
  { valor: 'DEBITO', rotulo: 'Saída / despesa' },
  { valor: 'CREDITO', rotulo: 'Entrada / receita' },
  { valor: 'PARCELAMENTO', rotulo: 'Parcelamento' },
  { valor: 'BOLETO', rotulo: 'Boleto' },
];

const formasPagamento: Array<{ valor: TipoPagamento; rotulo: string }> = [
  { valor: 'PIX', rotulo: 'Pix' },
  { valor: 'CARTAO_DEBITO', rotulo: 'Cartão de débito' },
  { valor: 'CARTAO_CREDITO', rotulo: 'Cartão de crédito' },
  { valor: 'DINHEIRO', rotulo: 'Dinheiro' },
  { valor: 'BOLETO', rotulo: 'Boleto' },
  { valor: 'TED_DOC', rotulo: 'TED/DOC' },
];

const obterRotuloTipoTransacao = (valor: TipoTransacao) =>
  tiposTransacao.find((tipo) => tipo.valor === valor)?.rotulo || valor;

const obterRotuloFormaPagamento = (valor?: TipoPagamento | null) =>
  formasPagamento.find((forma) => forma.valor === valor)?.rotulo || valor || 'Não informado';

const formatarMoeda = (valor: number | string) =>
  Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

const NovaTransacao: React.FC = () => {
  const { estaAutenticado, sair } = useAutenticacao();
  const [campos, setCampos] = useState<CamposTransacao>(valoresIniciais);
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [transacoes, setTransacoes] = useState<TransacaoResponse[]>([]);
  const [erros, setErros] = useState<Partial<Record<keyof CamposTransacao, string>>>({});
  const [erroGeral, setErroGeral] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [carregandoDados, setCarregandoDados] = useState(false);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (!estaAutenticado) return;

    const carregarDados = async () => {
      setCarregandoDados(true);
      setErroGeral('');

      try {
        const [contasCarregadas, categoriasCarregadas] = await Promise.all([
          listarContas(),
          listarCategorias(),
        ]);

        setContas(contasCarregadas);
        setCategorias(categoriasCarregadas);

        setCampos((prev) => ({
          ...prev,
          contaId: prev.contaId || contasCarregadas[0]?.contaId || '',
          categoriaId: prev.categoriaId || categoriasCarregadas[0]?.categoriaId || '',
        }));
      } catch (err: any) {
        if (err?.response?.status === 401 || err?.response?.status === 403) {
          setErroGeral('Sua sessão expirou. Faça login novamente.');
          sair();
          return;
        }

        setErroGeral('Não foi possível carregar contas e categorias.');
      } finally {
        setCarregandoDados(false);
      }
    };

    carregarDados();
  }, [estaAutenticado, sair]);

  const categoriaPorId = useMemo(
    () => new Map(categorias.map((categoria) => [categoria.categoriaId, categoria])),
    [categorias]
  );

  const contaPorId = useMemo(
    () => new Map(contas.map((conta) => [conta.contaId, conta])),
    [contas]
  );

  if (!estaAutenticado) {
    return <Navigate to="/login" replace />;
  }

  const alterarCampo = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
  const { name, value } = e.target;

  if (name === 'formaPagamento' && value === 'DINHEIRO') {
    setCampos((prev) => ({
      ...prev,
      formaPagamento: value as TipoPagamento,
      contaId: '',
    }));

    setErros((prev) => ({
      ...prev,
      formaPagamento: undefined,
      contaId: undefined,
    }));

    return;
  }

  setCampos((prev) => ({ ...prev, [name]: value }));
  setErros((prev) => ({ ...prev, [name]: undefined }));
};

  const validar = () => {
    const novosErros: Partial<Record<keyof CamposTransacao, string>> = {};
    const valorNumerico = Number(campos.valor);

    if (!campos.valor) {
      novosErros.valor = 'Valor é obrigatório.';
    } else if (Number.isNaN(valorNumerico) || valorNumerico <= 0) {
      novosErros.valor = 'Informe um valor positivo.';
    }

    if (!campos.data) {
      novosErros.data = 'Data é obrigatória.';
    }

    if (campos.formaPagamento !== 'DINHEIRO' && !campos.contaId) {
    novosErros.contaId = 'Conta é obrigatória.';
    } 

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setErroGeral('');
    setSucesso('');

    if (!validar()) return;

    setSalvando(true);

    try {
      const transacaoSalva = await registrarTransacaoManual({
        valor: Number(campos.valor),
        data: campos.data,
        descricao: campos.descricao.trim() || undefined,
        tipoTransacao: campos.tipoTransacao,
        formaPagamento: campos.formaPagamento,
        categoriaId: campos.categoriaId || null,
        contaId: campos.contaId,
      });

      setTransacoes((prev) => [transacaoSalva, ...prev]);
      setSucesso('Transação registrada com sucesso.');

      setCampos((prev) => ({
        ...valoresIniciais,
        data: new Date().toISOString().slice(0, 10),
        contaId: prev.contaId,
        categoriaId: prev.categoriaId,
      }));
    } catch (err: any) {
      const msg = err?.response?.data?.erro || err?.response?.data?.message;
      setErroGeral(msg || 'Não foi possível registrar a transação.');
    } finally {
      setSalvando(false);
    }
  };

  return (
    <main className="min-vh-100 py-4" style={{ background: 'var(--sb-bg)' }}>
      <div className="container" style={{ maxWidth: 1100 }}>
        <div className="d-flex flex-column flex-md-row justify-content-between gap-3 align-items-md-center mb-4">
          <div>
            <span className="badge rounded-pill mb-2" style={{ background: 'var(--sb-primary)' }}>
              SmartBudget
            </span>
            <h1 className="fw-bold mb-1" style={{ color: 'var(--sb-text)' }}>
              Nova transação
            </h1>
            <p className="text-muted mb-0">
              Registre manualmente gastos que não vieram de extrato ou nota fiscal.
            </p>
          </div>

          <Link to="/dashboard" className="btn btn-outline-secondary align-self-start align-self-md-center">
            Voltar ao painel
          </Link>
        </div>

        <MensagemAlerta mensagem={erroGeral} tipo="danger" />
        <MensagemAlerta mensagem={sucesso} tipo="success" />

        <div className="row g-4">
          <section className="col-lg-7">
            <div className="card border-0 shadow-sm" style={{ borderRadius: 18 }}>
              <div className="card-body p-4">
                <h2 className="h5 fw-bold mb-3">Dados da transação</h2>

                {carregandoDados ? (
                  <p className="text-muted mb-0">Carregando contas e categorias...</p>
                ) : (
                  <form onSubmit={enviar} noValidate>
                    <div className="row g-3">
                      <div className="col-md-6">
                        <label className="form-label fw-semibold small" htmlFor="valor">
                          Valor *
                        </label>
                        <input
                          id="valor"
                          name="valor"
                          type="number"
                          min="0.01"
                          step="0.01"
                          className={`form-control ${erros.valor ? 'is-invalid' : ''}`}
                          value={campos.valor}
                          onChange={alterarCampo}
                          placeholder="0,00"
                        />
                        {erros.valor && <div className="invalid-feedback">{erros.valor}</div>}
                      </div>

                      <div className="col-md-6">
                        <label className="form-label fw-semibold small" htmlFor="data">
                          Data *
                        </label>
                        <input
                          id="data"
                          name="data"
                          type="date"
                          className={`form-control ${erros.data ? 'is-invalid' : ''}`}
                          value={campos.data}
                          onChange={alterarCampo}
                        />
                        {erros.data && <div className="invalid-feedback">{erros.data}</div>}
                      </div>

                      <div className="col-12">
                        <label className="form-label fw-semibold small" htmlFor="descricao">
                          Descrição
                        </label>
                        <input
                          id="descricao"
                          name="descricao"
                          type="text"
                          className="form-control"
                          value={campos.descricao}
                          onChange={alterarCampo}
                          placeholder="Ex.: mercado, almoço, transporte..."
                        />
                      </div>

                      <div className="col-md-6">
                        <label className="form-label fw-semibold small" htmlFor="tipoTransacao">
                          Tipo
                        </label>
                        <select
                          id="tipoTransacao"
                          name="tipoTransacao"
                          className="form-select"
                          value={campos.tipoTransacao}
                          onChange={alterarCampo}
                        >
                          {tiposTransacao.map((tipo) => (
                            <option key={tipo.valor} value={tipo.valor}>
                              {tipo.rotulo}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="col-md-6">
                        <label className="form-label fw-semibold small" htmlFor="formaPagamento">
                          Forma de pagamento
                        </label>
                        <select
                          id="formaPagamento"
                          name="formaPagamento"
                          className="form-select"
                          value={campos.formaPagamento}
                          onChange={alterarCampo}
                        >
                          {formasPagamento.map((forma) => (
                            <option key={forma.valor} value={forma.valor}>
                              {forma.rotulo}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="col-md-6">
                        <label className="form-label fw-semibold small" htmlFor="categoriaId">
                          Categoria
                        </label>
                        <select
                          id="categoriaId"
                          name="categoriaId"
                          className="form-select"
                          value={campos.categoriaId}
                          onChange={alterarCampo}
                        >
                          <option value="">Sem categoria</option>
                          {categorias.map((categoria) => (
                            <option key={categoria.categoriaId} value={categoria.categoriaId}>
                              {categoria.nome}
                              {categoria.padrao ? ' (padrão)' : ''}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="col-md-6">
                        <label className="form-label fw-semibold small" htmlFor="contaId">
                          Conta *
                        </label>
                        <select
                          id="contaId"
                          name="contaId"
                          className={`form-select ${erros.contaId ? 'is-invalid' : ''}`}
                          value={campos.contaId}
                          onChange={alterarCampo}
                        >
                          <option value="">Selecione uma conta</option>
                          {contas.map((conta) => (
                            <option key={conta.contaId} value={conta.contaId}>
                              {conta.nome}
                              {conta.banco ? ` - ${conta.banco}` : ''}
                            </option>
                          ))}
                        </select>
                        {erros.contaId && <div className="invalid-feedback">{erros.contaId}</div>}
                      </div>
                    </div>

                    <BotaoCarregando
                      type="submit"
                      carregando={salvando}
                      textoCarregando="Salvando..."
                      className="w-100 mt-4 py-2 fw-semibold"
                      style={{ background: 'var(--sb-gradient)', border: 'none', borderRadius: 10 }}
                      disabled={contas.length === 0}
                    >
                      Salvar transação
                    </BotaoCarregando>

                    {contas.length === 0 && (
                      <p className="text-muted small mt-3 mb-0">
                        Cadastre-se com uma conta antes de registrar transações.
                      </p>
                    )}
                  </form>
                )}
              </div>
            </div>
          </section>

          <section className="col-lg-5">
            <div className="card border-0 shadow-sm" style={{ borderRadius: 18 }}>
              <div className="card-body p-4">
                <h2 className="h5 fw-bold mb-3">Histórico recém-criado</h2>

                {transacoes.length === 0 ? (
                  <p className="text-muted mb-0">
                    As transações salvas nesta tela aparecerão aqui sem recarregar a página.
                  </p>
                ) : (
                  <div className="d-flex flex-column gap-3">
                    {transacoes.map((transacao) => (
                      <article
                        key={transacao.transacaoId}
                        className="border rounded-3 p-3 bg-light"
                      >
                        <div className="d-flex justify-content-between gap-3">
                          <div>
                            <strong>{transacao.descricao || 'Transação manual'}</strong>
                            <div className="text-muted small">
                              {contaPorId.get(transacao.contaId)?.nome || 'Conta'} •{' '}
                              {transacao.categoriaId
                                ? categoriaPorId.get(transacao.categoriaId)?.nome || 'Categoria'
                                : 'Sem categoria'}
                            </div>
                          </div>

                          <strong>{formatarMoeda(transacao.valor)}</strong>
                        </div>

                        <div className="text-muted small mt-2">
                          {transacao.data} • {obterRotuloTipoTransacao(transacao.tipoTransacao)} •{' '} 
                          {obterRotuloFormaPagamento(transacao.formaPagamento)}
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </section>
        </div>
      </div>
    </main>
  );
};

export default NovaTransacao;