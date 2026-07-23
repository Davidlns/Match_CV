import type { AnaliseVagasResposta } from '@/lib/api-client';
import { GrupoDeSkills } from './GrupoDeSkills';

type Props = {
  resultado: AnaliseVagasResposta;
};

const PRIORIDADES = ['ALTA', 'MEDIA', 'BAIXA'] as const;

export function VisualizacaoDeSkills({ resultado }: Props) {
  const skillsPorPrioridade = Object.fromEntries(
    PRIORIDADES.map((p) => [p, resultado.skills.filter((s) => s.prioridade === p)]),
  ) as Record<(typeof PRIORIDADES)[number], typeof resultado.skills>;

  return (
    <section aria-label="Skills identificadas" className="space-y-4 pt-6">
      <p className="font-mono text-xs text-muted-foreground">
        {resultado.totalVagas} {resultado.totalVagas === 1 ? 'vaga analisada' : 'vagas analisadas'}{' '}
        · {resultado.skills.length} {resultado.skills.length === 1 ? 'skill identificada' : 'skills identificadas'}
      </p>

      {PRIORIDADES.map((prioridade) => (
        <GrupoDeSkills
          key={prioridade}
          prioridade={prioridade}
          skills={skillsPorPrioridade[prioridade]}
        />
      ))}
    </section>
  );
}
