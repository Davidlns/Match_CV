import type { AnaliseVagasResposta, EstratoConsenso } from '@/lib/api-client';
import { EstratoDeSkills } from './EstratoDeSkills';

type Props = {
  resultado: AnaliseVagasResposta;
};

const ESTRATOS: EstratoConsenso[] = ['PRATICAMENTE_TODAS', 'FREQUENTE', 'PONTUAL'];

export function VisualizacaoDeSkills({ resultado }: Props) {
  return (
    <section aria-label="Skills identificadas" className="space-y-4 pt-6">
      <p className="font-mono text-xs text-muted-foreground">
        {resultado.totalVagas} {resultado.totalVagas === 1 ? 'vaga analisada' : 'vagas analisadas'}{' '}
        · {resultado.skills.length}{' '}
        {resultado.skills.length === 1 ? 'skill identificada' : 'skills identificadas'}
      </p>

      {ESTRATOS.map((estrato) => (
        <EstratoDeSkills
          key={estrato}
          estrato={estrato}
          skills={resultado.skills.filter((s) => s.estrato === estrato)}
          totalVagas={resultado.totalVagas}
        />
      ))}
    </section>
  );
}
