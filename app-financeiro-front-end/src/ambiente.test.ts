import { describe, it, expect } from 'vitest';

describe('Configuração do Ambiente de Testes', () => {
  it('deve disponibilizar jsdom e matchers do jest-dom', () => {
    const elemento = document.createElement('div');
    elemento.textContent = 'SmartBudget';
    document.body.appendChild(elemento);

    expect(elemento).toBeInTheDocument();
  });
});