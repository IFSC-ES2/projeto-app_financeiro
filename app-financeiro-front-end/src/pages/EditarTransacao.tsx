import { useEffect, useMemo, useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import {
  editarTransacao,
  listarCategorias,
  listarContas,
  obterMensagemErroApi,
} from '../services/api';
import type {
  CategoriaResponse,
  ContaResponse,
  TipoPagamento,
  TipoTransacao,
  TransacaoResponse,
} from '../services/api';

interface CamposTransacao {
  valor: string;
  data: string;
  descricao: string;
  tipoTransacao: TipoTransacao;
  formaPagamento: TipoPagamento;
  categoriaId: string;
  contaId: string;
}

interface EstadoEdicaoTransacao {
  transacao?: TransacaoResponse;
}

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

const montarCampos = (transacao: TransacaoResponse): CamposTransacao => ({
  valor: String(transacao.valor),
  data: transacao.data.slice(0, 10),
  descricao: transacao.descricao ?? '',
  tipoTransacao: transacao.tipoTransacao,
  formaPagamento: transacao.formaPagamento ?? 'PIX',
  categoriaId: transacao.categoriaId ?? '',
  contaId: transacao.contaId ?? '',
});

const EditarTransacao = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { transacaoId = '' } = useParams();
  const estado = location.state as EstadoEdicaoTransacao | null;
  const transacao = estado?.transacao;
  // Limitação temporária: o backend ainda não expõe GET /transacoes/{id}.
  // Por isso, a tela usa location.state quando a edição é aberta pela listagem.
  const mensagemTransacaoAusente =
    'Não foi possível carregar os dados diretamente por esta URL. No momento a API não possui busca de transação por ID, então acesse a edição pela lista de transações.';

  const [campos, setCampos] = useState<CamposTransacao | null>(() => (transacao ? montarCampos(transacao) : null));
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [erros, setErros] = useState<Partial<Record<keyof CamposTransacao, string>>>({});
  const [erroGeral, setErroGeral] = useState(transacao ? '' : mensagemTransacaoAusente);
  const [carregandoDados, setCarregandoDados] = useState(Boolean(transacao));
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (!transacao) return;

    let ativo = true;

    const carregarDados = async () => {
      setCarregandoDados(true);
      setErroGeral('');

      try {
        const [contasCarregadas, categoriasCarregadas] = await Promise.all([listarContas(), listarCategorias()]);

        if (!ativo) return;
        setContas(contasCarregadas);
        setCategorias(categoriasCarregadas);
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
  }, [transacao]);

  const permiteSalvar = useMemo(() => {
    if (!campos) return false;
    return campos.formaPagamento === 'DINHEIRO' || contas.length > 0;
  }, [campos, contas.length]);

  const alterarCampo = (evento: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = evento.target;

    setCampos((atual) => {
      if (!atual) return atual;

      if (name === 'formaPagamento') {
        const formaPagamento = value as TipoPagamento;
        return {
          ...atual,
          formaPagamento,
          contaId: formaPagamento === 'DINHEIRO' ? '' : atual.contaId || contas[0]?.contaId || '',
        };
      }

      return { ...atual, [name]: value };
    });

    setErros((atuais) => ({ ...atuais, [name]: undefined }));
  };

  const validar = () => {
    if (!campos) return false;

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

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault();
    setErroGeral('');

    if (!campos || !validar()) return;

    setSalvando(true);

    try {
      await editarTransacao(transacaoId, {
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
          mensagem: 'Transação atualizada com sucesso.',
        },
      });
    } catch (err) {
      setErroGeral(obterMensagemErroApi(err, 'Não foi possível atualizar a transação.'));
    } finally {
      setSalvando(false);
    }
  };

  return (
    <LayoutPrivado
      titulo="Editar transação"
      subtitulo="Altere os dados de uma movimentação financeira existente."
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
          <p>Confira as informações carregadas antes de salvar a alteração.</p>
        </div>

        {carregandoDados ? (
          <div className="loading-inline" aria-live="polite">
            Carregando dados da transação...
          </div>
        ) : !campos ? (
          <div className="form-empty-state">
            <p>Não foi possível carregar a transação para edição.</p>
            <Link to="/transacoes" className="sb-button sb-button-secondary">
              Voltar para transações
            </Link>
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
                <select name="tipoTransacao" value={campos.tipoTransacao} onChange={alterarCampo}>
                  {tiposTransacao.map((tipo) => (
                    <option key={tipo.valor} value={tipo.valor}>
                      {tipo.rotulo}
                    </option>
                  ))}
                </select>
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
                  {contas.map((conta) => (
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
                Cadastre uma conta bancária antes de salvar ou selecione Dinheiro como forma de pagamento.
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
                Salvar alterações
              </BotaoCarregando>
            </div>
          </form>
        )}
      </section>
    </LayoutPrivado>
  );
};

export default EditarTransacao;
