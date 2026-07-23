package com.david.matchcv.service;

import java.util.List;
import java.util.stream.Collectors;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.david.matchcv.domain.Prioridade;
import com.david.matchcv.dto.SkillParaAprender;
import com.david.matchcv.exception.AiException;

import org.springframework.stereotype.Service;

/**
 * Gera o roadmap de estudos usando Sonnet 4.6.
 *
 * Por quê Sonnet e não Haiku aqui?
 * O roadmap exige raciocínio real: ordenar skills por dependência de aprendizado
 * (ex.: Docker antes de Kubernetes), recomendar recursos de qualidade e sugerir
 * projetos práticos coerentes. Haiku é bom para extração mecânica; para síntese
 * personalizada e geração de qualidade, Sonnet é o nível correto (CLAUDE.md).
 */
@Service
public class RoadmapService {

    private static final String INSTRUCAO = """
            Você é um coach de estudos para desenvolvedores que estão buscando a primeira vaga.
            Crie um roadmap de estudos prático e realista com base nas skills que o usuário precisa aprender.

            Organize em fases sequenciais respeitando pré-requisitos (ex.: Docker antes de Kubernetes,
            SQL antes de ORM). Dê mais atenção às skills de prioridade ALTA.

            Para cada skill inclua:
            - Por onde começar: 1 recurso gratuito (documentação oficial, tutorial ou curso free).
            - Projeto prático: 1 exercício simples para fixar o conteúdo.

            Formato: Markdown com cabeçalhos (##) e listas. Seja direto. Sem introdução genérica.
            """;

    private final AnthropicClient anthropicClient;

    public RoadmapService(AnthropicClient anthropicClient) {
        this.anthropicClient = anthropicClient;
    }

    public String gerarRoadmap(List<SkillParaAprender> skills) {
        String entrada = formatarEntrada(skills);
        try {
            Message response = anthropicClient.messages().create(
                    MessageCreateParams.builder()
                            .model("claude-sonnet-4-6")
                            .maxTokens(2048L)
                            .system(INSTRUCAO)
                            .addUserMessage(entrada)
                            .build());

            return response.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(textBlock -> textBlock.text())
                    .reduce("", String::concat)
                    .trim();
        } catch (RuntimeException ex) {
            throw new AiException("Não foi possível gerar o roadmap de estudos.", ex);
        }
    }

    private String formatarEntrada(List<SkillParaAprender> skills) {
        StringBuilder sb = new StringBuilder("Skills para aprender:\n");

        appendGrupo(sb, "ALTA (requisito na maioria das vagas)",
                filtrar(skills, Prioridade.ALTA));
        appendGrupo(sb, "MÉDIA (relevante em parte das vagas)",
                filtrar(skills, Prioridade.MEDIA));
        appendGrupo(sb, "BAIXA (diferencial)",
                filtrar(skills, Prioridade.BAIXA));

        List<String> semPrioridade = skills.stream()
                .filter(s -> s.prioridade() == null)
                .map(SkillParaAprender::nome)
                .toList();
        appendGrupo(sb, "Sem classificação", semPrioridade);

        return sb.toString().trim();
    }

    private List<String> filtrar(List<SkillParaAprender> skills, Prioridade prioridade) {
        return skills.stream()
                .filter(s -> prioridade.equals(s.prioridade()))
                .map(SkillParaAprender::nome)
                .toList();
    }

    private void appendGrupo(StringBuilder sb, String rotulo, List<String> nomes) {
        if (!nomes.isEmpty()) {
            sb.append("\nPrioridade ").append(rotulo).append(":\n");
            nomes.forEach(n -> sb.append("- ").append(n).append("\n"));
        }
    }
}
