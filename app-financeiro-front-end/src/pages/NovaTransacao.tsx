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

const tiposTransacao: Array<{ valor: TipoTransacao; rotulo: string }> = [
  { valor: 'DEBITO', rotulo: 'Saída / despesa' },
  { valor: 'CREDITO', rotulo: 'Entrada / receita' },
  { valor: 'PARCELAMENTO', rotulo: 'Parcelamento' },
  { valor: 'BOLETO', rotulo: 'Boleto' },
];

const normalizarValor = (valor: string) => valor.trim().replace(',', '.');

const validarValorMonetario = (valor: string) => {
  const texto = valor.trim();

  if (!texto) {
    return { valido: false, mensagem: 'Valor é obrigatório.' };
  }

  if (texto.startsWith('-')) {
    return { valido: false, mensagem: 'Informe um valor maior que zero.' };
  }

  const normalizado = normalizarValor(texto);

  if (!/^\d+(\.\d{1,2})?$/.test(normalizado)) {
    return { valido: false, mensagem: 'Valor inválido. Use apenas números.' };
  }

  const numero = Number(normalizado);

  if (numero <= 0) {
    return { valido: false, mensagem: 'Informe um valor maior que zero.' };
  }

  return { valido: true, valor: numero };
};

const formasPagamento: Array<{ valor: TipoPagamento; rotulo: string }> = [
  { valor: 'PIX', rotulo: 'Pix' },
  { valor: 'CARTAO_DEBITO', rotulo: 'Cartão de débito' },
  { valor: 'CARTAO_CREDITO', rotulo: 'Cartão de crédito' },
  { valor: 'DINHEIRO', rotulo: 'Dinheiro' },
  { valor: 'BOLETO', rotulo: 'Boleto' },
  { valor: 'TED_DOC', rotulo: 'TED/DOC' },
];

const NovaTransacao = () => {
  const navigate = useNavigate();

  const [campos, setCampos] = useState<CamposTransacao>(valoresIniciais);
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [erros, setErros] = useState<Partial<Record<keyof CamposTransacao, string>>>({});
  const [erroGeral, setErroGeral] = useState('');
  const [carregandoDados, setCarregandoDados] = useState(true);
  const [salvando, setSalvando] = useState(false);

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
        setCampos((atual) => ({
          ...atual,
          contaId: atual.contaId || contasCarregadas[0]?.contaId || '',
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
    () => campos.formaPagamento === 'DINHEIRO' || contas.length > 0,
    [campos.formaPagamento, contas.length]
  );

  const alterarCampo = (evento: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = evento.target;

    setCampos((atual) => {
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
    const novosErros: Partial<Record<keyof CamposTransacao, string>> = {};
    const valor = validarValorMonetario(campos.valor);

    if (!valor.valido) {
      novosErros.valor = valor.mensagem;
    }

    if (!campos.data) {
      novosErros.data = 'Data é obrigatória.';
    }

    if (!campos.descricao.trim()) {
      novosErros.descricao = 'Descrição é obrigatória.';
    }

    if (!campos.categoriaId) {
      novosErros.categoriaId = 'Categoria é obrigatória.';
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

    if (!validar()) return;

    setSalvando(true);

    try {
      const valorValidado = validarValorMonetario(campos.valor);
      if (!valorValidado.valido || valorValidado.valor === undefined) return;

      const transacaoCriada = await registrarTransacaoManual({
        valor: valorValidado.valor,
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
                  type="text"
                  inputMode="decimal"
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
                <span>Descrição *</span>
                <input
                  name="descricao"
                  type="text"
                  maxLength={120}
                  value={campos.descricao}
                  onChange={alterarCampo}
                  className={erros.descricao ? 'invalid' : ''}
                  placeholder="Ex.: mercado, salário, transporte"
                />
                {erros.descricao && <small className="field-error">{erros.descricao}</small>}
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
                <span>Categoria *</span>
                <select
                  name="categoriaId"
                  value={campos.categoriaId}
                  onChange={alterarCampo}
                  className={erros.categoriaId ? 'invalid' : ''}
                >
                  <option value="">Selecione uma categoria</option>
                  {categorias.map((categoria) => (
                    <option key={categoria.categoriaId} value={categoria.categoriaId}>
                      {categoria.nome}
                    </option>
                  ))}
                </select>
                {erros.categoriaId && <small className="field-error">{erros.categoriaId}</small>}
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
