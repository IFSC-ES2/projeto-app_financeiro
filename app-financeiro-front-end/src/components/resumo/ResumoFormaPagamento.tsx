import { useEffect, useState } from 'react';
import EstadoVazio from '../ui/EstadoVazio';
import MensagemAlerta from '../ui/MensagemAlerta';
import { buscarResumoPorPagamento, obterMensagemErroApi } from '../../services/api';
import type { ResumoPagamentoResponse } from '../../services/api';
import { formatarMoeda } from '../../utils/formatacao';

type ResumoFormaPagamentoProps = {
  limiteItens?: number;
  compacto?: boolean;
};

const formatarPercentual = (percentual: number) =>
  `${Number(percentual).toLocaleString('pt-BR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  })}%`;

const limitarPercentual = (percentual: number) =>
  Math.min(Math.max(Number(percentual) || 0, 0), 100);

const ResumoFormaPagamento = ({
  limiteItens,
  compacto = false,
}: ResumoFormaPagamentoProps) => {
  const [resumo, setResumo] = useState<ResumoPagamentoResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    let ativo = true;

    const carregarResumo = async () => {
      setCarregando(true);
      setErro('');

      try {
        const resumoCarregado = await buscarResumoPorPagamento();

        if (!ativo) return;

        setResumo(resumoCarregado);
      } catch (erroCapturado) {
        if (!ativo) return;

        setErro(
          obterMensagemErroApi(
            erroCapturado,
            'Não foi possível carregar o resumo por forma de pagamento.',
          ),
        );
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    carregarResumo();

    return () => {
      ativo = false;
    };
  }, []);

  const resumoExibido = limiteItens ? resumo.slice(0, limiteItens) : resumo;

  return (
    <section
      className={compacto ? 'payment-summary-panel payment-summary-panel-compact' : 'payment-summary-panel'}
      aria-label="Resumo por forma de pagamento"
    >
      <div className="payment-summary-header">
        <div>
          <h2>Resumo por forma de pagamento</h2>
          <p>
            {compacto
              ? 'Principais formas de pagamento utilizadas.'
              : 'Totais agrupados por Pix, cartão, dinheiro, boleto e TED/DOC.'}
          </p>
        </div>
      </div>

      {carregando ? (
        <div className="loading-inline" aria-live="polite">
          Carregando resumo por forma de pagamento...
        </div>
      ) : erro ? (
        <div className="payment-summary-content">
          <MensagemAlerta mensagem={erro} tipo="danger" />
        </div>
      ) : resumo.length === 0 ? (
        <EstadoVazio
          titulo="Nenhum resumo disponível"
          descricao="Ainda não existem transações suficientes para montar o resumo por forma de pagamento."
        />
      ) : (
        <div className="payment-summary-list">
          {resumoExibido.map((item) => {
            const percentual = limitarPercentual(item.percentual);
            const chave = item.formaPagamento ?? 'NAO_INFORMADO';

            return (
              <article className="payment-summary-item" key={chave}>
                <div className="payment-summary-item-header">
                  <div>
                    <strong>{item.rotulo ?? 'Não informado'}</strong>
                    <span>
                      {item.quantidade} transaç{item.quantidade === 1 ? 'ão' : 'ões'}
                    </span>
                  </div>

                  <strong>{formatarMoeda(item.total)}</strong>
                </div>

                <div className="payment-summary-progress" aria-hidden="true">
                  <span style={{ width: `${percentual}%` }} />
                </div>

                <p>{formatarPercentual(item.percentual)} do total movimentado</p>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
};

export default ResumoFormaPagamento;