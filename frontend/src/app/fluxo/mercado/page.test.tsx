import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import MercadoPage from './page';

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

describe('MercadoPage', () => {
  it('renderiza o título e o textarea', () => {
    render(<MercadoPage />);

    expect(screen.getByText(/o que o mercado pede/i)).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /descrição da vaga/i })).toBeInTheDocument();
  });

  it('exibe o link de voltar', () => {
    render(<MercadoPage />);

    expect(screen.getByRole('link', { name: /voltar/i })).toHaveAttribute('href', '/');
  });

  it('não exibe o botão de analisar antes do mínimo de 3 vagas', () => {
    render(<MercadoPage />);

    expect(screen.queryByRole('button', { name: /analisar/i })).not.toBeInTheDocument();
  });

  it('exibe o contador de vagas', () => {
    render(<MercadoPage />);

    expect(screen.getByText('0/8 vagas')).toBeInTheDocument();
  });
});
