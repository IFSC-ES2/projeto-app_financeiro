import axios from 'axios';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
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

const criarArquivoCsv = (conteudo = 'data,valor\ncompra,10') =>
  new File([conteudo], 'extrato.csv', { type: 'text/csv' });

describe('Tela de Importação de Extratos', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authState.estaAutenticado = true;
    mockListarContas.mockResolvedValue([contaPrincipal, contaSecundaria]);
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

  const selecionarArquivo = async (arquivo: File) => {
    const input = document.querySelector('input[type="file"]');
    expect(input).toBeTruthy();

    const user = userEvent.setup();
    await user.upload(input as HTMLInputElement, arquivo);
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
    const arquivoInvalido = new File(['dados'], 'extrato.pdf', { type: 'application/pdf' });
    fireEvent.change(input, { target: { files: [arquivoInvalido] } });

    await waitFor(() => {
      expect(
        screen.getByText('Formato não suportado. Use arquivos .csv, .xml ou .txt.'),
      ).toBeInTheDocument();
    });
  });

  it('deve chamar criarImportacao e exibir resultado quando a importacao concluir', async () => {
    const resultado: ImportacaoResponse = {
      id: 'importacao-1',
      status: 'CONCLUIDO',
      sucessos: 8,
      falhas: 1,
      importadoEm: '2026-06-01T12:00:00Z',
    };

    mockCriarImportacao.mockResolvedValueOnce(resultado);
    renderizarComponente();
    await aguardarContas();

    const user = userEvent.setup();
    await user.selectOptions(screen.getByLabelText(/Conta de destino/i), contaPrincipal.contaId);
    await selecionarArquivo(criarArquivoCsv());
    await user.click(screen.getByRole('button', { name: /Iniciar importação/i }));

    await waitFor(() => {
      expect(mockCriarImportacao).toHaveBeenCalledWith(expect.any(File), contaPrincipal.contaId);
      expect(screen.getByText('Importação concluída')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('1')).toBeInTheDocument();
    });
  });

  it('deve exibir mensagem de alerta quando a API de importacao retornar erro', async () => {
    mockCriarImportacao.mockRejectedValueOnce(
      new axios.AxiosError('Erro', 'ERR_BAD_REQUEST', undefined, undefined, {
        status: 400,
        data: { erro: 'Arquivo inválido para importação.' },
        statusText: 'Bad Request',
        headers: {},
        config: {},
      }),
    );

    renderizarComponente();
    await aguardarContas();

    const user = userEvent.setup();
    await user.selectOptions(screen.getByLabelText(/Conta de destino/i), contaPrincipal.contaId);
    await selecionarArquivo(criarArquivoCsv());
    await user.click(screen.getByRole('button', { name: /Iniciar importação/i }));

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
