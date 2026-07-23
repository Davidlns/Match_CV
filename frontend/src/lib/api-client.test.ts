import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api, ApiError } from './api-client';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('api-client', () => {
  beforeEach(() => {
    mockFetch.mockReset();
  });

  describe('api.ping', () => {
    it('retorna o reply quando o backend responde com sucesso', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ reply: 'pong' }),
      });

      const result = await api.ping();

      expect(result.reply).toBe('pong');
    });

    it('chama o endpoint correto', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ reply: 'pong' }),
      });

      await api.ping();

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/ai/ping'),
        undefined,
      );
    });

    it('lança ApiError com status e mensagem do backend em erro', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 503,
        json: async () => ({ error: 'Não foi possível chamar a IA.' }),
      });

      await expect(api.ping()).rejects.toMatchObject({
        name: 'ApiError',
        status: 503,
        message: 'Não foi possível chamar a IA.',
      });
    });

    it('lança ApiError com mensagem genérica quando o corpo não é JSON', async () => {
      mockFetch
        .mockResolvedValueOnce({
          ok: false,
          status: 429,
          json: async () => {
            throw new SyntaxError('not json');
          },
        })
        .mockResolvedValueOnce({
          ok: false,
          status: 429,
          json: async () => {
            throw new SyntaxError('not json');
          },
        });

      await expect(api.ping()).rejects.toMatchObject({
        status: 429,
        message: 'Erro inesperado.',
      });
    });

    it('lança ApiError com mensagem genérica quando o campo error está ausente', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({}),
      });

      await expect(api.ping()).rejects.toMatchObject({
        status: 400,
        message: 'Erro inesperado.',
      });
    });
  });
});
