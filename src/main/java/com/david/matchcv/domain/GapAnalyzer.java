package com.david.matchcv.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Lógica de gap analysis em Java puro (sem IA).
 *
 * Compara skills das vagas com skills do currículo usando comparação
 * case-insensitive (segurança extra, já que a IA normaliza para o mesmo
 * vocabulário, mas pode haver variações de caixa residuais).
 * O nome canônico exibido é o da primeira ocorrência nas vagas.
 */
@Component
public class GapAnalyzer {

    public ResultadoGap analisar(List<List<SkillExtraida>> skillsPorVaga,
                                  List<SkillExtraida> skillsDoCv) {

        Map<String, String> cvSkills = toCanonicalMap(skillsDoCv);

        // União de todas as skills de todas as vagas, deduplcada (lowercase → nome canônico).
        Map<String, String> allVagaSkills = new LinkedHashMap<>();
        for (List<SkillExtraida> vagaSkills : skillsPorVaga) {
            for (SkillExtraida skill : vagaSkills) {
                allVagaSkills.putIfAbsent(skill.nome().toLowerCase(), skill.nome());
            }
        }

        List<String> match = new ArrayList<>();
        List<String> gap = new ArrayList<>();
        for (Map.Entry<String, String> entry : allVagaSkills.entrySet()) {
            if (cvSkills.containsKey(entry.getKey())) {
                match.add(entry.getValue());
            } else {
                gap.add(entry.getValue());
            }
        }

        List<String> extra = cvSkills.entrySet().stream()
                .filter(e -> !allVagaSkills.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        List<SinergiaVaga> vagasComSinergia = calcularSinergias(skillsPorVaga, cvSkills);

        int sinergiaMedia = vagasComSinergia.isEmpty() ? 0
                : (int) vagasComSinergia.stream()
                        .mapToInt(SinergiaVaga::sinergia)
                        .average()
                        .orElse(0);

        return new ResultadoGap(match, gap, extra, sinergiaMedia, vagasComSinergia);
    }

    private List<SinergiaVaga> calcularSinergias(List<List<SkillExtraida>> skillsPorVaga,
                                                   Map<String, String> cvSkills) {
        List<SinergiaVaga> result = new ArrayList<>();
        for (int i = 0; i < skillsPorVaga.size(); i++) {
            Set<String> uniqueVagaSkills = skillsPorVaga.get(i).stream()
                    .map(s -> s.nome().toLowerCase())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            long matched = uniqueVagaSkills.stream().filter(cvSkills::containsKey).count();
            int sinergia = uniqueVagaSkills.isEmpty() ? 0
                    : (int) (matched * 100 / uniqueVagaSkills.size());

            result.add(new SinergiaVaga(i + 1, sinergia));
        }
        return result;
    }

    // Mapeia nome.toLowerCase() → nome (casing original), primeira ocorrência ganha.
    private Map<String, String> toCanonicalMap(List<SkillExtraida> skills) {
        Map<String, String> map = new LinkedHashMap<>();
        for (SkillExtraida s : skills) {
            map.putIfAbsent(s.nome().toLowerCase(), s.nome());
        }
        return map;
    }
}
