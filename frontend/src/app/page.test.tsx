import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import HomePage from './page';

describe('HomePage (tela de escolha)', () => {
  it('renderiza o título da tela de escolha', () => {
    render(<HomePage />);

    expect(screen.getByText(/o que você quer descobrir/i)).toBeInTheDocument();
  });

  it('renderiza os cinco fluxos', () => {
    render(<HomePage />);

    expect(screen.getByText('O que o mercado pede?')).toBeInTheDocument();
    expect(screen.getByText('Estou pronto pra esta vaga?')).toBeInTheDocument();
    expect(screen.getByText('Onde estou vs o mercado?')).toBeInTheDocument();
    expect(screen.getByText('Meu CV é bom?')).toBeInTheDocument();
    expect(screen.getByText('Dois CVs, qual está melhor?')).toBeInTheDocument();
  });

  it('os três primeiros fluxos têm links', () => {
    render(<HomePage />);

    const links = screen.getAllByRole('link');
    const hrefs = links.map((l) => l.getAttribute('href'));
    expect(hrefs).toContain('/fluxo/esta-vaga');
    expect(hrefs).toContain('/fluxo/mercado');
    expect(hrefs).toContain('/fluxo/minha-posicao');
  });

  it('fluxos 4 e 5 têm badge "em breve"', () => {
    render(<HomePage />);

    const badges = screen.getAllByText(/em breve/i);
    expect(badges).toHaveLength(2);
  });

  it('exibe os números dos fluxos', () => {
    render(<HomePage />);

    ['01', '02', '03', '04', '05'].forEach((n) =>
      expect(screen.getByText(n)).toBeInTheDocument(),
    );
  });
});
