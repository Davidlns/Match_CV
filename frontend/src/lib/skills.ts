import type { EstratoConsenso } from './api-client';

// Rótulo visível do estrato (texto de produto — muda sem tocar no backend).
export function rotuloEstrato(estrato: EstratoConsenso): string {
  switch (estrato) {
    case 'PRATICAMENTE_TODAS':
      return 'Ultra requisitadas';
    case 'FREQUENTE':
      return 'Muito requisitadas';
    case 'PONTUAL':
      return 'Pouco requisitadas';
  }
}

// Como a skill aparece nas vagas, derivado do dado bruto.
export function rotuloTipo(obrigatoriaEm: number, frequencia: number): string {
  if (obrigatoriaEm === frequencia) return 'sempre obrigatória';
  if (obrigatoriaEm === 0) return 'diferencial';
  return 'às vezes obrigatória';
}

// Fato de frequência: "4/4 vagas".
export function rotuloFrequencia(frequencia: number, totalVagas: number): string {
  return `${frequencia}/${totalVagas} ${totalVagas === 1 ? 'vaga' : 'vagas'}`;
}
