import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export default function EstaVagaPage() {
  return (
    <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-12">
      <Link
        href="/"
        className="mb-8 flex w-fit items-center gap-1.5 text-xs text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft className="h-3.5 w-3.5" />
        voltar
      </Link>

      <div className="mb-8">
        <h1 className="mb-2 font-heading text-3xl font-bold tracking-tight sm:text-4xl">
          <span className="font-mono text-primary">01</span> estou pronto pra esta vaga?
        </h1>
        <p className="max-w-2xl text-[0.95rem] leading-relaxed text-muted-foreground">
          Cole a descrição da vaga e faça upload do seu CV para ver o alinhamento,
          os gaps e um feedback de ATS personalizado.
        </p>
      </div>

      <div className="flex items-center justify-center rounded-xl border border-dashed border-border p-16">
        <p className="font-mono text-sm text-muted-foreground/60">em construção</p>
      </div>
    </main>
  );
}
