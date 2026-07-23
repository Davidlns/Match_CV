package com.david.matchcv.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record GerarRoadmapRequest(
        @NotEmpty(message = "Envie ao menos uma skill para o roadmap.")
        List<@Valid SkillParaAprender> skills) {
}
