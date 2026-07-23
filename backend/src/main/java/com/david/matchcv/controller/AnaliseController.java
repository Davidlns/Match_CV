package com.david.matchcv.controller;

import java.util.List;

import com.david.matchcv.dto.AnaliseCompletaResponse;
import com.david.matchcv.service.AnaliseCompletaService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analise")
public class AnaliseController {

    private final AnaliseCompletaService analiseCompletaService;

    public AnaliseController(AnaliseCompletaService analiseCompletaService) {
        this.analiseCompletaService = analiseCompletaService;
    }

    /**
     * Análise completa: vagas + currículo.
     * Recebe multipart/form-data com:
     *   - "arquivo": o PDF do currículo.
     *   - "descricoesVagas": uma ou mais descrições de vaga (um campo por vaga).
     * Devolve skills agregadas por prioridade, gap analysis e sinergia por vaga.
     * POST /api/analise/completa
     */
    @PostMapping(value = "/completa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnaliseCompletaResponse> analisar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("descricoesVagas") List<String> descricoesVagas) {
        return ResponseEntity.ok(analiseCompletaService.analisar(descricoesVagas, arquivo));
    }
}
