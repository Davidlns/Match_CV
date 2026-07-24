package com.david.matchcv.controller;

import java.util.List;

import com.david.matchcv.domain.SkillAgregada;
import com.david.matchcv.dto.AnalisarVagasRequest;
import com.david.matchcv.dto.AnaliseVagasResponse;
import com.david.matchcv.service.VagaAnalysisService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {

    private final VagaAnalysisService vagaAnalysisService;

    public AnalysisController(VagaAnalysisService vagaAnalysisService) {
        this.vagaAnalysisService = vagaAnalysisService;
    }

    @PostMapping("/api/skills/analyze")
    public AnaliseVagasResponse analisar(@Valid @RequestBody AnalisarVagasRequest request) {
        List<SkillAgregada> skills = vagaAnalysisService.analisar(request.descricoesVagas());
        return new AnaliseVagasResponse(request.descricoesVagas().size(), skills);
    }
}
