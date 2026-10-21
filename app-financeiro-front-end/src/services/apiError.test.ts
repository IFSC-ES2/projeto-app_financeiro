import { AxiosError } from 'axios';
import { describe, expect, it } from 'vitest';
import { obterMensagemErroApi, obterStatusHttp } from './apiError';

const erroAxios = (status: number, data?: unknown) =>
  new AxiosError('erro interno', AxiosError.ERR_BAD_REQUEST, undefined, undefined, {
    data,
    status,
    statusText: String(status),
    headers: {},
    config: {} as never,
  });

describe('apiError', () => {
  describe('obterMensagemErroApi', () => {
    it('retorna o fallback quando o erro nao e do axios', () => {
      expect(obterMensagemErroApi(new Error('falha'), 'Mensagem padrao')).toBe('Mensagem padrao');
      expect(obterMensagemErroApi('erro qualquer', 'Mensagem padrao')).toBe('Mensagem padrao');
    });

    it('retorna data.erro quando presente no payload', () => {
      const erro = erroAxios(400, { erro: 'Conta nao encontrada' });

      expect(obterMensagemErroApi(erro, 'Mensagem padrao')).toBe('Conta nao encontrada');
    });

    it('retorna data.message quando o payload nao tem erro', () => {
      const erro = erroAxios(400, { message: 'Dados invalidos' });

      expect(obterMensagemErroApi(erro, 'Mensagem padrao')).toBe('Dados invalidos');
    });

    it('retorna o fallback quando o texto do payload e vazio ou nao e string', () => {
      expect(obterMensagemErroApi(erroAxios(400, { erro: '   ' }), 'Mensagem padrao')).toBe(
        'Mensagem padrao',
      );
      expect(obterMensagemErroApi(erroAxios(400, { erro: 123 }), 'Mensagem padrao')).toBe(
        'Mensagem padrao',
      );
      expect(obterMensagemErroApi(erroAxios(400, {}), 'Mensagem padrao')).toBe('Mensagem padrao');
    });

    it('retorna o fallback quando o erro nao tem response', () => {
      const erro = new AxiosError('sem rede');

      expect(obterMensagemErroApi(erro, 'Mensagem padrao')).toBe('Mensagem padrao');
    });
  });

  describe('obterStatusHttp', () => {
    it('retorna o status quando o erro e do axios', () => {
      expect(obterStatusHttp(erroAxios(401, {}))).toBe(401);
      expect(obterStatusHttp(erroAxios(500, {}))).toBe(500);
    });

    it('retorna undefined quando o erro nao e do axios', () => {
      expect(obterStatusHttp(new Error('falha'))).toBeUndefined();
      expect(obterStatusHttp(undefined)).toBeUndefined();
    });

    it('retorna undefined quando o AxiosError nao tem response', () => {
      const erro = new AxiosError('sem rede');

      expect(obterStatusHttp(erro)).toBeUndefined();
    });
  });
});
