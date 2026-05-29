import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import Cadastro from './Cadastro';

const { mockCadastrar, mockNavigate } = vi.hoisted(() => ({
  mockCadastrar: vi.fn(),
  mockNavigate: vi.fn(),
}));

vi.mock('../contexts/ContextoAutenticacao', () => ({
  useAutenticacao: () => ({
    cadastrar: mockCadastrar,
  }),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Tela de Cadastro', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderizarComponente = () =>
    render(
      <BrowserRouter>
        <Cadastro />
      </BrowserRouter>
    );

  it('deve renderizar os campos do formulario e o botao de cadastrar', () => {
    renderizarComponente();

    expect(screen.getByLabelText(/Nome completo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/E-mail/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/CPF/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Senha$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Confirmar senha/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Cadastrar/i })).toBeInTheDocument();
  });

  it('deve exibir erros de validacao ao tentar submeter o formulario vazio', async () => {
    renderizarComponente();
    const user = userEvent.setup();

    const botaoCadastrar = screen.getByRole('button', { name: /Cadastrar/i });
    await user.click(botaoCadastrar);

    expect(mockCadastrar).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Nome é obrigatório.')).toBeInTheDocument();
      expect(screen.getByText('E-mail é obrigatório.')).toBeInTheDocument();
      expect(screen.getByText('CPF é obrigatório.')).toBeInTheDocument();
      expect(screen.getByText('Senha é obrigatória.')).toBeInTheDocument();
    });
  });

  it('deve exibir erro de validacao quando as senhas nao coincidem', async () => {
    renderizarComponente();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Nome completo/i), 'Dev de Sucesso');
    await user.type(screen.getByLabelText(/E-mail/i), 'dev@teste.com');
    await user.type(screen.getByLabelText(/CPF/i), '52998224725');
    await user.type(screen.getByLabelText(/^Senha$/i), 'senha123');
    await user.type(screen.getByLabelText(/Confirmar senha/i), 'senhaDiferente');

    const botaoCadastrar = screen.getByRole('button', { name: /Cadastrar/i });
    await user.click(botaoCadastrar);

    expect(mockCadastrar).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('As senhas não coincidem.')).toBeInTheDocument();
    });
  });

  it('deve chamar a funcao cadastrar e redirecionar para /contas/nova ao preencher dados validos', async () => {
    mockCadastrar.mockResolvedValueOnce(undefined);
    renderizarComponente();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Nome completo/i), 'Fulano de Tal');
    await user.type(screen.getByLabelText(/E-mail/i), 'fulano@teste.com');
    await user.type(screen.getByLabelText(/CPF/i), '529.982.247-25');
    await user.type(screen.getByLabelText(/^Senha$/i), 'senhaValida123');
    await user.type(screen.getByLabelText(/Confirmar senha/i), 'senhaValida123');

    const botaoCadastrar = screen.getByRole('button', { name: /Cadastrar/i });
    await user.click(botaoCadastrar);

    await waitFor(() => {
      expect(mockCadastrar).toHaveBeenCalledWith(
        'Fulano de Tal',
        'fulano@teste.com',
        'senhaValida123',
        '52998224725'
      );
      expect(mockNavigate).toHaveBeenCalledWith('/contas/nova');
    });
  });

  it('deve exibir mensagem de alerta na tela caso a API de cadastro retorne erro', async () => {
    mockCadastrar.mockRejectedValueOnce({
      response: {
        status: 409,
        data: { erro: 'E-mail já cadastrado no sistema.' },
      },
    });

    renderizarComponente();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Nome completo/i), 'Fulano Repetido');
    await user.type(screen.getByLabelText(/E-mail/i), 'jaexiste@teste.com');
    await user.type(screen.getByLabelText(/CPF/i), '52998224725');
    await user.type(screen.getByLabelText(/^Senha$/i), 'senha123');
    await user.type(screen.getByLabelText(/Confirmar senha/i), 'senha123');

    const botaoCadastrar = screen.getByRole('button', { name: /Cadastrar/i });
    await user.click(botaoCadastrar);

    await waitFor(() => {
      expect(screen.getByText('E-mail já cadastrado no sistema.')).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });
});