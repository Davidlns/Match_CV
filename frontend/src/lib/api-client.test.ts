import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from './api-client';

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

  describe('api.analisarVagas', () => {
    it('faz POST para /api/skills/analyze com as descrições das vagas', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ totalVagas: 1, skills: [] }),
      });

      await api.analisarVagas(['Descrição da vaga aqui']);

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/skills/analyze'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ descricoesVagas: ['Descrição da vaga aqui'] }),
        }),
      );
    });

    it('retorna a resposta de análise em sucesso', async () => {
      const resposta = {
        totalVagas: 1,
        skills: [{ nome: 'Java', frequencia: 1, obrigatoriaEm: 1, percentual: 100, estrato: 'PRATICAMENTE_TODAS' }],
      };
      mockFetch.mockResolvedValueOnce({ ok: true, json: async () => resposta });

      const resultado = await api.analisarVagas(['Vaga Java']);

      expect(resultado).toEqual(resposta);
    });
  });
});
