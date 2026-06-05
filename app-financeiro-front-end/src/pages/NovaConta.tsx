import { useEffect, useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { listarContas, obterMensagemErroApi, registrarConta } from '../services/api';
import type { ContaRequest, ContaResponse, TipoConta } from '../services/api';

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

const rotulosTipoConta: Record<TipoConta, string> = {
  CORRENTE: 'Conta corrente',
  POUPANCA: 'Conta poupança',
  CARTAO_CREDITO: 'Cartão de crédito',
  CARTEIRA: 'Carteira',
};

const NovaConta = () => {
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [campos, setCampos] = useState<CamposConta>(valoresIniciais);
  const [erros, setErros] = useState<Partial<Record<keyof CamposConta, string>>>({});
  const [erroGeral, setErroGeral] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [modalAberto, setModalAberto] = useState(false);

  useEffect(() => {
    let ativo = true;

    const carregarContas = async () => {
      setCarregando(true);
      setErroGeral('');

      try {
        const contasCarregadas = await listarContas();

        if (!ativo) return;

        setContas(contasCarregadas);
      } catch (erro) {
        if (!ativo) return;

        setErroGeral(obterMensagemErroApi(erro, 'Não foi possível carregar as contas bancárias.'));
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    carregarContas();

    return () => {
      ativo = false;
    };
  }, []);

  const abrirModal = () => {
    setCampos(valoresIniciais);
    setErros({});
    setErroGeral('');
    setSucesso('');
    setModalAberto(true);
  };

  const fecharModal = () => {
    if (salvando) return;

    setModalAberto(false);
    setCampos(valoresIniciais);
    setErros({});
  };

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
      novosErros.tipoConta = 'Tipo de conta é obrigatório.';
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

      const contaCriada = await registrarConta(novaConta);

      setContas((atuais) => [...atuais, contaCriada]);
      setSucesso('Conta bancária cadastrada com sucesso.');
      setCampos(valoresIniciais);
      setErros({});
      setModalAberto(false);
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
    >
      <MensagemAlerta mensagem={erroGeral} tipo="danger" />
      <MensagemAlerta mensagem={sucesso} tipo="success" />

      <section className="form-panel">
        <div className="form-panel-header accounts-panel-header">
          <div className="accounts-header-title">
            <span className="accounts-header-icon" aria-hidden="true">
              🏦
            </span>

            <div>
              <h2>Contas cadastradas</h2>
              <p>Visualize as contas bancárias associadas ao seu usuário.</p>
            </div>
          </div>

          {contas.length > 0 && (
            <button type="button" className="sb-button sb-button-primary sb-button-sm" onClick={abrirModal}>
              Adicionar nova conta
            </button>
          )}
        </div>

        {carregando ? (
          <div className="loading-inline" aria-live="polite">
            Carregando contas...
          </div>
        ) : contas.length === 0 ? (
          <div className="empty-state">
            <h3>Nenhuma conta cadastrada</h3>
            <p>Você ainda não possui contas bancárias cadastradas.</p>
            <button type="button" className="sb-button sb-button-primary" onClick={abrirModal}>
              Adicionar nova conta
            </button>
          </div>
        ) : (
          <div className="accounts-list">
            {contas.map((conta) => (
              <article key={conta.contaId} className="account-card">
                <div className="account-icon" aria-hidden="true">
                  {conta.banco?.slice(0, 2).toUpperCase() || conta.nome.slice(0, 2).toUpperCase()}
                </div>

                <div className="account-card-content">
                  <h3>{conta.nome}</h3>
                  <p>
                    {conta.banco || 'Banco não informado'} • {rotulosTipoConta[conta.tipoConta]}
                  </p>
                  {conta.descricao && <small>{conta.descricao}</small>}
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      {modalAberto && (
        <div className="modal-backdrop-custom" role="presentation">
          <section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="titulo-modal-conta">
            <div className="modal-card-header">
              <div>
                <h2 id="titulo-modal-conta">Adicionar nova conta</h2>
                <p>Preencha os dados da conta bancária.</p>
              </div>

              <button type="button" className="modal-close-button" onClick={fecharModal} aria-label="Fechar modal">
                ×
              </button>
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
                <button type="button" className="sb-button sb-button-secondary" onClick={fecharModal}>
                  Cancelar
                </button>

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
        </div>
      )}
    </LayoutPrivado>
  );
};

export default NovaConta;