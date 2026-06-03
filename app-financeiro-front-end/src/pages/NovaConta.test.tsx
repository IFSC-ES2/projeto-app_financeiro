import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
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

describe.sequential('Tela de Cadastro de Nova Conta Bancária (Issue #147)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.clearAllTimers();
    mockRegistrarConta.mockReset();
    mockNavigate.mockClear();
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  const renderizarComponente = () =>
    render(
      <BrowserRouter>
        <NovaConta />
      </BrowserRouter>,
    );

  const preencherCamposMinimos = () => {
    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: 'Conta Principal' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: 'Itaú' } });
  };

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

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

    expect(mockRegistrarConta).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Nome da conta é obrigatório.')).toBeInTheDocument();
    });
  });

  it('deve exibir erro de validação ao tentar submeter sem banco selecionado', async () => {
    renderizarComponente();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: 'Conta sem banco' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

    expect(mockRegistrarConta).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Banco é obrigatório.')).toBeInTheDocument();
    });
  });

  // Testes da issue #147; remover .skip quando NovaConta tiver Saldo inicial (issue #136).
  describe.skip('Saldo inicial', () => {
    it('deve renderizar o campo Saldo inicial com valor padrão zero', () => {
      renderizarComponente();
      expect(screen.getByLabelText(/Saldo inicial/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Saldo inicial/i)).toHaveValue('0');
    });

    it('deve exibir erro ao informar saldo inicial em formato inválido', async () => {
      renderizarComponente();
      preencherCamposMinimos();

      fireEvent.change(screen.getByLabelText(/Saldo inicial/i), { target: { value: 'abc' } });
      fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

      expect(mockRegistrarConta).not.toHaveBeenCalled();

      await waitFor(() => {
        expect(screen.getByText('Saldo inicial inválido. Use apenas números.')).toBeInTheDocument();
      });
    });

    it('deve permitir cadastrar com saldo inicial zerado', async () => {
      mockRegistrarConta.mockResolvedValueOnce(undefined);
      renderizarComponente();
      preencherCamposMinimos();

      fireEvent.change(screen.getByLabelText(/Saldo inicial/i), { target: { value: '0' } });
      fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

      await waitFor(() => {
        expect(mockRegistrarConta).toHaveBeenCalledWith(
          expect.objectContaining({
            nome: 'Conta Principal',
            saldoInicial: 0,
          }),
        );
      });
    });

    it('deve permitir cadastrar com saldo inicial positivo', async () => {
      mockRegistrarConta.mockResolvedValueOnce(undefined);
      renderizarComponente();
      preencherCamposMinimos();

      fireEvent.change(screen.getByLabelText(/Saldo inicial/i), { target: { value: '1250,50' } });
      fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

      await waitFor(() => {
        expect(mockRegistrarConta).toHaveBeenCalledWith(
          expect.objectContaining({
            saldoInicial: 1250.5,
          }),
        );
      });
    });
  });

  it('deve exibir mensagem de alerta geral em tela caso a API de registro falhe', async () => {
    mockRegistrarConta.mockImplementationOnce(() => Promise.reject(new Error('Erro na API')));
    renderizarComponente();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: 'Conta com Falha' } });
    fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

    await waitFor(() => {
      expect(screen.getByText('Não foi possível cadastrar a conta bancária.')).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
    vi.clearAllTimers();
  });

  it('deve exibir loading enquanto registrarConta estiver pendente', async () => {
    let resolver: () => void = () => undefined;

    mockRegistrarConta.mockImplementationOnce(
      () =>
        new Promise<void>((resolve) => {
          resolver = resolve;
        }),
    );

    renderizarComponente();
    preencherCamposMinimos();

    const botao = screen.getByRole('button', { name: /Cadastrar conta/i });
    fireEvent.click(botao);

    await waitFor(() => {
      expect(screen.getByText('Cadastrando...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Cadastrando/i })).toBeDisabled();
    });

    fireEvent.click(botao);
    expect(mockRegistrarConta).toHaveBeenCalledTimes(1);

    resolver();

    await waitFor(() => {
      expect(screen.getByText(/Conta bancária cadastrada com sucesso/i)).toBeInTheDocument();
    });

    vi.clearAllTimers();
  });

  it('deve chamar a função registrarConta com os dados tratados e redirecionar para o dashboard após o sucesso', async () => {
    mockRegistrarConta.mockResolvedValueOnce(undefined);
    renderizarComponente();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: '  Conta Salário Premium  ' } });
    fireEvent.change(screen.getByLabelText(/Tipo de conta/i), { target: { value: 'POUPANCA' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: 'Itaú' } });
    fireEvent.change(screen.getByLabelText(/Descrição/i), { target: { value: 'Fundo de reserva técnica.' } });

    fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

    await waitFor(() => {
      expect(mockRegistrarConta).toHaveBeenCalledWith({
        nome: 'Conta Salário Premium',
        tipoConta: 'POUPANCA',
        banco: 'Itaú',
        descricao: 'Fundo de reserva técnica.',
      });
    });

    await waitFor(() => {
      expect(
        screen.getByText(/Conta bancária cadastrada com sucesso. Redirecionando/i),
      ).toBeInTheDocument();
    });

    await waitFor(
      () => {
        expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true });
      },
      { timeout: 1500 },
    );

    vi.clearAllTimers();
  });
});
