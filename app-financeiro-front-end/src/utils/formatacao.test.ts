import { describe, expect, it } from 'vitest';
import { formatarData, formatarMoeda } from './formatacao';

describe('formatacao', () => {
  describe('formatarMoeda', () => {
    it('formata numero como moeda brasileira', () => {
      expect(formatarMoeda(1234.5)).toBe('R$ 1.234,50');
      expect(formatarMoeda(10)).toBe('R$ 10,00');
    });

    it('formata string numerica como moeda brasileira', () => {
      expect(formatarMoeda('99.9')).toBe('R$ 99,90');
    });

    it('trata valor vazio e zero como R$ 0,00', () => {
      expect(formatarMoeda('')).toBe('R$ 0,00');
      expect(formatarMoeda(0)).toBe('R$ 0,00');
    });
  });

  describe('formatarData', () => {
    it('converte a data ISO para dd/mm/aaaa', () => {
      expect(formatarData('2026-06-02')).toBe('02/06/2026');
      expect(formatarData('2026-12-25')).toBe('25/12/2026');
    });

    it('retorna Data não informada quando a entrada e vazia', () => {
      expect(formatarData('')).toBe('Data não informada');
    });

    it('retorna a propria entrada quando a data esta incompleta', () => {
      expect(formatarData('2026-06')).toBe('2026-06');
      expect(formatarData('2026')).toBe('2026');
    });
  });
});
