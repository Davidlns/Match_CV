import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import HomePage from './page';

vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: React.HTMLAttributes<HTMLDivElement>) => (
      <div {...props}>{children}</div>
    ),
    p: ({ children, ...props }: React.HTMLAttributes<HTMLParagraphElement>) => (
      <p {...props}>{children}</p>
    ),
  },
  AnimatePresence: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

vi.mock('@/lib/api-client', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/lib/api-client')>();
  return { ...actual, api: { ...actual.api, analisarVagas: vi.fn() } };
});

describe('HomePage', () => {
  it('renderiza o título e o textarea de entrada de vagas', () => {
    render(<HomePage />);

    expect(screen.getByText(/analise suas vagas/i)).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /descrição da vaga/i })).toBeInTheDocument();
  });

  it('exibe o botão de adicionar vaga', () => {
    render(<HomePage />);

    expect(screen.getByRole('button', { name: /adicionar vaga/i })).toBeInTheDocument();
  });

  it('não exibe o botão de analisar antes de adicionar vagas', () => {
    render(<HomePage />);

    expect(screen.queryByRole('button', { name: /analisar/i })).not.toBeInTheDocument();
  });
});
