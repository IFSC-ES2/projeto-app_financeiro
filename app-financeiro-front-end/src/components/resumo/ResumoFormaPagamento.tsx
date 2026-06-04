import { useEffect, useState } from 'react';
import EstadoVazio from '../ui/EstadoVazio';
import MensagemAlerta from '../ui/MensagemAlerta';
import { buscarResumoPorPagamento, obterMensagemErroApi } from '../../services/api';
import type { ResumoPagamentoResponse, TipoPagamento } from '../../services/api';
import { formatarMoeda } from '../../utils/formatacao'; 

const rotulosFormaPagamento: Record<TipoPagamento, string> = {
  PIX: 'Pix',
  CARTAO_DEBITO: 'Cartão de débito',
  CARTAO_CREDITO: 'Cartão de crédito',
  DINHEIRO: 'Dinheiro',
  BOLETO: 'Boleto',
  TED_DOC: 'TED/DOC',
};

const obterRotuloFormaPagamento = (formaPagamento: TipoPagamento | null) => {
  if (!formaPagamento) return 'Não informado';

  return rotulosFormaPagamento[formaPagamento];
};

const formatarPercentual = (percentual: number) =>
  `${Number(percentual).toLocaleString('pt-BR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  })}%`;

const limitarPercentual = (percentual: number) =>
  Math.min(Math.max(Number(percentual) || 0, 0), 100);

const ResumoFormaPagamento = () => {
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

  return (
    <section className="payment-summary-panel" aria-label="Resumo por forma de pagamento">
      <div className="payment-summary-header">
        <div>
          <h2>Resumo por forma de pagamento</h2>
          <p>Totais agrupados por Pix, cartão, dinheiro, boleto e TED/DOC.</p>
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
          {resumo.map((item) => {
            const percentual = limitarPercentual(item.percentual);
            const chave = item.formaPagamento ?? 'NAO_INFORMADO';

            return (
              <article className="payment-summary-item" key={chave}>
                <div className="payment-summary-item-header">
                  <div>
                    <strong>{obterRotuloFormaPagamento(item.formaPagamento)}</strong>
                    <span>
                      {item.quantidade} transação{item.quantidade === 1 ? '' : 'ões'}
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