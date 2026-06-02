import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import NovaTransacao from './NovaTransacao';
import * as api from '../services/api';

vi.mock('../hooks/useAutenticacao', () => {
  return {
    useAutenticacao: () => ({
      usuario: { id: 'user-123', nome: 'Usuário Teste', email: 'teste@teste.com' },
      autenticado: true,
      sair: vi.fn(),
    }),
  };
});

vi.mock('../services/api', async (importOriginal) => {
  const original = await importOriginal<typeof import('../services/api')>();
  return {
    ...original,
    listarContas: vi.fn(),
    listarCategorias: vi.fn(),
    registrarTransacaoManual: vi.fn(),
  };
});

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const original = await importOriginal<typeof import('react-router-dom')>();
  return {
    ...original,
    useNavigate: () => mockNavigate,
  };
});

const mockContas = [
  { contaId: '1', nome: 'NuConta', banco: 'Nubank' },
];

const mockCategorias = [
  { categoriaId: 'cat-1', nome: 'Alimentação' },
];

describe('Página NovaTransacao', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.listarContas).mockResolvedValue(mockContas);
    vi.mocked(api.listarCategorias).mockResolvedValue(mockCategorias);
  });

  it('deve exibir o estado de carregamento e depois renderizar os inputs do formulário', async () => {
    render(
      <MemoryRouter>
        <NovaTransacao />
      </MemoryRouter>
    );

    expect(screen.getByText(/Carregando dados de apoio.../i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText(/Carregando dados de apoio.../i)).not.toBeInTheDocument();
    });

    expect(screen.getByLabelText(/Valor \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Data \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Descrição/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Tipo \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Forma de pagamento/i)).toBeInTheDocument();
    expect(screen.getByRole('combobox', { name: /^categoria$/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/Conta \*/i)).toBeInTheDocument();
  });

  it('deve exibir erros de validação ao tentar submeter o formulário limpo', async () => {
    render(
      <MemoryRouter>
        <NovaTransacao />
      </MemoryRouter>
    );

    await screen.findByText(/Dados da transação/i);

    const botaoSalvar = screen.getByRole('button', { name: /Salvar transação/i });
    fireEvent.click(botaoSalvar);

    await waitFor(() => {
      expect(screen.getByText('Valor é obrigatório.')).toBeInTheDocument();
    });
  });

  it('deve exibir erro se o valor inserido for igual ou menor a zero', async () => {
    render(
      <MemoryRouter>
        <NovaTransacao />
      </MemoryRouter>
    );

    await screen.findByText(/Dados da transação/i);

    const inputValor = screen.getByLabelText(/Valor \*/i);
    fireEvent.change(inputValor, { target: { value: '-5' } });

    const botaoSalvar = screen.getByRole('button', { name: /Salvar transação/i });
    fireEvent.click(botaoSalvar);

    await waitFor(() => {
      expect(screen.getByText('Informe um valor maior que zero.')).toBeInTheDocument();
    });
  });

  it('deve enviar o formulário com sucesso e redirecionar o usuário', async () => {
    vi.mocked(api.registrarTransacaoManual).mockResolvedValue({ id: 'transacao-123' });

    render(
      <MemoryRouter>
        <NovaTransacao />
      </MemoryRouter>
    );

    await screen.findByText(/Dados da transação/i);

    fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: '150.50' } });
    fireEvent.change(screen.getByLabelText(/Descrição/i), { target: { value: 'Compras do mês' } });
    fireEvent.change(screen.getByRole('combobox', { name: /^categoria$/i }), { target: { value: 'cat-1' } });
    fireEvent.change(screen.getByLabelText(/Conta \*/i), { target: { value: '1' } });

    const botaoSalvar = screen.getByRole('button', { name: /Salvar transação/i });
    fireEvent.click(botaoSalvar);

    await waitFor(() => {
      expect(api.registrarTransacaoManual).toHaveBeenCalledWith({
        valor: 150.5,
        data: expect.any(String),
        descricao: 'Compras do mês',
        tipoTransacao: 'DEBITO',
        formaPagamento: 'PIX',
        categoriaId: 'cat-1',
        contaId: '1',
      });
      
      expect(mockNavigate).toHaveBeenCalledWith('/transacoes', {
        replace: true,
        state: {
          mensagem: 'Transação registrada com sucesso.',
          transacaoCriada: { id: 'transacao-123' },
        },
      });
    });
  });

  it('deve desabilitar o campo Conta se a forma de pagamento selecionada for DINHEIRO', async () => {
    render(
      <MemoryRouter>
        <NovaTransacao />
      </MemoryRouter>
    );

    await screen.findByText(/Dados da transação/i);

    const selectFormaPagamento = screen.getByLabelText(/Forma de pagamento/i);
    const selectConta = screen.getByLabelText(/Conta/i);

    fireEvent.change(selectFormaPagamento, { target: { value: 'DINHEIRO' } });

    expect(selectConta).toBeDisabled();
    expect(screen.getByText('Conta automática em dinheiro')).toBeInTheDocument();
  });
});