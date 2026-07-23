'use client';

import { AnimatePresence, motion } from 'framer-motion';
import { EntradaDeVagas } from '@/components/vagas/EntradaDeVagas';
import { VisualizacaoDeSkills } from '@/components/skills/VisualizacaoDeSkills';
import { useAnaliseDeVagas } from '@/hooks/useAnaliseDeVagas';
import { Card, CardContent } from '@/components/ui/card';

export default function HomePage() {
  const { vagas, adicionarVaga, removerVaga, resultado, estado, erroMsg, analisar } =
    useAnaliseDeVagas();

  return (
    <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-12">
      <div className="mb-8">
        <h1 className="mb-2 font-heading text-3xl font-bold tracking-tight sm:text-4xl">
          <span className="font-mono text-primary">_</span> analise suas vagas
        </h1>
        <p className="max-w-2xl text-[0.95rem] leading-relaxed text-muted-foreground">
          Cole as descrições das vagas que te interessam e descubra as skills
          mais pedidas pelo mercado.
        </p>
      </div>

      <Card>
        <CardContent className="py-6">
          <EntradaDeVagas
            vagas={vagas}
            aoAdicionarVaga={adicionarVaga}
            aoRemoverVaga={removerVaga}
            aoAnalisar={analisar}
            estaAnalisando={estado === 'analisando'}
          />
        </CardContent>
      </Card>

      <AnimatePresence mode="wait">
        {estado === 'analisando' && (
          <motion.div
            key="carregando"
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0 }}
            className="mt-10 text-center"
          >
            <motion.p
              className="font-mono text-sm text-primary"
              animate={{ opacity: [0.5, 1, 0.5] }}
              transition={{ repeat: Infinity, duration: 1.6, ease: 'easeInOut' }}
            >
              &gt; analisando vagas...
            </motion.p>
          </motion.div>
        )}

        {estado === 'erro' && (
          <motion.p
            key="erro"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="mt-8 text-center text-sm text-destructive"
          >
            {erroMsg}
          </motion.p>
        )}

        {estado === 'pronto' && resultado && (
          <motion.div
            key="resultado"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35 }}
          >
            <VisualizacaoDeSkills resultado={resultado} />
          </motion.div>
        )}
      </AnimatePresence>
    </main>
  );
}
