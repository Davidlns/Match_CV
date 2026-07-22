package com.david.matchcv.service;

import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.exception.AiException;

import org.springframework.stereotype.Service;

@Service
public class SkillExtractionService {

    // Prompt curto (economia de tokens), com classificação de tipo e normalização canônica.
    private static final String INSTRUCAO = """
            Você extrai as skills técnicas de uma descrição de vaga de emprego.
            Retorne apenas skills técnicas: linguagens, frameworks, bibliotecas,
            ferramentas, bancos de dados, plataformas e práticas de engenharia.

            Para cada skill, classifique o tipo:
            - OBRIGATORIA: listada como requisito/obrigatória.
            - DIFERENCIAL: listada como diferencial, desejável ou "nice to have".
            Se a vaga não distingue requisitos de diferenciais, use OBRIGATORIA
            (é requisito de contratação).

            Regras de normalização (CRÍTICAS: as skills serão comparadas por
            igualdade de texto entre vagas e currículos, então a grafia precisa
            ser sempre idêntica para a mesma skill):
            - Use sempre o nome padrão da indústria, em inglês quando esse for o
              uso consagrado (ex.: "Microservices", "Message Queue",
              "Relational Databases", "Unit Testing").
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

    public List<SkillExtraida> extrairSkills(String descricaoVaga) {
        try {
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
    public record SkillsExtraidas(List<SkillExtraida> skills) {
    }
}
