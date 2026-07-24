import { describe, it, expect } from 'vitest';
import { rotuloEstrato, rotuloTipo, rotuloFrequencia } from './skills';

describe('rotuloEstrato', () => {
  it('mapeia cada estrato para o rótulo de produto', () => {
    expect(rotuloEstrato('PRATICAMENTE_TODAS')).toBe('Ultra requisitadas');
    expect(rotuloEstrato('FREQUENTE')).toBe('Muito requisitadas');
    expect(rotuloEstrato('PONTUAL')).toBe('Pouco requisitadas');
  });
});

describe('rotuloTipo', () => {
  it('sempre obrigatória quando obrigatória em todas as aparições', () => {
    expect(rotuloTipo(3, 3)).toBe('sempre obrigatória');
  });
  it('diferencial quando nunca obrigatória', () => {
    expect(rotuloTipo(0, 3)).toBe('diferencial');
  });
  it('às vezes obrigatória quando parcial', () => {
    expect(rotuloTipo(1, 3)).toBe('às vezes obrigatória');
  });
});

describe('rotuloFrequencia', () => {
  it('plural para várias vagas', () => {
    expect(rotuloFrequencia(3, 4)).toBe('3/4 vagas');
  });
  it('singular para uma vaga', () => {
    expect(rotuloFrequencia(1, 1)).toBe('1/1 vaga');
  });
});
