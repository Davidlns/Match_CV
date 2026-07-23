package com.david.matchcv.domain;

import java.util.List;

/**
 * Resultado do gap analysis: cruzamento entre skills das vagas e do currículo.
 * match        — skills que o usuário tem e as vagas pedem.
 * gap          — skills que as vagas pedem mas o usuário não tem.
 * extra        — skills que o usuário tem mas nenhuma vaga pediu.
 * sinergiaMedia — média percentual de sinergia entre o CV e todas as vagas.
 * vagasComSinergia — sinergia individual por vaga (na ordem em que foram enviadas).
 */
public record ResultadoGap(
        List<String> match,
        List<String> gap,
        List<String> extra,
        int sinergiaMedia,
        List<SinergiaVaga> vagasComSinergia) {
}
