import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import HomePage from './page';
import { ApiError } from '@/lib/api-client';

vi.mock('@/lib/api-client', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/lib/api-client')>();
  return {
    ...actual,
    api: { ping: vi.fn() },
  };
});

// Importado após o mock para obter a versão mockada.
const { api } = await import('@/lib/api-client');
const mockPing = vi.mocked(api.ping);

afterEach(() => {
  vi.clearAllMocks();
});

describe('HomePage', () => {
  it('exibe estado de carregamento enquanto a chamada está pendente', () => {
    mockPing.mockReturnValue(new Promise(() => {}));

    render(<HomePage />);

    expect(
      screen.getByText(/conectando ao backend/i),
    ).toBeInTheDocument();
  });

  it('exibe a resposta do backend após sucesso', async () => {
    mockPing.mockResolvedValueOnce({ reply: 'pong' });

    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText(/pong/i)).toBeInTheDocument();
    });
  });

  it('exibe mensagem de erro em caso de ApiError', async () => {
    mockPing.mockRejectedValueOnce(new ApiError(503, 'Serviço indisponível.'));

    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText(/serviço indisponível/i)).toBeInTheDocument();
    });
  });

  it('exibe mensagem genérica para erros inesperados', async () => {
    mockPing.mockRejectedValueOnce(new Error('falha de rede'));

    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText(/falha inesperada/i)).toBeInTheDocument();
    });
  });
});
