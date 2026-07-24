import { CartaoDeFluxo } from '@/components/fluxo/CartaoDeFluxo';

const fluxos = [
  {
    numero: '01',
    titulo: 'Estou pronto pra esta vaga?',
    pede: '1 vaga + seu CV',
    entrega: 'cobertura, gaps por tipo, feedback de ATS e sugestões',
    rota: '/fluxo/esta-vaga',
  },
  {
    numero: '02',
    titulo: 'O que o mercado pede?',
    pede: '3 a 8 vagas (sem CV)',
    entrega: 'skills mais pedidas ordenadas por frequência e consenso',
    rota: '/fluxo/mercado',
  },
  {
    numero: '03',
    titulo: 'Onde estou vs o mercado?',
    pede: '3 a 8 vagas + seu CV',
    entrega: 'o que você tem, o que falta e sinergia com cada vaga',
    rota: '/fluxo/minha-posicao',
  },
  {
    numero: '04',
    titulo: 'Meu CV é bom?',
    pede: 'só seu CV',
    entrega: 'ATS estrutural, posicionamento e nota com critérios visíveis',
    rota: undefined,
  },
  {
    numero: '05',
    titulo: 'Dois CVs, qual está melhor?',
    pede: 'dois CVs seus',
    entrega: 'comparação neutra e modo evolução',
    rota: undefined,
  },
] as const;

export default function HomePage() {
  return (
    <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-12">
      <div className="mb-10">
        <h1 className="mb-2 font-heading text-3xl font-bold tracking-tight sm:text-4xl">
          <span className="font-mono text-primary">_</span> o que você quer descobrir?
        </h1>
        <p className="max-w-2xl text-[0.95rem] leading-relaxed text-muted-foreground">
          Escolha o fluxo mais parecido com o que você está buscando agora.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {fluxos.map((fluxo) => (
          <CartaoDeFluxo key={fluxo.numero} {...fluxo} />
        ))}
      </div>
    </main>
  );
}
