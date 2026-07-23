'use client';

import { useEffect, useState } from 'react';
import { api, ApiError } from '@/lib/api-client';

type Estado = 'carregando' | 'ok' | 'erro';

export default function HomePage() {
  const [estado, setEstado] = useState<Estado>('carregando');
  const [mensagem, setMensagem] = useState('');

  useEffect(() => {
    api
      .ping()
      .then((data) => {
        setMensagem(data.reply);
        setEstado('ok');
      })
      .catch((err: unknown) => {
        setMensagem(
          err instanceof ApiError ? err.message : 'Falha inesperada.',
        );
        setEstado('erro');
      });
  }, []);

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-8">
      <h1 className="font-mono text-2xl font-bold tracking-tight">Match CV</h1>

      {estado === 'carregando' && (
        <p className="text-sm text-gray-400">Conectando ao backend...</p>
      )}

      {estado === 'ok' && (
        <p className="text-sm text-green-400">
          Backend respondeu:{' '}
          <span className="font-mono font-semibold">{mensagem}</span>
        </p>
      )}

      {estado === 'erro' && (
        <p className="text-sm text-red-400">Erro: {mensagem}</p>
      )}
    </main>
  );
}
