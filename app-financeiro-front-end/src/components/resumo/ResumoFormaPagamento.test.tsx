import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ResumoFormaPagamento from './ResumoFormaPagamento';

const { mockBuscarResumoPorPagamento, mockObterMensagemErroApi } = vi.hoisted(() => ({
  mockBuscarResumoPorPagamento: vi.fn(),
  mockObterMensagemErroApi: vi.fn((_erro: unknown, fallback: string) => fallback),
}));

vi.mock('../../services/api', () => ({
  buscarResumoPorPagamento: mockBuscarResumoPorPagamento,
  obterMensagemErroApi: mockObterMensagemErroApi,
}));

describe('ResumoFormaPagamento (Issue #158)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockObterMensagemErroApi.mockImplementation((_erro: unknown, fallback: string) => fallback);
  });

  it('exibe o estado de carregamento enquanto a API não responde', () => {
    mockBuscarResumoPorPagamento.mockReturnValue(new Promise(() => {}));

    render(<ResumoFormaPagamento />);

    expect(
      screen.getByText('Carregando resumo por forma de pagamento...'),
    ).toBeInTheDocument();
  });

  it('exibe o estado vazio quando não há dados de resumo', async () => {
    mockBuscarResumoPorPagamento.mockResolvedValue([]);

    render(<ResumoFormaPagamento />);

    expect(await screen.findByText('Nenhum resumo disponível')).toBeInTheDocument();
  });

  it('exibe mensagem de erro quando a chamada do resumo falha', async () => {
    mockBuscarResumoPorPagamento.mockRejectedValue(new Error('falha'));

    render(<ResumoFormaPagamento />);

    expect(
      await screen.findByText('Não foi possível carregar o resumo por forma de pagamento.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('renderiza forma de pagamento, total, quantidade e percentual', async () => {
    mockBuscarResumoPorPagamento.mockResolvedValue([
      { formaPagamento: 'PIX', rotulo: 'Pix', total: 1500.5, quantidade: 2, percentual: 75 },
      { formaPagamento: 'DINHEIRO', rotulo: 'Dinheiro', total: 400, quantidade: 1, percentual: 20 },
      { formaPagamento: null, rotulo: 'Não informado', total: 100, quantidade: 3, percentual: 5 },
    ]);

    render(<ResumoFormaPagamento />);

    // forma de pagamento (rótulos), incluindo o mapeamento de null -> "Não informado"
    expect(await screen.findByText('Pix')).toBeInTheDocument();
    expect(screen.getByText('Dinheiro')).toBeInTheDocument();
    expect(screen.getByText('Não informado')).toBeInTheDocument();

    // total formatado em moeda
    expect(screen.getByText(/1\.500,50/)).toBeInTheDocument();

    // quantidade (forma singular do item com 1 transação)
    expect(screen.getByText('1 transação')).toBeInTheDocument();

    // percentual do total movimentado
    expect(screen.getByText(/75,0% do total movimentado/)).toBeInTheDocument();
  });
});
