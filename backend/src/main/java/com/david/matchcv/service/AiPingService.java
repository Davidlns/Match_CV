package com.david.matchcv.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.david.matchcv.exception.AiException;

import org.springframework.stereotype.Service;

@Service
public class AiPingService {

    private final AnthropicClient anthropicClient;

    // O Spring injeta o AnthropicClient que registramos no AnthropicConfig.
    public AiPingService(AnthropicClient anthropicClient) {
        this.anthropicClient = anthropicClient;
    }

    public String ping() {
        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model("claude-haiku-4-5")
                    .maxTokens(50L)
                    .addUserMessage("Responda apenas com a palavra: pong")
                    .build();

            Message response = anthropicClient.messages().create(params);

            return extrairTexto(response);
        } catch (RuntimeException ex) {
            // Fronteira com o mundo externo: qualquer falha do SDK vira exceção de domínio.
            throw new AiException("Não foi possível obter resposta da IA.", ex);
        }
    }

    // A resposta vem em "blocos" de conteúdo; juntamos o texto de todos.
    private String extrairTexto(Message response) {
        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .reduce("", String::concat)
                .trim();
    }
}
