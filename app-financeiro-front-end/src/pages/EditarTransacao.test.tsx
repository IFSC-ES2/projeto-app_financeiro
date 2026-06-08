import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import EditarTransacao from './EditarTransacao';
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
    editarTransacao: vi.fn(),
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
  { contaId: 'conta-1', nome: 'NuConta', banco: 'Nubank', tipoConta: 'CORRENTE' },
];

const mockCategorias: api.CategoriaResponse[] = [
  { categoriaId: 'cat-1', nome: 'Alimentação', padrao: true },
];

const transacaoEdicao: api.TransacaoResponse = {
  transacaoId: 'tx-1',
  valor: 80,
  data: '2026-06-02',
  descricao: 'Supermercado',
  tipoTransacao: 'DEBITO',
  formaPagamento: 'PIX',
  categoriaId: 'cat-1',
  contaId: 'conta-1',
  categorizada: true,
};

const renderEditarTransacao = (transacao?: api.TransacaoResponse) =>
  render(
    <MemoryRouter
      initialEntries={[
        transacao
          ? {
              pathname: `/transacoes/${transacao.transacaoId}/editar`,
              state: { transacao },
            }
          : { pathname: '/transacoes/tx-1/editar' },
      ]}
    >
      <Routes>
        <Route path="/transacoes/:transacaoId/editar" element={<EditarTransacao />} />
        <Route path="/transacoes" element={<span>Lista de transações</span>} />
      </Routes>
    </MemoryRouter>,
  );

const aguardarFormulario = async () => {
  await waitFor(() => {
    expect(screen.queryByText(/Carregando dados da transação/i)).not.toBeInTheDocument();
  });

  expect(screen.getByLabelText(/Valor \*/i)).toBeInTheDocument();
};

describe('Tela de Edição de Transações (Issue #149)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.clearAllTimers();
    mockNavigate.mockClear();
    vi.mocked(api.listarContas).mockReset();
    vi.mocked(api.listarCategorias).mockReset();
    vi.mocked(api.editarTransacao).mockReset();
    vi.mocked(api.listarContas).mockResolvedValue(mockContas);
    vi.mocked(api.listarCategorias).mockResolvedValue(mockCategorias);
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  it('deve exibir mensagem quando a transação não for carregada pelo state da navegação', async () => {
    renderEditarTransacao();

    expect(
      screen.getByText(
        /Não foi possível carregar os dados diretamente por esta URL/i,
      ),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Voltar para transações/i })).toHaveAttribute('href', '/transacoes');
    expect(api.editarTransacao).not.toHaveBeenCalled();
  });

  it('deve carregar os dados da transação nos campos do formulário', async () => {
    renderEditarTransacao(transacaoEdicao);
    await aguardarFormulario();

    expect(screen.getByLabelText(/Valor \*/i)).toHaveValue(80);
    expect(screen.getByLabelText(/Data \*/i)).toHaveValue('2026-06-02');
    expect(screen.getByLabelText(/Descrição/i)).toHaveValue('Supermercado');
    expect(screen.getByLabelText(/Tipo \*/i)).toHaveValue('DEBITO');
    expect(screen.getByLabelText(/Forma de pagamento/i)).toHaveValue('PIX');
    expect(screen.getByRole('combobox', { name: /^categoria$/i })).toHaveValue('cat-1');
    expect(screen.getByLabelText(/Conta \*/i)).toHaveValue('conta-1');
  });

  it('deve exibir loading enquanto carrega contas e categorias', async () => {
    let resolverContas: (valor: api.ContaResponse[]) => void = () => undefined;
    let resolverCategorias: (valor: api.CategoriaResponse[]) => void = () => undefined;

    vi.mocked(api.listarContas).mockImplementationOnce(
      () =>
        new Promise<api.ContaResponse[]>((resolve) => {
          resolverContas = resolve;
        }),
    );
    vi.mocked(api.listarCategorias).mockImplementationOnce(
      () =>
        new Promise<api.CategoriaResponse[]>((resolve) => {
          resolverCategorias = resolve;
        }),
    );

    renderEditarTransacao(transacaoEdicao);

    expect(screen.getByText(/Carregando dados da transação/i)).toBeInTheDocument();

    resolverContas(mockContas);
    resolverCategorias(mockCategorias);

    await aguardarFormulario();
  });

  it('deve validar campos obrigatórios antes de enviar a atualização', async () => {
    renderEditarTransacao(transacaoEdicao);
    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: '' } });
    fireEvent.change(screen.getByLabelText(/Data \*/i), { target: { value: '' } });
    fireEvent.change(screen.getByLabelText(/Conta \*/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar alterações/i }));

    await waitFor(() => {
      expect(screen.getByText('Valor é obrigatório.')).toBeInTheDocument();
      expect(screen.getByText('Data é obrigatória.')).toBeInTheDocument();
      expect(screen.getByText('Conta é obrigatória para esta forma de pagamento.')).toBeInTheDocument();
    });

    expect(api.editarTransacao).not.toHaveBeenCalled();
  });

  it('deve exibir loading enquanto editarTransacao estiver pendente', async () => {
    let resolver: (valor: api.TransacaoResponse) => void = () => undefined;

    vi.mocked(api.editarTransacao).mockImplementationOnce(
      () =>
        new Promise<api.TransacaoResponse>((resolve) => {
          resolver = resolve;
        }),
    );

    renderEditarTransacao(transacaoEdicao);
    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: '95' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar alterações/i }));

    await waitFor(() => {
      expect(screen.getByText('Salvando...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Salvando/i })).toBeDisabled();
    });

    resolver(transacaoEdicao);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalled();
    });
  });

  it('deve enviar alterações com sucesso e redirecionar para a listagem', async () => {
    vi.mocked(api.editarTransacao).mockResolvedValueOnce(transacaoEdicao);

    renderEditarTransacao(transacaoEdicao);
    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: '95.50' } });
    fireEvent.change(screen.getByLabelText(/Descrição/i), { target: { value: 'Mercado atualizado' } });
    fireEvent.change(screen.getByRole('combobox', { name: /^categoria$/i }), { target: { value: 'cat-1' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar alterações/i }));

    await waitFor(() => {
      expect(api.editarTransacao).toHaveBeenCalledWith('tx-1', {
        valor: 95.5,
        data: '2026-06-02',
        descricao: 'Mercado atualizado',
        tipoTransacao: 'DEBITO',
        formaPagamento: 'PIX',
        categoriaId: 'cat-1',
        contaId: 'conta-1',
      });

      expect(mockNavigate).toHaveBeenCalledWith('/transacoes', {
        replace: true,
        state: { mensagem: 'Transação atualizada com sucesso.' },
      });
    });
  });

  it('deve exibir erro quando a atualização falhar', async () => {
    vi.mocked(api.editarTransacao).mockRejectedValueOnce(new Error('Falha na API'));

    renderEditarTransacao(transacaoEdicao);
    await aguardarFormulario();

    fireEvent.click(screen.getByRole('button', { name: /Salvar alterações/i }));

    await waitFor(() => {
      expect(screen.getByText('Não foi possível atualizar a transação.')).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('deve voltar para a listagem ao cancelar sem persistir alterações', async () => {
    renderEditarTransacao(transacaoEdicao);
    await aguardarFormulario();

    fireEvent.change(screen.getByLabelText(/Valor \*/i), { target: { value: '999' } });
    fireEvent.click(screen.getByRole('link', { name: /Cancelar/i }));

    expect(await screen.findByText('Lista de transações')).toBeInTheDocument();
    expect(api.editarTransacao).not.toHaveBeenCalled();
  });
});
