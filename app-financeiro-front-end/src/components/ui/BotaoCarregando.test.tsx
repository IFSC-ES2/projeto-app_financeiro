import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import BotaoCarregando from './BotaoCarregando';

describe('BotaoCarregando', () => {
  it('mostra o conteúdo normal quando não está carregando', () => {
    render(<BotaoCarregando carregando={false}>Entrar</BotaoCarregando>);

    expect(screen.getByRole('button', { name: 'Entrar' })).toBeEnabled();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('desabilita o botão e anuncia o texto de carregamento', () => {
    render(
      <BotaoCarregando carregando textoCarregando="Entrando...">
        Entrar
      </BotaoCarregando>,
    );

    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByRole('status')).toHaveTextContent('Entrando...');
  });
});
