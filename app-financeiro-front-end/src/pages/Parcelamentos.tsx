import { useCallback } from 'react';
import { Navigate } from 'react-router-dom';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useAsync } from '../hooks/useAsync';
import { useAutenticacao } from '../hooks/useAutenticacao';
import { buscarExtratoFuturo } from '../services/extratoFuturoService';
import type { ProjecaoMensal, StatusFatura } from '../types/extratoFuturo';
import { formatarData, formatarMoeda } from '../utils/formatacao';

const MESES_PADRAO = 3;

const rotulosStatus: Record<StatusFatura, string> = {
  ABERTA: 'Aberta',
  FECHADA: 'Fechada',
  PAGA: 'Paga',
};

const rotuloMes = (mes: string) => {
  const [ano, numero] = mes.split('-').map(Number);
  return new Date(ano, numero - 1, 1).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
};

const semCompromissos = (projecoes: ProjecaoMensal[]) =>
  projecoes.length === 0 || projecoes.every((mes) => mes.transacoes.length === 0 && mes.faturas.length === 0);

const Parcelamentos = () => {
  const { estaAutenticado } = useAutenticacao();

  const carregar = useCallback(() => buscarExtratoFuturo(MESES_PADRAO), []);

  const { dados, carregando, erro } = useAsync(carregar, {
    valorInicial: [] as ProjecaoMensal[],
    mensagemErro: 'Não foi possível carregar o extrato futuro.',
    habilitado: estaAutenticado,
  });

  if (!estaAutenticado) {
    return <Navigate to="/login" replace />;
  }

  return (
    <LayoutPrivado
      titulo="Extrato futuro"
      subtitulo="Projeção dos próximos 3 meses, com saldo, faturas e transações futuras."
    >
      {carregando ? (
        <p>Carregando extrato futuro</p>
      ) : erro ? (
        <MensagemAlerta mensagem={erro} />
      ) : semCompromissos(dados) ? (
        <EstadoVazio
          titulo="Nenhum compromisso futuro"
          descricao="Quando houver parcelas, boletos ou faturas, a projeção aparece aqui."
        />
      ) : (
        dados.map((projecao) => {
          const rotulo = rotuloMes(projecao.mes);

          return (
            <section key={projecao.mes} className="transactions-panel" aria-label={rotulo}>
              <h2>{rotulo}</h2>
              <div className="summary-grid">
                <article className="summary-card">
                  <span>Saldo previsto</span>
                  <strong>{formatarMoeda(projecao.saldoPrevisto)}</strong>
                </article>
                <article className="summary-card summary-card-negative">
                  <span>Total de débitos</span>
                  <strong>{formatarMoeda(projecao.totalDebitos)}</strong>
                </article>
                <article className="summary-card summary-card-positive">
                  <span>Total de créditos</span>
                  <strong>{formatarMoeda(projecao.totalCreditos)}</strong>
                </article>
              </div>

              <h3>Faturas</h3>
              {projecao.faturas.length === 0 ? (
                <p>Nenhuma fatura neste mês</p>
              ) : (
                <ul>
                  {projecao.faturas.map((fatura) => (
                    <li key={fatura.faturaId}>
                      <span>{fatura.nomeConta}</span>
                      <span>{formatarData(fatura.dataVencimento)}</span>
                      <span>{formatarMoeda(fatura.valorTotal)}</span>
                      <span>{rotulosStatus[fatura.status]}</span>
                    </li>
                  ))}
                </ul>
              )}

              <h3>Transações futuras</h3>
              {projecao.transacoes.length === 0 ? (
                <p>Nenhuma transação neste mês</p>
              ) : (
                <ul>
                  {projecao.transacoes.map((transacao) => (
                    <li key={transacao.transacaoId}>
                      <span>{transacao.descricao || 'Transação'}</span>
                      <span>{formatarData(transacao.data)}</span>
                      <span>{formatarMoeda(transacao.valor)}</span>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          );
        })
      )}
    </LayoutPrivado>
  );
};

export default Parcelamentos;
