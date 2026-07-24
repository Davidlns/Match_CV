package com.david.matchcv.domain;

/**
 * Estrato de consenso de uma skill entre as vagas, por densidade de presença.
 * A ordem dos valores dá a ordem de exibição (mais requisitada primeiro).
 * O rótulo visível ("Ultra/Muito/Pouco requisitadas") é responsabilidade do
 * frontend — este enum é só a faixa determinística.
 */
public enum EstratoConsenso {
    PRATICAMENTE_TODAS,
    FREQUENTE,
    PONTUAL
}
