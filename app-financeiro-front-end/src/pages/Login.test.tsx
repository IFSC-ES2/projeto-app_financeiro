import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import Login from './Login';

const { mockLogin, mockNavigate } = vi.hoisted(() => ({
  mockLogin: vi.fn(),
  mockNavigate: vi.fn(),
}));

vi.mock('../contexts/ContextoAutenticacao', () => ({
  useAutenticacao: () => ({
    login: mockLogin,
  }),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');

  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Tela de Login', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderizarComponente = () =>
    render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>,
    );

  it('deve renderizar os campos de e-mail, senha e o botão de entrar', () => {
    renderizarComponente();

    expect(screen.getByLabelText(/Usuário ou e-mail/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Senha/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument();
  });

  it('deve exibir mensagens de erro de validação ao enviar o formulário vazio', async () => {
    renderizarComponente();

    const user = userEvent.setup();
    const botaoEntrar = screen.getByRole('button', { name: /entrar/i });

    await user.click(botaoEntrar);

    expect(mockLogin).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('E-mail é obrigatório.')).toBeInTheDocument();
      expect(screen.getByText('Senha é obrigatória.')).toBeInTheDocument();
    });
  });

  it('deve chamar a função login e redirecionar para o dashboard ao preencher dados válidos', async () => {
    renderizarComponente();

    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Usuário ou e-mail/i), 'teste@email.com');
    await user.type(screen.getByLabelText(/Senha/i), 'senha123');
    await user.click(screen.getByRole('button', { name: /entrar/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('teste@email.com', 'senha123');
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('deve exibir mensagem de alerta na tela caso a API de login retorne erro', async () => {
    mockLogin.mockRejectedValueOnce({
      response: {
        data: {
          erro: 'Credenciais inválidas. Verifique seu e-mail e senha.',
        },
      },
    });

    renderizarComponente();

    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Usuário ou e-mail/i), 'errado@email.com');
    await user.type(screen.getByLabelText(/Senha/i), 'senhaerrada');
    await user.click(screen.getByRole('button', { name: /entrar/i }));

    await waitFor(() => {
      expect(screen.getByText('Credenciais inválidas. Verifique seu e-mail e senha.')).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });
});