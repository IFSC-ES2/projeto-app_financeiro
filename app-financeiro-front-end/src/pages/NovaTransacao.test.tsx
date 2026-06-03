import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import NovaTransacao from './NovaTransacao';
import Transacoes from './Transacoes';
import * as api from '../services/api';

vi.mock('../hooks/useAutenticacao', () => ({
  useAutenticacao: () => ({
    usuario: { id: 'user-123', nome: 'Usuário Teste', email: 'teste@teste.com' },
    autenticado: true,
    sair: vi.fn(),
  }),
}));

vi.mock('../services/api', async (importOriginal) => {
  const original = await importOriginal<typeof import('../services/api')>();
  return {
    ...original,
    listarContas: vi.fn(),
    listarCategorias: vi.fn(),
    listarTransacoes: vi.fn(),
    registrarTransacaoManual: vi.fn(),
    obterMensagemErroApi: vi.fn((_err: unknown, fallback: string) => fallback),
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

const mockContas: api.ContaResponse[] = [
  { contaId: '1', nome: 'NuConta', banco: 'Nubank', tipoConta: 'CORRENTE' },
];

const mockCategorias: api.CategoriaResponse[] = [
  { categoriaId: 'cat-1', nome: 'Alimentação', padrao: false },
];

const mockTransacaoCriada: api.TransacaoResponse = {
  transacaoId: 'transacao-123',
  valor: 150.5,
  data: '2026-06-02',
  descricao: 'Compras do mês',
  tipoTransacao: 'DEBITO',
  formaPagamento: 'PIX',
  categoriaId: 'cat-1',
  contaId: '1',
};

const renderNovaTransacao = () =>
  render(
    <MemoryRouter>
      <NovaTransacao />
    </MemoryRouter>,
  );

const aguardarFormulario = async () => {
  await screen.findByText(/Dados da transação/i);
};

describe.sequential('Página NovaTransacao', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.clearAllTimers();
    mockNavigate.mockClear();
    vi.mocked(api.listarContas).mockReset();
    vi.mocked(api.listarCategorias).mockReset();
    vi.mocked(api.registrarTransacaoManual).mockReset();
    vi.mocked(api.listarTransacoes).mockReset();
    vi.mocked(api.listarContas).mockResolvedValue(mockContas);
    vi.mocked(api.listarCategorias).mockResolvedValue(mockCategorias);
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  it('deve exibir o estado de carregamento e depois renderizar os inputs do formulário', async () => {
    renderNovaTransacao();

    expect(screen.getByText(/Carregando dados de apoio.../i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText(/Carregando dados de apoio.../i)).not.toBeInTheDocument();
    });

    expect(screen.getByLabelText(/Valor \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Data \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Descrição \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Tipo \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Forma de pagamento/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Categoria \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Conta \*/i)).toBeInTheDocument();
  });

  it('deve exibir erros de validação ao tentar submeter o formulário limpo', async () => {
    renderNovaTransacao();
    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Data \*/i), { target: { value: '' } });
    fireEvent.change(screen.getByLabelText(/Conta \*/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar transação/i }));

    await waitFor(() => {
      expect(screen.getByText('Valor é obrigatório.')).toBeInTheDocument();
      expect(screen.getByText('Data é obrigatória.')).toBeInTheDocument();
      expect(screen.getByText('Descrição é obrigatória.')).toBeInTheDocument();
      expect(screen.getByText('Categoria é obrigatória.')).toBeInTheDocument();
      expect(screen.getByText('Conta é obrigatória para esta forma de pagamento.')).toBeInTheDocument();
    });

    expect(api.registrarTransacaoManual).not.toHaveBeenCalled();
  });

  it('deve exibir erro se o valor inserido for igual ou menor a zero', async () => {
    renderNovaTransacao();
    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: '-5' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar transação/i }));

    await waitFor(() => {
      expect(screen.getByText('Informe um valor maior que zero.')).toBeInTheDocument();
    });

    expect(api.registrarTransacaoManual).not.toHaveBeenCalled();
  });

  it.each(['abc', '10,10,10', 'R$ teste', '12..50'])(
    'deve bloquear envio com valor monetario invalido: %s',
    async (valorInvalido) => {
      renderNovaTransacao();
      await aguardarFormulario();

      fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: valorInvalido } });
      fireEvent.change(screen.getByLabelText(/Descrição \*/i), { target: { value: 'Teste' } });
      fireEvent.change(screen.getByLabelText(/Categoria \*/i), { target: { value: 'cat-1' } });
      fireEvent.click(screen.getByRole('button', { name: /Salvar transação/i }));

      await waitFor(() => {
        expect(screen.getByText('Valor inválido. Use apenas números.')).toBeInTheDocument();
      });

      expect(api.registrarTransacaoManual).not.toHaveBeenCalled();
    },
  );

  it('deve navegar para transacoes ao clicar em Cancelar', async () => {
    render(
      <MemoryRouter initialEntries={['/transacoes/nova']}>
        <Routes>
          <Route path="/transacoes/nova" element={<NovaTransacao />} />
          <Route path="/transacoes" element={<span>Lista de transações</span>} />
        </Routes>
      </MemoryRouter>,
    );

    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: '99' } });
    fireEvent.change(screen.getByLabelText(/Descrição \*/i), { target: { value: 'Compra cancelada' } });
    fireEvent.click(screen.getByRole('link', { name: /Cancelar/i }));

    expect(await screen.findByText('Lista de transações')).toBeInTheDocument();
    expect(api.registrarTransacaoManual).not.toHaveBeenCalled();
  });

  it('deve enviar o formulário com sucesso e redirecionar o usuário', async () => {
    vi.mocked(api.registrarTransacaoManual).mockResolvedValueOnce(mockTransacaoCriada);

    renderNovaTransacao();
    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: '150.50' } });
    fireEvent.change(screen.getByLabelText(/Descrição \*/i), { target: { value: 'Compras do mês' } });
    fireEvent.change(screen.getByLabelText(/Categoria \*/i), { target: { value: 'cat-1' } });
    fireEvent.change(screen.getByLabelText(/Conta \*/i), { target: { value: '1' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar transação/i }));

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
          transacaoCriada: mockTransacaoCriada,
        },
      });
    });
  });

  it('deve desabilitar o campo Conta se a forma de pagamento selecionada for DINHEIRO', async () => {
    renderNovaTransacao();
    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Forma de pagamento/i), { target: { value: 'DINHEIRO' } });

    expect(screen.getByLabelText(/Conta/i)).toBeDisabled();
    expect(screen.getByText('Conta automática em dinheiro')).toBeInTheDocument();
  });
});

describe('Mensagem de sucesso na listagem de Transacoes', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.listarContas).mockResolvedValue(mockContas);
    vi.mocked(api.listarCategorias).mockResolvedValue(mockCategorias);
    vi.mocked(api.listarTransacoes).mockResolvedValue([]);
  });

  it(
    'deve exibir a mensagem recebida pelo state da navegacao',
    async () => {
      render(
        <MemoryRouter
          initialEntries={[
            {
              pathname: '/transacoes',
              state: { mensagem: 'Transação registrada com sucesso.' },
            },
          ]}
        >
          <Transacoes />
        </MemoryRouter>,
      );

      expect(screen.getByText('Transação registrada com sucesso.')).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.queryByText(/Carregando transações/i)).not.toBeInTheDocument();
      });
    },
    10_000,
  );
});
