import { render, screen, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Parcelamentos from './Parcelamentos';
import type { ProjecaoMensal } from '../types/extratoFuturo';

const authState = vi.hoisted(() => ({
  estaAutenticado: true,
  sair: vi.fn(),
}));

const { mockBuscarExtratoFuturo } = vi.hoisted(() => ({
  mockBuscarExtratoFuturo: vi.fn(),
}));

vi.mock('../hooks/useAutenticacao', () => ({
  useAutenticacao: () => authState,
}));

vi.mock('../services/extratoFuturoService', () => ({
  buscarExtratoFuturo: mockBuscarExtratoFuturo,
}));

const projecao = (mes: string, parcial: Partial<ProjecaoMensal> = {}): ProjecaoMensal => ({
  mes,
  saldoPrevisto: 0,
  totalDebitos: 0,
  totalCreditos: 0,
  quantidadeVencimentos: 0,
  transacoes: [],
  faturas: [],
  ...parcial,
});

const mesesComDados: ProjecaoMensal[] = [
  projecao('2026-11', {
    saldoPrevisto: -1500,
    totalDebitos: 1400,
    totalCreditos: 320,
    quantidadeVencimentos: 2,
    faturas: [
      {
        faturaId: 'fat-1',
        nomeConta: 'Nubank',
        mesReferencia: '2026-11',
        dataVencimento: '2026-11-10',
        valorTotal: 900,
        status: 'ABERTA',
      },
    ],
    transacoes: [
      {
        transacaoId: 'tx-1',
        valor: 500,
        data: '2026-11-05',
        descricao: 'Aluguel',
        tipoTransacao: 'DEBITO',
        contaId: 'conta-1',
        categorizada: false,
        formaPagamento: 'BOLETO',
      },
    ],
  }),
  projecao('2026-12', {
    saldoPrevisto: 200,
    totalDebitos: 100,
    totalCreditos: 300,
  }),
  projecao('2027-01', {
    saldoPrevisto: 0,
    totalDebitos: 50,
    totalCreditos: 50,
  }),
];

describe('Extrato futuro', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authState.estaAutenticado = true;
    mockBuscarExtratoFuturo.mockResolvedValue(mesesComDados);
  });

  const renderizar = () =>
    render(
      <MemoryRouter initialEntries={['/parcelamentos']}>
        <Routes>
          <Route path="/parcelamentos" element={<Parcelamentos />} />
          <Route path="/login" element={<h1>Tela de login</h1>} />
        </Routes>
      </MemoryRouter>,
    );

  it('redireciona usuario nao autenticado para o login sem consultar a API', () => {
    authState.estaAutenticado = false;
    renderizar();

    expect(screen.getByRole('heading', { name: 'Tela de login' })).toBeInTheDocument();
    expect(mockBuscarExtratoFuturo).not.toHaveBeenCalled();
  });

  it('renderiza os proximos 3 meses com saldo, debitos, creditos, fatura e transacao', async () => {
    renderizar();

    expect(await screen.findByRole('heading', { level: 1, name: 'Extrato futuro' })).toBeInTheDocument();
    expect(mockBuscarExtratoFuturo).toHaveBeenCalledWith(3);

    const novembro = within(screen.getByRole('region', { name: /novembro de 2026/i }));
    expect(novembro.getByText('Saldo previsto')).toBeInTheDocument();
    expect(novembro.getByText(/1\.500,00/)).toBeInTheDocument();
    expect(novembro.getByText('Total de débitos')).toBeInTheDocument();
    expect(novembro.getByText(/1\.400,00/)).toBeInTheDocument();
    expect(novembro.getByText('Total de créditos')).toBeInTheDocument();
    expect(novembro.getByText(/320,00/)).toBeInTheDocument();
    expect(novembro.getByText('Nubank')).toBeInTheDocument();
    expect(novembro.getByText('10/11/2026')).toBeInTheDocument();
    expect(novembro.getByText(/900,00/)).toBeInTheDocument();
    expect(novembro.getByText('Aberta')).toBeInTheDocument();
    expect(novembro.getByText('Aluguel')).toBeInTheDocument();
    expect(novembro.getByText('05/11/2026')).toBeInTheDocument();

    expect(screen.getByRole('region', { name: /dezembro de 2026/i })).toBeInTheDocument();
    expect(screen.getByRole('region', { name: /janeiro de 2027/i })).toBeInTheDocument();
  });

  it('mostra estado vazio quando nao ha compromissos futuros', async () => {
    mockBuscarExtratoFuturo.mockResolvedValue([]);

    renderizar();

    expect(await screen.findByText('Nenhum compromisso futuro')).toBeInTheDocument();
    expect(screen.queryByText('Saldo previsto')).not.toBeInTheDocument();
  });

  it('mostra o carregamento enquanto o extrato nao chega', async () => {
    mockBuscarExtratoFuturo.mockReturnValue(new Promise(() => undefined));

    renderizar();

    expect(await screen.findByText('Carregando extrato futuro')).toBeInTheDocument();
  });

  it('mostra erro quando a API falha', async () => {
    mockBuscarExtratoFuturo.mockRejectedValue(new Error('falha'));

    renderizar();

    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível carregar o extrato futuro.');
  });
});
