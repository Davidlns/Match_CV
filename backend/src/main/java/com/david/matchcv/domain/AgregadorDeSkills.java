package com.david.matchcv.domain;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Agrega as skills extraídas de várias vagas (Java puro, sem IA).
 *
 * Para cada skill conta em quantas vagas aparece (frequencia) e em quantas é
 * obrigatória (obrigatoriaEm). Ordena por FREQUÊNCIA PONDERADA PELO TIPO
 * (peso = frequencia + obrigatoriaEm — cada aparição conta 1, cada aparição como
 * obrigatória conta +1) e estratifica por densidade de presença.
 *
 * O que se EXIBE é o dado bruto; o peso só ordena.
 */
@Component
public class AgregadorDeSkills {

    private static final int LIMIAR_PRATICAMENTE_TODAS = 80;
    private static final int LIMIAR_FREQUENTE = 40;

    public List<SkillAgregada> agregar(List<List<SkillExtraida>> skillsPorVaga) {
        int totalVagas = skillsPorVaga.size();
        if (totalVagas == 0) {
            return List.of();
        }

        Map<String, Contagem> contagens = contar(skillsPorVaga);

        return contagens.entrySet().stream()
                .map(entrada -> criar(entrada.getKey(), entrada.getValue(), totalVagas))
                .sorted(ordem())
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

    private SkillAgregada criar(String nome, Contagem c, int totalVagas) {
        int percentual = (c.frequencia * 100) / totalVagas;
        EstratoConsenso estrato = estratificar(percentual);
        return new SkillAgregada(nome, c.frequencia, c.obrigatoriaEm, percentual, estrato);
    }

    private EstratoConsenso estratificar(int percentual) {
        if (percentual >= LIMIAR_PRATICAMENTE_TODAS) {
            return EstratoConsenso.PRATICAMENTE_TODAS;
        }
        if (percentual >= LIMIAR_FREQUENTE) {
            return EstratoConsenso.FREQUENTE;
        }
        return EstratoConsenso.PONTUAL;
    }

    // Estrato (mais requisitado primeiro) → peso desc → frequencia desc
    // → obrigatoriaEm desc → nome asc. Desempates estáveis.
    private Comparator<SkillAgregada> ordem() {
        return Comparator.comparing(SkillAgregada::estrato)
                .thenComparing(Comparator.comparingInt(this::peso).reversed())
                .thenComparing(Comparator.comparingInt(SkillAgregada::frequencia).reversed())
                .thenComparing(Comparator.comparingInt(SkillAgregada::obrigatoriaEm).reversed())
                .thenComparing(SkillAgregada::nome);
    }

    private int peso(SkillAgregada s) {
        return s.frequencia() + s.obrigatoriaEm();
    }

    // Contador mutável interno da agregação.
    private static final class Contagem {
        private int frequencia;
        private int obrigatoriaEm;
    }
}
