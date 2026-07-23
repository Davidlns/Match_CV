const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, options);
  if (!res.ok) {
    let message = 'Erro inesperado.';
    try {
      const body = (await res.json()) as { error?: string };
      message = body.error ?? message;
    } catch {
      // resposta não é JSON — mantém a mensagem padrão
    }
    throw new ApiError(res.status, message);
  }
  return res.json() as Promise<T>;
}

export type SkillAnalisada = {
  nome: string;
  frequencia: number;
  obrigatoriaEm: number;
  percentual: number;
  prioridade: 'ALTA' | 'MEDIA' | 'BAIXA';
};

export type AnaliseVagasResposta = {
  totalVagas: number;
  skills: SkillAnalisada[];
};

export const api = {
  ping: () => request<{ reply: string }>('/api/ai/ping'),

  analisarVagas: (descricoesVagas: string[]) =>
    request<AnaliseVagasResposta>('/api/skills/analyze', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ descricoesVagas }),
    }),
};
