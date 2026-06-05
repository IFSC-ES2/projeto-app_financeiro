import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import NovaConta from './NovaConta';
import { ProvedorAutenticacao } from '../contexts/ContextoAutenticacao';


const { mockListarContas, mockRegistrarConta, mockNavigate } = vi.hoisted(() => ({
  mockListarContas: vi.fn(),
  mockRegistrarConta: vi.fn(),
  mockNavigate: vi.fn(),
}));

vi.mock('../services/api', () => ({
  listarContas: mockListarContas,
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

describe.sequential('Tela de Cadastro de Nova Conta Bancária (Issue #136)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.clearAllTimers();
    mockRegistrarConta.mockReset();
    mockNavigate.mockClear();
    mockListarContas.mockReset();
    mockListarContas.mockResolvedValue([]);
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  const renderizarComponente = () =>
    render(
      <ProvedorAutenticacao>
        <BrowserRouter>
          <NovaConta />
        </BrowserRouter>
      </ProvedorAutenticacao>
    );

  const abrirModalCadastro = async () => {
    renderizarComponente();

    const botaoAdicionar = await screen.findByRole('button', {
      name: /Adicionar nova conta/i,
    });

    fireEvent.click(botaoAdicionar);
  };

  const preencherCamposMinimos = () => {
    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: 'Conta Principal' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: 'Itaú' } });
  };

  it('deve renderizar os campos do formulário com os valores padrões e o botão de submissão', async () => {
    await abrirModalCadastro();

    expect(screen.getByLabelText(/Nome da conta/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Tipo de conta/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Banco/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Descrição/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Cadastrar conta/i })).toBeInTheDocument();

    expect(screen.getByLabelText(/Tipo de conta/i)).toHaveValue('CORRENTE');
  });

  it('deve exibir erro de validação ao tentar submeter o formulário com o nome em branco', async () => {
    await abrirModalCadastro();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

    expect(mockRegistrarConta).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Nome da conta é obrigatório.')).toBeInTheDocument();
    });
  });

  it('deve exibir erro de validação ao tentar submeter sem banco selecionado', async () => {
    await abrirModalCadastro();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), { target: { value: 'Conta sem banco' } });
    fireEvent.change(screen.getByLabelText(/Banco/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

    expect(mockRegistrarConta).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Banco é obrigatório.')).toBeInTheDocument();
    });
  });

  it('deve exibir mensagem de alerta geral em tela caso a API de registro falhe', async () => {
    mockRegistrarConta.mockImplementationOnce(() => Promise.reject(new Error('Erro na API')));
    await abrirModalCadastro();

    fireEvent.change(screen.getByLabelText(/Nome da conta/i), {
      target: { value: 'Conta com Falha' },
    });

    fireEvent.change(screen.getByLabelText(/Banco/i), {
      target: { value: 'Itaú' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Cadastrar conta/i }));

    await waitFor(() => {
      expect(screen.getByText('Não foi possível cadastrar a conta bancária.')).toBeInTheDocument();
    });

    expect(mockRegistrarConta).toHaveBeenCalledTimes(1);
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('deve exibir loading enquanto registrarConta estiver pendente', async () => {
    const contaCriada = {
      contaId: 'conta-loading',
      nome: 'Conta Principal',
      tipoConta: 'CORRENTE' as const,
      banco: 'Itaú',
      descricao: '',
    };
    let resolver: (conta: typeof contaCriada) => void = () => undefined;

    mockRegistrarConta.mockImplementationOnce(
      () =>
        new Promise<typeof contaCriada>((resolve) => {
          resolver = resolve;
        }),
    );

    await abrirModalCadastro();
    preencherCamposMinimos();

    const botao = screen.getByRole('button', { name: /Cadastrar conta/i });
    fireEvent.click(botao);

    await waitFor(() => {
      expect(screen.getByText('Cadastrando...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Cadastrando/i })).toBeDisabled();
    });

    fireEvent.click(botao);
    expect(mockRegistrarConta).toHaveBeenCalledTimes(1);

    resolver(contaCriada);

    await waitFor(() => {
      expect(screen.getByText(/Conta bancária cadastrada com sucesso/i)).toBeInTheDocument();
    });

    vi.clearAllTimers();
  });

  it('deve chamar a função registrarConta com os dados tratados e atualizar a listagem após o sucesso', async () => {
    mockRegistrarConta.mockResolvedValueOnce({
      contaId: 'conta-1',
      nome: 'Conta Salário Premium',
      tipoConta: 'POUPANCA',
      banco: 'Itaú',
      descricao: 'Fundo de reserva técnica.',
    });
    await abrirModalCadastro();

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
        screen.getByText(/Conta bancária cadastrada com sucesso/i),
      ).toBeInTheDocument();
    });

    await waitFor(() => {
      expect(screen.getByText('Conta Salário Premium')).toBeInTheDocument();
    });

    vi.clearAllTimers();
  });
});
