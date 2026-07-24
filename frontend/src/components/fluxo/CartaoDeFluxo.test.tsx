import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CartaoDeFluxo } from './CartaoDeFluxo';

const baseProps = {
  numero: '02',
  titulo: 'O que o mercado pede?',
  pede: '3 a 8 vagas',
  entrega: 'skills por consenso',
};

describe('CartaoDeFluxo', () => {
  it('renderiza número e título', () => {
    render(<CartaoDeFluxo {...baseProps} rota="/fluxo/mercado" />);

    expect(screen.getByText('02')).toBeInTheDocument();
    expect(screen.getByText('O que o mercado pede?')).toBeInTheDocument();
  });

  it('renderiza as labels pede e entrega', () => {
    render(<CartaoDeFluxo {...baseProps} rota="/fluxo/mercado" />);

    expect(screen.getByText('3 a 8 vagas')).toBeInTheDocument();
    expect(screen.getByText('skills por consenso')).toBeInTheDocument();
  });

  it('é um link quando rota está definida', () => {
    render(<CartaoDeFluxo {...baseProps} rota="/fluxo/mercado" />);

    expect(screen.getByRole('link')).toHaveAttribute('href', '/fluxo/mercado');
  });

  it('não é um link quando em breve (sem rota)', () => {
    render(<CartaoDeFluxo {...baseProps} />);

    expect(screen.queryByRole('link')).not.toBeInTheDocument();
  });

  it('mostra badge "em breve" quando sem rota', () => {
    render(<CartaoDeFluxo {...baseProps} />);

    expect(screen.getByText(/em breve/i)).toBeInTheDocument();
  });

  it('não mostra badge "em breve" quando tem rota', () => {
    render(<CartaoDeFluxo {...baseProps} rota="/fluxo/mercado" />);

    expect(screen.queryByText(/em breve/i)).not.toBeInTheDocument();
  });
});
