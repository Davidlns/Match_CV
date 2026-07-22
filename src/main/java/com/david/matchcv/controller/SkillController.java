package com.david.matchcv.controller;

import java.util.List;

import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.dto.ExtrairSkillsRequest;
import com.david.matchcv.dto.SkillsResponse;
import com.david.matchcv.service.SkillExtractionService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SkillController {

    private final SkillExtractionService skillExtractionService;

    public SkillController(SkillExtractionService skillExtractionService) {
        this.skillExtractionService = skillExtractionService;
    }

    @PostMapping("/api/skills/extract")
    public SkillsResponse extrair(@Valid @RequestBody ExtrairSkillsRequest request) {
        List<SkillExtraida> skills = skillExtractionService.extrairSkills(request.descricaoVaga());
        return new SkillsResponse(skills);
    }
}
