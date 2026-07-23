package com.david.matchcv.controller;

import com.david.matchcv.dto.AnaliseVagaUnicaResponse;
import com.david.matchcv.service.AnaliseVagaUnicaService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analise")
public class VagaUnicaController {

    private final AnaliseVagaUnicaService analiseVagaUnicaService;

    public VagaUnicaController(AnaliseVagaUnicaService analiseVagaUnicaService) {
        this.analiseVagaUnicaService = analiseVagaUnicaService;
    }

    @PostMapping(value = "/vaga-unica", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnaliseVagaUnicaResponse> analisarVagaUnica(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("descricaoVaga") String descricaoVaga) {
        return ResponseEntity.ok(analiseVagaUnicaService.analisar(descricaoVaga, arquivo));
    }
}
