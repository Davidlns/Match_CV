import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { EstratoDeSkills } from './EstratoDeSkills';
import type { SkillAgregada } from '@/lib/api-client';

const ultra: SkillAgregada[] = [
  { nome: 'Java', frequencia: 4, obrigatoriaEm: 4, percentual: 100, estrato: 'PRATICAMENTE_TODAS' },
];

describe('EstratoDeSkills', () => {
  it('não renderiza nada quando não há skills', () => {
    const { container } = render(
      <EstratoDeSkills estrato="PONTUAL" skills={[]} totalVagas={4} />,
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('mostra o rótulo e a contagem', () => {
    render(<EstratoDeSkills estrato="PRATICAMENTE_TODAS" skills={ultra} totalVagas={4} />);
    expect(screen.getByText(/ultra requisitadas · 1/i)).toBeInTheDocument();
  });

  it('variante solido (sem barra) no estrato PRATICAMENTE_TODAS', () => {
    render(<EstratoDeSkills estrato="PRATICAMENTE_TODAS" skills={ultra} totalVagas={4} />);
    expect(screen.getByText('Java')).toBeInTheDocument();
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
  });

  it('variante barra no estrato FREQUENTE', () => {
    const freq: SkillAgregada[] = [
      { nome: 'Docker', frequencia: 2, obrigatoriaEm: 1, percentual: 50, estrato: 'FREQUENTE' },
    ];
    render(<EstratoDeSkills estrato="FREQUENTE" skills={freq} totalVagas={4} />);
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '50');
  });

  it('chips (sem barra) no estrato PONTUAL', () => {
    const pontual: SkillAgregada[] = [
      { nome: 'Kafka', frequencia: 1, obrigatoriaEm: 0, percentual: 25, estrato: 'PONTUAL' },
    ];
    render(<EstratoDeSkills estrato="PONTUAL" skills={pontual} totalVagas={4} />);
    expect(screen.getByText('Kafka')).toBeInTheDocument();
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
  });
});
