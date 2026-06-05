import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LayoutAutenticacao from '../components/layout/LayoutAutenticacao';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { obterMensagemErroApi, registrarConta } from '../services/api';
import type { ContaRequest, TipoConta } from '../services/api';

type CamposConta = {
  nome: string;
  banco: string;
  tipoConta: TipoConta;
  descricao: string;
};

const valoresIniciais: CamposConta = {
  nome: '',
  banco: 'Nubank',
  tipoConta: 'CORRENTE',
  descricao: '',
};

const tiposConta: Array<{ valor: TipoConta; rotulo: string }> = [
  { valor: 'CORRENTE', rotulo: 'Conta corrente' },
  { valor: 'POUPANCA', rotulo: 'Conta poupança' },
  { valor: 'CARTAO_CREDITO', rotulo: 'Cartão de crédito' },
  { valor: 'CARTEIRA', rotulo: 'Carteira' },
];

const PrimeiraConta: React.FC = () => {
  const navigate = useNavigate();

  const [campos, setCampos] = useState<CamposConta>(valoresIniciais);
  const [erros, setErros] = useState<Partial<Record<keyof CamposConta, string>>>({});
  const [erroGeral, setErroGeral] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [salvando, setSalvando] = useState(false);

  const alterarCampo = (
    evento: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = evento.target;

    setCampos((atual) => ({
      ...atual,
      [name]: value,
    }));

    setErros((atuais) => ({
      ...atuais,
      [name]: undefined,
    }));
  };

  const validar = () => {
    const novosErros: Partial<Record<keyof CamposConta, string>> = {};

    if (!campos.nome.trim()) {
      novosErros.nome = 'Nome da conta é obrigatório.';
    }

    if (!campos.banco.trim()) {
      novosErros.banco = 'Banco é obrigatório.';
    }

    if (!campos.tipoConta) {
      novosErros.tipoConta = 'Tipo de conta é obrigatório.';
    }

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const enviar = async (evento: React.FormEvent) => {
    evento.preventDefault();

    setErroGeral('');
    setSucesso('');

    if (!validar()) return;

    setSalvando(true);

    try {
      const novaConta: ContaRequest = {
        nome: campos.nome.trim(),
        banco: campos.banco.trim(),
        tipoConta: campos.tipoConta,
        descricao: campos.descricao.trim() || undefined,
      };

      await registrarConta(novaConta);

      setSucesso('Conta bancária cadastrada com sucesso. Redirecionando para o Dashboard...');

      setTimeout(() => {
        navigate('/dashboard', { replace: true });
      }, 900);
    } catch (erro) {
      setErroGeral(obterMensagemErroApi(erro, 'Não foi possível cadastrar a conta bancária.'));
    } finally {
      setSalvando(false);
    }
  };

  return (
    <LayoutAutenticacao
      tituloPainel="Cadastro quase finalizado!"
      subtituloPainel="Agora cadastre sua primeira conta bancária para começar a usar o SmartBudget."
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
          Cadastre sua primeira conta
        </h2>

        <p className="text-muted small mb-0">
          Essa conta será usada para organizar suas primeiras transações.
        </p>
      </div>

      <MensagemAlerta mensagem={erroGeral} tipo="danger" />
      <MensagemAlerta mensagem={sucesso} tipo="success" />

      <form onSubmit={enviar} noValidate>
        <div className="mb-3">
          <label htmlFor="nome" className="form-label fw-semibold text-secondary small mb-1">
            Nome da conta
          </label>
          <input
            id="nome"
            name="nome"
            type="text"
            className={`form-control ${erros.nome ? 'is-invalid' : ''}`}
            value={campos.nome}
            onChange={alterarCampo}
            placeholder="Ex.: Conta principal"
          />
          {erros.nome && <div className="invalid-feedback">{erros.nome}</div>}
        </div>

        <div className="mb-3">
          <label htmlFor="banco" className="form-label fw-semibold text-secondary small mb-1">
            Banco
          </label>
          <input
            id="banco"
            name="banco"
            type="text"
            className={`form-control ${erros.banco ? 'is-invalid' : ''}`}
            value={campos.banco}
            onChange={alterarCampo}
            placeholder="Ex.: Nubank"
          />
          {erros.banco && <div className="invalid-feedback">{erros.banco}</div>}
        </div>

        <div className="mb-3">
          <label htmlFor="tipoConta" className="form-label fw-semibold text-secondary small mb-1">
            Tipo de conta
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

        <div className="mb-3">
          <label htmlFor="descricao" className="form-label fw-semibold text-secondary small mb-1">
            Descrição
          </label>
          <textarea
            id="descricao"
            name="descricao"
            className="form-control"
            value={campos.descricao}
            onChange={alterarCampo}
            placeholder="Ex.: Conta usada para despesas do dia a dia"
          />
        </div>

        <BotaoCarregando
          type="submit"
          carregando={salvando}
          textoCarregando="Cadastrando..."
          className="w-100 py-2 fw-semibold mt-2"
        >
          Finalizar cadastro
        </BotaoCarregando>
      </form>
    </LayoutAutenticacao>
  );
};

export default PrimeiraConta;