import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Categorias from './Categorias';
import { listarCategorias } from '../services/categoriaService';
import { listarContas } from '../services/contaService';
import { listarTransacoes } from '../services/transacaoService';
import type { CategoriaResponse } from '../types/categoria';
import type { ContaResponse } from '../types/conta';
import type { PaginaResponse, TransacaoResponse } from '../types/transacao';

vi.mock('../hooks/useAutenticacao', () => ({
  useAutenticacao: () => ({ autenticado: true, sair: vi.fn() }),
}));
vi.mock('../services/categoriaService', () => ({ listarCategorias: vi.fn() }));
vi.mock('../services/contaService', () => ({ listarContas: vi.fn() }));
vi.mock('../services/transacaoService', () => ({ listarTransacoes: vi.fn() }));
vi.mock('../services/apiError', () => ({
  obterMensagemErroApi: vi.fn((_erro: unknown, fallback: string) => fallback),
}));

const categorias: CategoriaResponse[] = [
  { categoriaId: 'alimentacao', nome: 'Alimentação', padrao: true, cor: '#2FA98F' },
  { categoriaId: 'transporte', nome: 'Transporte', padrao: false, cor: '#4263A8' },
];

const contas: ContaResponse[] = [
  { contaId: 'conta-1', nome: 'Conta principal', tipoConta: 'CORRENTE' },
];

const pagina = (conteudo: TransacaoResponse[]): PaginaResponse<TransacaoResponse> => ({
  conteudo,
  pagina: 0,
  tamanho: 100,
  totalElementos: conteudo.length,
  totalPaginas: 1,
  primeira: true,
  ultima: true,
});

const despesaAlimentacao: TransacaoResponse = {
  transacaoId: 'tx-alimentacao',
  valor: 86.5,
  data: '2026-06-07',
  descricao: 'Mercado da semana',
  tipoTransacao: 'DEBITO',
  categoriaId: 'alimentacao',
  contaId: 'conta-1',
  categorizada: true,
};

const despesaTransporte: TransacaoResponse = {
  ...despesaAlimentacao,
  transacaoId: 'tx-transporte',
  valor: 45,
  descricao: 'Combustível',
  categoriaId: 'transporte',
};

const renderCategorias = () => render(<MemoryRouter><Categorias /></MemoryRouter>);

describe('Tela de categorias (Issue #196)', () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    vi.setSystemTime(new Date('2026-06-15T12:00:00'));
    vi.mocked(listarCategorias).mockResolvedValue(categorias);
    vi.mocked(listarContas).mockResolvedValue(contas);
    vi.mocked(listarTransacoes).mockResolvedValue(pagina([despesaAlimentacao, despesaTransporte]));
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.clearAllMocks();
  });

  it('exibe carregamento enquanto busca os gastos do mês', async () => {
    let resolver: (value: CategoriaResponse[]) => void = () => undefined;
    vi.mocked(listarCategorias).mockImplementationOnce(() => new Promise((resolve) => { resolver = resolve; }));

    renderCategorias();
    expect(screen.getByText('Carregando categorias...')).toBeInTheDocument();

    resolver(categorias);
    expect(await screen.findByRole('heading', { name: 'Movimentações de Junho de 2026' })).toBeInTheDocument();
  });

  it('exibe resumo mensal, gráfico e valores proporcionais por categoria', async () => {
    renderCategorias();

    expect(await screen.findByText('R$ 131,50')).toBeInTheDocument();
    expect(screen.getByLabelText('Distribuição das movimentações de Junho de 2026')).toBeInTheDocument();
    expect(screen.getByText('2 transações')).toBeInTheDocument();
    expect(screen.getByText('Alimentação')).toBeInTheDocument();
    expect(screen.getByText('Transporte')).toBeInTheDocument();
    expect(listarTransacoes).toHaveBeenCalledWith({
      page: 0,
      size: 100,
      dataInicio: '2026-06-01',
      dataFim: '2026-06-30',
    });
  });

  it('expande somente as transações da categoria selecionada, com conta e tipo', async () => {
    const usuario = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderCategorias();
    await screen.findByText('Alimentação');

    await usuario.click(screen.getByRole('button', { name: /Alimentação/i }));

    expect(screen.getByText('Mercado da semana')).toBeInTheDocument();
    expect(screen.queryByText('Combustível')).not.toBeInTheDocument();
    expect(screen.getByText('07/06/2026')).toBeInTheDocument();
    expect(screen.getByText('Despesa')).toBeInTheDocument();
    expect(screen.getByText('Conta principal')).toBeInTheDocument();
  });

  it('lista créditos e débitos da categoria nos totais', async () => {
    const usuario = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    vi.mocked(listarTransacoes).mockResolvedValueOnce(pagina([
      despesaAlimentacao,
      despesaTransporte,
      {
        ...despesaAlimentacao,
        transacaoId: 'tx-credito',
        descricao: 'Reembolso do mercado',
        tipoTransacao: 'CREDITO',
        valor: 30,
      },
      {
        ...despesaAlimentacao,
        transacaoId: 'tx-outro-mes',
        descricao: 'Compra de maio',
        data: '2026-05-31',
      },
    ]));
    renderCategorias();

    expect(await screen.findByText('R$ 161,50')).toBeInTheDocument();
    expect(screen.getByText('3 transações')).toBeInTheDocument();
    const categoria = screen.getByRole('button', { name: /Alimentação/i });
    expect(within(categoria).getByLabelText('2 transações')).toBeInTheDocument();
    expect(within(categoria).getByText('-R$ 56,50')).toHaveClass('amount-negative');
    await usuario.click(categoria);

    expect(screen.getByText('Mercado da semana')).toBeInTheDocument();
    expect(screen.getByText('Despesa')).toBeInTheDocument();
    const receita = screen.getByRole('row', { name: /Reembolso do mercado/i });
    expect(within(receita).getByText('Receita')).toBeInTheDocument();
    expect(within(receita).getByText('R$ 30,00')).toHaveClass('amount-positive');
    expect(within(receita).getByText('Conta principal')).toBeInTheDocument();
    expect(screen.queryByText('Combustível')).not.toBeInTheDocument();
    expect(screen.queryByText('Compra de maio')).not.toBeInTheDocument();
  });

  it('exibe transações de uma categoria com somente créditos como entrada', async () => {
    const usuario = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    vi.mocked(listarTransacoes).mockResolvedValueOnce(pagina([{
      ...despesaAlimentacao,
      transacaoId: 'tx-credito',
      descricao: 'Reembolso do mercado',
      tipoTransacao: 'CREDITO',
      valor: 30,
    }]));
    renderCategorias();

    const categoria = await screen.findByRole('button', { name: /Alimentação/i });
    expect(within(categoria).getByLabelText('1 transações')).toBeInTheDocument();
    expect(within(categoria).getByText('R$ 30,00')).toHaveClass('amount-positive');
    expect(screen.getByText('1 transação')).toBeInTheDocument();
    await usuario.click(categoria);

    expect(screen.getByText('Reembolso do mercado')).toBeInTheDocument();
    expect(screen.getByText('Receita')).toBeInTheDocument();
    expect(screen.queryByText('Sem transações neste mês')).not.toBeInTheDocument();

    await usuario.click(categoria);
    await usuario.click(screen.getByRole('button', { name: /Transporte/i }));
    expect(screen.getByText('Sem transações neste mês')).toBeInTheDocument();
    expect(screen.getByText('Essa categoria não possui transações no mês selecionado.')).toBeInTheDocument();
  });

  it('mostra gastos em vermelho e entradas em verde nas barras das categorias', async () => {
    const categoriasComServicos: CategoriaResponse[] = [
      { categoriaId: 'lazer', nome: 'Lazer', padrao: false, cor: '#2FA98F' },
      { categoriaId: 'servicos', nome: 'Serviços', padrao: false, cor: '#D78CE2' },
    ];
    vi.mocked(listarCategorias).mockResolvedValueOnce(categoriasComServicos);
    vi.mocked(listarTransacoes).mockResolvedValueOnce(pagina([
      { ...despesaAlimentacao, transacaoId: 'tx-lazer', categoriaId: 'lazer', valor: 10_000 },
      {
        ...despesaAlimentacao,
        transacaoId: 'tx-servicos',
        categoriaId: 'servicos',
        tipoTransacao: 'CREDITO',
        valor: 200_000,
      },
    ]));
    renderCategorias();

    const lazer = await screen.findByRole('button', { name: /Lazer/i });
    const servicos = screen.getByRole('button', { name: /Serviços/i });

    expect(within(lazer).getByText('-R$ 10.000,00')).toHaveClass('amount-negative');
    expect(lazer.querySelector('.category-expense-progress-expense')).toBeInTheDocument();
    expect(lazer.querySelector('.category-expense-progress-income')).not.toBeInTheDocument();
    expect(within(servicos).getByText('R$ 200.000,00')).toHaveClass('amount-positive');
    expect(servicos.querySelector('.category-expense-progress-income')).toBeInTheDocument();
    expect(servicos.querySelector('.category-expense-progress-expense')).not.toBeInTheDocument();
  });

  it('recarrega os dados ao selecionar o próximo mês', async () => {
    const usuario = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderCategorias();
    await screen.findByText('Alimentação');

    await usuario.click(screen.getByRole('button', { name: 'Próximo mês' }));

    await waitFor(() => expect(listarTransacoes).toHaveBeenLastCalledWith({
      page: 0,
      size: 100,
      dataInicio: '2026-07-01',
      dataFim: '2026-07-31',
    }));
    expect(listarCategorias).toHaveBeenCalledTimes(1);
    expect(listarContas).toHaveBeenCalledTimes(1);
  });

  it('exibe estado vazio quando não existem categorias nem transações', async () => {
    vi.mocked(listarCategorias).mockResolvedValueOnce([]);
    vi.mocked(listarTransacoes).mockResolvedValueOnce(pagina([]));
    renderCategorias();

    expect(await screen.findByText('Nenhuma categoria encontrada')).toBeInTheDocument();
  });

  it('exibe erro da API de forma clara', async () => {
    vi.mocked(listarTransacoes).mockRejectedValueOnce(new Error('Falha da API'));
    renderCategorias();

    expect(await screen.findByText('Não foi possível carregar as transações por categoria.')).toBeInTheDocument();
  });
});
