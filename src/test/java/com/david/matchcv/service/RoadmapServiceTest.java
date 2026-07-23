package com.david.matchcv.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlock;
import com.david.matchcv.domain.Prioridade;
import com.david.matchcv.dto.SkillParaAprender;
import com.david.matchcv.exception.AiException;

import org.junit.jupiter.api.Test;

class RoadmapServiceTest {

    private static final List<SkillParaAprender> SKILLS_DE_TESTE = List.of(
            new SkillParaAprender("Java", Prioridade.ALTA),
            new SkillParaAprender("Docker", Prioridade.MEDIA)
    );

    @Test
    void deveRetornarRoadmapNoSucesso() {
        AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);

        TextBlock textBlock = mock(TextBlock.class);
        when(textBlock.text()).thenReturn("## Fase 1: Java\n- Comece pela documentação oficial.");

        ContentBlock block = mock(ContentBlock.class);
        when(block.text()).thenReturn(Optional.of(textBlock));

        Message message = mock(Message.class);
        when(message.content()).thenReturn(List.of(block));

        when(client.messages().create(any(MessageCreateParams.class))).thenReturn(message);

        RoadmapService service = new RoadmapService(client);
        String roadmap = service.gerarRoadmap(SKILLS_DE_TESTE);

        assertThat(roadmap).contains("## Fase 1: Java");
    }

    @Test
    void deveLancarAiExceptionQuandoAIaFalha() {
        AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);
        when(client.messages().create(any(MessageCreateParams.class)))
                .thenThrow(new RuntimeException("falha simulada do SDK"));

        RoadmapService service = new RoadmapService(client);

        assertThatThrownBy(() -> service.gerarRoadmap(SKILLS_DE_TESTE))
                .isInstanceOf(AiException.class)
                .hasMessageContaining("roadmap");
    }
}
