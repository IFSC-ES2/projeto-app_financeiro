import React, { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
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

      setSucesso('Conta bancária cadastrada com sucesso.');
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
    <main className="min-vh-100 py-4" style={{ background: 'var(--sb-bg)' }}>
      <div className="container" style={{ maxWidth: 900 }}>
        <div className="d-flex flex-column flex-md-row justify-content-between gap-3 align-items-md-center mb-4">
          <div>
            <span className="badge rounded-pill mb-2" style={{ background: 'var(--sb-primary)' }}>
              SmartBudget
            </span>
            <h1 className="fw-bold mb-1" style={{ color: 'var(--sb-text)' }}>
              Associar conta bancária
            </h1>
            <p className="text-muted mb-0">
              Cadastre uma conta para utilizar em funcionalidades como transações manuais.
            </p>
          </div>

          <Link to="/transacoes/nova" className="btn btn-outline-secondary align-self-start align-self-md-center">
          Ir para transação manual
        </Link>
        </div>

        <MensagemAlerta mensagem={erroGeral} tipo="danger" />
        <MensagemAlerta mensagem={sucesso} tipo="success" />

        <div className="card border-0 shadow-sm" style={{ borderRadius: 18 }}>
          <div className="card-body p-4">
            <h2 className="h5 fw-bold mb-3">Dados da conta</h2>

            <form onSubmit={enviar} noValidate>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label fw-semibold small" htmlFor="nome">
                    Nome da conta *
                  </label>
                  <input
                    id="nome"
                    name="nome"
                    type="text"
                    className={`form-control ${erros.nome ? 'is-invalid' : ''}`}
                    value={campos.nome}
                    onChange={alterarCampo}
                    placeholder="Ex.: Conta Principal"
                  />
                  {erros.nome && <div className="invalid-feedback">{erros.nome}</div>}
                </div>

                <div className="col-md-6">
                  <label className="form-label fw-semibold small" htmlFor="tipoConta">
                    Tipo de conta *
                  </label>
                  <select
                    id="tipoConta"
                    name="tipoConta"
                    className={`form-select ${erros.tipoConta ? 'is-invalid' : ''}`}
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

                <div className="col-md-6">
                  <label className="form-label fw-semibold small" htmlFor="banco">
                    Banco *
                  </label>
                  <select
                    id="banco"
                    name="banco"
                    className={`form-select ${erros.banco ? 'is-invalid' : ''}`}
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

                <div className="col-md-6">
                  <label className="form-label fw-semibold small" htmlFor="descricao">
                    Descrição
                  </label>
                  <textarea
                    id="descricao"
                    name="descricao"
                    className="form-control"
                    value={campos.descricao}
                    onChange={alterarCampo}
                    placeholder="Ex.: Conta para teste de transação manual"
                    rows={3}
                  />
                </div>
              </div>

              <BotaoCarregando
                type="submit"
                carregando={salvando}
                textoCarregando="Cadastrando..."
                className="w-100 mt-4 py-2 fw-semibold"
                style={{ background: 'var(--sb-gradient)', border: 'none', borderRadius: 10 }}
              >
                Cadastrar conta
              </BotaoCarregando>
            </form>
          </div>
        </div>
      </div>
    </main>
  );
};

export default NovaConta;