import { describe, it, expect, vi, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useAnaliseDeVagas } from './useAnaliseDeVagas';
import { ApiError } from '@/lib/api-client';

vi.mock('@/lib/api-client', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/lib/api-client')>();
  return { ...actual, api: { ...actual.api, analisarVagas: vi.fn() } };
});

const { api } = await import('@/lib/api-client');
const mockAnalisarVagas = vi.mocked(api.analisarVagas);

afterEach(() => {
  vi.clearAllMocks();
});

describe('useAnaliseDeVagas', () => {
  it('inicia sem vagas e no estado ocioso', () => {
    const { result } = renderHook(() => useAnaliseDeVagas());

    expect(result.current.vagas).toEqual([]);
    expect(result.current.estado).toBe('ocioso');
    expect(result.current.resultado).toBeNull();
  });

  it('adiciona uma vaga com trim', () => {
    const { result } = renderHook(() => useAnaliseDeVagas());

    act(() => {
      result.current.adicionarVaga('  Vaga de dev  ');
    });

    expect(result.current.vagas).toEqual(['Vaga de dev']);
  });

  it('ignora vaga vazia ou só espaços', () => {
    const { result } = renderHook(() => useAnaliseDeVagas());

    act(() => {
      result.current.adicionarVaga('   ');
    });

    expect(result.current.vagas).toHaveLength(0);
  });

  it('remove uma vaga pelo índice e reseta o resultado', () => {
    const { result } = renderHook(() => useAnaliseDeVagas());

    act(() => {
      result.current.adicionarVaga('Vaga 1');
      result.current.adicionarVaga('Vaga 2');
    });

    act(() => {
      result.current.removerVaga(0);
    });

    expect(result.current.vagas).toEqual(['Vaga 2']);
    expect(result.current.estado).toBe('ocioso');
  });

  it('chama a API e muda para pronto em sucesso', async () => {
    const resposta = { totalVagas: 1, skills: [] };
    mockAnalisarVagas.mockResolvedValueOnce(resposta);

    const { result } = renderHook(() => useAnaliseDeVagas());

    act(() => {
      result.current.adicionarVaga('Desenvolvedor Java');
    });

    await act(async () => {
      await result.current.analisar();
    });

    expect(mockAnalisarVagas).toHaveBeenCalledWith(['Desenvolvedor Java']);
    expect(result.current.estado).toBe('pronto');
    expect(result.current.resultado).toEqual(resposta);
  });

  it('captura ApiError e muda para estado erro', async () => {
    mockAnalisarVagas.mockRejectedValueOnce(new ApiError(429, 'Limite de requisições atingido.'));

    const { result } = renderHook(() => useAnaliseDeVagas());

    act(() => {
      result.current.adicionarVaga('Vaga de dev');
    });

    await act(async () => {
      await result.current.analisar();
    });

    expect(result.current.estado).toBe('erro');
    expect(result.current.erroMsg).toBe('Limite de requisições atingido.');
  });

  it('não chama a API se não houver vagas', async () => {
    const { result } = renderHook(() => useAnaliseDeVagas());

    await act(async () => {
      await result.current.analisar();
    });

    expect(mockAnalisarVagas).not.toHaveBeenCalled();
    expect(result.current.estado).toBe('ocioso');
  });

  it('não chama a API se o mínimo de vagas não foi atingido', async () => {
    const { result } = renderHook(() => useAnaliseDeVagas({ minimoVagas: 3 }));

    act(() => {
      result.current.adicionarVaga('Vaga 1');
      result.current.adicionarVaga('Vaga 2');
    });

    await act(async () => {
      await result.current.analisar();
    });

    expect(mockAnalisarVagas).not.toHaveBeenCalled();
    expect(result.current.estado).toBe('ocioso');
  });

  it('não adiciona vaga além do máximo configurado', () => {
    const { result } = renderHook(() => useAnaliseDeVagas({ maximoVagas: 2 }));

    act(() => {
      result.current.adicionarVaga('Vaga 1');
      result.current.adicionarVaga('Vaga 2');
      result.current.adicionarVaga('Vaga 3'); // deve ser ignorada
    });

    expect(result.current.vagas).toHaveLength(2);
  });
});
