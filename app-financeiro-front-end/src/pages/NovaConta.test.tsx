import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import NovaConta from './NovaConta';

const { mockRegistrarConta, mockNavigate } = vi.hoisted(() => ({
  mockRegistrarConta: vi.fn(),
  mockNavigate: vi.fn(),
}));

vi.mock('../services/api', () => ({
  registrarConta: mockRegistrarConta,
  obterMensagemErroApi: vi.fn((_err: unknown, fallback: string) => fallback),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Tela de Cadastro de Nova Conta Bancária (Issue #147)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderizarComponente = () =>
    render(
      <BrowserRouter>
        <NovaConta />
      </BrowserRouter>
    );

  it('deve renderizar os campos do formulário com os valores padrões e o botão de submissão', () => {
    renderizarComponente();

    expect(screen.getByLabelText(/Nome da conta/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Tipo de conta/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Banco/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Descrição/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Cadastrar conta/i })).toBeInTheDocument();

    expect(screen.getByLabelText(/Tipo de conta/i)).toHaveValue('CORRENTE');
    expect(screen.getByLabelText(/Banco/i)).toHaveValue('Nubank');
  });

  it('deve exibir erro de validação ao tentar submeter o formulário com o nome em branco', async () => {
    renderizarComponente();

    const inputNome = screen.getByLabelText(/Nome da conta/i);
    fireEvent.change(inputNome, { target: { value: '' } });

    const botaoSubmeter = screen.getByRole('button', { name: /Cadastrar conta/i });
    fireEvent.click(botaoSubmeter);

    expect(mockRegistrarConta).not.toHaveBeenCalled();
    
    await waitFor(() => {
      expect(screen.getByText('Nome da conta é obrigatório.')).toBeInTheDocument();
    });
  });

  it('deve chamar a função registrarConta com os dados tratados e redirecionar para o dashboard após o sucesso', async () => {
    mockRegistrarConta.mockResolvedValueOnce(undefined);
    renderizarComponente();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: '  Conta Salário Premium  ' } });
    fireEvent.change(screen.getByLabelText(/Tipo de conta/i), { target: { value: 'POUPANCA' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: 'Itaú' } });
    fireEvent.change(screen.getByLabelText(/Descrição/i), { target: { value: 'Fundo de reserva técnica.' } });

    const botaoSubmeter = screen.getByRole('button', { name: /Cadastrar conta/i });
    fireEvent.click(botaoSubmeter);

    expect(mockRegistrarConta).toHaveBeenCalledWith({
      nome: 'Conta Salário Premium',
      tipoConta: 'POUPANCA',
      banco: 'Itaú',
      descricao: 'Fundo de reserva técnica.',
    });

    await waitFor(() => {
      expect(
        screen.getByText(/Conta bancária cadastrada com sucesso. Redirecionando/i)
      ).toBeInTheDocument();
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true });
    }, { timeout: 1500 });
  });

  it('deve exibir mensagem de alerta geral em tela caso a API de registro falhe', async () => {
    mockRegistrarConta.mockRejectedValueOnce(new Error('Erro na API'));
    renderizarComponente();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: 'Conta com Falha' } });

    const botaoSubmeter = screen.getByRole('button', { name: /Cadastrar conta/i });
    fireEvent.click(botaoSubmeter);

    await waitFor(() => {
      expect(screen.getByText('Não foi possível cadastrar a conta bancária.')).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });
});