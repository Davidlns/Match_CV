package com.david.matchcv.service;

import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.david.matchcv.exception.AiException;

import org.springframework.stereotype.Service;

@Service
public class SkillExtractionService {

    // Prompt de instrução: curto e direto (economia de tokens). A normalização
    // de nomes é crítica para o vocabulário ser consistente entre vagas e CV.
    private static final String INSTRUCAO = """
            Você extrai as skills técnicas de uma descrição de vaga de emprego.
            Retorne apenas skills técnicas: linguagens, frameworks, bibliotecas,
            ferramentas, bancos de dados, plataformas e práticas de engenharia.

            Regras de normalização (CRÍTICAS: as skills serão comparadas por
            igualdade de texto entre vagas e currículos, então a grafia precisa
            ser sempre idêntica para a mesma skill):
            - Use sempre o nome padrão da indústria de tecnologia, em inglês
              quando esse for o uso consagrado (ex.: "Microservices",
              "Message Queue", "Relational Databases", "Unit Testing").
            - Colapse variações para uma única forma canônica (ex.:
              "React"/"ReactJS"/"React.js" -> "React"; "Node"/"NodeJS" -> "Node.js";
              "Postgres" -> "PostgreSQL").
            - Não repita skills.

            Ignore itens não técnicos: idiomas, soft skills, benefícios, tipo de
            contrato e tempo de experiência.
            """;

    private final AnthropicClient anthropicClient;

    public SkillExtractionService(AnthropicClient anthropicClient) {
        this.anthropicClient = anthropicClient;
    }

    public List<String> extrairSkills(String descricaoVaga) {
        try {
            // Saída estruturada: a IA preenche o record SkillsExtraidas diretamente.
            StructuredMessageCreateParams<SkillsExtraidas> params = MessageCreateParams.builder()
                    .model("claude-haiku-4-5")
                    .maxTokens(1024L)
                    .system(INSTRUCAO)
                    .outputConfig(SkillsExtraidas.class)
                    .addUserMessage(descricaoVaga)
                    .build();

            return anthropicClient.messages().create(params).content().stream()
                    .flatMap(block -> block.text().stream())
                    .flatMap(extraidas -> extraidas.text().skills().stream())
                    .toList();
        } catch (RuntimeException ex) {
            throw new AiException("Não foi possível extrair as skills da vaga.", ex);
        }
    }

    // Formato tipado que a IA preenche (saída estruturada).
    public record SkillsExtraidas(List<String> skills) {
    }
}
