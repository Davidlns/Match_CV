import { useState } from 'react';
import { api, ApiError, type AnaliseVagasResposta } from '@/lib/api-client';

type Estado = 'ocioso' | 'analisando' | 'pronto' | 'erro';

type Config = {
  minimoVagas?: number;
  maximoVagas?: number;
};

export function useAnaliseDeVagas({ minimoVagas = 1, maximoVagas }: Config = {}) {
  const [vagas, setVagas] = useState<string[]>([]);
  const [resultado, setResultado] = useState<AnaliseVagasResposta | null>(null);
  const [estado, setEstado] = useState<Estado>('ocioso');
  const [erroMsg, setErroMsg] = useState('');

  function adicionarVaga(descricao: string) {
    const trimada = descricao.trim();
    if (!trimada) return;
    setVagas((prev) => {
      if (maximoVagas !== undefined && prev.length >= maximoVagas) return prev;
      return [...prev, trimada];
    });
  }

  function removerVaga(indice: number) {
    setVagas((prev) => prev.filter((_, i) => i !== indice));
    setResultado(null);
    setEstado('ocioso');
  }

  async function analisar() {
    if (vagas.length < minimoVagas) return;
    setEstado('analisando');
    setErroMsg('');
    try {
      const dados = await api.analisarVagas(vagas);
      setResultado(dados);
      setEstado('pronto');
    } catch (err) {
      setErroMsg(err instanceof ApiError ? err.message : 'Erro inesperado.');
      setEstado('erro');
    }
  }

  return { vagas, adicionarVaga, removerVaga, resultado, estado, erroMsg, analisar, minimoVagas, maximoVagas };
}
