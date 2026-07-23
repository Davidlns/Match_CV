package com.david.matchcv.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.david.matchcv.exception.AiException;

import org.junit.jupiter.api.Test;

class SkillExtractionServiceTest {

    @Test
    void deveLancarAiExceptionQuandoAIaFalha() {
        AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);
        when(client.messages().create(any(StructuredMessageCreateParams.class)))
                .thenThrow(new RuntimeException("falha simulada do SDK"));

        SkillExtractionService service = new SkillExtractionService(client);

        assertThatThrownBy(() -> service.extrairSkills("Vaga com React"))
                .isInstanceOf(AiException.class);
    }
}
