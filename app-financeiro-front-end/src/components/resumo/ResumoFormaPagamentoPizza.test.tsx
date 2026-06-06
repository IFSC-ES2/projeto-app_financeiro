import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ResumoFormaPagamentoPizza from './ResumoFormaPagamentoPizza';

const { mockBuscarResumoPorPagamento, mockObterMensagemErroApi } = vi.hoisted(() => ({
  mockBuscarResumoPorPagamento: vi.fn(),
  mockObterMensagemErroApi: vi.fn((_erro: unknown, fallback: string) => fallback),
}));

vi.mock('../../services/api', () => ({
  buscarResumoPorPagamento: mockBuscarResumoPorPagamento,
  obterMensagemErroApi: mockObterMensagemErroApi,
}));

describe('ResumoFormaPagamentoPizza (Issue #158)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockObterMensagemErroApi.mockImplementation((_erro: unknown, fallback: string) => fallback);
  });

  it('exibe o estado de carregamento enquanto a API não responde', () => {
    mockBuscarResumoPorPagamento.mockReturnValue(new Promise(() => {}));

    render(<ResumoFormaPagamentoPizza />);

    expect(
      screen.getByText('Carregando resumo por forma de pagamento...'),
    ).toBeInTheDocument();
  });

  it('exibe o estado vazio quando não há dados de resumo', async () => {
    mockBuscarResumoPorPagamento.mockResolvedValue([]);

    render(<ResumoFormaPagamentoPizza />);

    expect(await screen.findByText('Nenhum resumo disponível')).toBeInTheDocument();
  });

  it('exibe mensagem de erro quando a chamada do resumo falha', async () => {
    mockBuscarResumoPorPagamento.mockRejectedValue(new Error('falha'));

    render(<ResumoFormaPagamentoPizza />);

    expect(
      await screen.findByText('Não foi possível carregar o resumo por forma de pagamento.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('renderiza os rótulos e percentuais na legenda', async () => {
    mockBuscarResumoPorPagamento.mockResolvedValue([
      { formaPagamento: 'PIX', total: 600, quantidade: 3, percentual: 60 },
      { formaPagamento: 'DINHEIRO', total: 300, quantidade: 2, percentual: 30 },
      { formaPagamento: null, total: 100, quantidade: 1, percentual: 10 },
    ]);

    render(<ResumoFormaPagamentoPizza />);

    // rótulos da legenda (inclui o mapeamento de null -> "Não informado")
    expect(await screen.findByText('Pix')).toBeInTheDocument();
    expect(screen.getByText('Dinheiro')).toBeInTheDocument();
    expect(screen.getByText('Não informado')).toBeInTheDocument();

    // percentuais da legenda
    expect(screen.getByText('60,0%')).toBeInTheDocument();
    expect(screen.getByText('30,0%')).toBeInTheDocument();
    expect(screen.getByText('10,0%')).toBeInTheDocument();

    // o gráfico de pizza é renderizado
    expect(
      screen.getByRole('img', { name: /Gráfico de pizza do resumo por forma de pagamento/i }),
    ).toBeInTheDocument();
  });
});
