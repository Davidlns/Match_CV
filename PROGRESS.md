/# PROGRESS — Match_CV

Estado atual do projeto. **Atualizar sempre que uma fatia for concluída ou uma
mudança relevante for feita.** Para as decisões fixas do projeto, ver `CLAUDE.md`;
para o roadmap do redesenho e os contratos, ver `plano-tecnico.md`.

- **Última atualização:** 2026-07-23
- **Fatia atual:** **redesenho de produto planejado** (`redesenho-produto.md` +
  `plano-tecnico.md`); **Fase A ainda não iniciada**. Próximo: **Fatia 1 do
  redesenho** — nova agregação e apresentação de skills (dado bruto + estratos de
  consenso, substitui Alta/Média/Baixa). O núcleo do backend (fatias 0–7 + rate
  limiting) e o frontend F0–F2 estão concluídos; o redesenho reorganiza o produto
  em cinco fluxos por cima disso.

## Fatias

- [x] **Fatia 0** — `GET /api/hello` retornando JSON. App sobe e responde.
- [x] **Fatia 1** — Integração com a IA (validada ponta a ponta: `pong` no navegador):
  - `config/AnthropicConfig` registra o `AnthropicClient` (chave via propriedade `anthropic.api-key` — ver Decisões técnicas).
  - `service/AiPingService` faz um "ping" no Haiku; `controller/AiPingController` expõe `GET /api/ai/ping`.
  - Tratamento de erro: `exception/AiException` + `exception/GlobalExceptionHandler` (`@RestControllerAdvice` → 503; loga a causa real no servidor).
  - Testes: `AiPingServiceTest` (Mockito) e `AiPingControllerTest` (`@WebMvcTest`). Suíte verde.
- [x] **Fatia 2** — Extrair skills de 1 vaga (validada ponta a ponta: normalização e filtro OK):
  - `POST /api/skills/extract` recebe `{"descricaoVaga": "..."}` e devolve `{"skills": [{nome, tipo}]}` (tipo = OBRIGATORIA/DIFERENCIAL, classificado pela IA).
  - `service/SkillExtractionService` chama o Haiku com **saída estruturada** (`SkillsExtraidas` → `SkillExtraida{nome, tipo}`) + prompt de **normalização canônica em inglês** e **classificação obrigatória/diferencial**; `max_tokens` justo (1024), sem thinking. Domínio: enum `TipoSkill`, record `SkillExtraida`.
  - DTOs `dto/ExtrairSkillsRequest` (com `@NotBlank`) e `dto/SkillsResponse`; validação `@Valid` → 400 no handler global.
  - Dependência nova: `spring-boot-starter-validation`.
  - Testes: `SkillControllerTest` (200/400/503) e `SkillExtractionServiceTest` (falha → `AiException`). Suíte verde (9 testes).
- [x] **Fatia 3** — N vagas → agregação → prioridade (por **tipo + frequência**):
  - `POST /api/skills/analyze` recebe `{"descricoesVagas": [...]}` e devolve `{"totalVagas": N, "skills": [{nome, frequencia, obrigatoriaEm, percentual, prioridade}]}` (`percentual` = % de vagas em que aparece, pronto para o front visualizar).
  - **Prioridade:** ALTA se obrigatória em ≥70% das vagas; senão MEDIA se aparece em ≥40%; senão BAIXA. Funciona com 1 vaga (obrigatória→ALTA, diferencial→MEDIA). Sem `priorizacaoAplicavel` (o tipo torna a prioridade sempre significativa).
  - `domain/SkillPriorityCalculator` (Java puro, `@Component`): agrega frequência e obrigatoriedade por skill e classifica. Enum `Prioridade`, record `SkillPrioridade`.
  - `service/VagaAnalysisService` orquestra: extrai skills (com tipo) de cada vaga (reusa Fatia 2) → calcula.
  - DTOs `AnalisarVagasRequest` (`@NotEmpty` + itens `@NotBlank`) e `AnaliseVagasResponse`.
  - Testes: `SkillPriorityCalculatorTest` (4, lógica pura), `VagaAnalysisServiceTest` (2), `AnalysisControllerTest` (4). Suíte verde (19 testes).
- [x] **Fatia 4** — Upload do CV em PDF → PDFBox em memória → skills do CV (validada com testes).
  - `POST /api/cv/extract` recebe `multipart/form-data` (campo `arquivo`) e devolve `{"skills": [{nome, tipo}]}`.
  - `service/PdfTextExtractor` usa PDFBox 3.0.3 (`Loader.loadPDF(byte[])`) — tudo em memória, nunca grava em disco (LGPD).
  - `service/CvAnalysisService` valida arquivo vazio e orquestra PdfTextExtractor → SkillExtractionService (reutiliza Fatia 2 para garantir vocabulário canônico consistente entre vagas e CV).
  - `exception/InvalidPdfException` + handler no `GlobalExceptionHandler` → 400. Handler para `MaxUploadSizeExceededException` → 400 ("excede 10 MB").
  - Limite de upload configurado: `spring.servlet.multipart.max-file-size=10MB`.
  - Dependência nova: `org.apache.pdfbox:pdfbox:3.0.3`.
  - Testes: `CvAnalysisServiceTest` (3 casos) e `CvControllerTest` (3 casos). Suíte verde (25 testes).
- [x] **Fatia 5** — Gap analysis (match / gap / extra) + sinergia por vaga (validada com testes).
  - `POST /api/analise/completa` recebe `multipart/form-data` com `arquivo` (PDF) e `descricoesVagas[]`.
  - Resposta: `{totalVagas, skillsAgregadas, match, gap, extra, sinergiaMedia, vagasComSinergia}`.
  - `domain/GapAnalyzer` (Java puro): compara CV × vagas com case-insensitive matching; calcula `match`/`gap`/`extra` e sinergia por vaga (% das skills da vaga cobertas pelo CV) + média.
  - `domain/SinergiaVaga` e `domain/ResultadoGap`: tipos de domínio do gap analysis.
  - `service/AnaliseCompletaService`: orquestra as 4 etapas — extração por vaga (IA), agregação (Java puro), extração do CV (IA, mesmo prompt → vocabulário consistente), gap analysis (Java puro).
  - `controller/AnaliseController`: thin controller, param ausente → 400 automático do Spring.
  - Testes: `GapAnalyzerTest` (5 casos), `AnaliseCompletaServiceTest` (3), `AnaliseControllerTest` (4). Suíte verde (37 testes).
- [x] **Fatia 6** — Roadmap de estudo sob demanda (IA; Sonnet 4.6).
  - `POST /api/roadmap` recebe `{"skills": [{nome, prioridade}]}` e devolve `{"roadmap": "...markdown..."}`.
  - `service/RoadmapService`: chama Sonnet 4.6 (`claude-sonnet-4-6`, `max_tokens=2048`). Monta a entrada agrupada por prioridade (ALTA/MÉDIA/BAIXA) para o modelo priorizar o que importa nas vagas. Prompt curto e direto — pede fases sequenciais com pré-requisitos, 1 recurso gratuito e 1 projeto prático por skill.
  - `dto/GerarRoadmapRequest` (`@NotEmpty` + itens `@Valid @NotBlank`), `dto/SkillParaAprender`, `dto/RoadmapResponse`.
  - Modelo escolhido: Sonnet 4.6 (geração/síntese personalizada — raciocínio real para ordenar dependências de aprendizado). Haiku não é candidato para esta tarefa (CLAUDE.md).
  - Testes: `RoadmapServiceTest` (2 casos: sucesso + falha), `RoadmapControllerTest` (4 casos: 200/400 lista vazia/400 nome vazio/503). Suíte verde (43 testes).
- [x] **Fatia 7** — Experiência de vaga única: análise focada em uma vaga específica + feedback ATS + roadmap direcionado.
  - `POST /api/analise/vaga-unica` recebe `multipart/form-data` com `arquivo` (PDF) e `descricaoVaga` e devolve `{sinergia, match, gap, extra, roadmapDirecionado, feedbackAts}`.
  - **Uma única chamada ao Sonnet 4.6** (economia de tokens): `VagaUnicaAiAnalyzer` gera roadmap + feedback ATS em conjunto via saída estruturada (`ResultadoIa{roadmapDirecionado, FeedbackAts}`).
  - `FeedbackAts`: `pontuacaoEstimada` (0–100, honesto), `pontosFavoraveis` (o que funciona bem), `pontosDeAtencao` (lista de `PontoDeAtencao{categoria, descricao, impacto}`) e `acoesPrioritarias` (max 5). Prompt instrui explicitamente: **não fabricar sugestões se o CV já está forte** — lista vazia é um elogio.
  - Enums de domínio: `CategoriaAtencao` (FORMATO/KEYWORDS/CONTEUDO/ESTRUTURA) e `ImpactoAtencao` (ALTO/MEDIO/BAIXO).
  - `AnaliseVagaUnicaService`: injeta `PdfTextExtractor` + `SkillExtractionService` diretamente (não `CvAnalysisService`) para obter texto bruto do CV (necessário para o ATS) **e** skills do CV em uma única leitura do PDF. Reutiliza `GapAnalyzer` (Java puro) para sinergia/match/gap.
  - Sinergia por vaga única via `GapAnalyzer.analisar(List.of(skillsVaga), skillsDoCv).sinergiaMedia()`.
  - Rate limiting aplicado automaticamente (URI começa com `/api/analise/`).
  - Testes: `VagaUnicaAiAnalyzerTest` (2: falha SDK → AiException; AiException repassa sem embrulhar), `AnaliseVagaUnicaServiceTest` (5: orquestração OK, sinergia 100%, PDF inválido, falha IA, arquivo vazio), `VagaUnicaControllerTest` (4: 200/400 param ausente/400 PDF inválido/503). Suíte verde (60 testes).
- [x] **Transversal** — Rate limiting por IP nos endpoints de IA.
  - `filter/RateLimitFilter` (`@Component`, `@Order(1)`): janela fixa de 1 minuto, 20 req/IP. Reseta com double-checked locking. Cobre `/api/skills/*`, `/api/cv/*`, `/api/analise/*`, `/api/roadmap`, `/api/ai/ping`. Responde 429 com `{"error":"..."}` antes de chegar ao controller.
  - IP identificado via `X-Forwarded-For` (proxy/load balancer); fallback para `remoteAddr`.
  - Implementação em memória (`ConcurrentHashMap`) — suficiente para MVP. Nota no código: em produção multi-instância, substituir por Redis + Bucket4j.
  - Por ser `@Component Filter`, o Spring Boot carrega automaticamente em `@WebMvcTest` sem quebrar nenhum teste existente (cada classe tem contexto isolado; max 4 req/classe < 20 limite).
  - Testes: `RateLimitFilterTest` (6 casos: passa até o limite, bloqueia no limite+1, sem limite em endpoints livres, X-Forwarded-For, isolamento por IP, cobertura de todos os endpoints de IA). Suíte verde (49 testes).

## Fatias — Frontend (`frontend/`)

Backend concluído; frontend em andamento. Stack e decisões travadas (detalhe no
`CLAUDE.md` → "Frontend — decisões técnicas fixas"): Next.js (App Router) + TypeScript,
chamada **direta ao backend + CORS**, **Tailwind + shadcn/ui**, **Framer Motion**,
testes com **Vitest + RTL** em toda fatia, cliente de API único com tratamento de erro central.

- [x] **F0** — Scaffold Next.js 16 (App Router + TS + Tailwind v4) em `frontend/` + fumaça (`GET /api/ai/ping` na tela com estado carregando/ok/erro).
  - **CORS no backend**: `config/CorsConfig.java` (`WebMvcConfigurer`) libera `http://localhost:3000` (configurável via `cors.allowed-origins` em `application.properties`).
  - **API client central** (`src/lib/api-client.ts`): única camada de acesso à API, traduz status HTTP + `{error}` do backend em `ApiError(status, message)`. `NEXT_PUBLIC_API_BASE_URL` para base URL. Transversal criado — amadurece a cada fatia.
  - **Stack instalada**: Next.js 16.2.11 · React 19 · Tailwind v4 · Geist fonts. Dev tools: Vitest 4 + Testing Library 16 + jsdom.
  - Testes: `api-client.test.ts` (5 casos) e `page.test.tsx` (4 casos). 9 testes, todos verdes.
  - Backend: 60 testes, sem regressão com a nova `CorsConfig`.
- [x] **F1** — Design system + tema claro/escuro + header com identidade "de dev".
  - **Paleta**: fundo `oklch(0.11)` quase-preto + acento ciano (`oklch(0.78 0.14 204)`) no modo escuro; ciano-700 no modo claro. Tokens shadcn sobrescritos em `globals.css`.
  - **Tema**: `src/providers/ProvedorTema.tsx` — wrapper `'use client'` sobre `ThemeProvider` (next-themes): `defaultTheme="dark"`, `enableSystem={false}`, `attribute="class"`. `suppressHydrationWarning` em `<html>` para evitar mismatch de hidratação.
  - **Header**: `src/components/layout/Cabecalho.tsx` — `> match_cv` em Geist Mono com `>` em ciano (identidade terminal), botão ghost com ícone Sol/Lua (lucide-react) que alterna o tema via `useTheme`. `aria-label` dinâmico para acessibilidade.
  - **`globals.css`**: corrigido `--font-sans: var(--font-sans)` (auto-referência gerada pelo shadcn) → `var(--font-geist-sans)`. Transição suave no `html` (`background-color 0.2s`, `color 0.15s`). Modo escuro customizado para identidade "dev" (fundo quase-preto, border sutil via `oklch(1 0 0 / 8%)`).
  - **`layout.tsx`**: `lang="en"` → `lang="pt-BR"`, adicionado `ProvedorTema` + `Cabecalho`, `suppressHydrationWarning`.
  - **`page.tsx`**: aproveitou os componentes shadcn (`Card`, `CardContent`) para exibir o status do backend com design system real.
  - Testes: `Cabecalho.test.tsx` (5 casos: renderiza nome, aria-label no escuro, aria-label no claro, toggle escuro→claro, toggle claro→escuro). 14 testes totais no frontend, todos verdes.
- [x] **F2** — Entrada de N vagas + visualização de skills por prioridade (`POST /api/skills/analyze`) — momento de destaque.
  - **Hook** `useAnaliseDeVagas.ts`: estado (`ocioso/analisando/pronto/erro`), lista de vagas, `adicionarVaga`/`removerVaga`/`analisar`. Remoção reseta resultado e volta ao estado ocioso.
  - **`EntradaDeVagas.tsx`**: textarea + botão "Adicionar vaga" (Ctrl+Enter também funciona), lista de vagas com preview truncado e botão de remoção, botão "Analisar N vagas" (com ícone Sparkles) que só aparece quando há vagas.
  - **`VisualizacaoDeSkills.tsx`**: seção com resumo (N vagas · M skills) + 3 `GrupoDeSkills`.
  - **`GrupoDeSkills.tsx`**: card com borda esquerda colorida por prioridade (ciano/amarelo/cinza), título em mono uppercase, badges com stagger animation (Framer Motion spring). Grupos vazios não são renderizados.
  - **`api-client.ts`**: tipos `SkillAnalisada` e `AnaliseVagasResposta` adicionados; método `api.analisarVagas()`.
  - **`page.tsx`**: título em mono + tagline, monta `EntradaDeVagas` + `AnimatePresence` com 3 estados (carregando pulsante em ciano, erro, resultado).
  - Testes: hook (6 casos), EntradaDeVagas (9 casos), VisualizacaoDeSkills (5 casos), api-client (2 novos), page (3 smoke tests). **37 testes, todos verdes.**
Do roadmap de frontend abaixo, **F3–F5 foram substituídas pelo fatiamento do
redesenho** (ver "Redesenho de produto") — cada uma tem equivalente no
`plano-tecnico.md`. A **F6 (polimento) NÃO foi substituída**: continua **pendente**,
pois nenhuma fatia do redesenho a cobre por inteiro (ver "Pendências sem fatia").
Fica registrado para preservar o rastro:

- [~] **F3** — Upload do CV + gap analysis + sinergia (`/api/analise/completa`) —
  *substituída* (o conteúdo migra para as Fatias 3 e 4 do redesenho).
- [~] **F4** — Roadmap sob demanda (`/api/roadmap`) — *substituída* (Fatia 5 do
  redesenho, agora com variantes completo/direcionado + salvável).
- [~] **F5** — Experiência de vaga única (`/api/analise/vaga-unica`) —
  *substituída* (Fatia 3 do redesenho, a opção "Estou pronto pra esta vaga?").
- [ ] **F6** — Polimento (responsividade mobile, acessibilidade contraste/foco/aria,
  micro-interações finais, performance) — **pendente**. A Fatia 6 do redesenho cobre
  só estados de erro/vazio + docs, um subconjunto pequeno; o restante do polimento
  não tem fatia atribuída (ver "Pendências sem fatia").
- [x] **Transversal** — cliente de API + tratamento de erro central: criado na F0
  (`ApiError`, `NEXT_PUBLIC_API_BASE_URL`), amadurece a cada fatia.

## Redesenho de produto (Fase A: fluxos 1–3 · Fase B: fluxos 4–5)

Reorganiza o produto em cinco fluxos por cima do núcleo já pronto. Motivação em
`redesenho-produto.md`; contratos, decisões e escopo por fatia em `plano-tecnico.md`.
**Estado: Fase A não iniciada.** Modelo por fatia registrado na memória
(`uso-de-modelos-nas-fatias`); nas fatias de Opus, plano antes de executar.

**Fase A — fluxos que reaproveitam backend**

- [ ] **Fatia 1** — Nova agregação e apresentação de skills (dado bruto + estratos
  de consenso, ordenação por frequência ponderada; substitui Alta/Média/Baixa).
  Back: novo agregador + `SkillAgregada`/`EstratoConsenso`. Front: visualização por
  densidade. *(Opus)*
- [ ] **Fatia 2** — Tela de escolha de fluxo + limites 3–8 (validados no backend) +
  entrada por fluxo. *(Sonnet)*
- [ ] **Fatia 3** — Opção 1 (1 vaga + CV): gap por tipo, ATS direcionado, sugestões
  em duas frentes. *(Opus)*
- [ ] **Fatia 4** — Opção 3 (3–8 + CV): gap-first, sinergia, ATS por segmento (novo). *(Opus)*
- [ ] **Fatia 5** — Roadmap com variantes completo/direcionado + salvável (Markdown). *(Sonnet)*
- [ ] **Fatia 6** — Fechamento: bug do resultado órfão, estados de erro, docs. *(Sonnet)*

**Fase B — fluxos novos**

- [ ] **Fatia 7** — Opção 4 (só CV): ATS estrutural, posicionamento, nota com
  critérios visíveis. *(Opus)*
- [ ] **Fatia 8** — Opção 5 (dois CVs): comparação neutra + modo evolução cego. *(Opus)*

## Pendências sem fatia (revisar ao fim da Fase A)

Itens decididos ao longo do trabalho que **não pertencem a nenhuma fatia** do plano
atual. Ficam aqui para não sumirem na transição — revisar quando os cinco fluxos
existirem.

- [ ] **Polimento de frontend** (herdado da antiga F6): responsividade mobile,
  passe de acessibilidade (contraste/foco/aria), micro-interações finais,
  performance. A Fatia 6 do redesenho cobre só estados de erro/vazio + docs.
- [ ] **Identidade visual / logo** — adiada por decisão consciente para depois de
  os fluxos existirem, quando a linguagem visual do app estiver estabelecida.
- [ ] **Limpar assets de exemplo do create-next-app** — `next.svg`, `vercel.svg`,
  `file.svg`, `globe.svg`, `window.svg` em `frontend/public/`, que ficarão órfãos.

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

O repositório é um **monorepo**: a raiz guarda o comum (`CLAUDE.md`, `PROGRESS.md`,
`.gitignore`, `.gitattributes`) e cada app vive em sua subpasta. O backend Spring
está em `backend/`; o frontend Next.js em `frontend/`. Todos os caminhos Java
abaixo são relativos a `backend/`.

```
backend/src/main/java/com/david/matchcv/
├── MatchCvApplication.java
├── config/AnthropicConfig.java
├── controller/HelloController.java, AiPingController.java, SkillController.java, AnalysisController.java, CvController.java, AnaliseController.java, VagaUnicaController.java
├── service/AiPingService.java, SkillExtractionService.java, VagaAnalysisService.java, PdfTextExtractor.java, CvAnalysisService.java, AnaliseCompletaService.java, RoadmapService.java, VagaUnicaAiAnalyzer.java, AnaliseVagaUnicaService.java
├── domain/Prioridade.java, TipoSkill.java, SkillExtraida.java, SkillPrioridade.java, SkillPriorityCalculator.java, SinergiaVaga.java, ResultadoGap.java, GapAnalyzer.java, ImpactoAtencao.java, CategoriaAtencao.java, PontoDeAtencao.java, FeedbackAts.java
├── dto/ExtrairSkillsRequest.java, SkillsResponse.java, AnalisarVagasRequest.java, AnaliseVagasResponse.java, AnaliseCompletaResponse.java, GerarRoadmapRequest.java, SkillParaAprender.java, RoadmapResponse.java, AnaliseVagaUnicaResponse.java
├── filter/RateLimitFilter.java
└── exception/AiException.java, InvalidPdfException.java, GlobalExceptionHandler.java

backend/src/test/java/com/david/matchcv/
├── MatchCvApplicationTests.java
├── controller/AiPingControllerTest.java, SkillControllerTest.java, AnalysisControllerTest.java, CvControllerTest.java, AnaliseControllerTest.java, RoadmapControllerTest.java, VagaUnicaControllerTest.java
├── domain/SkillPriorityCalculatorTest.java, GapAnalyzerTest.java
├── filter/RateLimitFilterTest.java
└── service/AiPingServiceTest.java, SkillExtractionServiceTest.java, VagaAnalysisServiceTest.java, CvAnalysisServiceTest.java, AnaliseCompletaServiceTest.java, RoadmapServiceTest.java, VagaUnicaAiAnalyzerTest.java, AnaliseVagaUnicaServiceTest.java
```

## Como rodar / testar

O backend roda a partir da pasta `backend/` (onde ficam o `pom.xml` e o wrapper).

1. Copie `backend/src/main/resources/application-local.properties.example` para
   `application-local.properties` (mesma pasta) e cole sua chave no lugar do placeholder.
2. Rode a partir de `backend/`:

```powershell
cd backend
.\mvnw.cmd spring-boot:run   # sobe a app (porta 8080)
.\mvnw.cmd test              # roda a suite de testes
```

(Alternativa sem arquivo local: definir a variavel de ambiente `ANTHROPIC_API_KEY`.)

Endpoints atuais: `GET /api/hello`, `GET /api/ai/ping`, `POST /api/skills/extract`, `POST /api/skills/analyze`, `POST /api/cv/extract`, `POST /api/analise/completa`, `POST /api/roadmap`, `POST /api/analise/vaga-unica`.
