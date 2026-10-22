import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Dashboard from './Dashboard';
import type { GrupoCategoria, ResumoMensal } from '../types/resumo';

const authState = vi.hoisted(() => ({
  estaAutenticado: true,
  sair: vi.fn(),
}));

const { mockBuscarResumoMensal, mockBuscarResumoPorCategoria } = vi.hoisted(() => ({
  mockBuscarResumoMensal: vi.fn(),
  mockBuscarResumoPorCategoria: vi.fn(),
}));

vi.mock('../hooks/useAutenticacao', () => ({
  useAutenticacao: () => authState,
}));

vi.mock('../components/resumo/ResumoFormaPagamentoPizza', () => ({
  default: () => null,
}));

vi.mock('../services/resumoService', () => ({
  buscarResumoMensal: mockBuscarResumoMensal,
  buscarResumoPorCategoria: mockBuscarResumoPorCategoria,
}));

const resumoComDados: ResumoMensal = {
  ano: 2026,
  mes: 10,
  dataInicio: '2026-10-01',
  dataFim: '2026-10-31',
  totalRecebido: 3500,
  totalGasto: 1200.5,
  saldo: 2299.5,
  categoriaMaiorGastoId: 'cat-1',
  categoriaMaiorGastoNome: 'Moradia',
  categoriaMaiorGastoTotal: 800,
  variacaoPercentualGastos: 12.5,
  possuiTransacoes: true,
};

const categorias: GrupoCategoria[] = [
  {
    categoriaID: 'cat-1',
    nome: 'Moradia',
    icone: null,
    cor: null,
    total: 800,
    quantidade: 1,
    percentual: 66.6,
  },
  {
    categoriaID: 'cat-2',
    nome: 'Alimentação',
    icone: null,
    cor: null,
    total: 400.5,
    quantidade: 4,
    percentual: 33.4,
  },
];

const periodoAtual = () => {
  const data = new Date();
  return { ano: data.getFullYear(), mes: data.getMonth() + 1 };
};

const periodoDeslocado = (delta: number) => {
  const atual = periodoAtual();
  const data = new Date(atual.ano, atual.mes - 1 + delta, 1);
  return { ano: data.getFullYear(), mes: data.getMonth() + 1 };
};

const rotuloMes = (ano: number, mes: number) =>
  new Date(ano, mes - 1, 1).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });

describe('Dashboard mensal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authState.estaAutenticado = true;
    mockBuscarResumoMensal.mockResolvedValue(resumoComDados);
    mockBuscarResumoPorCategoria.mockResolvedValue(categorias);
  });

  const renderizar = () =>
    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/login" element={<h1>Tela de login</h1>} />
        </Routes>
      </MemoryRouter>,
    );

  const aguardarResumo = async () => {
    expect(await screen.findByText('Total recebido')).toBeInTheDocument();
  };

  it('redireciona usuario nao autenticado para o login sem consultar a API', () => {
    authState.estaAutenticado = false;
    renderizar();

    expect(screen.getByRole('heading', { name: 'Tela de login' })).toBeInTheDocument();
    expect(mockBuscarResumoMensal).not.toHaveBeenCalled();
    expect(mockBuscarResumoPorCategoria).not.toHaveBeenCalled();
  });

  it('renderiza o dashboard com totais, saldo, maior gasto, variacao e categorias', async () => {
    renderizar();
    await aguardarResumo();

    expect(screen.getByRole('heading', { level: 1, name: 'Dashboard' })).toBeInTheDocument();
    const totais = within(screen.getByRole('region', { name: 'Resumo do mês' }));
    expect(totais.getByText('Total recebido')).toBeInTheDocument();
    expect(totais.getByText(/3\.500,00/)).toBeInTheDocument();
    expect(totais.getByText('Total gasto')).toBeInTheDocument();
    expect(totais.getByText(/1\.200,50/)).toBeInTheDocument();
    expect(totais.getByText('Saldo do mês')).toBeInTheDocument();
    expect(totais.getByText(/2\.299,50/)).toBeInTheDocument();

    const maiorGasto = within(screen.getByRole('region', { name: 'Maior gasto' }));
    expect(maiorGasto.getByText('Moradia')).toBeInTheDocument();
    expect(maiorGasto.getByText(/800,00/)).toBeInTheDocument();

    expect(screen.getByText('+12,5%')).toBeInTheDocument();

    const lista = within(screen.getByRole('region', { name: 'Gastos por categoria' }));
    expect(lista.getByText('Moradia')).toBeInTheDocument();
    expect(lista.getByText('Alimentação')).toBeInTheDocument();
    expect(lista.getByText(/400,50/)).toBeInTheDocument();
  });

  it('mostra estado vazio quando o mes nao tem transacoes', async () => {
    mockBuscarResumoMensal.mockResolvedValue({ ...resumoComDados, possuiTransacoes: false });
    mockBuscarResumoPorCategoria.mockResolvedValue([]);

    renderizar();

    expect(await screen.findByText('Nenhuma transação neste mês')).toBeInTheDocument();
    expect(screen.queryByText('Total recebido')).not.toBeInTheDocument();
  });

  it('mostra o carregamento enquanto o resumo nao chega', async () => {
    mockBuscarResumoMensal.mockReturnValue(new Promise(() => undefined));
    mockBuscarResumoPorCategoria.mockReturnValue(new Promise(() => undefined));

    renderizar();

    expect(await screen.findByText('Carregando resumo mensal')).toBeInTheDocument();
  });

  it('mostra erro quando a API falha', async () => {
    mockBuscarResumoMensal.mockRejectedValue(new Error('falha'));

    renderizar();

    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível carregar o resumo do mês.');
  });

  it('mostra aviso quando nao ha variacao do mes anterior', async () => {
    mockBuscarResumoMensal.mockResolvedValue({ ...resumoComDados, variacaoPercentualGastos: null });

    renderizar();
    await aguardarResumo();

    expect(screen.getByText('Sem comparação com o mês anterior')).toBeInTheDocument();
  });

  it('navega para o mes anterior e para o proximo consultando a API de novo', async () => {
    const user = userEvent.setup();
    const atual = periodoAtual();
    const anterior = periodoDeslocado(-1);
    const proximo = periodoDeslocado(1);

    renderizar();
    await aguardarResumo();

    expect(mockBuscarResumoMensal).toHaveBeenCalledWith(atual.ano, atual.mes);
    expect(mockBuscarResumoPorCategoria).toHaveBeenCalledWith(atual.ano, atual.mes);

    await user.click(screen.getByRole('button', { name: 'Mês anterior' }));

    await waitFor(() => {
      expect(mockBuscarResumoMensal).toHaveBeenCalledWith(anterior.ano, anterior.mes);
    });
    expect(screen.getByRole('heading', { level: 2, name: rotuloMes(anterior.ano, anterior.mes) })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Próximo mês' }));
    await user.click(screen.getByRole('button', { name: 'Próximo mês' }));

    await waitFor(() => {
      expect(mockBuscarResumoMensal).toHaveBeenCalledWith(proximo.ano, proximo.mes);
    });
    expect(screen.getByRole('heading', { level: 2, name: rotuloMes(proximo.ano, proximo.mes) })).toBeInTheDocument();
  });
});
