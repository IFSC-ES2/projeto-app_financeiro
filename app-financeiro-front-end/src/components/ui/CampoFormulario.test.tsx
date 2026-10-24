import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import CampoFormulario from './CampoFormulario';

describe('CampoFormulario', () => {
  it('não marca o campo como inválido enquanto não foi tocado', () => {
    render(<CampoFormulario rotulo="E-mail" id="email" erro="E-mail é obrigatório." />);

    const campo = screen.getByLabelText('E-mail');
    expect(campo).not.toHaveAttribute('aria-invalid');
    expect(campo).not.toHaveAttribute('aria-describedby');
  });

  it('marca o campo como inválido quando tocado com erro', () => {
    render(<CampoFormulario rotulo="E-mail" id="email" erro="E-mail é obrigatório." tocado />);

    expect(screen.getByLabelText('E-mail')).toHaveAttribute('aria-invalid', 'true');
  });

  it('liga a mensagem de erro ao campo como descrição acessível', () => {
    render(<CampoFormulario rotulo="E-mail" id="email" erro="E-mail é obrigatório." tocado />);

    expect(screen.getByLabelText('E-mail')).toHaveAccessibleDescription('E-mail é obrigatório.');
  });
});
