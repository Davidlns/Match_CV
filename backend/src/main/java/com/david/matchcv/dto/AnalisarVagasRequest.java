package com.david.matchcv.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

// Entrada: uma lista de descrições de vaga. Cada item não pode ser vazio.
public record AnalisarVagasRequest(
        @NotEmpty(message = "Envie ao menos uma descrição de vaga.")
        List<@NotBlank(message = "A descrição da vaga não pode ser vazia.") String> descricoesVagas
) {
}
