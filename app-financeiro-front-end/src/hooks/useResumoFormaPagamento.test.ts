import {
  renderHook,
  waitFor,
} from '@testing-library/react';
import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest';

import { buscarResumoPorPagamento } from '../services/resumoService';
import type { ResumoPagamentoResponse } from '../types/resumo';

import { useResumoFormaPagamento } from './useResumoFormaPagamento';

vi.mock(
  '../services/resumoService',
  () => ({
    buscarResumoPorPagamento:
      vi.fn(),
  }),
);

const resumoPix =
  (): ResumoPagamentoResponse[] => [
    {
      formaPagamento: 'PIX',
      rotulo: 'Pix',
      total: 100,
      quantidade: 1,
      percentual: 100,
    },
  ];

describe(
  'useResumoFormaPagamento',
  () => {
    beforeEach(() => {
      vi.mocked(
        buscarResumoPorPagamento,
      ).mockReset();
    });

    it(
      'consulta o resumo utilizando ano e mês selecionados',
      async () => {
        vi.mocked(
          buscarResumoPorPagamento,
        ).mockResolvedValue(
          resumoPix(),
        );

        const { result } =
          renderHook(() =>
            useResumoFormaPagamento(
              '2026-05',
            ),
          );

        await waitFor(() =>
          expect(
            result.current.resumo,
          ).toHaveLength(1),
        );

        expect(
          buscarResumoPorPagamento,
        ).toHaveBeenCalledWith({
          ano: 2026,
          mes: 5,
        });
      },
    );

    it(
      'não mantém os dados do mês anterior enquanto o novo mês carrega',
      async () => {
        vi.mocked(
          buscarResumoPorPagamento,
        )
          .mockResolvedValueOnce(
            resumoPix(),
          )
          .mockReturnValueOnce(
            new Promise(
              () => undefined,
            ),
          );

        const {
          result,
          rerender,
        } = renderHook(
          ({
            anoMes,
          }: {
            anoMes: string;
          }) =>
            useResumoFormaPagamento(
              anoMes,
            ),
          {
            initialProps: {
              anoMes:
                '2026-04',
            },
          },
        );

        await waitFor(() =>
          expect(
            result.current.resumo,
          ).toHaveLength(1),
        );

        rerender({
          anoMes: '2026-05',
        });

        expect(
          result.current.resumo,
        ).toEqual([]);

        expect(
          result.current.carregando,
        ).toBe(true);

        expect(
          buscarResumoPorPagamento,
        ).toHaveBeenLastCalledWith({
          ano: 2026,
          mes: 5,
        });
      },
    );
  },
);