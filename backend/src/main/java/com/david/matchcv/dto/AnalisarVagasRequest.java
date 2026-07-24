package com.david.matchcv.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnalisarVagasRequest(
        @NotNull(message = "Envie entre 3 e 8 descrições de vaga.")
        @Size(min = 3, max = 8, message = "Envie entre 3 e 8 descrições de vaga.")
        List<@NotBlank(message = "A descrição da vaga não pode ser vazia.") String> descricoesVagas
) {
}
