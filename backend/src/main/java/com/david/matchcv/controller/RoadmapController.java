package com.david.matchcv.controller;

import com.david.matchcv.dto.GerarRoadmapRequest;
import com.david.matchcv.dto.RoadmapResponse;
import com.david.matchcv.service.RoadmapService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    /**
     * Gera o roadmap de estudos personalizado com base nas skills do gap analysis.
     * O frontend envia as skills que o usuário ainda não tem (campo "gap" da análise)
     * com suas prioridades, e recebe um roadmap em Markdown.
     * POST /api/roadmap
     */
    @PostMapping
    public ResponseEntity<RoadmapResponse> gerarRoadmap(
            @Valid @RequestBody GerarRoadmapRequest request) {
        String roadmap = roadmapService.gerarRoadmap(request.skills());
        return ResponseEntity.ok(new RoadmapResponse(roadmap));
    }
}
