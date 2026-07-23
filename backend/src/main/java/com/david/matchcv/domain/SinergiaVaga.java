package com.david.matchcv.domain;

/**
 * Sinergia de uma vaga específica com o currículo do usuário.
 * indice: posição 1-based na ordem em que as vagas foram enviadas.
 * sinergia: % das skills daquela vaga que o currículo já cobre (0–100).
 */
public record SinergiaVaga(int indice, int sinergia) {
}
