import React, { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import LayoutAutenticacao from '../components/layout/LayoutAutenticacao';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useAutenticacao } from '../contexts/ContextoAutenticacao';
import { registrarConta } from '../services/api';
import type { ContaRequest, TipoConta } from '../services/api';

type CamposConta = {
  nome: string;
  tipoConta: TipoConta;
  banco: string;
  descricao: string;
};

const valoresIniciais: CamposConta = {
  nome: '',
  tipoConta: 'CORRENTE',
  banco: 'Nubank',
  descricao: '',
};

const tiposConta: Array<{ valor: TipoConta; rotulo: string }> = [
  { valor: 'CORRENTE', rotulo: 'Conta corrente' },
  { valor: 'POUPANCA', rotulo: 'Conta poupança' },
  { valor: 'CARTAO_CREDITO', rotulo: 'Cartão de crédito' },
  { valor: 'CARTEIRA', rotulo: 'Outro / Carteira' },
];

const bancos = [
  'Nubank',
  'Banco do Brasil',
  'Caixa',
  'Itaú',
  'Bradesco',
  'Santander',
  'Outro',
];

const NovaConta: React.FC = () => {
  const { estaAutenticado, sair } = useAutenticacao();

  const [campos, setCampos] = useState<CamposConta>(valoresIniciais);
  const [erros, setErros] = useState<Partial<Record<keyof CamposConta, string>>>({});
  const [erroGeral, setErroGeral] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [salvando, setSalvando] = useState(false);

  if (!estaAutenticado) {
    return <Navigate to="/login" replace />;
  }

  const alterarCampo = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setCampos((prev) => ({ ...prev, [name]: value }));
    setErros((prev) => ({ ...prev, [name]: undefined }));
  };

  const validar = () => {
    const novosErros: Partial<Record<keyof CamposConta, string>> = {};

    if (!campos.nome.trim()) {
      novosErros.nome = 'Nome da conta é obrigatório.';
    }

    if (!campos.tipoConta) {
      novosErros.tipoConta = 'Tipo de conta é obrigatório.';
    }

    if (!campos.banco.trim()) {
      novosErros.banco = 'Banco é obrigatório.';
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
      const novaConta: ContaRequest = {
        nome: campos.nome.trim(),
        tipoConta: campos.tipoConta,
        banco: campos.banco.trim(),
        descricao: campos.descricao.trim() || undefined,
      };

      await registrarConta(novaConta);

      setSucesso('Conta bancária cadastrada com sucesso. Agora ela já pode ser usada em transações manuais.');
      setCampos(valoresIniciais);
      setErros({});
    } catch (err: any) {
      if (err?.response?.status === 401 || err?.response?.status === 403) {
        setErroGeral('Sua sessão expirou. Faça login novamente.');
        sair();
        return;
      }

      const msg = err?.response?.data?.erro || err?.response?.data?.message;
      setErroGeral(msg || 'Não foi possível cadastrar a conta bancária.');
    } finally {
      setSalvando(false);
    }
  };

  return (
    <LayoutAutenticacao
      tituloPainel="Quase pronto!"
      subtituloPainel="Agora associe sua primeira conta bancária para começar a organizar suas finanças."
    >
      <div className="mb-4">
        <div className="d-flex d-lg-none align-items-center gap-2 mb-4">
          <img
            src="/smartbudget-logo.png"
            alt="SmartBudget"
            width="32"
            height="32"
            style={{ objectFit: 'contain' }}
          />
          <span className="fw-bold" style={{ fontSize: '1.2rem', color: 'var(--sb-primary-dark)' }}>
            SmartBudget
          </span>
        </div>

        <h2 className="fw-bold mb-1" style={{ color: 'var(--sb-text)', fontSize: '1.8rem' }}>
          Associar conta bancária
        </h2>

        <p className="text-muted small mb-0">
          Informe os dados da conta que será usada nas transações.
        </p>
      </div>

      <MensagemAlerta mensagem={erroGeral} tipo="danger" />
      <MensagemAlerta mensagem={sucesso} tipo="success" />

      <form onSubmit={enviar} noValidate>
        <div className="mb-3">
          <label htmlFor="nome" className="form-label fw-semibold text-secondary small mb-1">
            Nome da conta
          </label>

          <div className="input-group">
            <span className="input-group-text bg-light border-end-0 text-muted">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="4" width="18" height="16" rx="2" />
                <path d="M7 8h10" />
                <path d="M7 12h7" />
                <path d="M7 16h5" />
              </svg>
            </span>

            <input
              id="nome"
              name="nome"
              type="text"
              className={`form-control border-start-0 ${erros.nome ? 'is-invalid' : ''}`}
              style={{ boxShadow: 'none' }}
              value={campos.nome}
              onChange={alterarCampo}
              placeholder="Ex: Conta Principal"
            />

            {erros.nome && <div className="invalid-feedback">{erros.nome}</div>}
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="tipoConta" className="form-label fw-semibold text-secondary small mb-1">
            Tipo de conta
          </label>

          <div className="input-group">
            <span className="input-group-text bg-light border-end-0 text-muted">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M3 10h18" />
                <path d="M5 10V7l7-4 7 4v3" />
                <path d="M6 10v8" />
                <path d="M10 10v8" />
                <path d="M14 10v8" />
                <path d="M18 10v8" />
                <path d="M4 18h16" />
              </svg>
            </span>

            <select
              id="tipoConta"
              name="tipoConta"
              className={`form-select border-start-0 ${erros.tipoConta ? 'is-invalid' : ''}`}
              style={{ boxShadow: 'none' }}
              value={campos.tipoConta}
              onChange={alterarCampo}
            >
              {tiposConta.map((tipo) => (
                <option key={tipo.valor} value={tipo.valor}>
                  {tipo.rotulo}
                </option>
              ))}
            </select>

            {erros.tipoConta && <div className="invalid-feedback">{erros.tipoConta}</div>}
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="banco" className="form-label fw-semibold text-secondary small mb-1">
            Banco
          </label>

          <div className="input-group">
            <span className="input-group-text bg-light border-end-0 text-muted">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M4 10h16" />
                <path d="M6 10V7l6-3 6 3v3" />
                <path d="M6 14h12" />
                <path d="M6 18h12" />
              </svg>
            </span>

            <select
              id="banco"
              name="banco"
              className={`form-select border-start-0 ${erros.banco ? 'is-invalid' : ''}`}
              style={{ boxShadow: 'none' }}
              value={campos.banco}
              onChange={alterarCampo}
            >
              {bancos.map((banco) => (
                <option key={banco} value={banco}>
                  {banco}
                </option>
              ))}
            </select>

            {erros.banco && <div className="invalid-feedback">{erros.banco}</div>}
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="descricao" className="form-label fw-semibold text-secondary small mb-1">
            Descrição
          </label>

          <textarea
            id="descricao"
            name="descricao"
            className="form-control"
            style={{ boxShadow: 'none', minHeight: 88 }}
            value={campos.descricao}
            onChange={alterarCampo}
            placeholder="Ex: Conta para transações manuais"
          />
        </div>

        <BotaoCarregando
          type="submit"
          carregando={salvando}
          textoCarregando="Cadastrando..."
          className="w-100 py-2 fw-semibold mt-2"
          style={{
            background: 'var(--sb-gradient)',
            border: 'none',
            borderRadius: '8px',
            fontSize: '1rem',
          }}
        >
          Cadastrar conta
        </BotaoCarregando>

        <p className="text-center text-muted small mt-4 mb-0">
          Quer fazer isso depois?{' '}
          <Link
            to="/transacoes/nova"
            className="fw-semibold text-decoration-none"
            style={{ color: 'var(--sb-primary)' }}
          >
            Ir para transação manual
          </Link>
        </p>
      </form>
    </LayoutAutenticacao>
  );
};

export default NovaConta;