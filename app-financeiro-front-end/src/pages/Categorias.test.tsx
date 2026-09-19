import { render, screen, waitFor } from '@testing-library/react';
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
    expect(await screen.findByRole('heading', { name: 'Gastos de Junho de 2026' })).toBeInTheDocument();
  });

  it('exibe resumo mensal, gráfico e valores proporcionais por categoria', async () => {
    renderCategorias();

    expect(await screen.findByText('R$ 131,50')).toBeInTheDocument();
    expect(screen.getByLabelText('Distribuição dos gastos de Junho de 2026')).toBeInTheDocument();
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

    expect(await screen.findByText('Não foi possível carregar os gastos por categoria.')).toBeInTheDocument();
  });
});
