'use client';

import { Moon, Sun } from 'lucide-react';
import { useTheme } from 'next-themes';
import { Button } from '@/components/ui/button';

export function Cabecalho() {
  const { resolvedTheme, setTheme } = useTheme();
  const ehEscuro = resolvedTheme !== 'light';

  function alternarTema() {
    setTheme(ehEscuro ? 'light' : 'dark');
  }

  return (
    <header className="sticky top-0 z-10 border-b border-border bg-background/80 backdrop-blur-sm">
      <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
        <div className="flex items-center gap-1.5">
          <span
            className="select-none font-mono text-lg font-bold text-primary"
            aria-hidden
          >
            &gt;
          </span>
          <span className="font-mono text-sm font-semibold tracking-tight">
            match_cv
          </span>
        </div>

        <Button
          variant="ghost"
          size="icon"
          onClick={alternarTema}
          aria-label={ehEscuro ? 'Mudar para modo claro' : 'Mudar para modo escuro'}
        >
          {ehEscuro ? (
            <Sun className="h-4 w-4" />
          ) : (
            <Moon className="h-4 w-4" />
          )}
        </Button>
      </div>
    </header>
  );
}
