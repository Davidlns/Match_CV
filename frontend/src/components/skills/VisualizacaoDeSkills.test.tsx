import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { VisualizacaoDeSkills } from './VisualizacaoDeSkills';
import type { AnaliseVagasResposta } from '@/lib/api-client';

const resultadoBase: AnaliseVagasResposta = {
  totalVagas: 4,
  skills: [
    { nome: 'Java', frequencia: 4, obrigatoriaEm: 4, percentual: 100, estrato: 'PRATICAMENTE_TODAS' },
    { nome: 'Spring', frequencia: 3, obrigatoriaEm: 3, percentual: 75, estrato: 'FREQUENTE' },
    { nome: 'Docker', frequencia: 2, obrigatoriaEm: 1, percentual: 50, estrato: 'FREQUENTE' },
    { nome: 'Kafka', frequencia: 1, obrigatoriaEm: 0, percentual: 25, estrato: 'PONTUAL' },
  ],
};

describe('VisualizacaoDeSkills', () => {
  it('exibe o resumo de vagas e skills', () => {
    render(<VisualizacaoDeSkills resultado={resultadoBase} />);
    expect(screen.getByText(/4 vagas analisadas/i)).toBeInTheDocument();
    expect(screen.getByText(/4 skills identificadas/i)).toBeInTheDocument();
  });

  it('renderiza os três estratos com seus rótulos', () => {
    render(<VisualizacaoDeSkills resultado={resultadoBase} />);
    expect(screen.getByText(/ultra requisitadas/i)).toBeInTheDocument();
    expect(screen.getByText(/muito requisitadas/i)).toBeInTheDocument();
    expect(screen.getByText(/pouco requisitadas/i)).toBeInTheDocument();
  });

  it('renderiza todos os nomes de skills (nada escondido)', () => {
    render(<VisualizacaoDeSkills resultado={resultadoBase} />);
    ['Java', 'Spring', 'Docker', 'Kafka'].forEach((nome) =>
      expect(screen.getByText(nome)).toBeInTheDocument(),
    );
  });

  it('omite estrato vazio', () => {
    const resultado: AnaliseVagasResposta = {
      totalVagas: 4,
      skills: resultadoBase.skills.filter((s) => s.estrato !== 'PONTUAL'),
    };
    render(<VisualizacaoDeSkills resultado={resultado} />);
    expect(screen.queryByText(/pouco requisitadas/i)).not.toBeInTheDocument();
  });

  it('usa singular para 1 vaga e 1 skill', () => {
    const resultado: AnaliseVagasResposta = {
      totalVagas: 1,
      skills: [
        { nome: 'React', frequencia: 1, obrigatoriaEm: 1, percentual: 100, estrato: 'PRATICAMENTE_TODAS' },
      ],
    };
    render(<VisualizacaoDeSkills resultado={resultado} />);
    expect(screen.getByText(/1 vaga analisada/i)).toBeInTheDocument();
    expect(screen.getByText(/1 skill identificada/i)).toBeInTheDocument();
  });
});
