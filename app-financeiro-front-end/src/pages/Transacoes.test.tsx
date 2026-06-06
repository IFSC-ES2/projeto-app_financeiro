import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Transacoes from './Transacoes';
import * as api from '../services/api';

const mockNavigate = vi.fn();

vi.mock('../hooks/useAutenticacao', () => ({
  useAutenticacao: () => ({
    usuario: { id: 'user-123', nome: 'Usuário Teste', email: 'teste@teste.com' },
    autenticado: true,
    sair: vi.fn(),
  }),
}));

vi.mock('../components/resumo/ResumoFormaPagamento', () => ({
  default: () => null,
}));

vi.mock('../services/api', async (importOriginal) => {
  const original = await importOriginal<typeof import('../services/api')>();
  return {
    ...original,
    listarContas: vi.fn(),
    listarCategorias: vi.fn(),
    listarTransacoes: vi.fn(),
    categorizarTransacao: vi.fn(),
    obterMensagemErroApi: vi.fn((_err: unknown, fallback: string) => fallback),
  };
});

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

const paginaVazia = (): api.PaginaResponse<api.TransacaoResponse> => ({
  conteudo: [],
  pagina: 0,
  tamanho: 20,
  totalElementos: 0,
  totalPaginas: 0,
  primeira: true,
  ultima: true,
});

const paginaComConteudo = (
  conteudo: api.TransacaoResponse[],
  overrides: Partial<api.PaginaResponse<api.TransacaoResponse>> = {},
): api.PaginaResponse<api.TransacaoResponse> => ({
  conteudo,
  pagina: 0,
  tamanho: 20,
  totalElementos: conteudo.length,
  totalPaginas: 1,
  primeira: true,
  ultima: true,
  ...overrides,
});

const transacaoDespesa: api.TransacaoResponse = {
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

const transacaoReceita: api.TransacaoResponse = {
  transacaoId: 'tx-2',
  valor: 200,
  data: '2026-06-02',
  descricao: 'Salário',
  tipoTransacao: 'CREDITO',
  formaPagamento: 'TED_DOC',
  categoriaId: null,
  contaId: 'conta-1',
  categorizada: false,
};

const renderTransacoes = (
  initialEntry: string | { pathname: string; state?: { mensagem?: string } } = '/transacoes',
) =>
  render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Transacoes />
    </MemoryRouter>,
  );

const aguardarCarregamento = async () => {
  await waitFor(() => {
    expect(screen.queryByText(/Carregando transações/i)).not.toBeInTheDocument();
  });
};

describe('Tela de listagem de Transações (Issue #155)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();
    vi.mocked(api.listarContas).mockReset();
    vi.mocked(api.listarCategorias).mockReset();
    vi.mocked(api.listarTransacoes).mockReset();
    vi.mocked(api.categorizarTransacao).mockReset();
    vi.mocked(api.listarContas).mockResolvedValue(mockContas);
    vi.mocked(api.listarCategorias).mockResolvedValue(mockCategorias);
    vi.mocked(api.listarTransacoes).mockResolvedValue(paginaVazia());
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  it('deve exibir loading e depois os filtros e painel de movimentações', async () => {
    let resolver: (value: api.PaginaResponse<api.TransacaoResponse>) => void = () => undefined;
    vi.mocked(api.listarTransacoes).mockImplementationOnce(
      () =>
        new Promise<api.PaginaResponse<api.TransacaoResponse>>((resolve) => {
          resolver = resolve;
        }),
    );

    renderTransacoes();

    expect(screen.getByText(/Carregando transações/i)).toBeInTheDocument();

    resolver(paginaVazia());

    await aguardarCarregamento();

    expect(screen.getByLabelText(/Filtros de transações/i)).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /Movimentações/i })).toBeInTheDocument();
  });

  it('deve exibir estado vazio quando não houver transações para os filtros', async () => {
    renderTransacoes();
    await aguardarCarregamento();

    expect(screen.getByText(/Nenhuma transação encontrada/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Criar transação/i })).toHaveAttribute('href', '/transacoes/nova');
  });

  it('deve listar transações da página e exibir resumo da página atual', async () => {
    vi.mocked(api.listarTransacoes).mockResolvedValueOnce(
      paginaComConteudo([transacaoDespesa, transacaoReceita], { totalElementos: 2 }),
    );

    renderTransacoes();
    await aguardarCarregamento();

    expect(screen.getByText('Supermercado')).toBeInTheDocument();
    expect(screen.getByText('Salário')).toBeInTheDocument();
    expect(screen.getByText('Pendente')).toBeInTheDocument();

    const resumo = screen.getByLabelText(/Resumo das transações \(página atual\)/i);
    const normalizarMoeda = (texto: string | null) => texto?.replace(/\u00a0/g, ' ') ?? '';
    const valoresResumo = Array.from(resumo.querySelectorAll('strong')).map((el) =>
      normalizarMoeda(el.textContent),
    );

    expect(valoresResumo).toEqual(expect.arrayContaining(['2', 'R$ 200,00', 'R$ 80,00', 'R$ 120,00']));
  });

  it('deve exibir mensagem de sucesso recebida pelo state da navegação', async () => {
    renderTransacoes({
      pathname: '/transacoes',
      state: { mensagem: 'Transação registrada com sucesso.' },
    });

    await waitFor(() => {
      expect(screen.getByText('Transação registrada com sucesso.')).toBeInTheDocument();
    });

    expect(mockNavigate).toHaveBeenCalledWith('/transacoes', { replace: true, state: null });
  });

  it('deve exibir erro quando listarTransacoes falhar', async () => {
    vi.mocked(api.listarTransacoes).mockRejectedValueOnce(new Error('Falha na API'));

    renderTransacoes();
    await aguardarCarregamento();

    expect(screen.getByText('Não foi possível carregar as transações.')).toBeInTheDocument();
  });

  it('deve exibir erro quando o carregamento de contas e categorias falhar', async () => {
    vi.mocked(api.listarContas).mockRejectedValueOnce(new Error('Falha nas contas'));

    renderTransacoes();
    await aguardarCarregamento();

    expect(screen.getByText('Não foi possível carregar contas e categorias.')).toBeInTheDocument();
  });

  it('deve exibir link para nova transação no cabeçalho', async () => {
    renderTransacoes();
    await aguardarCarregamento();

    expect(screen.getByRole('link', { name: /Nova transação/i })).toHaveAttribute('href', '/transacoes/nova');
  });

  it('deve solicitar nova listagem no servidor ao alterar o filtro de tipo', async () => {
    vi.mocked(api.listarTransacoes)
      .mockResolvedValueOnce(paginaComConteudo([transacaoDespesa, transacaoReceita]))
      .mockResolvedValueOnce(paginaComConteudo([transacaoReceita]));

    const usuario = userEvent.setup();

    renderTransacoes();
    await aguardarCarregamento();

    const filtroTipo = screen.getByRole('combobox', { name: /^Tipo$/i });
    await usuario.selectOptions(filtroTipo, 'CREDITO');

    await waitFor(() => {
      expect(api.listarTransacoes).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 0, size: 20, tipo: 'CREDITO' }),
      );
    });

    expect(screen.getByText('Salário')).toBeInTheDocument();
    expect(screen.queryByText('Supermercado')).not.toBeInTheDocument();
  });

  it('deve navegar entre páginas e solicitar listagem com page correto', async () => {
    const transacaoPagina1: api.TransacaoResponse = {
      ...transacaoDespesa,
      transacaoId: 'tx-p1',
      descricao: 'Transação página 1',
    };
    const transacaoPagina2: api.TransacaoResponse = {
      ...transacaoDespesa,
      transacaoId: 'tx-p2',
      descricao: 'Transação página 2',
    };

    vi.mocked(api.listarTransacoes)
      .mockResolvedValueOnce(
        paginaComConteudo([transacaoPagina1], {
          pagina: 0,
          totalPaginas: 2,
          totalElementos: 2,
          primeira: true,
          ultima: false,
        }),
      )
      .mockResolvedValueOnce(
        paginaComConteudo([transacaoPagina2], {
          pagina: 1,
          totalPaginas: 2,
          totalElementos: 2,
          primeira: false,
          ultima: true,
        }),
      )
      .mockResolvedValueOnce(
        paginaComConteudo([transacaoPagina1], {
          pagina: 0,
          totalPaginas: 2,
          totalElementos: 2,
          primeira: true,
          ultima: false,
        }),
      );

    const usuario = userEvent.setup();

    renderTransacoes();
    await aguardarCarregamento();

    expect(screen.getByText('Transação página 1')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Próxima/i })).toBeEnabled();
    expect(screen.getByRole('button', { name: /Anterior/i })).toBeDisabled();

    await usuario.click(screen.getByRole('button', { name: /Próxima/i }));

    await waitFor(() => {
      expect(api.listarTransacoes).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 1, size: 20 }),
      );
    });

    await waitFor(() => {
      expect(screen.getByText('Transação página 2')).toBeInTheDocument();
    });

    await usuario.click(screen.getByRole('button', { name: /Anterior/i }));

    await waitFor(() => {
      expect(api.listarTransacoes).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 0, size: 20 }),
      );
    });

    await waitFor(() => {
      expect(screen.getByText('Transação página 1')).toBeInTheDocument();
    });
  });

  it('deve chamar categorizarTransacao ao selecionar categoria na linha', async () => {
    vi.mocked(api.listarTransacoes).mockResolvedValueOnce(paginaComConteudo([transacaoReceita]));
    vi.mocked(api.categorizarTransacao).mockResolvedValueOnce({
      ...transacaoReceita,
      categoriaId: 'cat-1',
      categorizada: true,
    });

    const usuario = userEvent.setup();

    renderTransacoes();
    await aguardarCarregamento();

    const seletorCategoria = screen.getByRole('combobox', {
      name: /Categoria da transação Salário/i,
    });
    await usuario.selectOptions(seletorCategoria, 'cat-1');

    await waitFor(() => {
      expect(api.categorizarTransacao).toHaveBeenCalledWith('tx-2', 'cat-1');
      expect(screen.getByText('Categoria atualizada com sucesso.')).toBeInTheDocument();
    });
  });
});
