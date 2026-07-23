package com.david.matchcv.dto;

import java.util.List;

import com.david.matchcv.domain.FeedbackAts;

public record AnaliseVagaUnicaResponse(
        int sinergia,
        List<String> match,
        List<String> gap,
        List<String> extra,
        String roadmapDirecionado,
        FeedbackAts feedbackAts) {
}
