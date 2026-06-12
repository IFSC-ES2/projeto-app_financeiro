import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import ExtratoFuturo from './ExtratoFuturo';
import * as api from '../services/api';

vi.mock('../hooks/useAutenticacao', () => ({
  useAutenticacao: () => ({
    usuario: { id: 'user-123', nome: 'Usuário Teste', email: 'teste@teste.com' },
    autenticado: true,
    sair: vi.fn(),
  }),
}));

vi.mock('../services/api', async (importOriginal) => {
  const original = await importOriginal<typeof import('../services/api')>();
  return {
    ...original,
    obterExtratoFuturo: vi.fn(),
    pagarFatura: vi.fn(),
    obterMensagemErroApi: vi.fn((_err: unknown, fallback: string) => fallback),
  };
});

const mesVazio = (ano: number, mes: number): api.ProjecaoMensalResponse => ({
  ano,
  mes,
  dataInicio: `${ano}-${String(mes).padStart(2, '0')}-01`,
  dataFim: `${ano}-${String(mes).padStart(2, '0')}-28`,
  saldoPrevisto: 0,
  totalDebitos: 0,
  totalCreditos: 0,
  faturas: [],
  transacoes: [],
});

const projecaoVazia = [mesVazio(2026, 6), mesVazio(2026, 7), mesVazio(2026, 8)];

const parcelaJulho: api.TransacaoResponse = {
  transacaoId: 'tx-parcela',
  valor: 250,
  data: '2026-07-10',
  descricao: 'Notebook 2/10',
  tipoTransacao: 'DEBITO',
  formaPagamento: 'CARTAO_CREDITO',
  categoriaId: null,
  contaId: 'conta-1',
  categorizada: false,
};

const faturaJulho: api.FaturaResumoResponse = {
  faturaId: 'fat-1',
  contaId: 'conta-1',
  contaNome: 'Cartão Nubank',
  mesReferencia: '2026-06',
  dataVencimento: '2026-07-08',
  valorTotal: 980,
  status: 'ABERTA',
};

const projecaoComDados: api.ProjecaoMensalResponse[] = [
  mesVazio(2026, 6),
  {
    ...mesVazio(2026, 7),
    saldoPrevisto: -1230,
    totalDebitos: 1230,
    faturas: [faturaJulho],
    transacoes: [parcelaJulho],
  },
  mesVazio(2026, 8),
];

const renderExtratoFuturo = () =>
  render(
    <MemoryRouter initialEntries={['/extrato-futuro']}>
      <ExtratoFuturo />
    </MemoryRouter>,
  );

const aguardarCarregamento = async () => {
  await waitFor(() => {
    expect(screen.queryByText(/Carregando extrato futuro/i)).not.toBeInTheDocument();
  });
};

describe('Tela de Extrato Futuro (Issue #68)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.obterExtratoFuturo).mockReset();
    vi.mocked(api.pagarFatura).mockReset();
    vi.mocked(api.obterExtratoFuturo).mockResolvedValue(projecaoVazia);
  });

  it('deve exibir loading e depois o resumo da projeção', async () => {
    renderExtratoFuturo();

    expect(screen.getByText(/Carregando extrato futuro/i)).toBeInTheDocument();

    await aguardarCarregamento();

    expect(api.obterExtratoFuturo).toHaveBeenCalledWith(3);
    expect(screen.getByLabelText(/Resumo da projeção/i)).toBeInTheDocument();
  });

  it('deve exibir estado vazio informativo quando não há movimentações futuras', async () => {
    renderExtratoFuturo();
    await aguardarCarregamento();

    expect(screen.getByText(/Nada projetado por aqui/i)).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: /Registrar transação futura/i }),
    ).toHaveAttribute('href', '/transacoes/nova');
  });

  it('deve listar parcelas futuras no mês correspondente', async () => {
    vi.mocked(api.obterExtratoFuturo).mockResolvedValue(projecaoComDados);

    renderExtratoFuturo();
    await aguardarCarregamento();

    expect(screen.getByText('Julho de 2026')).toBeInTheDocument();
    expect(screen.getByText('Notebook 2/10')).toBeInTheDocument();
    expect(screen.getByText(/Parcela no cartão/i)).toBeInTheDocument();
  });

  it('deve exibir fatura com vencimento, status e valor', async () => {
    vi.mocked(api.obterExtratoFuturo).mockResolvedValue(projecaoComDados);

    renderExtratoFuturo();
    await aguardarCarregamento();

    expect(screen.getByText(/Fatura Cartão Nubank/i)).toBeInTheDocument();
    expect(screen.getByText(/Vence em 08\/07\/2026/i)).toBeInTheDocument();
    expect(screen.getByText('Aberta')).toBeInTheDocument();
    expect(screen.getByText(/980,00/)).toBeInTheDocument();
  });

  it('deve exibir saldo previsto e totais do mês projetado', async () => {
    vi.mocked(api.obterExtratoFuturo).mockResolvedValue(projecaoComDados);

    renderExtratoFuturo();
    await aguardarCarregamento();

    // -1.230,00 no saldo previsto de julho; 1.230,00 nos débitos do mês e no "A pagar" do hero
    expect(screen.getAllByText(/-?R\$\s?1\.230,00/).length).toBeGreaterThanOrEqual(2);
    expect(screen.getAllByText(/Saldo previsto/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/Débitos/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/Créditos/i).length).toBeGreaterThan(0);
  });

  it('deve manter a projeção na tela quando o pagamento da fatura falha', async () => {
    vi.mocked(api.obterExtratoFuturo).mockResolvedValue(projecaoComDados);
    vi.mocked(api.pagarFatura).mockRejectedValueOnce(new Error('Falha na API'));

    const usuario = userEvent.setup();

    renderExtratoFuturo();
    await aguardarCarregamento();

    await usuario.click(screen.getByRole('button', { name: /Marcar paga/i }));

    await waitFor(() => {
      expect(screen.getByText('Não foi possível pagar a fatura.')).toBeInTheDocument();
    });

    expect(screen.getByText('Julho de 2026')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Marcar paga/i })).toBeEnabled();
  });

  it('deve marcar fatura como paga e recarregar a projeção', async () => {
    vi.mocked(api.obterExtratoFuturo).mockResolvedValue(projecaoComDados);
    vi.mocked(api.pagarFatura).mockResolvedValue({ ...faturaJulho, status: 'PAGA' });

    const usuario = userEvent.setup();

    renderExtratoFuturo();
    await aguardarCarregamento();

    await usuario.click(screen.getByRole('button', { name: /Marcar paga/i }));

    await waitFor(() => {
      expect(api.pagarFatura).toHaveBeenCalledWith('fat-1');
      expect(screen.getByText('Fatura marcada como paga.')).toBeInTheDocument();
    });

    expect(api.obterExtratoFuturo).toHaveBeenCalledTimes(2);
  });

  it('deve solicitar novo horizonte ao alterar a quantidade de meses', async () => {
    const usuario = userEvent.setup();

    renderExtratoFuturo();
    await aguardarCarregamento();

    const seletor = screen.getByRole('combobox', { name: /Quantidade de meses projetados/i });
    await usuario.selectOptions(seletor, '6');

    await waitFor(() => {
      expect(api.obterExtratoFuturo).toHaveBeenLastCalledWith(6);
    });
  });

  it('deve exibir erro quando a API falhar', async () => {
    vi.mocked(api.obterExtratoFuturo).mockRejectedValueOnce(new Error('Falha na API'));

    renderExtratoFuturo();
    await aguardarCarregamento();

    expect(screen.getByText('Não foi possível carregar o extrato futuro.')).toBeInTheDocument();
  });
});
