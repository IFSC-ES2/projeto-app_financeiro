import { useEffect, useMemo, useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import {
  listarCategorias,
  listarContas,
  obterMensagemErroApi,
  registrarTransacaoManual,
} from '../services/api';
import type { CategoriaResponse, ContaResponse, TipoPagamento, TipoTransacao } from '../services/api';

interface CamposTransacao {
  valor: string;
  data: string;
  descricao: string;
  tipoTransacao: TipoTransacao;
  formaPagamento: TipoPagamento;
  categoriaId: string;
  contaId: string;
}

const valoresIniciais: CamposTransacao = {
  valor: '',
  data: new Date().toISOString().slice(0, 10),
  descricao: '',
  tipoTransacao: 'DEBITO',
  formaPagamento: 'PIX',
  categoriaId: '',
  contaId: '',
};

const formasPagamento: Array<{ valor: TipoPagamento; rotulo: string }> = [
  { valor: 'PIX', rotulo: 'Pix' },
  { valor: 'CARTAO_DEBITO', rotulo: 'Cartão de débito' },
  { valor: 'CARTAO_CREDITO', rotulo: 'Cartão de crédito' },
  { valor: 'DINHEIRO', rotulo: 'Dinheiro' },
  { valor: 'BOLETO', rotulo: 'Boleto' },
  { valor: 'TED_DOC', rotulo: 'TED/DOC' },
];

const ehCarteiraAutomaticaDinheiro = (conta: ContaResponse) => {
  const nome = conta.nome.trim().toLowerCase();
  const banco = (conta.banco || '').trim().toLowerCase();
  const descricao = (conta.descricao || '').trim().toLowerCase();

  return (
    conta.tipoConta === 'CARTEIRA' &&
    banco === 'dinheiro' &&
    nome.startsWith('dinheiro / carteira') &&
    descricao.includes('transações em dinheiro')
  );
};

const NovaTransacao = () => {
  const navigate = useNavigate();

  const [campos, setCampos] = useState<CamposTransacao>(valoresIniciais);
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [erros, setErros] = useState<Partial<Record<keyof CamposTransacao, string>>>({});
  const [erroGeral, setErroGeral] = useState('');
  const [carregandoDados, setCarregandoDados] = useState(true);
  const [salvando, setSalvando] = useState(false);

  const contasSelecionaveis = useMemo(
  () => contas.filter((conta) => !ehCarteiraAutomaticaDinheiro(conta)),
  [contas]
);

  useEffect(() => {
    let ativo = true;

    const carregarDados = async () => {
      setCarregandoDados(true);
      setErroGeral('');

      try {
        const [contasCarregadas, categoriasCarregadas] = await Promise.all([listarContas(), listarCategorias()]);

        if (!ativo) return;
        setContas(contasCarregadas);
        setCategorias(categoriasCarregadas);
        const contasDisponiveis = contasCarregadas.filter(
        (conta) => !ehCarteiraAutomaticaDinheiro(conta)
        );

        setCampos((atual) => ({
          ...atual,
          contaId: atual.contaId || contasDisponiveis[0]?.contaId || '',
        }));
      } catch (err) {
        if (!ativo) return;
        setErroGeral(obterMensagemErroApi(err, 'Não foi possível carregar contas e categorias.'));
      } finally {
        if (ativo) setCarregandoDados(false);
      }
    };

    carregarDados();

    return () => {
      ativo = false;
    };
  }, []);

  const permiteSalvar = useMemo(
  () => campos.formaPagamento === 'DINHEIRO' || contasSelecionaveis.length > 0,
  [campos.formaPagamento, contasSelecionaveis.length]
  );

  const alterarCampo = (evento: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = evento.target;

    setCampos((atual) => {
      if (name === 'formaPagamento') {
        const formaPagamento = value as TipoPagamento;

        return {
          ...atual,
          formaPagamento,
          contaId:
            formaPagamento === 'DINHEIRO'
              ? ''
              : atual.contaId || contasSelecionaveis[0]?.contaId || '',
        };
      }

      return { ...atual, [name]: value };
    });

    setErros((atuais) => ({ ...atuais, [name]: undefined }));
  };

  const validar = () => {
    const novosErros: Partial<Record<keyof CamposTransacao, string>> = {};
    const valorNumerico = Number(campos.valor);

    if (!campos.valor) {
      novosErros.valor = 'Valor é obrigatório.';
    } else if (Number.isNaN(valorNumerico) || valorNumerico <= 0) {
      novosErros.valor = 'Informe um valor maior que zero.';
    }

    if (!campos.data) {
      novosErros.data = 'Data é obrigatória.';
    }

    if (campos.formaPagamento !== 'DINHEIRO' && !campos.contaId) {
      novosErros.contaId = 'Conta é obrigatória para esta forma de pagamento.';
    }

    const contaSelecionada = contas.find((conta) => conta.contaId === campos.contaId);

    if (
      campos.formaPagamento !== 'DINHEIRO' &&
      contaSelecionada &&
      ehCarteiraAutomaticaDinheiro(contaSelecionada)
    ) {
      novosErros.contaId = 'A carteira automática só pode ser usada para transações em dinheiro.';
    }

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault();
    setErroGeral('');

    if (!validar()) return;

    setSalvando(true);

    try {
      const transacaoCriada = await registrarTransacaoManual({
        valor: Number(campos.valor),
        data: campos.data,
        descricao: campos.descricao.trim() || undefined,
        tipoTransacao: campos.tipoTransacao,
        formaPagamento: campos.formaPagamento,
        categoriaId: campos.categoriaId || null,
        contaId: campos.formaPagamento === 'DINHEIRO' ? null : campos.contaId,
      });

      navigate('/transacoes', {
        replace: true,
        state: {
          mensagem: 'Transação registrada com sucesso.',
          transacaoCriada,
        },
      });
    } catch (err) {
      setErroGeral(obterMensagemErroApi(err, 'Não foi possível registrar a transação.'));
    } finally {
      setSalvando(false);
    }
  };

  return (
    <LayoutPrivado
      titulo="Nova transação"
      subtitulo="Registre manualmente uma movimentação financeira."
      acaoPrimaria={
        <Link to="/transacoes" className="sb-button sb-button-secondary sb-button-sm">
          Voltar
        </Link>
      }
    >
      <MensagemAlerta mensagem={erroGeral} tipo="danger" />

      <section className="form-panel">
        <div className="form-panel-header">
          <h2>Dados da transação</h2>
          <p>Campos marcados com asterisco são obrigatórios pelo contrato atual do backend.</p>
        </div>

        {carregandoDados ? (
          <div className="loading-inline" aria-live="polite">
            Carregando dados de apoio...
          </div>
        ) : (
          <form onSubmit={enviar} noValidate className="sb-form">
            <div className="form-grid">
              <label>
                <span>Valor *</span>
                <input
                  name="valor"
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={campos.valor}
                  onChange={alterarCampo}
                  className={erros.valor ? 'invalid' : ''}
                  placeholder="0,00"
                />
                {erros.valor && <small className="field-error">{erros.valor}</small>}
              </label>

              <label>
                <span>Data *</span>
                <input
                  name="data"
                  type="date"
                  value={campos.data}
                  onChange={alterarCampo}
                  className={erros.data ? 'invalid' : ''}
                />
                {erros.data && <small className="field-error">{erros.data}</small>}
              </label>

              <label className="span-2">
                <span>Descrição</span>
                <input
                  name="descricao"
                  type="text"
                  maxLength={120}
                  value={campos.descricao}
                  onChange={alterarCampo}
                  placeholder="Ex.: mercado, salário, transporte"
                />
              </label>

                <label>
                  <span>Tipo *</span>
                  <select
                    name="tipoTransacao"
                    value={campos.tipoTransacao}
                    onChange={alterarCampo}
                    className={erros.tipoTransacao ? 'invalid' : ''}
                  >
                    <option value="DEBITO">Saída / despesa</option>
                    <option value="CREDITO">Entrada / receita</option>
                    <option value="PARCELAMENTO">Parcelamento</option>
                    <option value="BOLETO">Boleto</option>
                  </select>
                  {erros.tipoTransacao && <small className="field-error">{erros.tipoTransacao}</small>}
                </label>

              <label>
                <span>Forma de pagamento</span>
                <select name="formaPagamento" value={campos.formaPagamento} onChange={alterarCampo}>
                  {formasPagamento.map((forma) => (
                    <option key={forma.valor} value={forma.valor}>
                      {forma.rotulo}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>Categoria</span>
                <select name="categoriaId" value={campos.categoriaId} onChange={alterarCampo}>
                  <option value="">Sem categoria</option>
                  {categorias.map((categoria) => (
                    <option key={categoria.categoriaId} value={categoria.categoriaId}>
                      {categoria.nome}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>Conta {campos.formaPagamento !== 'DINHEIRO' ? '*' : ''}</span>
                <select
                  name="contaId"
                  value={campos.contaId}
                  onChange={alterarCampo}
                  disabled={campos.formaPagamento === 'DINHEIRO'}
                  className={erros.contaId ? 'invalid' : ''}
                >
                  <option value="">
                    {campos.formaPagamento === 'DINHEIRO' ? 'Conta automática em dinheiro' : 'Selecione uma conta'}
                  </option>
                    {contasSelecionaveis.map((conta) => (
                      <option key={conta.contaId} value={conta.contaId}>
                        {conta.nome}
                        {conta.banco ? ` - ${conta.banco}` : ''}
                      </option>
                    ))}
                </select>
                {erros.contaId && <small className="field-error">{erros.contaId}</small>}
              </label>
            </div>

              {!permiteSalvar && (
                <p className="helper-text">
                  Cadastre uma conta bancária para pagamentos digitais ou selecione Dinheiro para usar a carteira automática.
                </p>
              )}

            <div className="form-actions">
              <Link to="/transacoes" className="sb-button sb-button-secondary">
                Cancelar
              </Link>
              <BotaoCarregando
                type="submit"
                carregando={salvando}
                textoCarregando="Salvando..."
                className="sb-button sb-button-primary"
                disabled={!permiteSalvar}
              >
                Salvar transação
              </BotaoCarregando>
            </div>
          </form>
        )}
      </section>
    </LayoutPrivado>
  );
};

export default NovaTransacao;
