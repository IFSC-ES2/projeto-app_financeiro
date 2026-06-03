import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import ImportarExtrato from './ImportarExtrato';
import type { ContaResponse, ImportacaoResponse } from '../services/api';

const contaPrincipal: ContaResponse = {
  contaId: 'conta-1',
  nome: 'Conta Corrente',
  tipoConta: 'CORRENTE',
  banco: 'Nubank',
};

const contaSecundaria: ContaResponse = {
  contaId: 'conta-2',
  nome: 'Poupança',
  tipoConta: 'POUPANCA',
  banco: 'Itaú',
};

const importacaoConcluida: ImportacaoResponse = {
  id: 'importacao-1',
  status: 'CONCLUIDO',
  sucessos: 8,
  falhas: 1,
  importadoEm: '2026-06-01T12:00:00Z',
};

const authState = vi.hoisted(() => ({
  estaAutenticado: true,
  sair: vi.fn(),
}));

const { mockListarContas, mockCriarImportacao, mockConsultarStatusImportacao } = vi.hoisted(() => ({
  mockListarContas: vi.fn(),
  mockCriarImportacao: vi.fn(),
  mockConsultarStatusImportacao: vi.fn(),
}));

vi.mock('../contexts/ContextoAutenticacao', () => ({
  useAutenticacao: () => authState,
}));

vi.mock('../services/api', async () => {
  const actual = await vi.importActual<typeof import('../services/api')>('../services/api');

  return {
    ...actual,
    listarContas: mockListarContas,
    criarImportacao: mockCriarImportacao,
    consultarStatusImportacao: mockConsultarStatusImportacao,
  };
});

const criarArquivo = (nome: string, tipo: string, conteudo = 'conteudo') =>
  new File([conteudo], nome, { type: tipo });

describe('Tela de Importação de Extratos', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useRealTimers();
    authState.estaAutenticado = true;
    mockListarContas.mockReset();
    mockCriarImportacao.mockReset();
    mockConsultarStatusImportacao.mockReset();
    mockListarContas.mockResolvedValue([contaPrincipal, contaSecundaria]);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  const renderizarComponente = () =>
    render(
      <BrowserRouter>
        <ImportarExtrato />
      </BrowserRouter>,
    );

  const aguardarContas = async () => {
    await waitFor(() => {
      expect(screen.getByLabelText(/Conta de destino/i)).toBeInTheDocument();
    });
  };

  const preencherEEnviar = async (arquivo: File) => {
    const user = userEvent.setup();
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    await user.selectOptions(screen.getByLabelText(/Conta de destino/i), contaPrincipal.contaId);
    await user.upload(input, arquivo);
    await user.click(screen.getByRole('button', { name: /Iniciar importação/i }));
  };

  const enviarComFireEvent = (arquivo: File) => {
    fireEvent.change(screen.getByLabelText(/Conta de destino/i), {
      target: { value: contaPrincipal.contaId },
    });

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(input, { target: { files: [arquivo] } });

    const form = input.closest('form');
    expect(form).toBeTruthy();
    fireEvent.submit(form!);
  };

  it('nao deve exibir o formulario quando o usuario nao estiver autenticado', () => {
    authState.estaAutenticado = false;
    renderizarComponente();

    expect(screen.queryByText(/Importar extrato ou NF-e/i)).not.toBeInTheDocument();
    expect(mockListarContas).not.toHaveBeenCalled();
  });

  it('deve renderizar o titulo, a selecao de conta e o botao de importacao', async () => {
    renderizarComponente();
    await aguardarContas();

    expect(screen.getByText(/Importar extrato ou NF-e/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Conta de destino/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Iniciar importação/i })).toBeInTheDocument();
    expect(screen.getByText(/Arraste um arquivo ou clique para selecionar/i)).toBeInTheDocument();
  });

  it('deve exibir erros de validacao ao enviar o formulario sem conta e sem arquivo', async () => {
    renderizarComponente();
    await aguardarContas();

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: /Iniciar importação/i }));

    expect(mockCriarImportacao).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Selecione uma conta de destino.')).toBeInTheDocument();
      expect(screen.getByText('Selecione um arquivo para importar.')).toBeInTheDocument();
    });
  });

  it('deve exibir erro de validacao ao enviar sem arquivo com conta ja selecionada', async () => {
    mockListarContas.mockResolvedValueOnce([contaPrincipal]);
    renderizarComponente();
    await aguardarContas();

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: /Iniciar importação/i }));

    expect(mockCriarImportacao).not.toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByText('Selecione um arquivo para importar.')).toBeInTheDocument();
    });
    expect(screen.queryByText('Selecione uma conta de destino.')).not.toBeInTheDocument();
  });

  it('deve exibir erro ao selecionar arquivo com formato invalido', async () => {
    renderizarComponente();
    await aguardarContas();

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(input, { target: { files: [criarArquivo('extrato.pdf', 'application/pdf')] } });

    await waitFor(() => {
      expect(
        screen.getByText('Formato não suportado. Use arquivos .csv, .xml ou .txt.'),
      ).toBeInTheDocument();
    });
  });

  it.each([
    ['extrato.csv', 'text/csv', 'data,valor\ncompra,10'],
    ['nfe.xml', 'application/xml', '<nfe><id>1</id></nfe>'],
    ['extrato.txt', 'text/plain', 'linha1;linha2'],
  ])('deve aceitar arquivo %s e chamar criarImportacao', async (nome, tipo, conteudo) => {
    mockCriarImportacao.mockResolvedValueOnce(importacaoConcluida);
    renderizarComponente();
    await aguardarContas();

    await preencherEEnviar(criarArquivo(nome, tipo, conteudo));

    await waitFor(() => {
      expect(mockCriarImportacao).toHaveBeenCalledWith(
        expect.objectContaining({ name: nome }),
        contaPrincipal.contaId,
      );
    });

    expect(
      screen.queryByText('Formato não suportado. Use arquivos .csv, .xml ou .txt.'),
    ).not.toBeInTheDocument();
  });

  it('deve exibir loading ao enviar enquanto criarImportacao estiver pendente', async () => {
    let resolverImportacao: (valor: ImportacaoResponse) => void = () => undefined;

    mockCriarImportacao.mockImplementationOnce(
      () =>
        new Promise<ImportacaoResponse>((resolve) => {
          resolverImportacao = resolve;
        }),
    );

    renderizarComponente();
    await aguardarContas();

    await preencherEEnviar(criarArquivo('extrato.csv', 'text/csv'));

    await waitFor(() => {
      expect(screen.getByText('Enviando arquivo...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Enviando arquivo/i })).toBeDisabled();
    });

    resolverImportacao(importacaoConcluida);

    await waitFor(() => {
      expect(screen.getByText('Importação concluída')).toBeInTheDocument();
    });
  });

  it(
    'deve exibir processamento e concluir apos polling de status',
    async () => {
      mockCriarImportacao.mockResolvedValueOnce({
        id: 'importacao-poll',
        status: 'PROCESSANDO',
        sucessos: 0,
        falhas: 0,
        importadoEm: '2026-06-01T12:00:00Z',
      });
      mockConsultarStatusImportacao
        .mockResolvedValueOnce('PROCESSANDO')
        .mockResolvedValueOnce('CONCLUIDO');

      renderizarComponente();
      await aguardarContas();

      vi.useFakeTimers();
      enviarComFireEvent(criarArquivo('extrato.csv', 'text/csv'));

      await act(async () => {
        await Promise.resolve();
      });

      expect(screen.getByText('Processando arquivo')).toBeInTheDocument();

      await act(async () => {
        await vi.advanceTimersByTimeAsync(1500);
      });

      expect(mockConsultarStatusImportacao).toHaveBeenCalledWith('importacao-poll');

      await act(async () => {
        await vi.advanceTimersByTimeAsync(1500);
      });

      expect(screen.getByText('Importação concluída')).toBeInTheDocument();
    },
    10_000,
  );

  it('deve exibir falha quando criarImportacao retornar status ERRO', async () => {
    mockCriarImportacao.mockResolvedValueOnce({
      id: 'importacao-erro',
      status: 'ERRO',
      sucessos: 0,
      falhas: 2,
      importadoEm: '2026-06-01T12:00:00Z',
      mensagemErro: 'Linha 3 inválida no arquivo.',
    });

    renderizarComponente();
    await aguardarContas();
    await preencherEEnviar(criarArquivo('extrato.csv', 'text/csv'));

    await waitFor(() => {
      expect(screen.getByText('Importação falhou')).toBeInTheDocument();
      expect(screen.getByText('Linha 3 inválida no arquivo.')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Nova importação/i })).toBeInTheDocument();
    });
  });

  it(
    'deve exibir falha quando o polling retornar status ERRO',
    async () => {
      mockCriarImportacao.mockResolvedValueOnce({
        id: 'importacao-poll-erro',
        status: 'PROCESSANDO',
        sucessos: 0,
        falhas: 0,
        importadoEm: '2026-06-01T12:00:00Z',
      });
      mockConsultarStatusImportacao.mockResolvedValueOnce('ERRO');

      renderizarComponente();
      await aguardarContas();
      await preencherEEnviar(criarArquivo('extrato.csv', 'text/csv'));

      await waitFor(
        () => {
          expect(mockConsultarStatusImportacao).toHaveBeenCalledWith('importacao-poll-erro');
          expect(screen.getByText('Importação falhou')).toBeInTheDocument();
          expect(
            screen.getByText('Ocorreu um erro durante o processamento do arquivo.'),
          ).toBeInTheDocument();
          expect(screen.getByRole('button', { name: /Nova importação/i })).toBeInTheDocument();
        },
        { timeout: 4000 },
      );
    },
    10_000,
  );

  it('deve exibir o resultado final quando a importacao concluir com sucesso', async () => {
    mockCriarImportacao.mockResolvedValueOnce(importacaoConcluida);
    renderizarComponente();
    await aguardarContas();
    await preencherEEnviar(criarArquivo('extrato.csv', 'text/csv'));

    await waitFor(() => {
      expect(screen.getByText('Importação concluída')).toBeInTheDocument();
      expect(screen.getByText('Seu arquivo foi processado com sucesso.')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('1')).toBeInTheDocument();
      expect(screen.getByText('transações importadas')).toBeInTheDocument();
      expect(screen.getByText('registros descartados')).toBeInTheDocument();
    });
  });

  it('deve exibir mensagem de alerta quando a API de importacao retornar erro', async () => {
    mockCriarImportacao.mockRejectedValueOnce({
      isAxiosError: true,
      response: {
        status: 400,
        data: { erro: 'Arquivo inválido para importação.' },
      },
    });

    renderizarComponente();
    await aguardarContas();
    await preencherEEnviar(criarArquivo('extrato.csv', 'text/csv'));

    await waitFor(() => {
      expect(screen.getByText('Arquivo inválido para importação.')).toBeInTheDocument();
    });
  });

  it('deve exibir mensagem de erro quando nao for possivel carregar as contas', async () => {
    mockListarContas.mockRejectedValueOnce(new Error('falha de rede'));
    renderizarComponente();

    await waitFor(() => {
      expect(screen.getByText('Não foi possível carregar suas contas. Tente novamente.')).toBeInTheDocument();
    });
  });
});
