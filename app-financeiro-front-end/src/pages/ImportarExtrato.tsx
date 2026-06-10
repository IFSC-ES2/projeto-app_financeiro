import React, { useEffect, useRef, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useAutenticacao } from '../contexts/ContextoAutenticacao';
import {
  consultarStatusImportacao,
  criarImportacao,
  listarContas,
  obterMensagemErroApi,
  obterStatusHttp,
} from '../services/api';
import type {
  ContaResponse,
  ImportacaoResponse,
  StatusImportacao,
} from '../services/api';

const EXTENSOES_ACEITAS = ['csv', 'xml', 'txt'];
const ACCEPT_INPUT = '.csv,.xml,.txt';
const INTERVALO_POLLING_MS = 1500;
const TIMEOUT_POLLING_MS = 60_000;

const ROTULOS_STATUS: Record<StatusImportacao, string> = {
  PENDENTE: 'Aguardando processamento',
  PROCESSANDO: 'Processando arquivo',
  CONCLUIDO: 'Importação concluída',
  ERRO: 'Falha na importação',
};

const formatarTamanho = (bytes: number) => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const obterExtensao = (nome: string) =>
  nome.includes('.') ? nome.split('.').pop()!.toLowerCase() : '';

type ErrosFormulario = {
  arquivo?: string;
  contaId?: string;
};

const ImportarExtrato: React.FC = () => {
  const { estaAutenticado, sair } = useAutenticacao();

  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [carregandoContas, setCarregandoContas] = useState(false);

  const [arquivo, setArquivo] = useState<File | null>(null);
  const [contaId, setContaId] = useState('');
  const [erros, setErros] = useState<ErrosFormulario>({});

  const [erroGeral, setErroGeral] = useState('');
  const [enviando, setEnviando] = useState(false);

  const [importacao, setImportacao] = useState<ImportacaoResponse | null>(null);
  const [statusAtual, setStatusAtual] = useState<StatusImportacao | null>(null);

  const [arrastando, setArrastando] = useState(false);
  const inputArquivoRef = useRef<HTMLInputElement>(null);

  const finalizada = statusAtual === 'CONCLUIDO' || statusAtual === 'ERRO';
  const processando = !!importacao && !finalizada;

  useEffect(() => {
    if (!estaAutenticado) return;

    const carregar = async () => {
      setCarregandoContas(true);
      try {
        const data = await listarContas();
        setContas(data);
        if (data.length === 1) setContaId(data[0].contaId);
      } catch (err) {
        const status = obterStatusHttp(err);
        if (status === 401) {
          sair();
          return;
        }
        setErroGeral(
          obterMensagemErroApi(err, 'Não foi possível carregar suas contas. Tente novamente.'),
        );
      } finally {
        setCarregandoContas(false);
      }
    };

    carregar();
  }, [estaAutenticado, sair]);

  useEffect(() => {
    if (!importacao) return;
    if (statusAtual === 'CONCLUIDO' || statusAtual === 'ERRO') return;

    const inicio = Date.now();
    let cancelado = false;
    let timeoutId = 0;

    const verificar = async () => {
      if (cancelado) return;
      try {
        const status = await consultarStatusImportacao(importacao.id);
        if (cancelado) return;
        setStatusAtual(status);
        if (status === 'CONCLUIDO' || status === 'ERRO') return;
      } catch (err) {
        if (cancelado) return;
        const status = obterStatusHttp(err);
        if (status === 401) {
          sair();
          return;
        }
        if (status === 403) {
          setErroGeral(obterMensagemErroApi(err, 'Acesso negado ao acompanhar a importação.'));
          setStatusAtual('ERRO');
          return;
        }
        // erro pontual no polling não interrompe — segue tentando até timeout
      }

      if (Date.now() - inicio >= TIMEOUT_POLLING_MS) {
        setErroGeral('Tempo limite excedido aguardando o processamento. Verifique mais tarde.');
        setStatusAtual('ERRO');
        return;
      }

      timeoutId = window.setTimeout(verificar, INTERVALO_POLLING_MS);
    };

    timeoutId = window.setTimeout(verificar, INTERVALO_POLLING_MS);

    return () => {
      cancelado = true;
      window.clearTimeout(timeoutId);
    };
  }, [importacao, statusAtual, sair]);

  if (!estaAutenticado) {
    return <Navigate to="/login" replace />;
  }

  const validarArquivo = (file: File): string | undefined => {
    const ext = obterExtensao(file.name);
    if (!EXTENSOES_ACEITAS.includes(ext)) {
      return 'Formato não suportado. Use arquivos .csv, .xml ou .txt.';
    }
    if (file.size === 0) {
      return 'O arquivo está vazio.';
    }
    return undefined;
  };

  const definirArquivo = (file: File | null) => {
    if (!file) {
      setArquivo(null);
      return;
    }
    const erro = validarArquivo(file);
    if (erro) {
      setErros((prev) => ({ ...prev, arquivo: erro }));
      setArquivo(null);
      return;
    }
    setErros((prev) => ({ ...prev, arquivo: undefined }));
    setArquivo(file);
  };

  const aoSoltarArquivo = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setArrastando(false);
    const file = e.dataTransfer.files?.[0];
    if (file) definirArquivo(file);
  };

  const limparArquivo = () => {
    setArquivo(null);
    setErros((prev) => ({ ...prev, arquivo: undefined }));
    if (inputArquivoRef.current) inputArquivoRef.current.value = '';
  };

  const reiniciar = () => {
    setImportacao(null);
    setStatusAtual(null);
    setErroGeral('');
    limparArquivo();
  };

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setErroGeral('');

    const novosErros: ErrosFormulario = {};
    if (!contaId) novosErros.contaId = 'Selecione uma conta de destino.';
    if (!arquivo) novosErros.arquivo = 'Selecione um arquivo para importar.';
    setErros(novosErros);
    if (Object.keys(novosErros).length > 0) return;

    setEnviando(true);
    try {
      const resultado = await criarImportacao(arquivo!, contaId);
      setImportacao(resultado);
      setStatusAtual(resultado.status);
    } catch (err) {
      const status = obterStatusHttp(err);
      if (status === 401) {
        sair();
        return;
      }
      setErroGeral(obterMensagemErroApi(err, 'Não foi possível iniciar a importação. Tente novamente.'));
    } finally {
      setEnviando(false);
    }
  };

  const semContas = !carregandoContas && contas.length === 0;
  const importacaoEmAndamento = !!importacao && !finalizada;
  const formularioBloqueado = enviando || importacaoEmAndamento || finalizada;

  return (
    <main className="min-vh-100 py-4" style={{ background: 'var(--sb-bg)' }}>
      <div className="container" style={{ maxWidth: 920 }}>
        <div className="d-flex flex-column flex-md-row justify-content-between gap-3 align-items-md-center mb-4">
          <div>
            <span className="badge rounded-pill mb-2" style={{ background: 'var(--sb-primary)' }}>
              SmartBudget
            </span>
            <h1 className="fw-bold mb-1" style={{ color: 'var(--sb-text)' }}>
              Importar extrato ou NF-e
            </h1>
            <p className="text-muted mb-0">
              Envie um arquivo CSV, XML ou TXT e nós cadastramos suas transações automaticamente.
            </p>
          </div>

          <Link to="/transacoes" className="btn btn-outline-secondary align-self-start align-self-md-center">
            Voltar ao painel
          </Link>
        </div>

        <MensagemAlerta mensagem={erroGeral} tipo="danger" />

        <div className="card border-0 shadow-sm" style={{ borderRadius: 18 }}>
          <div className="card-body p-4 p-md-5">
            {!finalizada && (
              <form onSubmit={enviar} noValidate>
                <div className="mb-4">
                  <label htmlFor="contaId" className="form-label fw-semibold small mb-1">
                    Conta de destino *
                  </label>
                  <select
                    id="contaId"
                    name="contaId"
                    className={`form-select ${erros.contaId ? 'is-invalid' : ''}`}
                    value={contaId}
                    onChange={(e) => {
                      setContaId(e.target.value);
                      setErros((prev) => ({ ...prev, contaId: undefined }));
                    }}
                    disabled={formularioBloqueado || carregandoContas || semContas}
                  >
                    <option value="">
                      {carregandoContas
                        ? 'Carregando contas...'
                        : semContas
                          ? 'Nenhuma conta cadastrada'
                          : 'Selecione uma conta'}
                    </option>
                    {contas.map((conta) => (
                      <option key={conta.contaId} value={conta.contaId}>
                        {conta.nome}
                        {conta.banco ? ` — ${conta.banco}` : ''}
                      </option>
                    ))}
                  </select>
                  {erros.contaId && <div className="invalid-feedback">{erros.contaId}</div>}
                  {semContas && (
                    <p className="text-muted small mt-2 mb-0">
                      Você precisa{' '}
                      <Link to="/contas" className="sb-button sb-button-primary">
                        Cadastrar conta
                      </Link>{' '}
                      antes de importar transações.
                    </p>
                  )}
                </div>

                <div className="mb-4">
                  <label className="form-label fw-semibold small mb-1">Arquivo *</label>

                  {!arquivo ? (
                    <div
                      onDragOver={(e) => {
                        e.preventDefault();
                        setArrastando(true);
                      }}
                      onDragLeave={() => setArrastando(false)}
                      onDrop={aoSoltarArquivo}
                      onClick={() => inputArquivoRef.current?.click()}
                      role="button"
                      tabIndex={0}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault();
                          inputArquivoRef.current?.click();
                        }
                      }}
                      className="d-flex flex-column align-items-center justify-content-center text-center p-4"
                      style={{
                        border: `2px dashed ${arrastando ? 'var(--sb-primary)' : erros.arquivo ? '#dc3545' : 'var(--sb-border)'}`,
                        borderRadius: 14,
                        background: arrastando ? 'rgba(47, 169, 143, 0.06)' : 'var(--sb-surface)',
                        cursor: 'pointer',
                        transition: 'border-color 0.18s ease, background 0.18s ease',
                        minHeight: 180,
                      }}
                    >
                      <div
                        className="d-flex align-items-center justify-content-center mb-3"
                        style={{
                          width: 56,
                          height: 56,
                          borderRadius: '50%',
                          background: 'rgba(47, 169, 143, 0.12)',
                          color: 'var(--sb-primary)',
                        }}
                      >
                        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                          <polyline points="17 8 12 3 7 8" />
                          <line x1="12" y1="3" x2="12" y2="15" />
                        </svg>
                      </div>
                      <p className="fw-semibold mb-1" style={{ color: 'var(--sb-text)' }}>
                        {arrastando ? 'Solte o arquivo aqui' : 'Arraste um arquivo ou clique para selecionar'}
                      </p>
                      <p className="text-muted small mb-0">Formatos suportados: .csv, .xml, .txt</p>
                    </div>
                  ) : (
                    <div
                      className="d-flex align-items-center justify-content-between p-3"
                      style={{
                        border: '1px solid var(--sb-border)',
                        borderRadius: 14,
                        background: 'rgba(47, 169, 143, 0.06)',
                      }}
                    >
                      <div className="d-flex align-items-center gap-3 text-truncate">
                        <div
                          className="d-flex align-items-center justify-content-center flex-shrink-0"
                          style={{
                            width: 44,
                            height: 44,
                            borderRadius: 10,
                            background: 'var(--sb-primary)',
                            color: '#fff',
                            fontWeight: 600,
                            fontSize: '0.78rem',
                            textTransform: 'uppercase',
                          }}
                        >
                          {obterExtensao(arquivo.name)}
                        </div>
                        <div className="text-truncate">
                          <div className="fw-semibold text-truncate" title={arquivo.name}>
                            {arquivo.name}
                          </div>
                          <div className="text-muted small">{formatarTamanho(arquivo.size)}</div>
                        </div>
                      </div>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-secondary ms-3"
                        onClick={limparArquivo}
                        disabled={formularioBloqueado}
                      >
                        Trocar
                      </button>
                    </div>
                  )}

                  <input
                    ref={inputArquivoRef}
                    type="file"
                    accept={ACCEPT_INPUT}
                    className="d-none"
                    onChange={(e) => definirArquivo(e.target.files?.[0] ?? null)}
                  />

                  {erros.arquivo && (
                    <p className="text-danger small mt-2 mb-0">{erros.arquivo}</p>
                  )}
                </div>

                {importacaoEmAndamento && statusAtual && (
                  <div
                    className="d-flex align-items-center gap-3 p-3 mb-4"
                    style={{
                      border: '1px solid var(--sb-border)',
                      borderRadius: 12,
                      background: 'rgba(47, 169, 143, 0.05)',
                    }}
                  >
                    <span className="spinner-border spinner-border-sm" style={{ color: 'var(--sb-primary)' }} role="status" aria-hidden="true" />
                    <div>
                      <div className="fw-semibold" style={{ color: 'var(--sb-text)' }}>
                        {ROTULOS_STATUS[statusAtual]}
                      </div>
                      <div className="text-muted small">
                        Não feche esta tela enquanto o arquivo é processado.
                      </div>
                    </div>
                  </div>
                )}

                <BotaoCarregando
                  type="submit"
                  carregando={enviando || processando}
                  textoCarregando={enviando ? 'Enviando arquivo...' : 'Processando...'}
                  className="w-100 py-2 fw-semibold"
                  style={{
                    background: 'var(--sb-gradient)',
                    border: 'none',
                    borderRadius: 10,
                    fontSize: '1rem',
                  }}
                  disabled={semContas}
                >
                  Iniciar importação
                </BotaoCarregando>
              </form>
            )}

            {finalizada && importacao && (
              <ResultadoImportacao
                importacao={importacao}
                status={statusAtual!}
                onNovaImportacao={reiniciar}
              />
            )}
          </div>
        </div>
      </div>
    </main>
  );
};

interface PropsResultado {
  importacao: ImportacaoResponse;
  status: StatusImportacao;
  onNovaImportacao: () => void;
}

const ResultadoImportacao: React.FC<PropsResultado> = ({ importacao, status, onNovaImportacao }) => {
  const sucesso = status === 'CONCLUIDO';

  return (
    <div className="text-center">
      <div
        className="d-inline-flex align-items-center justify-content-center mb-3"
        style={{
          width: 72,
          height: 72,
          borderRadius: '50%',
          background: sucesso ? 'rgba(47, 169, 143, 0.15)' : 'rgba(220, 53, 69, 0.12)',
          color: sucesso ? 'var(--sb-primary)' : '#dc3545',
        }}
      >
        {sucesso ? (
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        ) : (
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        )}
      </div>

      <h2 className="h4 fw-bold mb-1">
        {sucesso ? 'Importação concluída' : 'Importação falhou'}
      </h2>
      <p className="text-muted mb-4">
        {sucesso
          ? 'Seu arquivo foi processado com sucesso.'
          : 'Não foi possível processar o arquivo enviado.'}
      </p>

      {sucesso ? (
        <div className="row g-3 mb-4 justify-content-center">
          <div className="col-6 col-sm-5">
            <div
              className="p-3 h-100"
              style={{
                border: '1px solid var(--sb-border)',
                borderRadius: 12,
                background: 'rgba(47, 169, 143, 0.06)',
              }}
            >
              <div className="text-muted small text-uppercase fw-semibold" style={{ letterSpacing: '0.04em' }}>
                Sucessos
              </div>
              <div className="fw-bold mt-1" style={{ fontSize: '1.8rem', color: 'var(--sb-primary)' }}>
                {importacao.sucessos}
              </div>
              <div className="text-muted small">transações importadas</div>
            </div>
          </div>
          <div className="col-6 col-sm-5">
            <div
              className="p-3 h-100"
              style={{
                border: '1px solid var(--sb-border)',
                borderRadius: 12,
                background: importacao.falhas > 0 ? 'rgba(220, 53, 69, 0.06)' : 'var(--sb-surface)',
              }}
            >
              <div className="text-muted small text-uppercase fw-semibold" style={{ letterSpacing: '0.04em' }}>
                Falhas
              </div>
              <div
                className="fw-bold mt-1"
                style={{
                  fontSize: '1.8rem',
                  color: importacao.falhas > 0 ? '#dc3545' : 'var(--sb-text-muted)',
                }}
              >
                {importacao.falhas}
              </div>
              <div className="text-muted small">registros descartados</div>
            </div>
          </div>
        </div>
      ) : (
        <MensagemAlerta
          mensagem={importacao.mensagemErro || 'Ocorreu um erro durante o processamento do arquivo.'}
          tipo="danger"
        />
      )}

      <div className="d-flex flex-column flex-sm-row gap-2 justify-content-center mt-3">
        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={onNovaImportacao}
        >
          Nova importação
        </button>
        <Link to="/dashboard" className="btn fw-semibold" style={{ background: 'var(--sb-gradient)', color: '#fff', borderRadius: 8 }}>
          Ir para o painel
        </Link>
      </div>
    </div>
  );
};

export default ImportarExtrato;
