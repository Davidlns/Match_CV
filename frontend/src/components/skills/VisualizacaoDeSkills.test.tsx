import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { VisualizacaoDeSkills } from './VisualizacaoDeSkills';
import type { AnaliseVagasResposta } from '@/lib/api-client';

vi.mock('framer-motion', () => ({
  motion: {
    ul: ({ children, ...props }: React.HTMLAttributes<HTMLUListElement>) => (
      <ul {...props}>{children}</ul>
    ),
    li: ({ children, ...props }: React.HTMLAttributes<HTMLLIElement>) => (
      <li {...props}>{children}</li>
    ),
  },
  AnimatePresence: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

const resultadoBase: AnaliseVagasResposta = {
  totalVagas: 2,
  skills: [
    { nome: 'Java', frequencia: 2, obrigatoriaEm: 2, percentual: 100, prioridade: 'ALTA' },
    { nome: 'Spring Boot', frequencia: 2, obrigatoriaEm: 2, percentual: 100, prioridade: 'ALTA' },
    { nome: 'Docker', frequencia: 1, obrigatoriaEm: 0, percentual: 50, prioridade: 'MEDIA' },
    { nome: 'Scrum', frequencia: 1, obrigatoriaEm: 0, percentual: 50, prioridade: 'BAIXA' },
  ],
};

describe('VisualizacaoDeSkills', () => {
  it('exibe o resumo de vagas e skills analisadas', () => {
    render(<VisualizacaoDeSkills resultado={resultadoBase} />);

    expect(screen.getByText(/2 vagas analisadas/i)).toBeInTheDocument();
    expect(screen.getByText(/4 skills identificadas/i)).toBeInTheDocument();
  });

  it('renderiza todos os nomes de skills', () => {
    render(<VisualizacaoDeSkills resultado={resultadoBase} />);

    expect(screen.getByText('Java')).toBeInTheDocument();
    expect(screen.getByText('Spring Boot')).toBeInTheDocument();
    expect(screen.getByText('Docker')).toBeInTheDocument();
    expect(screen.getByText('Scrum')).toBeInTheDocument();
  });

  it('exibe os três grupos de prioridade quando há skills em cada um', () => {
    render(<VisualizacaoDeSkills resultado={resultadoBase} />);

    expect(screen.getByText(/alta prioridade/i)).toBeInTheDocument();
    expect(screen.getByText(/média prioridade/i)).toBeInTheDocument();
    expect(screen.getByText(/baixa prioridade/i)).toBeInTheDocument();
  });

  it('não renderiza o grupo BAIXA quando não há skills nele', () => {
    const resultado: AnaliseVagasResposta = {
      ...resultadoBase,
      skills: resultadoBase.skills.filter((s) => s.prioridade !== 'BAIXA'),
    };

    render(<VisualizacaoDeSkills resultado={resultado} />);

    expect(screen.queryByText(/baixa prioridade/i)).not.toBeInTheDocument();
  });

  it('usa o singular para 1 vaga e 1 skill', () => {
    const resultado: AnaliseVagasResposta = {
      totalVagas: 1,
      skills: [{ nome: 'React', frequencia: 1, obrigatoriaEm: 1, percentual: 100, prioridade: 'ALTA' }],
    };

    render(<VisualizacaoDeSkills resultado={resultado} />);

    expect(screen.getByText(/1 vaga analisada/i)).toBeInTheDocument();
    expect(screen.getByText(/1 skill identificada/i)).toBeInTheDocument();
  });
});
