package com.david.matchcv.service;

import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.david.matchcv.domain.FeedbackAts;
import com.david.matchcv.exception.AiException;

import org.springframework.stereotype.Service;

@Service
public class VagaUnicaAiAnalyzer {

    private static final String INSTRUCAO = """
            Você é um especialista em carreiras que realiza duas tarefas simultâneas para uma vaga específica.

            TAREFA 1 — Roadmap de estudos direcionado:
            Crie um roadmap prático para preencher APENAS as skills do gap (o que o candidato ainda não tem).
            - Se não houver gap, retorne a string "Você já possui as skills necessárias para esta vaga." em roadmapDirecionado.
            - Organize em fases sequenciais, respeitando pré-requisitos.
            - Para cada skill: 1 recurso gratuito + 1 projeto prático sugerido.
            - Formato: Markdown conciso, sem introdução genérica.

            TAREFA 2 — Simulação ATS (Applicant Tracking System):
            Avalie o currículo com HONESTIDADE como um ATS avaliaria para esta vaga específica.
            - pontuacaoEstimada: 0 a 100 — reflete a realidade sem inflar nem deflacionar.
            - pontosFavoraveis: o que está funcionando bem para esta vaga. Liste algo genuíno.
            - pontosDeAtencao: APENAS problemas REAIS e verificáveis no texto do currículo.
              Um CV bem estruturado e alinhado pode ter lista vazia — isso é um elogio, não uma falha.
              NÃO fabrique sugestões onde não há problema real.
            - acoesPrioritarias: até 5 ações de maior impacto. Lista vazia se o CV já está forte.
            """;

    private final AnthropicClient anthropicClient;

    public VagaUnicaAiAnalyzer(AnthropicClient anthropicClient) {
        this.anthropicClient = anthropicClient;
    }

    public ResultadoIa analisar(String descricaoVaga, String textoCv, List<String> match, List<String> gap) {
        String entrada = formatarEntrada(descricaoVaga, textoCv, match, gap);
        try {
            StructuredMessageCreateParams<ResultadoIa> params = MessageCreateParams.builder()
                    .model("claude-sonnet-4-6")
                    .maxTokens(3072L)
                    .system(INSTRUCAO)
                    .outputConfig(ResultadoIa.class)
                    .addUserMessage(entrada)
                    .build();

            return anthropicClient.messages().create(params).content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(typed -> typed.text())
                    .findFirst()
                    .orElseThrow(() -> new AiException("Resposta vazia da IA.", null));
        } catch (AiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AiException("Não foi possível gerar a análise da vaga.", ex);
        }
    }

    private String formatarEntrada(String descricaoVaga, String textoCv, List<String> match, List<String> gap) {
        String matchStr = match.isEmpty() ? "(nenhuma)" : String.join(", ", match);
        String gapStr = gap.isEmpty() ? "(nenhum — CV já cobre todas as skills da vaga)" : String.join(", ", gap);
        return """
                ## Descrição da vaga
                %s

                ## Currículo do candidato (texto extraído do PDF)
                %s

                ## Análise de skills (para guiar o roadmap)
                Skills que o candidato JÁ TEM para esta vaga: %s
                Skills que o candidato AINDA FALTA (foco do roadmap): %s
                """.formatted(descricaoVaga, textoCv, matchStr, gapStr);
    }

    // Tipo estruturado que a IA preenche em uma única chamada (roadmap + ATS juntos).
    public record ResultadoIa(String roadmapDirecionado, FeedbackAts feedbackAts) {
    }
}
