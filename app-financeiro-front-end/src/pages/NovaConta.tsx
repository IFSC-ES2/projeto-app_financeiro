import { useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
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
  banco: '',
  tipoConta: 'CORRENTE',
  descricao: '',
};

const tiposConta: Array<{ valor: TipoConta; rotulo: string }> = [
  { valor: 'CORRENTE', rotulo: 'Conta corrente' },
  { valor: 'POUPANCA', rotulo: 'Conta poupança' },
  { valor: 'CARTAO_CREDITO', rotulo: 'Cartão de crédito' },
  { valor: 'CARTEIRA', rotulo: 'Carteira' },
];

const NovaConta = () => {
  const navigate = useNavigate();

  const [campos, setCampos] = useState<CamposConta>(valoresIniciais);
  const [erros, setErros] = useState<Partial<Record<keyof CamposConta, string>>>({});
  const [erroGeral, setErroGeral] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [salvando, setSalvando] = useState(false);

  const alterarCampo = (evento: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
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
      novosErros.tipoConta = 'Tipo da conta é obrigatório.';
    }

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const enviar = async (evento: FormEvent) => {
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
      setCampos(valoresIniciais);
      setErros({});

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
    <LayoutPrivado
      titulo="Contas"
      subtitulo="Gerencie suas contas bancárias cadastradas no SmartBudget."
      acaoPrimaria={
        <Link to="/dashboard" className="sb-button sb-button-secondary sb-button-sm">
          Voltar
        </Link>
      }
    >
      <MensagemAlerta mensagem={erroGeral} tipo="danger" />
      <MensagemAlerta mensagem={sucesso} tipo="success" />

      <section className="form-panel">
        <div className="form-panel-header">
          <h2>Dados da conta</h2>
          <p>Nome, banco e tipo da conta são obrigatórios.</p>
        </div>

        <form onSubmit={enviar} noValidate className="sb-form">
          <div className="form-grid">
            <label>
              <span>Nome da conta *</span>
              <input
                name="nome"
                type="text"
                value={campos.nome}
                onChange={alterarCampo}
                className={erros.nome ? 'invalid' : ''}
                placeholder="Ex.: Conta principal"
              />
              {erros.nome && <small className="field-error">{erros.nome}</small>}
            </label>

            <label>
              <span>Banco *</span>
              <input
                name="banco"
                type="text"
                value={campos.banco}
                onChange={alterarCampo}
                className={erros.banco ? 'invalid' : ''}
                placeholder="Ex.: Nubank"
              />
              {erros.banco && <small className="field-error">{erros.banco}</small>}
            </label>

            <label>
              <span>Tipo de conta *</span>
              <select
                name="tipoConta"
                value={campos.tipoConta}
                onChange={alterarCampo}
                className={erros.tipoConta ? 'invalid' : ''}
              >
                {tiposConta.map((tipo) => (
                  <option key={tipo.valor} value={tipo.valor}>
                    {tipo.rotulo}
                  </option>
                ))}
              </select>
              {erros.tipoConta && <small className="field-error">{erros.tipoConta}</small>}
            </label>

            <label className="span-2">
              <span>Descrição</span>
              <textarea
                name="descricao"
                value={campos.descricao}
                onChange={alterarCampo}
                placeholder="Ex.: Conta usada para despesas do dia a dia"
              />
            </label>
          </div>

          <div className="form-actions">
            <Link to="/dashboard" className="sb-button sb-button-secondary">
              Cancelar
            </Link>

            <BotaoCarregando
              type="submit"
              carregando={salvando}
              textoCarregando="Cadastrando..."
              className="sb-button sb-button-primary"
            >
              Cadastrar conta
            </BotaoCarregando>
          </div>
        </form>
      </section>
    </LayoutPrivado>
  );
};

export default NovaConta;