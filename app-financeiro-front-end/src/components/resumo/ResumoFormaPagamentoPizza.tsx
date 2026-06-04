import { useEffect, useMemo, useState } from 'react';
import EstadoVazio from '../ui/EstadoVazio';
import MensagemAlerta from '../ui/MensagemAlerta';
import { buscarResumoPorPagamento, obterMensagemErroApi } from '../../services/api';
import type { ResumoPagamentoResponse, TipoPagamento } from '../../services/api';

const rotulosFormaPagamento: Record<TipoPagamento, string> = {
  PIX: 'Pix',
  CARTAO_DEBITO: 'Cartão de débito',
  CARTAO_CREDITO: 'Cartão de crédito',
  DINHEIRO: 'Dinheiro',
  BOLETO: 'Boleto',
  TED_DOC: 'TED/DOC',
};

const classesFormaPagamento: Record<TipoPagamento, string> = {
  PIX: 'payment-chart-color-pix',
  CARTAO_DEBITO: 'payment-chart-color-debito',
  CARTAO_CREDITO: 'payment-chart-color-credito',
  DINHEIRO: 'payment-chart-color-dinheiro',
  BOLETO: 'payment-chart-color-boleto',
  TED_DOC: 'payment-chart-color-ted',
};

const coresFormaPagamento: Record<TipoPagamento, string> = {
  PIX: 'var(--sb-primary)',
  CARTAO_DEBITO: '#35B779',
  CARTAO_CREDITO: '#F4C542',
  DINHEIRO: '#FF8F3D',
  BOLETO: '#7C5CFF',
  TED_DOC: '#4D96FF',
};

const obterRotuloFormaPagamento = (formaPagamento: TipoPagamento | null) => {
  if (!formaPagamento) return 'Não informado';

  return rotulosFormaPagamento[formaPagamento];
};

const obterCorFormaPagamento = (formaPagamento: TipoPagamento | null) => {
  if (!formaPagamento) return 'var(--sb-text-muted)';

  return coresFormaPagamento[formaPagamento];
};

const obterClasseFormaPagamento = (formaPagamento: TipoPagamento | null) => {
  if (!formaPagamento) return 'payment-chart-color-nao-informado';

  return classesFormaPagamento[formaPagamento];
};

const formatarPercentual = (percentual: number) =>
  `${Number(percentual).toLocaleString('pt-BR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  })}%`;

const ResumoFormaPagamentoPizza = () => {
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

  const resumoValido = useMemo(
    () => resumo.filter((item) => Number(item.percentual) > 0),
    [resumo],
  );

  const graficoPizza = useMemo(() => {
    if (resumoValido.length === 0) return 'var(--sb-border) 0deg 360deg';

    let inicio = 0;

    return resumoValido
      .map((item) => {
        const graus = (Number(item.percentual) / 100) * 360;
        const fim = inicio + graus;
        const fatia = `${obterCorFormaPagamento(item.formaPagamento)} ${inicio}deg ${fim}deg`;

        inicio = fim;

        return fatia;
      })
      .join(', ');
  }, [resumoValido]);

  return (
    <section className="dashboard-payment-card" aria-label="Resumo por forma de pagamento">
      <div className="dashboard-payment-card-header">
        <div>
          <h2>Formas de pagamento</h2>
          <p>Distribuição das transações por meio de pagamento.</p>
        </div>
      </div>

      {carregando ? (
        <div className="loading-inline" aria-live="polite">
          Carregando resumo por forma de pagamento...
        </div>
      ) : erro ? (
        <div className="dashboard-payment-card-content">
          <MensagemAlerta mensagem={erro} tipo="danger" />
        </div>
      ) : resumoValido.length === 0 ? (
        <EstadoVazio
          titulo="Nenhum resumo disponível"
          descricao="Ainda não existem transações suficientes para montar o gráfico."
        />
      ) : (
        <div className="dashboard-payment-chart-area">
          <div
            className="dashboard-payment-chart"
            style={{ background: `conic-gradient(${graficoPizza})` }}
            role="img"
            aria-label="Gráfico de pizza do resumo por forma de pagamento"
          >
            <span />
          </div>

          <div className="dashboard-payment-legend">
            {resumoValido.map((item) => (
              <div className="dashboard-payment-legend-item" key={item.formaPagamento ?? 'NAO_INFORMADO'}>
                <span className={obterClasseFormaPagamento(item.formaPagamento)} />
                <div>
                  <strong>{obterRotuloFormaPagamento(item.formaPagamento)}</strong>
                  <small>{formatarPercentual(item.percentual)}</small>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </section>
  );
};

export default ResumoFormaPagamentoPizza;