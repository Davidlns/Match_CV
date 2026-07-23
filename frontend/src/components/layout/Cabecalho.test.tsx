import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Cabecalho } from './Cabecalho';

vi.mock('next-themes', () => ({
  useTheme: vi.fn(),
}));

const { useTheme } = await import('next-themes');
const mockUseTheme = vi.mocked(useTheme);

const mockSetTheme = vi.fn();

afterEach(() => {
  vi.clearAllMocks();
});

describe('Cabecalho', () => {
  it('renderiza o nome da aplicação', () => {
    mockUseTheme.mockReturnValue({ resolvedTheme: 'dark', setTheme: mockSetTheme } as unknown as ReturnType<typeof useTheme>);

    render(<Cabecalho />);

    expect(screen.getByText('match_cv')).toBeInTheDocument();
  });

  it('exibe botão com label para mudar para modo claro quando o tema é escuro', () => {
    mockUseTheme.mockReturnValue({ resolvedTheme: 'dark', setTheme: mockSetTheme } as unknown as ReturnType<typeof useTheme>);

    render(<Cabecalho />);

    expect(
      screen.getByRole('button', { name: /mudar para modo claro/i }),
    ).toBeInTheDocument();
  });

  it('exibe botão com label para mudar para modo escuro quando o tema é claro', () => {
    mockUseTheme.mockReturnValue({ resolvedTheme: 'light', setTheme: mockSetTheme } as unknown as ReturnType<typeof useTheme>);

    render(<Cabecalho />);

    expect(
      screen.getByRole('button', { name: /mudar para modo escuro/i }),
    ).toBeInTheDocument();
  });

  it('chama setTheme com "light" ao clicar no toggle em modo escuro', async () => {
    mockUseTheme.mockReturnValue({ resolvedTheme: 'dark', setTheme: mockSetTheme } as unknown as ReturnType<typeof useTheme>);

    const user = userEvent.setup();
    render(<Cabecalho />);

    await user.click(screen.getByRole('button'));

    expect(mockSetTheme).toHaveBeenCalledWith('light');
  });

  it('chama setTheme com "dark" ao clicar no toggle em modo claro', async () => {
    mockUseTheme.mockReturnValue({ resolvedTheme: 'light', setTheme: mockSetTheme } as unknown as ReturnType<typeof useTheme>);

    const user = userEvent.setup();
    render(<Cabecalho />);

    await user.click(screen.getByRole('button'));

    expect(mockSetTheme).toHaveBeenCalledWith('dark');
  });
});
