import { useCallback, useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import {
  obterExtratoFuturo,
  obterMensagemErroApi,
  pagarFatura,
  type ProjecaoMensalResponse,
  type StatusFatura,
  type TipoPagamento,
} from '../services/api';
import { formatarData, formatarMoeda } from '../utils/formatacao';

const NOMES_MESES = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

const ROTULOS_PAGAMENTO: Record<TipoPagamento, string> = {
  PIX: 'Pix',
  CARTAO_DEBITO: 'Cartão de débito',
  CARTAO_CREDITO: 'Parcela no cartão',
  DINHEIRO: 'Dinheiro',
  BOLETO: 'Boleto',
  TED_DOC: 'TED/DOC',
};

const ROTULOS_STATUS: Record<StatusFatura, string> = {
  ABERTA: 'Aberta',
  FECHADA: 'Fechada',
  PAGA: 'Paga',
};

const rotuloMes = (projecao: ProjecaoMensalResponse) =>
  `${NOMES_MESES[projecao.mes - 1]} de ${projecao.ano}`;

const ExtratoFuturo = () => {
  const [projecao, setProjecao] = useState<ProjecaoMensalResponse[]>([]);
  const [horizonte, setHorizonte] = useState(3);
  const [carregando, setCarregando] = useState(true);
  const [erroCarregamento, setErroCarregamento] = useState('');
  const [erro, setErro] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [faturaPagandoId, setFaturaPagandoId] = useState<string | null>(null);
  const [recarregar, setRecarregar] = useState(0);

  useEffect(() => {
    let ativo = true;

    const carregar = async () => {
      setCarregando(true);
      setErroCarregamento('');

      try {
        const dados = await obterExtratoFuturo(horizonte);
        if (ativo) setProjecao(dados);
      } catch (erroApi) {
        if (ativo) {
          setErroCarregamento(obterMensagemErroApi(erroApi, 'Não foi possível carregar o extrato futuro.'));
        }
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    void carregar();

    return () => {
      ativo = false;
    };
  }, [horizonte, recarregar]);

  useEffect(() => {
    if (!mensagem) return;
    const temporizador = setTimeout(() => setMensagem(''), 3000);
    return () => clearTimeout(temporizador);
  }, [mensagem]);

  const marcarFaturaComoPaga = useCallback(async (faturaId: string) => {
    setFaturaPagandoId(faturaId);
    setErro('');

    try {
      await pagarFatura(faturaId);
      setMensagem('Fatura marcada como paga.');
      setRecarregar((atual) => atual + 1);
    } catch (erroApi) {
      setErro(obterMensagemErroApi(erroApi, 'Não foi possível pagar a fatura.'));
    } finally {
      setFaturaPagandoId(null);
    }
  }, []);

  const semMovimentacoesFuturas =
    !carregando &&
    !erroCarregamento &&
    projecao.every((mes) => mes.transacoes.length === 0 && mes.faturas.length === 0);

  const ultimoMes = projecao.at(-1);
  const totalAReceber = projecao.reduce((soma, mes) => soma + mes.totalCreditos, 0);
  const totalAPagar = projecao.reduce((soma, mes) => soma + mes.totalDebitos, 0);

  return (
    <LayoutPrivado
      titulo="Extrato futuro"
      subtitulo="Projeção de parcelas, boletos e faturas dos próximos meses."
    >
      <MensagemAlerta mensagem={mensagem} tipo="success" />
      <MensagemAlerta mensagem={erroCarregamento || erro} tipo="danger" />

      {carregando && (
        <p className="loading-inline" aria-live="polite">
          Carregando extrato futuro…
        </p>
      )}

      {!carregando && !erroCarregamento && (
        <>
          <section className="future-hero" aria-label="Resumo da projeção">
            <div className="future-hero-glow" aria-hidden="true" />

            <header className="future-hero-header">
              <div>
                <p className="future-hero-eyebrow">Horizonte de projeção</p>
                <h2>O seu dinheiro, alguns meses à frente</h2>
              </div>

              <label className="future-horizon">
                <span>Meses</span>
                <select
                  value={horizonte}
                  onChange={(evento) => setHorizonte(Number(evento.target.value))}
                  aria-label="Quantidade de meses projetados"
                >
                  <option value={3}>3 meses</option>
                  <option value={6}>6 meses</option>
                  <option value={12}>12 meses</option>
                </select>
              </label>
            </header>

            <dl className="future-hero-stats">
              <div className="future-hero-stat">
                <dt>Saldo previsto {ultimoMes ? `em ${rotuloMes(ultimoMes)}` : ''}</dt>
                <dd className={ultimoMes && ultimoMes.saldoPrevisto < 0 ? 'negativo' : ''}>
                  {formatarMoeda(ultimoMes?.saldoPrevisto ?? 0)}
                </dd>
              </div>
              <div className="future-hero-stat">
                <dt>A receber no período</dt>
                <dd className="positivo">{formatarMoeda(totalAReceber)}</dd>
              </div>
              <div className="future-hero-stat">
                <dt>A pagar no período</dt>
                <dd className="negativo">{formatarMoeda(totalAPagar)}</dd>
              </div>
            </dl>
          </section>

          {semMovimentacoesFuturas ? (
            <EstadoVazio
              titulo="Nada projetado por aqui — ainda"
              descricao="Parcelamentos, boletos a vencer e faturas de cartão em aberto aparecem nesta linha do tempo assim que forem registrados."
              acao={
                <NavLink className="sb-button sb-button-primary" to="/transacoes/nova">
                  Registrar transação futura
                </NavLink>
              }
            />
          ) : (
            <ol className="future-timeline" aria-label="Projeção mês a mês">
              {projecao.map((mes, indice) => (
                <li
                  key={`${mes.ano}-${mes.mes}`}
                  className="future-month"
                  style={{ animationDelay: `${indice * 90}ms` }}
                >
                  <div className="future-month-marker" aria-hidden="true">
                    <span className={indice === 0 ? 'future-dot future-dot-atual' : 'future-dot'} />
                  </div>

                  <article className="future-month-card">
                    <header className="future-month-header">
                      <div>
                        <h3>{rotuloMes(mes)}</h3>
                        {indice === 0 && <span className="future-month-tag">Mês atual</span>}
                      </div>
                      <p
                        className={
                          mes.saldoPrevisto < 0
                            ? 'future-month-saldo negativo'
                            : 'future-month-saldo'
                        }
                      >
                        <span>Saldo previsto</span>
                        <strong>{formatarMoeda(mes.saldoPrevisto)}</strong>
                      </p>
                    </header>

                    <div className="future-month-totais">
                      <p>
                        <span>Créditos</span>
                        <strong className="amount-positive">{formatarMoeda(mes.totalCreditos)}</strong>
                      </p>
                      <p>
                        <span>Débitos</span>
                        <strong className="amount-negative">{formatarMoeda(mes.totalDebitos)}</strong>
                      </p>
                    </div>

                    {mes.faturas.length > 0 && (
                      <ul className="future-faturas" aria-label={`Faturas com vencimento em ${rotuloMes(mes)}`}>
                        {mes.faturas.map((fatura) => (
                          <li key={fatura.faturaId} className="future-fatura">
                            <div className="future-fatura-icone" aria-hidden="true">
                              <svg viewBox="0 0 24 24">
                                <rect x="2" y="5" width="20" height="14" rx="3" />
                                <path d="M2 10h20" />
                              </svg>
                            </div>
                            <div className="future-fatura-info">
                              <strong>Fatura {fatura.contaNome ?? 'do cartão'}</strong>
                              <small>
                                Vence em{' '}
                                {fatura.dataVencimento ? formatarData(fatura.dataVencimento) : 'data não informada'}
                              </small>
                            </div>
                            <span className={`future-status future-status-${fatura.status.toLowerCase()}`}>
                              {ROTULOS_STATUS[fatura.status]}
                            </span>
                            <strong className="future-fatura-valor">{formatarMoeda(fatura.valorTotal)}</strong>
                            {fatura.status !== 'PAGA' && (
                              <button
                                type="button"
                                className="sb-button sb-button-secondary sb-button-xs"
                                disabled={faturaPagandoId === fatura.faturaId}
                                onClick={() => void marcarFaturaComoPaga(fatura.faturaId)}
                              >
                                {faturaPagandoId === fatura.faturaId ? 'Pagando…' : 'Marcar paga'}
                              </button>
                            )}
                          </li>
                        ))}
                      </ul>
                    )}

                    {mes.transacoes.length > 0 ? (
                      <ul className="future-transacoes" aria-label={`Lançamentos futuros de ${rotuloMes(mes)}`}>
                        {mes.transacoes.map((transacao) => (
                          <li key={transacao.transacaoId} className="future-transacao">
                            <span
                              className={
                                transacao.tipoTransacao === 'CREDITO'
                                  ? 'transaction-dot positive'
                                  : 'transaction-dot negative'
                              }
                              aria-hidden="true"
                            />
                            <div className="future-transacao-info">
                              <strong>{transacao.descricao || 'Sem descrição'}</strong>
                              <small>
                                {formatarData(transacao.data)}
                                {transacao.formaPagamento
                                  ? ` · ${ROTULOS_PAGAMENTO[transacao.formaPagamento]}`
                                  : ''}
                              </small>
                            </div>
                            <strong
                              className={
                                transacao.tipoTransacao === 'CREDITO' ? 'amount-positive' : 'amount-negative'
                              }
                            >
                              {transacao.tipoTransacao === 'CREDITO' ? '+' : '−'}
                              {formatarMoeda(transacao.valor)}
                            </strong>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      mes.faturas.length === 0 && (
                        <p className="future-month-vazio">Nenhum lançamento projetado para este mês.</p>
                      )
                    )}
                  </article>
                </li>
              ))}
            </ol>
          )}
        </>
      )}
    </LayoutPrivado>
  );
};

export default ExtratoFuturo;
