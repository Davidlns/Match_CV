'use client';

import { useState } from 'react';
import { Sparkles, X } from 'lucide-react';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';

type Props = {
  vagas: string[];
  aoAdicionarVaga: (descricao: string) => void;
  aoRemoverVaga: (indice: number) => void;
  aoAnalisar: () => void;
  estaAnalisando: boolean;
};

export function EntradaDeVagas({
  vagas,
  aoAdicionarVaga,
  aoRemoverVaga,
  aoAnalisar,
  estaAnalisando,
}: Props) {
  const [texto, setTexto] = useState('');

  function handleAdicionar() {
    const trimado = texto.trim();
    if (!trimado) return;
    aoAdicionarVaga(trimado);
    setTexto('');
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      e.preventDefault();
      handleAdicionar();
    }
  }

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Textarea
          placeholder="Cole aqui a descrição de uma vaga..."
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          onKeyDown={handleKeyDown}
          rows={6}
          className="resize-none font-mono text-sm"
          aria-label="Descrição da vaga"
        />
        <div className="flex justify-end">
          <Button
            variant="outline"
            size="sm"
            onClick={handleAdicionar}
            disabled={!texto.trim()}
          >
            Adicionar vaga
          </Button>
        </div>
      </div>

      {vagas.length > 0 && (
        <ul className="space-y-2" aria-label="Vagas adicionadas">
          {vagas.map((vaga, i) => (
            <li
              key={i}
              className="flex items-start gap-2 rounded-md border border-border bg-card px-3 py-2"
            >
              <span className="flex-1 truncate font-mono text-xs text-muted-foreground">
                {vaga.length > 80 ? `${vaga.slice(0, 80)}…` : vaga}
              </span>
              <button
                onClick={() => aoRemoverVaga(i)}
                className="mt-0.5 shrink-0 text-muted-foreground/60 transition-colors hover:text-foreground"
                aria-label={`Remover vaga ${i + 1}`}
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </li>
          ))}
        </ul>
      )}

      {vagas.length > 0 && (
        <Button
          className="w-full gap-2"
          onClick={aoAnalisar}
          disabled={estaAnalisando}
        >
          <Sparkles className="h-4 w-4" />
          {estaAnalisando
            ? 'Analisando...'
            : `Analisar ${vagas.length} ${vagas.length === 1 ? 'vaga' : 'vagas'}`}
        </Button>
      )}
    </div>
  );
}
