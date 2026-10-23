import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from '@testing-library/react';

import userEvent from '@testing-library/user-event';
import { AxiosError } from 'axios';

import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest';

import { BrowserRouter } from 'react-router-dom';

import ImportarExtrato from './ImportarExtrato';

import type {
  ContaResponse,
  ImportacaoResponse,
} from '../services/api';

const criarErroAxios = (
  status: number,
  mensagem: string,
) =>
  new AxiosError(
    mensagem,
    AxiosError.ERR_BAD_REQUEST,
    undefined,
    undefined,
    {
      status,
      statusText:
        String(status),
      headers: {},
      config:
        {} as never,
      data: {
        erro:
          mensagem,
      },
    },
  );

const contaPrincipal:
  ContaResponse = {
    contaId: 'conta-1',
    nome:
      'Conta Corrente',
    tipoConta:
      'CORRENTE',
    banco: 'Nubank',
  };

const contaSecundaria:
  ContaResponse = {
    contaId: 'conta-2',
    nome: 'Poupança',
    tipoConta:
      'POUPANCA',
    banco: 'Itaú',
  };

const importacaoConcluida:
  ImportacaoResponse = {
    id: 'importacao-1',
    status:
      'CONCLUIDO',
    sucessos: 8,
    falhas: 0,
    ignoradasPorDuplicidade:
      0,
    importadoEm:
      '2026-06-01T12:00:00Z',
    erros: [],
  };

const authState =
  vi.hoisted(() => ({
    estaAutenticado:
      true,
    sair: vi.fn(),
  }));

const {
  mockListarContas,
  mockCriarImportacao,
  mockConsultarStatusImportacao,
} = vi.hoisted(() => ({
  mockListarContas:
    vi.fn(),

  mockCriarImportacao:
    vi.fn(),

  mockConsultarStatusImportacao:
    vi.fn(),
}));

vi.mock(
  '../contexts/ContextoAutenticacao',
  () => ({
    useAutenticacao:
      () => authState,
  }),
);

vi.mock(
  '../services/contaService',
  () => ({
    listarContas:
      mockListarContas,
  }),
);

vi.mock(
  '../services/importacaoService',
  () => ({
    criarImportacao:
      mockCriarImportacao,

    consultarStatusImportacao:
      mockConsultarStatusImportacao,
  }),
);

const criarArquivo = (
  nome: string,
  tipo: string,
  conteudo =
    'conteudo',
) =>
  new File(
    [conteudo],
    nome,
    {
      type: tipo,
    },
  );

describe(
  'Tela de Importação de Extratos',
  () => {
    beforeEach(() => {
      vi.clearAllMocks();
      vi.useRealTimers();

      authState.estaAutenticado =
        true;

      mockListarContas.mockReset();
      mockCriarImportacao.mockReset();
      mockConsultarStatusImportacao.mockReset();

      mockListarContas.mockResolvedValue(
        [
          contaPrincipal,
          contaSecundaria,
        ],
      );
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    const renderizarComponente =
      () =>
        render(
          <BrowserRouter>
            <ImportarExtrato />
          </BrowserRouter>,
        );

    const aguardarContas =
      async () => {
        await waitFor(
          () => {
            expect(
              screen.getByLabelText(
                /Conta de destino/i,
              ),
            ).toBeInTheDocument();

            expect(
              screen.getByRole(
                'option',
                {
                  name:
                    new RegExp(
                      contaPrincipal.nome,
                    ),
                },
              ),
            ).toBeInTheDocument();
          },
        );
      };

    const preencherEEnviar =
      async (
        arquivo: File,
      ) => {
        const user =
          userEvent.setup();

        const input =
          document.querySelector(
            'input[type="file"]',
          ) as HTMLInputElement;

        await user.selectOptions(
          screen.getByLabelText(
            /Conta de destino/i,
          ),
          contaPrincipal.contaId,
        );

        await user.upload(
          input,
          arquivo,
        );

        await user.click(
          screen.getByRole(
            'button',
            {
              name:
                /Iniciar importação/i,
            },
          ),
        );
      };

    const enviarComFireEvent =
      (
        arquivo: File,
      ) => {
        fireEvent.change(
          screen.getByLabelText(
            /Conta de destino/i,
          ),
          {
            target: {
              value:
                contaPrincipal.contaId,
            },
          },
        );

        const input =
          document.querySelector(
            'input[type="file"]',
          ) as HTMLInputElement;

        fireEvent.change(
          input,
          {
            target: {
              files: [
                arquivo,
              ],
            },
          },
        );

        const form =
          input.closest(
            'form',
          );

        expect(
          form,
        ).toBeTruthy();

        fireEvent.submit(
          form!,
        );
      };

    it(
      'nao deve exibir o formulario quando o usuario nao estiver autenticado',
      () => {
        authState.estaAutenticado =
          false;

        renderizarComponente();

        expect(
          screen.queryByText(
            /Importar extrato ou NF-e/i,
          ),
        ).not.toBeInTheDocument();

        expect(
          mockListarContas,
        ).not.toHaveBeenCalled();
      },
    );

    it(
      'deve renderizar o titulo, a selecao de conta e o botao de importacao',
      async () => {
        renderizarComponente();

        await aguardarContas();

        expect(
          screen.getByText(
            /Importar extrato ou NF-e/i,
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            'button',
            {
              name:
                /Iniciar importação/i,
            },
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'deve exibir erros ao enviar sem conta e sem arquivo',
      async () => {
        renderizarComponente();

        await aguardarContas();

        const user =
          userEvent.setup();

        await user.click(
          screen.getByRole(
            'button',
            {
              name:
                /Iniciar importação/i,
            },
          ),
        );

        expect(
          mockCriarImportacao,
        ).not.toHaveBeenCalled();

        expect(
          await screen.findByText(
            'Selecione uma conta de destino.',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Selecione um arquivo para importar.',
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'aceita arquivo válido e conclui com sucesso',
      async () => {
        mockCriarImportacao.mockResolvedValueOnce(
          importacaoConcluida,
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        expect(
          await screen.findByText(
            'Importação concluída',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Seu arquivo foi processado com sucesso.',
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'deve detalhar uma importação parcial',
      async () => {
        mockCriarImportacao.mockResolvedValueOnce(
          {
            ...importacaoConcluida,

            sucessos: 8,
            falhas: 2,

            erros: [
              {
                numeroRegistro:
                  3,
                campo:
                  'valor',
                mensagem:
                  'Valor monetário inválido.',
              },
              {
                numeroRegistro:
                  7,
                campo:
                  'data',
                mensagem:
                  'Data inválida. Verifique o formato informado.',
              },
            ],
          },
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        expect(
          await screen.findByText(
            'Importação concluída com ressalvas',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Registros não importados',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Valor monetário inválido.',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Data inválida. Verifique o formato informado.',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            'cell',
            {
              name:
                'valor',
            },
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            'cell',
            {
              name:
                'data',
            },
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'deve mostrar orientação e detalhes quando nenhum registro é válido',
      async () => {
        mockCriarImportacao.mockResolvedValueOnce(
          {
            id:
              'importacao-invalida',
            status: 'ERRO',
            sucessos: 0,
            falhas: 2,
            ignoradasPorDuplicidade:
              0,
            importadoEm:
              '2026-06-01T12:00:00Z',

            mensagemErro:
              'Nenhuma transação válida foi encontrada no arquivo. Revise os registros rejeitados e verifique os campos obrigatórios de data, descrição e valor.',

            erros: [
              {
                numeroRegistro:
                  1,
                campo:
                  'data',
                mensagem:
                  'Data inválida.',
              },
              {
                numeroRegistro:
                  2,
                campo:
                  'valor',
                mensagem:
                  'Valor monetário inválido.',
              },
            ],
          },
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        expect(
          await screen.findByText(
            'Importação falhou',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            /Nenhuma transação válida foi encontrada/i,
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Registros não importados',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Data inválida.',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Valor monetário inválido.',
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'deve informar transações ignoradas por duplicidade',
      async () => {
        mockCriarImportacao.mockResolvedValueOnce(
          {
            ...importacaoConcluida,
            sucessos: 0,
            ignoradasPorDuplicidade:
              10,
          },
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        expect(
          await screen.findByText(
            'Ignoradas',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            '10',
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'deve exibir loading ao enviar enquanto criarImportacao estiver pendente',
      async () => {
        let resolverImportacao:
          (
            valor:
              ImportacaoResponse,
          ) => void =
          () => undefined;

        mockCriarImportacao.mockImplementationOnce(
          () =>
            new Promise<ImportacaoResponse>(
              (
                resolve,
              ) => {
                resolverImportacao =
                  resolve;
              },
            ),
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        expect(
          await screen.findByText(
            'Enviando arquivo...',
          ),
        ).toBeInTheDocument();

        resolverImportacao(
          importacaoConcluida,
        );

        expect(
          await screen.findByText(
            'Importação concluída',
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'deve exibir processamento e concluir apos polling',
      async () => {
        mockCriarImportacao.mockResolvedValueOnce(
          {
            id:
              'importacao-poll',
            status:
              'PROCESSANDO',
            sucessos: 0,
            falhas: 0,
            ignoradasPorDuplicidade:
              0,
            importadoEm:
              '2026-06-01T12:00:00Z',
            erros: [],
          },
        );

        mockConsultarStatusImportacao
          .mockResolvedValueOnce(
            'PROCESSANDO',
          )
          .mockResolvedValueOnce(
            'CONCLUIDO',
          );

        renderizarComponente();

        await aguardarContas();

        vi.useFakeTimers();

        enviarComFireEvent(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        await act(
          async () => {
            await Promise.resolve();
          },
        );

        expect(
          screen.getByText(
            'Processando arquivo',
          ),
        ).toBeInTheDocument();

        await act(
          async () => {
            await vi.advanceTimersByTimeAsync(
              1500,
            );
          },
        );

        await act(
          async () => {
            await vi.advanceTimersByTimeAsync(
              1500,
            );
          },
        );

        expect(
          screen.getByText(
            'Importação concluída',
          ),
        ).toBeInTheDocument();
      },
      10_000,
    );

    it(
      'deve exibir falha retornada pela API',
      async () => {
        mockCriarImportacao.mockResolvedValueOnce(
          {
            id:
              'importacao-erro',
            status: 'ERRO',
            sucessos: 0,
            falhas: 1,
            ignoradasPorDuplicidade:
              0,
            importadoEm:
              '2026-06-01T12:00:00Z',
            mensagemErro:
              'Arquivo inválido.',
            erros: [
              {
                numeroRegistro:
                  3,
                campo:
                  'estrutura',
                mensagem:
                  'Registro inválido.',
              },
            ],
          },
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        expect(
          await screen.findByText(
            'Importação falhou',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Arquivo inválido.',
          ),
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'Registro inválido.',
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'deve exibir erro quando API de importação rejeitar a requisição',
      async () => {
        mockCriarImportacao.mockRejectedValueOnce(
          {
            isAxiosError:
              true,

            response: {
              status: 400,

              data: {
                erro:
                  'Arquivo inválido para importação.',
              },
            },
          },
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        expect(
          await screen.findByText(
            'Arquivo inválido para importação.',
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'deve exibir erro ao carregar contas',
      async () => {
        mockListarContas.mockRejectedValueOnce(
          new Error(
            'falha de rede',
          ),
        );

        renderizarComponente();

        expect(
          await screen.findByText(
            'Não foi possível carregar suas contas. Tente novamente.',
          ),
        ).toBeInTheDocument();
      },
    );

    it(
      'não deve deslogar em erro 403',
      async () => {
        mockCriarImportacao.mockRejectedValueOnce(
          criarErroAxios(
            403,
            'Conta não pertence ao usuário autenticado.',
          ),
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        expect(
          await screen.findByText(
            'Conta não pertence ao usuário autenticado.',
          ),
        ).toBeInTheDocument();

        expect(
          authState.sair,
        ).not.toHaveBeenCalled();
      },
    );

    it(
      'deve deslogar em erro 401',
      async () => {
        mockCriarImportacao.mockRejectedValueOnce(
          criarErroAxios(
            401,
            'Sessão expirada.',
          ),
        );

        renderizarComponente();

        await aguardarContas();

        await preencherEEnviar(
          criarArquivo(
            'extrato.csv',
            'text/csv',
          ),
        );

        await waitFor(
          () => {
            expect(
              authState.sair,
            ).toHaveBeenCalledTimes(
              1,
            );
          },
        );
      },
    );
  },
);