package com.david.matchcv.domain;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Lógica determinística (sem IA). Combina o tipo da skill (obrigatória/diferencial)
 * com a frequência entre vagas:
 *   - ALTA  se é obrigatória em >=70% das vagas;
 *   - senão MEDIA se aparece (obrigatória ou diferencial) em >=40% das vagas;
 *   - senão BAIXA.
 * Funciona com 1 vaga (obrigatória -> ALTA, diferencial -> MEDIA).
 */
@Component
public class SkillPriorityCalculator {

    private static final int LIMIAR_OBRIGATORIA_ALTA = 70;
    private static final int LIMIAR_PRESENCA_MEDIA = 40;

    public List<SkillPrioridade> calcular(List<List<SkillExtraida>> skillsPorVaga) {
        int totalVagas = skillsPorVaga.size();
        if (totalVagas == 0) {
            return List.of();
        }

        Map<String, Contagem> contagens = contar(skillsPorVaga);

        return contagens.entrySet().stream()
                .map(entrada -> criar(entrada.getKey(), entrada.getValue(), totalVagas))
                .sorted(Comparator.comparing(SkillPrioridade::prioridade)
                        .thenComparing(Comparator.comparingInt(SkillPrioridade::frequencia).reversed())
                        .thenComparing(SkillPrioridade::nome))
                .toList();
    }

    private Map<String, Contagem> contar(List<List<SkillExtraida>> skillsPorVaga) {
        Map<String, Contagem> contagens = new LinkedHashMap<>();
        for (List<SkillExtraida> skillsDaVaga : skillsPorVaga) {
            // Colapsa por nome dentro da vaga; se for obrigatória em qualquer
            // ocorrência, conta como obrigatória naquela vaga.
            Map<String, Boolean> obrigatoriaNaVaga = new LinkedHashMap<>();
            for (SkillExtraida skill : skillsDaVaga) {
                boolean obrigatoria = skill.tipo() == TipoSkill.OBRIGATORIA;
                obrigatoriaNaVaga.merge(skill.nome(), obrigatoria, (a, b) -> a || b);
            }
            obrigatoriaNaVaga.forEach((nome, obrigatoria) -> {
                Contagem c = contagens.computeIfAbsent(nome, k -> new Contagem());
                c.frequencia++;
                if (obrigatoria) {
                    c.obrigatoriaEm++;
                }
            });
        }
        return contagens;
    }

    private SkillPrioridade criar(String nome, Contagem c, int totalVagas) {
        int percentualObrigatoria = (c.obrigatoriaEm * 100) / totalVagas;
        int percentualPresenca = (c.frequencia * 100) / totalVagas;
        Prioridade prioridade = classificar(percentualObrigatoria, percentualPresenca);
        return new SkillPrioridade(nome, c.frequencia, c.obrigatoriaEm, percentualPresenca, prioridade);
    }

    private Prioridade classificar(int percentualObrigatoria, int percentualPresenca) {
        if (percentualObrigatoria >= LIMIAR_OBRIGATORIA_ALTA) {
            return Prioridade.ALTA;
        }
        if (percentualPresenca >= LIMIAR_PRESENCA_MEDIA) {
            return Prioridade.MEDIA;
        }
        return Prioridade.BAIXA;
    }

    // Contador mutável interno da agregação.
    private static final class Contagem {
        private int frequencia;
        private int obrigatoriaEm;
    }
}
