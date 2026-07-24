package com.david.matchcv.controller;

import java.util.List;

import com.david.matchcv.dto.AnaliseCompletaResponse;
import com.david.matchcv.exception.VagasForaDoLimiteException;
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

    private static final int MINIMO_VAGAS = 3;
    private static final int MAXIMO_VAGAS = 8;

    private final AnaliseCompletaService analiseCompletaService;

    public AnaliseController(AnaliseCompletaService analiseCompletaService) {
        this.analiseCompletaService = analiseCompletaService;
    }

    @PostMapping(value = "/completa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnaliseCompletaResponse> analisar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("descricoesVagas") List<String> descricoesVagas) {
        if (descricoesVagas.size() < MINIMO_VAGAS || descricoesVagas.size() > MAXIMO_VAGAS) {
            throw new VagasForaDoLimiteException("Envie entre 3 e 8 descrições de vaga.");
        }
        return ResponseEntity.ok(analiseCompletaService.analisar(descricoesVagas, arquivo));
    }
}
