package com.david.matchcv.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.david.matchcv.exception.AiException;

import org.junit.jupiter.api.Test;

class VagaUnicaAiAnalyzerTest {

    private final AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);
    private final VagaUnicaAiAnalyzer analyzer = new VagaUnicaAiAnalyzer(client);

    @Test
    void deveEmbrulharExcecaoDoSdkEmAiException() {
        when(client.messages().create(any(StructuredMessageCreateParams.class)))
                .thenThrow(new RuntimeException("falha simulada do SDK"));

        assertThatThrownBy(() -> analyzer.analisar("descricao", "texto cv", List.of(), List.of()))
                .isInstanceOf(AiException.class)
                .hasMessageContaining("Não foi possível gerar a análise da vaga.");
    }

    @Test
    void deveRepassarAiExceptionSemEmbrulhar() {
        when(client.messages().create(any(StructuredMessageCreateParams.class)))
                .thenThrow(new AiException("erro direto", null));

        assertThatThrownBy(() -> analyzer.analisar("descricao", "texto cv", List.of("Java"), List.of("Docker")))
                .isInstanceOf(AiException.class)
                .hasMessageContaining("erro direto");
    }
}
