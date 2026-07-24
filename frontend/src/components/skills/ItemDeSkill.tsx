import { cn } from '@/lib/utils';
import type { SkillAgregada } from '@/lib/api-client';
import { rotuloFrequencia, rotuloTipo } from '@/lib/skills';

export type VarianteItem = 'solido' | 'barra' | 'chip';

type Props = {
  skill: SkillAgregada;
  totalVagas: number;
  variante: VarianteItem;
};

export function ItemDeSkill({ skill, totalVagas, variante }: Props) {
  if (variante === 'chip') {
    return (
      <li className="list-none">
        <span className="inline-flex items-center gap-1.5 rounded-md border border-border bg-muted/60 px-2 py-0.5 font-mono text-[0.7rem] text-muted-foreground">
          <span className="font-medium text-foreground/80">{skill.nome}</span>
          <span aria-hidden>·</span>
          <span>
            {skill.frequencia}/{totalVagas}
          </span>
        </span>
      </li>
    );
  }

  const ehSolido = variante === 'solido';
  const fato = `${rotuloFrequencia(skill.frequencia, totalVagas)} · ${rotuloTipo(skill.obrigatoriaEm, skill.frequencia)}`;

  return (
    <li className="flex items-center gap-3">
      {ehSolido && (
        <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-primary" aria-hidden />
      )}
      <span
        className={cn(
          'font-mono',
          ehSolido ? 'font-semibold text-foreground' : 'text-sm text-foreground/90',
        )}
      >
        {skill.nome}
      </span>
      <span className="font-mono text-xs text-muted-foreground">{fato}</span>

      {variante === 'barra' && (
        <span className="ml-auto flex items-center gap-2">
          <span
            role="progressbar"
            aria-valuenow={skill.percentual}
            aria-valuemin={0}
            aria-valuemax={100}
            aria-label={`Presente em ${skill.percentual}% das vagas`}
            className="block h-1.5 w-20 overflow-hidden rounded-full bg-muted"
          >
            <span
              className="block h-full rounded-full bg-primary/70"
              style={{ width: `${skill.percentual}%` }}
            />
          </span>
          <span className="w-9 text-right font-mono text-xs tabular-nums text-muted-foreground">
            {skill.percentual}%
          </span>
        </span>
      )}
    </li>
  );
}
