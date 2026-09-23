import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Dashboard from './Dashboard';
import RotaPrivada from '../routes/RotaPrivada';
import type { GrupoCategoriaResponse, ResumoMensalResponse } from '../services/api';

const authState = vi.hoisted(() => ({
  autenticacaoPronta: true,
  estaAutenticado: true,
  sair: vi.fn(),
}));

const { mockBuscarResumoMensal, mockBuscarResumoPorCategorias, mockObterMensagemErroApi } =
  vi.hoisted(() => ({
    mockBuscarResumoMensal: vi.fn(),
    mockBuscarResumoPorCategorias: vi.fn(),
    mockObterMensagemErroApi: vi.fn((_err: unknown, fallback: string) => fallback),
  }));

vi.mock('../hooks/useAutenticacao', () => ({
  useAutenticacao: () => authState,
}));

vi.mock('../components/resumo/ResumoFormaPagamentoPizza', () => ({
  default: () => <div data-testid="pizza-pagamento" />,
}));

vi.mock('../services/api', async () => {
  const actual = await vi.importActual<typeof import('../services/api')>('../services/api');

  return {
    ...actual,
    buscarResumoMensal: mockBuscarResumoMensal,
    buscarResumoPorCategorias: mockBuscarResumoPorCategorias,
    obterMensagemErroApi: mockObterMensagemErroApi,
  };
});

const resumoComDados: ResumoMensalResponse = {
  ano: 2026,
  mes: 6,
  dataInicio: '2026-06-01',
  dataFim: '2026-06-30',
  totalRecebido: 2500,
  totalGasto: 1000,
  saldo: 1500,
  categoriaMaiorGastoId: 'cat-1',
  categoriaMaiorGastoNome: 'Alimentação',
  categoriaMaiorGastoTotal: 700,
  variacaoPercentualGastos: 12.5,
  possuiTransacoes: true,
};

const categoriasComDados: GrupoCategoriaResponse[] = [
  {
    categoriaID: 'cat-1',
    nome: 'Alimentação',
    icone: 'icone-alimentacao',
    cor: '#FFAA00',
    total: 700,
    quantidade: 4,
    percentual: 70,
  },
  {
    categoriaID: 'cat-2',
    nome: 'Transporte',
    total: 300,
    quantidade: 2,
    percentual: 30,
  },
];

const resumoVazio: ResumoMensalResponse = {
  ano: 2026,
  mes: 6,
  dataInicio: '2026-06-01',
  dataFim: '2026-06-30',
  totalRecebido: 0,
  totalGasto: 0,
  saldo: 0,
  variacaoPercentualGastos: 0,
  possuiTransacoes: false,
};

describe('Dashboard mensal (Issue #173)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authState.autenticacaoPronta = true;
    authState.estaAutenticado = true;
    mockBuscarResumoMensal.mockReset();
    mockBuscarResumoPorCategorias.mockReset();
    mockObterMensagemErroApi.mockImplementation((_err: unknown, fallback: string) => fallback);
  });

  const renderizarDashboard = () =>
    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<RotaPrivada />}>
            <Route path="/dashboard" element={<Dashboard />} />
          </Route>
          <Route path="/login" element={<div>Tela de login</div>} />
        </Routes>
      </MemoryRouter>,
    );

  it('renderiza a tela de dashboard com navegação de mês', async () => {
    mockBuscarResumoMensal.mockResolvedValue(resumoComDados);
    mockBuscarResumoPorCategorias.mockResolvedValue(categoriasComDados);

    renderizarDashboard();

    expect(await screen.findByRole('heading', { name: /Dashboard/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/Navegação entre meses/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Mês anterior/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Próximo mês/i)).toBeInTheDocument();
  });

  it('exibe estado de carregamento enquanto busca o resumo', () => {
    mockBuscarResumoMensal.mockReturnValue(new Promise(() => {}));
    mockBuscarResumoPorCategorias.mockReturnValue(new Promise(() => {}));

    renderizarDashboard();

    expect(screen.getByText('Carregando resumo mensal...')).toBeInTheDocument();
  });

  it('exibe total recebido, total gasto e saldo do mês', async () => {
    mockBuscarResumoMensal.mockResolvedValue(resumoComDados);
    mockBuscarResumoPorCategorias.mockResolvedValue(categoriasComDados);

    renderizarDashboard();

    const resumo = await screen.findByLabelText(/Resumo mensal/i);
    expect(resumo).toHaveTextContent('Total recebido');
    expect(resumo).toHaveTextContent('R$ 2.500,00');
    expect(resumo).toHaveTextContent('Total gasto');
    expect(resumo).toHaveTextContent('R$ 1.000,00');
    expect(resumo).toHaveTextContent('Saldo do mês');
    expect(resumo).toHaveTextContent('R$ 1.500,00');
  });

  it('exibe variação percentual e categoria com maior gasto', async () => {
    mockBuscarResumoMensal.mockResolvedValue(resumoComDados);
    mockBuscarResumoPorCategorias.mockResolvedValue(categoriasComDados);

    renderizarDashboard();

    expect(await screen.findByText('Variação de gastos')).toBeInTheDocument();
    expect(screen.getByText('+12,5%')).toBeInTheDocument();
    expect(screen.getByLabelText(/Categoria com maior gasto/i)).toHaveTextContent('Alimentação');
    expect(screen.getByLabelText(/Categoria com maior gasto/i)).toHaveTextContent('R$ 700,00');
  });

  it('exibe gastos agrupados por categoria', async () => {
    mockBuscarResumoMensal.mockResolvedValue(resumoComDados);
    mockBuscarResumoPorCategorias.mockResolvedValue(categoriasComDados);

    renderizarDashboard();

    const secao = await screen.findByLabelText(/Gastos por categoria/i);
    expect(secao).toHaveTextContent('Alimentação');
    expect(secao).toHaveTextContent('Transporte');
    expect(secao).toHaveTextContent('R$ 700,00');
    expect(secao).toHaveTextContent('R$ 300,00');
    expect(secao).toHaveTextContent('70%');
    expect(secao).toHaveTextContent('30%');
  });

  it('exibe estado vazio quando não há transações no mês', async () => {
    mockBuscarResumoMensal.mockResolvedValue(resumoVazio);
    mockBuscarResumoPorCategorias.mockResolvedValue([]);

    renderizarDashboard();

    expect(await screen.findByText('Nenhuma transação neste mês')).toBeInTheDocument();
    expect(screen.queryByLabelText(/Resumo mensal/i)).not.toBeInTheDocument();
  });

  it('exibe mensagem de erro quando a API falha', async () => {
    mockBuscarResumoMensal.mockRejectedValue(new Error('falha'));
    mockBuscarResumoPorCategorias.mockRejectedValue(new Error('falha'));

    renderizarDashboard();

    expect(
      await screen.findByText('Não foi possível carregar o resumo mensal. Tente novamente.'),
    ).toBeInTheDocument();
  });

  it('navega entre meses e busca o resumo do período selecionado', async () => {
    const agora = new Date();
    const anoAtual = agora.getFullYear();
    const mesAtual = agora.getMonth() + 1;

    let mesAnterior = mesAtual - 1;
    let anoAnterior = anoAtual;
    if (mesAnterior === 0) {
      mesAnterior = 12;
      anoAnterior -= 1;
    }

    mockBuscarResumoMensal.mockResolvedValue(resumoComDados);
    mockBuscarResumoPorCategorias.mockResolvedValue(categoriasComDados);

    renderizarDashboard();
    await screen.findByLabelText(/Resumo mensal/i);

    expect(mockBuscarResumoMensal).toHaveBeenCalledWith(anoAtual, mesAtual);

    const user = userEvent.setup();
    await user.click(screen.getByLabelText(/Mês anterior/i));

    await waitFor(() => {
      expect(mockBuscarResumoMensal).toHaveBeenCalledWith(anoAnterior, mesAnterior);
      expect(mockBuscarResumoPorCategorias).toHaveBeenCalledWith(anoAnterior, mesAnterior);
    });
  });

  it('redireciona usuário não autenticado para o login', async () => {
    authState.estaAutenticado = false;
    mockBuscarResumoMensal.mockResolvedValue(resumoComDados);
    mockBuscarResumoPorCategorias.mockResolvedValue([]);

    renderizarDashboard();

    expect(await screen.findByText('Tela de login')).toBeInTheDocument();
    expect(mockBuscarResumoMensal).not.toHaveBeenCalled();
  });
});
