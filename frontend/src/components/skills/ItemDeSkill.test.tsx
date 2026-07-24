import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ItemDeSkill, type VarianteItem } from './ItemDeSkill';
import type { SkillAgregada } from '@/lib/api-client';

const base: SkillAgregada = {
  nome: 'Docker',
  frequencia: 3,
  obrigatoriaEm: 1,
  percentual: 75,
  estrato: 'FREQUENTE',
};

function renderItem(variante: VarianteItem, skill: SkillAgregada = base, total = 4) {
  return render(
    <ul>
      <ItemDeSkill skill={skill} totalVagas={total} variante={variante} />
    </ul>,
  );
}

describe('ItemDeSkill', () => {
  it('variante solido: nome + fato, sem barra', () => {
    renderItem('solido', {
      ...base,
      nome: 'Java',
      frequencia: 4,
      obrigatoriaEm: 4,
      percentual: 100,
    });
    expect(screen.getByText('Java')).toBeInTheDocument();
    expect(screen.getByText(/4\/4 vagas · sempre obrigatória/i)).toBeInTheDocument();
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
  });

  it('variante barra: barra com o percentual + fato "às vezes obrigatória"', () => {
    renderItem('barra');
    const barra = screen.getByRole('progressbar');
    expect(barra).toHaveAttribute('aria-valuenow', '75');
    expect(screen.getByText('75%')).toBeInTheDocument();
    expect(screen.getByText(/3\/4 vagas · às vezes obrigatória/i)).toBeInTheDocument();
  });

  it('variante chip: nome + n/N, sem barra', () => {
    renderItem('chip', {
      ...base,
      nome: 'Kafka',
      frequencia: 1,
      obrigatoriaEm: 0,
      percentual: 25,
    });
    expect(screen.getByText('Kafka')).toBeInTheDocument();
    expect(screen.getByText('1/4')).toBeInTheDocument();
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
  });
});
