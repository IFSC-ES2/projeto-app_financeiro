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

    expect(screen.getByLabelText(/Nome Completo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Usuário ou e-mail/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Senha$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Confirmar Senha/i)).toBeInTheDocument();
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
      expect(screen.getByText('Senha é obrigatória.')).toBeInTheDocument();
    });
  });

  it('deve exibir erro de validacao quando as senhas nao coincidem', async () => {
    renderizarComponente();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Nome Completo/i), 'Dev de Sucesso');
    await user.type(screen.getByLabelText(/Usuário ou e-mail/i), 'dev@teste.com');
    await user.type(screen.getByLabelText(/^Senha$/i), 'senha123');
    await user.type(screen.getByLabelText(/Confirmar Senha/i), 'senhaDiferente');

    const botaoCadastrar = screen.getByRole('button', { name: /Cadastrar/i });
    await user.click(botaoCadastrar);

    expect(mockCadastrar).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('As senhas não coincidem.')).toBeInTheDocument();
    });
  });
});