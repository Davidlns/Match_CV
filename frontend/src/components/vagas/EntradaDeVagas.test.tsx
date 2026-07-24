import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EntradaDeVagas } from './EntradaDeVagas';

const defaultProps = {
  vagas: [] as string[],
  aoAdicionarVaga: vi.fn(),
  aoRemoverVaga: vi.fn(),
  aoAnalisar: vi.fn(),
  estaAnalisando: false,
};

afterEach(() => {
  vi.clearAllMocks();
});

describe('EntradaDeVagas', () => {
  it('renderiza o textarea e o botão de adicionar', () => {
    render(<EntradaDeVagas {...defaultProps} />);

    expect(screen.getByRole('textbox', { name: /descrição da vaga/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /adicionar vaga/i })).toBeInTheDocument();
  });

  it('botão adicionar começa desabilitado com textarea vazia', () => {
    render(<EntradaDeVagas {...defaultProps} />);

    expect(screen.getByRole('button', { name: /adicionar vaga/i })).toBeDisabled();
  });

  it('habilita o botão ao digitar no textarea', async () => {
    const user = userEvent.setup();
    render(<EntradaDeVagas {...defaultProps} />);

    await user.type(screen.getByRole('textbox'), 'Engenheiro Backend');

    expect(screen.getByRole('button', { name: /adicionar vaga/i })).toBeEnabled();
  });

  it('chama aoAdicionarVaga e limpa o textarea ao clicar em adicionar', async () => {
    const mockAdicionar = vi.fn();
    const user = userEvent.setup();
    render(<EntradaDeVagas {...defaultProps} aoAdicionarVaga={mockAdicionar} />);

    await user.type(screen.getByRole('textbox'), 'Engenheiro Backend Java');
    await user.click(screen.getByRole('button', { name: /adicionar vaga/i }));

    expect(mockAdicionar).toHaveBeenCalledWith('Engenheiro Backend Java');
    expect(screen.getByRole('textbox')).toHaveValue('');
  });

  it('chama aoAdicionarVaga ao pressionar Ctrl+Enter', async () => {
    const mockAdicionar = vi.fn();
    const user = userEvent.setup();
    render(<EntradaDeVagas {...defaultProps} aoAdicionarVaga={mockAdicionar} />);

    const textarea = screen.getByRole('textbox');
    await user.type(textarea, 'Vaga de teste');
    await user.keyboard('{Control>}{Enter}{/Control}');

    expect(mockAdicionar).toHaveBeenCalledWith('Vaga de teste');
  });

  it('exibe a lista de vagas adicionadas', () => {
    render(
      <EntradaDeVagas
        {...defaultProps}
        vagas={['Vaga Backend Java', 'Vaga Frontend React']}
      />,
    );

    const itens = screen.getAllByRole('listitem');
    expect(itens).toHaveLength(2);
  });

  it('chama aoRemoverVaga ao clicar no botão de remover', async () => {
    const mockRemover = vi.fn();
    const user = userEvent.setup();
    render(
      <EntradaDeVagas
        {...defaultProps}
        vagas={['Vaga 1', 'Vaga 2']}
        aoRemoverVaga={mockRemover}
      />,
    );

    await user.click(screen.getByRole('button', { name: /remover vaga 1/i }));

    expect(mockRemover).toHaveBeenCalledWith(0);
  });

  it('exibe o botão de analisar quando há vagas (sem mínimo configurado)', () => {
    render(<EntradaDeVagas {...defaultProps} vagas={['Vaga 1']} />);

    expect(screen.getByRole('button', { name: /analisar 1 vaga/i })).toBeInTheDocument();
  });

  it('desabilita o botão de analisar enquanto está analisando', () => {
    render(
      <EntradaDeVagas {...defaultProps} vagas={['Vaga 1']} estaAnalisando />,
    );

    expect(screen.getByRole('button', { name: /analisando/i })).toBeDisabled();
  });

  it('não exibe o botão de analisar sem vagas', () => {
    render(<EntradaDeVagas {...defaultProps} />);

    expect(screen.queryByRole('button', { name: /analisar/i })).not.toBeInTheDocument();
  });

  it('não exibe o botão de analisar quando abaixo do mínimo configurado', () => {
    render(
      <EntradaDeVagas
        {...defaultProps}
        vagas={['Vaga 1', 'Vaga 2']}
        minimoVagas={3}
      />,
    );

    expect(screen.queryByRole('button', { name: /analisar/i })).not.toBeInTheDocument();
  });

  it('exibe aviso quando há vagas mas abaixo do mínimo', () => {
    render(
      <EntradaDeVagas
        {...defaultProps}
        vagas={['Vaga 1', 'Vaga 2']}
        minimoVagas={3}
      />,
    );

    expect(screen.getByRole('status')).toHaveTextContent(/adicione mais 1 vaga para analisar/i);
  });

  it('desabilita o botão de adicionar ao atingir o máximo', () => {
    render(
      <EntradaDeVagas
        {...defaultProps}
        vagas={['V1', 'V2', 'V3']}
        maximoVagas={3}
      />,
    );

    expect(screen.getByRole('button', { name: /adicionar vaga/i })).toBeDisabled();
  });

  it('exibe o contador quando maximoVagas está configurado', () => {
    render(
      <EntradaDeVagas
        {...defaultProps}
        vagas={['V1', 'V2']}
        maximoVagas={8}
      />,
    );

    expect(screen.getByText('2/8 vagas')).toBeInTheDocument();
  });
});
