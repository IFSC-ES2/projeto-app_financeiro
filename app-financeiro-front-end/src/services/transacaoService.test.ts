import { beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import {
  categorizarTransacao,
  editarTransacao,
  excluirTransacao,
  listarTransacoes,
  registrarTransacaoManual,
} from './transacaoService';
import type { PaginaResponse, TransacaoRequest, TransacaoResponse } from '../types/transacao';

vi.mock('./httpClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(), patch: vi.fn() },
}));

const transacao: TransacaoResponse = {
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

const pagina: PaginaResponse<TransacaoResponse> = {
  conteudo: [transacao],
  pagina: 0,
  tamanho: 20,
  totalElementos: 1,
  totalPaginas: 1,
  primeira: true,
  ultima: true,
};

describe('transacaoService', () => {
  beforeEach(() => {
    vi.mocked(httpClient.get).mockReset();
    vi.mocked(httpClient.post).mockReset();
    vi.mocked(httpClient.put).mockReset();
    vi.mocked(httpClient.delete).mockReset();
    vi.mocked(httpClient.patch).mockReset();
  });

  it('registra a transacao em POST /transacoes/manual', async () => {
    const nova: TransacaoRequest = {
      valor: 80,
      data: '2026-06-02',
      tipoTransacao: 'DEBITO',
      formaPagamento: 'PIX',
      contaId: 'conta-1',
    };
    vi.mocked(httpClient.post).mockResolvedValue({ data: transacao });

    await expect(registrarTransacaoManual(nova)).resolves.toEqual(transacao);
    expect(httpClient.post).toHaveBeenCalledWith('/transacoes/manual', nova);
  });

  it('edita a transacao em PUT /transacoes/{transacaoId}', async () => {
    const edicao: TransacaoRequest = {
      valor: 95.5,
      data: '2026-06-15',
      tipoTransacao: 'DEBITO',
      formaPagamento: 'PIX',
      contaId: 'conta-1',
      categoriaId: 'cat-1',
    };
    vi.mocked(httpClient.put).mockResolvedValue({ data: transacao });

    await expect(editarTransacao('tx-1', edicao)).resolves.toEqual(transacao);
    expect(httpClient.put).toHaveBeenCalledWith('/transacoes/tx-1', edicao);
  });

  it('exclui a transacao em DELETE /transacoes/{transacaoId}', async () => {
    vi.mocked(httpClient.delete).mockResolvedValue({ data: undefined });

    await excluirTransacao('tx-1');

    expect(httpClient.delete).toHaveBeenCalledWith('/transacoes/tx-1');
  });

  it('categoriza a transacao em PATCH /transacoes/{transacaoId}/categoria', async () => {
    vi.mocked(httpClient.patch).mockResolvedValue({ data: transacao });

    await expect(categorizarTransacao('tx-1', 'cat-2')).resolves.toEqual(transacao);
    expect(httpClient.patch).toHaveBeenCalledWith('/transacoes/tx-1/categoria', {
      categoriaId: 'cat-2',
    });
  });

  it('lista transacoes sem filtros enviando a query vazia', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: pagina });

    await expect(listarTransacoes()).resolves.toEqual(pagina);
    expect(httpClient.get).toHaveBeenCalledWith('/transacoes', { params: {} });
  });

  it('lista transacoes enviando apenas os filtros informados, com page/size como string', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: pagina });

    await listarTransacoes({
      page: 0,
      size: 20,
      dataInicio: '2026-06-01',
      tipo: 'DEBITO',
    });

    expect(httpClient.get).toHaveBeenCalledWith('/transacoes', {
      params: {
        page: '0',
        size: '20',
        dataInicio: '2026-06-01',
        tipo: 'DEBITO',
      },
    });
  });

  it('propaga o erro da API para quem chama', async () => {
    const erro = new Error('Falha na API');
    vi.mocked(httpClient.get).mockRejectedValue(erro);

    await expect(listarTransacoes()).rejects.toBe(erro);
  });
});
