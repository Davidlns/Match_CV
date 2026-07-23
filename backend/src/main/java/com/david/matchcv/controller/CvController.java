package com.david.matchcv.controller;

import java.util.List;

import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.dto.SkillsResponse;
import com.david.matchcv.service.CvAnalysisService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final CvAnalysisService cvAnalysisService;

    public CvController(CvAnalysisService cvAnalysisService) {
        this.cvAnalysisService = cvAnalysisService;
    }

    /**
     * Recebe o currículo em PDF via multipart/form-data (campo "arquivo").
     * Retorna as skills técnicas extraídas com seus tipos (obrigatória/diferencial).
     * POST /api/cv/extract
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SkillsResponse> extrairSkillsDoCv(
            @RequestParam("arquivo") MultipartFile arquivo) {
        List<SkillExtraida> skills = cvAnalysisService.extrairSkillsDoCv(arquivo);
        return ResponseEntity.ok(new SkillsResponse(skills));
    }
}
