import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import PrimeiraConta from './PrimeiraConta';

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

const aguardarRedirecionamentoPendente = () =>
  new Promise<void>((resolve) => {
    setTimeout(resolve, 1000);
  });

describe.sequential('Tela de Primeira Conta (onboarding)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockRegistrarConta.mockReset();
    mockNavigate.mockClear();
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  const renderizarComponente = () =>
    render(
      <BrowserRouter>
        <PrimeiraConta />
      </BrowserRouter>,
    );

  const preencherCamposMinimos = () => {
    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: 'Conta Principal' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: 'Itaú' } });
  };

  it('deve renderizar os campos do formulário com valores padrão', () => {
    renderizarComponente();

    expect(screen.getByLabelText(/Nome da conta/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Banco/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Tipo de conta/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Descrição/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Finalizar cadastro/i })).toBeInTheDocument();

    expect(screen.getByLabelText(/Banco/i)).toHaveValue('Nubank');
    expect(screen.getByLabelText(/Tipo de conta/i)).toHaveValue('CORRENTE');
  });

  it('deve exibir erro de validação ao tentar submeter com o nome em branco', async () => {
    renderizarComponente();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /Finalizar cadastro/i }));

    expect(mockRegistrarConta).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Nome da conta é obrigatório.')).toBeInTheDocument();
    });
  });

  it('deve exibir erro de validação ao tentar submeter sem banco informado', async () => {
    renderizarComponente();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: 'Conta sem banco' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /Finalizar cadastro/i }));

    expect(mockRegistrarConta).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Banco é obrigatório.')).toBeInTheDocument();
    });
  });

  it('deve exibir mensagem de alerta geral em tela caso a API de registro falhe', async () => {
    mockRegistrarConta.mockRejectedValueOnce(new Error('Erro na API'));

    renderizarComponente();
    preencherCamposMinimos();

    fireEvent.click(screen.getByRole('button', { name: /Finalizar cadastro/i }));

    await waitFor(() => {
      expect(screen.getByText('Não foi possível cadastrar a conta bancária.')).toBeInTheDocument();
    });

    expect(mockRegistrarConta).toHaveBeenCalledTimes(1);
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('deve exibir loading enquanto registrarConta estiver pendente', async () => {
    let resolver: (conta: { contaId: string }) => void = () => undefined;

    mockRegistrarConta.mockImplementationOnce(
      () =>
        new Promise<{ contaId: string }>((resolve) => {
          resolver = resolve;
        }),
    );

    renderizarComponente();
    preencherCamposMinimos();

    fireEvent.click(screen.getByRole('button', { name: /Finalizar cadastro/i }));

    await waitFor(() => {
      expect(screen.getByText('Cadastrando...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Cadastrando/i })).toBeDisabled();
    });

    resolver({ contaId: 'conta-1' });

    await waitFor(() => {
      expect(
        screen.getByText(/Conta bancária cadastrada com sucesso. Redirecionando para o Dashboard/i),
      ).toBeInTheDocument();
    });

    await aguardarRedirecionamentoPendente();
    mockNavigate.mockClear();
  });

  it('deve chamar registrarConta com dados tratados e redirecionar para o dashboard', async () => {
    mockRegistrarConta.mockResolvedValueOnce({
      contaId: 'conta-1',
      nome: 'Conta Salário',
      tipoConta: 'POUPANCA',
      banco: 'Itaú',
      descricao: 'Reserva de emergência.',
    });

    renderizarComponente();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: '  Conta Salário  ' } });
    fireEvent.change(screen.getByLabelText(/Tipo de conta/i), { target: { value: 'POUPANCA' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: 'Itaú' } });
    fireEvent.change(screen.getByLabelText(/Descrição/i), { target: { value: 'Reserva de emergência.' } });

    fireEvent.click(screen.getByRole('button', { name: /Finalizar cadastro/i }));

    await waitFor(() => {
      expect(mockRegistrarConta).toHaveBeenCalledWith({
        nome: 'Conta Salário',
        tipoConta: 'POUPANCA',
        banco: 'Itaú',
        descricao: 'Reserva de emergência.',
      });
    });

    await waitFor(() => {
      expect(
        screen.getByText(/Conta bancária cadastrada com sucesso. Redirecionando para o Dashboard/i),
      ).toBeInTheDocument();
    });

    await waitFor(
      () => {
        expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true });
      },
      { timeout: 2000 },
    );
  });
});
