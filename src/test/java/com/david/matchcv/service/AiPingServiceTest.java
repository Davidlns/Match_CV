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
import com.david.matchcv.exception.AiException;

import org.junit.jupiter.api.Test;

class AiPingServiceTest {

    @Test
    void deveRetornarOTextoDaIaNoSucesso() {
        // Dublamos o client do SDK: nenhuma chamada real de rede acontece.
        AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);

        TextBlock textBlock = mock(TextBlock.class);
        when(textBlock.text()).thenReturn("pong");

        ContentBlock block = mock(ContentBlock.class);
        when(block.text()).thenReturn(Optional.of(textBlock));

        Message message = mock(Message.class);
        when(message.content()).thenReturn(List.of(block));

        when(client.messages().create(any(MessageCreateParams.class))).thenReturn(message);

        AiPingService service = new AiPingService(client);

        assertThat(service.ping()).isEqualTo("pong");
    }

    @Test
    void deveLancarAiExceptionQuandoAIaFalha() {
        AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);
        when(client.messages().create(any(MessageCreateParams.class)))
                .thenThrow(new RuntimeException("falha simulada do SDK"));

        AiPingService service = new AiPingService(client);

        assertThatThrownBy(service::ping).isInstanceOf(AiException.class);
    }
}
