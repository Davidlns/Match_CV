package com.david.matchcv.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicConfig {

    @Bean
    public AnthropicClient anthropicClient(@Value("${anthropic.api-key:}") String apiKey) {
        // A propriedade anthropic.api-key vem de application-local.properties (dev)
        // ou da variavel de ambiente ANTHROPIC_API_KEY (fallback). Nunca do codigo.
        return AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}
