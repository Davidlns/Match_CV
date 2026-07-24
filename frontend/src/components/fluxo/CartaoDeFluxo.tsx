import Link from 'next/link';
import { ArrowRight } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';

type Props = {
  numero: string;
  titulo: string;
  pede: string;
  entrega: string;
  rota?: string;
};

export function CartaoDeFluxo({ numero, titulo, pede, entrega, rota }: Props) {
  const emBreve = !rota;

  const corpo = (
    <Card
      className={cn(
        'h-full transition-all duration-200',
        !emBreve && 'cursor-pointer group hover:-translate-y-0.5 hover:border-primary/40',
        emBreve && 'opacity-55 select-none',
      )}
    >
      <CardContent className="flex h-full flex-col gap-3 p-5">
        <div className="flex items-center justify-between">
          <span className="font-mono text-xl font-bold text-primary/30">{numero}</span>
          {emBreve && (
            <Badge variant="secondary" className="text-[10px]">
              em breve
            </Badge>
          )}
        </div>

        <h2 className="font-heading text-base font-semibold leading-snug">{titulo}</h2>

        <div className="mt-auto space-y-1.5">
          <div className="flex gap-1.5 text-xs">
            <span className="shrink-0 font-mono text-primary/50">pede</span>
            <span className="text-muted-foreground">{pede}</span>
          </div>
          <div className="flex gap-1.5 text-xs">
            <span className="shrink-0 font-mono text-primary/50">entrega</span>
            <span className="text-muted-foreground">{entrega}</span>
          </div>
        </div>

        {!emBreve && (
          <ArrowRight className="h-4 w-4 self-end text-primary/50 transition-transform group-hover:translate-x-0.5" />
        )}
      </CardContent>
    </Card>
  );

  if (emBreve) return <div className="cursor-not-allowed">{corpo}</div>;
  return (
    <Link href={rota} className="block h-full">
      {corpo}
    </Link>
  );
}
