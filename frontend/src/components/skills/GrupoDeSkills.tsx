import { motion } from 'framer-motion';
import { cn } from '@/lib/utils';
import { Card, CardContent } from '@/components/ui/card';
import type { SkillAnalisada } from '@/lib/api-client';

type Prioridade = 'ALTA' | 'MEDIA' | 'BAIXA';

type Props = {
  prioridade: Prioridade;
  skills: SkillAnalisada[];
};

const rotulos: Record<Prioridade, string> = {
  ALTA: 'Alta prioridade',
  MEDIA: 'Média prioridade',
  BAIXA: 'Baixa prioridade',
};

// Hierarquia por PESO VISUAL: ALTA salta, BAIXA recua — legível antes de ler.
const cardEstilo: Record<Prioridade, string> = {
  ALTA: 'border-l-4 border-l-primary ring-primary/25',
  MEDIA: 'border-l-[3px] border-l-yellow-500/80 ring-foreground/10',
  BAIXA: 'border-l-2 border-l-muted-foreground/25 ring-foreground/[0.06]',
};

const tituloEstilo: Record<Prioridade, string> = {
  ALTA: 'text-primary text-sm',
  MEDIA: 'text-yellow-600 dark:text-yellow-400 text-xs',
  BAIXA: 'text-muted-foreground text-xs',
};

const badgeEstilo: Record<Prioridade, string> = {
  ALTA: 'bg-primary/15 text-primary border border-primary/30 px-2.5 py-1 text-xs shadow-[0_2px_10px_-4px_var(--primary)]',
  MEDIA: 'bg-yellow-500/10 text-yellow-700 dark:text-yellow-300 border border-yellow-500/25 px-2 py-0.5 text-xs',
  BAIXA: 'bg-muted text-muted-foreground border border-border px-2 py-0.5 text-[0.7rem]',
};

const containerVariantes = {
  visivel: { transition: { staggerChildren: 0.04 } },
};

const itemVariantes = {
  oculto: { opacity: 0, scale: 0.85 },
  visivel: { opacity: 1, scale: 1, transition: { type: 'spring' as const, stiffness: 300, damping: 24 } },
};

export function GrupoDeSkills({ prioridade, skills }: Props) {
  if (skills.length === 0) return null;

  return (
    <Card className={cn(cardEstilo[prioridade])}>
      <CardContent className="pt-4 pb-4">
        <p
          className={cn(
            'mb-3 font-mono font-semibold uppercase tracking-widest',
            tituloEstilo[prioridade],
          )}
        >
          {rotulos[prioridade]} · {skills.length}
        </p>
        <motion.ul
          className="flex flex-wrap gap-2"
          initial="oculto"
          animate="visivel"
          variants={containerVariantes}
          aria-label={`Skills de ${rotulos[prioridade].toLowerCase()}`}
        >
          {skills.map((skill) => (
            <motion.li key={skill.nome} variants={itemVariantes} className="list-none">
              <span
                className={cn(
                  'inline-block rounded-md font-mono font-medium',
                  badgeEstilo[prioridade],
                )}
              >
                {skill.nome}
              </span>
            </motion.li>
          ))}
        </motion.ul>
      </CardContent>
    </Card>
  );
}
