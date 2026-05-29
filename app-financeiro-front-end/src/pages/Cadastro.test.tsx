import { render, screen } from '@testing-library/react';
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
});