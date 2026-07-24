import { cn } from '@/lib/utils';
import { Card, CardContent } from '@/components/ui/card';
import type { EstratoConsenso, SkillAgregada } from '@/lib/api-client';
import { rotuloEstrato } from '@/lib/skills';
import { ItemDeSkill, type VarianteItem } from './ItemDeSkill';

type Props = {
  estrato: EstratoConsenso;
  skills: SkillAgregada[];
  totalVagas: number;
};

// Proeminência decrescente: cor da borda/título + variante de item.
const config: Record<EstratoConsenso, { card: string; titulo: string; variante: VarianteItem }> = {
  PRATICAMENTE_TODAS: {
    card: 'border-l-4 border-l-primary ring-primary/25',
    titulo: 'text-primary text-sm',
    variante: 'solido',
  },
  FREQUENTE: {
    card: 'border-l-[3px] border-l-primary/50',
    titulo: 'text-foreground/80 text-xs',
    variante: 'barra',
  },
  PONTUAL: {
    card: 'border-l-2 border-l-muted-foreground/25',
    titulo: 'text-muted-foreground text-xs',
    variante: 'chip',
  },
};

export function EstratoDeSkills({ estrato, skills, totalVagas }: Props) {
  if (skills.length === 0) return null;

  const { card, titulo, variante } = config[estrato];
  const rotulo = rotuloEstrato(estrato);
  const ehChip = variante === 'chip';

  return (
    <Card className={cn(card)}>
      <CardContent className="pt-4 pb-4">
        <p className={cn('mb-3 font-mono font-semibold uppercase tracking-widest', titulo)}>
          {rotulo} · {skills.length}
        </p>
        <ul
          className={cn(ehChip ? 'flex flex-wrap gap-2' : 'space-y-2')}
          aria-label={`Skills: ${rotulo}`}
        >
          {skills.map((skill) => (
            <ItemDeSkill
              key={skill.nome}
              skill={skill}
              totalVagas={totalVagas}
              variante={variante}
            />
          ))}
        </ul>
      </CardContent>
    </Card>
  );
}
