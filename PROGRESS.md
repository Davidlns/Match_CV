# PROGRESS — Match_CV

Estado atual do projeto. **Atualizar sempre que uma fatia for concluída ou uma
mudança relevante for feita.** Para o contexto e as decisões fixas do projeto,
ver `CLAUDE.md` (inclui o roadmap completo das fatias).

- **Última atualização:** 2026-07-21
- **Fatia atual:** Fatia 2 concluída → **próxima: Fatia 3** (N vagas + agregação + prioridade).

## Fatias

- [x] **Fatia 0** — `GET /api/hello` retornando JSON. App sobe e responde.
- [x] **Fatia 1** — Integração com a IA (validada ponta a ponta: `pong` no navegador):
  - `config/AnthropicConfig` registra o `AnthropicClient` (chave via propriedade `anthropic.api-key` — ver Decisões técnicas).
  - `service/AiPingService` faz um "ping" no Haiku; `controller/AiPingController` expõe `GET /api/ai/ping`.
  - Tratamento de erro: `exception/AiException` + `exception/GlobalExceptionHandler` (`@RestControllerAdvice` → 503; loga a causa real no servidor).
  - Testes: `AiPingServiceTest` (Mockito) e `AiPingControllerTest` (`@WebMvcTest`). Suíte verde.
- [x] **Fatia 2** — Extrair skills de 1 vaga (validada ponta a ponta: normalização e filtro OK):
  - `POST /api/skills/extract` recebe `{"descricaoVaga": "..."}` e devolve `{"skills": [...]}`.
  - `service/SkillExtractionService` chama o Haiku com **saída estruturada** (record `SkillsExtraidas`) + prompt de normalização com **forma canônica em inglês** (grafia idêntica entre vaga e CV, para o gap analysis casar na Fatia 5); `max_tokens` justo (1024), sem thinking.
  - DTOs `dto/ExtrairSkillsRequest` (com `@NotBlank`) e `dto/SkillsResponse`; validação `@Valid` → 400 no handler global.
  - Dependência nova: `spring-boot-starter-validation`.
  - Testes: `SkillControllerTest` (200/400/503) e `SkillExtractionServiceTest` (falha → `AiException`). Suíte verde (9 testes).
- [ ] **Fatia 3** — N vagas + agregação por frequência + prioridade Alta/Média/Baixa (Java puro + testes).
- [ ] **Fatia 4** — Upload do CV em PDF → PDFBox em memória → skills do CV.
- [ ] **Fatia 5** — Gap analysis (match / gap / extra) — lógica de conjunto em Java puro.
- [ ] **Fatia 6** — Roadmap de estudo sob demanda (IA).
- [ ] **Transversal** — Rate limiting por IP nos endpoints de IA (após a Fatia 2).

## Decisões técnicas já tomadas

- **Stack:** Java 21, Spring Boot 4.0.7 (Maven, wrapper `mvnw`).
- **IA:** SDK oficial `com.anthropic:anthropic-java:2.34.0`. Modelo de extração: Haiku 4.5 (`claude-haiku-4-5`).
- **Chave da API:** só no backend. Mecanismo: propriedade `anthropic.api-key`, lida via `@Value("${anthropic.api-key:}")` no `AnthropicConfig`.
  - Dev: profile `local` (ativo por padrão em `application.properties`) carrega `application-local.properties` (no `.gitignore`), onde a chave real fica. Template versionado: `application-local.properties.example`.
  - Fallback: `application.properties` define `anthropic.api-key=${ANTHROPIC_API_KEY:}`, então a variável de ambiente também funciona (prod/CI). Default vazio → app sobe mesmo sem chave; erro (503) só na chamada.
- **Frontend:** decidido fazer backend primeiro, Next.js depois.
- **Padrões obrigatórios:** testes JUnit, `@RestControllerAdvice`, Clean Code (ver `CLAUDE.md`).
- **Modelos e tokens:** começar no modelo mais básico que resolve (Java puro > Haiku > Sonnet > Opus); `max_tokens` justo, saída estruturada, sem thinking em tarefa mecânica (política completa no `CLAUDE.md`).

## Estrutura atual (arquivos-chave)

```
src/main/java/com/david/matchcv/
├── MatchCvApplication.java
├── config/AnthropicConfig.java
├── controller/HelloController.java, AiPingController.java, SkillController.java
├── service/AiPingService.java, SkillExtractionService.java
├── dto/ExtrairSkillsRequest.java, SkillsResponse.java
└── exception/AiException.java, GlobalExceptionHandler.java

src/test/java/com/david/matchcv/
├── MatchCvApplicationTests.java
├── controller/AiPingControllerTest.java, SkillControllerTest.java
└── service/AiPingServiceTest.java, SkillExtractionServiceTest.java
```

## Como rodar / testar

1. Copie `src/main/resources/application-local.properties.example` para
   `application-local.properties` (mesma pasta) e cole sua chave no lugar do placeholder.
2. Rode:

```powershell
.\mvnw.cmd spring-boot:run   # sobe a app (porta 8080)
.\mvnw.cmd test              # roda a suite de testes
```

(Alternativa sem arquivo local: definir a variavel de ambiente `ANTHROPIC_API_KEY`.)

Endpoints atuais: `GET /api/hello`, `GET /api/ai/ping`, `POST /api/skills/extract`.
