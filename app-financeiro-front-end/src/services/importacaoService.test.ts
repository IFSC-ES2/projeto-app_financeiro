import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest';
import httpClient from './httpClient';
import {
  consultarStatusImportacao,
  criarImportacao,
} from './importacaoService';
import type {
  ImportacaoResponse,
  StatusImportacao,
} from '../types/importacao';
vi.mock(
  './httpClient',
  () => ({
    default: {
      get: vi.fn(),
      post: vi.fn(),
    },
  }),
);

const importacao:
  ImportacaoResponse = {
    id: 'importacao-1',
    status: 'PENDENTE',
    sucessos: 0,
    falhas: 0,
    ignoradasPorDuplicidade: 0,
    importadoEm:
      '2026-06-01T12:00:00Z',
    erros: [],
  };

describe(
  'importacaoService',
  () => {
    beforeEach(() => {
      vi.mocked(
        httpClient.get,
      ).mockReset();

      vi.mocked(
        httpClient.post,
      ).mockReset();
    });

    it(
      'cria a importacao em POST /importacoes com FormData de arquivo e contaId',
      async () => {
        vi.mocked(
          httpClient.post,
        ).mockResolvedValue({
          data: importacao,
        });

        const arquivo =
          new File(
            ['conteudo'],
            'extrato.csv',
            {
              type: 'text/csv',
            },
          );

        await expect(
          criarImportacao(
            arquivo,
            'conta-1',
          ),
        ).resolves.toEqual(
          importacao,
        );

        expect(
          httpClient.post,
        ).toHaveBeenCalledWith(
          '/importacoes',
          expect.any(
            FormData,
          ),
        );

        const formData =
          vi.mocked(
            httpClient.post,
          ).mock.calls[0][1] as FormData;

        expect(
          formData.get(
            'contaId',
          ),
        ).toBe('conta-1');

        const arquivoEnviado =
          formData.get(
            'arquivo',
          ) as File;

        expect(
          arquivoEnviado,
        ).toBeInstanceOf(
          File,
        );

        expect(
          arquivoEnviado.name,
        ).toBe(
          'extrato.csv',
        );
      },
    );

    it(
      'consulta o status em GET /importacoes/{importacaoId}/status e retorna o data',
      async () => {
        const status:
          StatusImportacao =
            'CONCLUIDO';

        vi.mocked(
          httpClient.get,
        ).mockResolvedValue({
          data: status,
        });

        await expect(
          consultarStatusImportacao(
            'importacao-1',
          ),
        ).resolves.toBe(
          status,
        );

        expect(
          httpClient.get,
        ).toHaveBeenCalledWith(
          '/importacoes/importacao-1/status',
        );
      },
    );

    it(
      'propaga o erro da API para quem chama',
      async () => {
        const erro =
          new Error(
            'Falha na API',
          );

        vi.mocked(
          httpClient.get,
        ).mockRejectedValue(
          erro,
        );

        await expect(
          consultarStatusImportacao(
            'importacao-1',
          ),
        ).rejects.toBe(
          erro,
        );
      },
    );
  },
);