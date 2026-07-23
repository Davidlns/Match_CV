# Match_CV — Plano técnico do redesenho

Este documento traduz o **`redesenho-produto.md`** (o *quê*) em um plano de
execução (o *como*): estado atual, contratos de API, fatiamento e as decisões do
`CLAUDE.md` que precisam ser revistas. É um documento vivo — revisamos aqui antes
de escrever código.

**Decisões de condução já tomadas (sessão de planejamento):**

- **Valor primeiro:** a primeira fatia ataca a apresentação de skills (a causa
  direta do problema de "análise entrega pouco valor"), não a tela de escolha.
- **Faseado:** Fase A entrega os fluxos que reaproveitam backend (1, 2, 3) ponta
  a ponta; Fase B entrega os fluxos novos e pesados de IA (4, 5).
- **Fatiamento vertical com testes**, no padrão do projeto — cada fatia sobe
  ponta a ponta antes da próxima.

---

## 1. Estado atual (inventário)

O que já existe e como cada peça se relaciona com os cinco fluxos.

### Endpoints

| Endpoint | Entrada | Saída (resumo) | Fluxo alvo |
|---|---|---|---|
| `POST /api/skills/analyze` | `{descricoesVagas: [...]}` (≥1) | `{totalVagas, skills:[{nome,frequencia,obrigatoriaEm,percentual,prioridade}]}` | **Opção 2** (reformar) |
| `POST /api/analise/completa` | multipart `arquivo` + `descricoesVagas[]` | agregadas + `match/gap/extra` + `sinergiaMedia` + `vagasComSinergia` | **Opção 3** (reformar) |
| `POST /api/analise/vaga-unica` | multipart `arquivo` + `descricaoVaga` | `sinergia` + `match/gap/extra` + `roadmapDirecionado` + `feedbackAts` | **Opção 1** (refinar) |
| `POST /api/roadmap` | `{skills:[{nome,prioridade}]}` | `{roadmap: "<markdown>"}` | Roadmap (reformar) |
| `POST /api/skills/extract`, `POST /api/cv/extract` | — | extração unitária | Blocos de apoio |
| `GET /api/ai/ping`, `GET /api/hello` | — | fumaça | — |

Rate limiting por IP cobre `/api/skills/*`, `/api/cv/*`, `/api/analise/*`,
`/api/roadmap`, `/api/ai/ping` (20 req/min/IP).

### Domínio determinístico (Java puro, sem IA)

- **`SkillPriorityCalculator`** — agrega por nome entre vagas (frequência +
  em quantas é obrigatória) e classifica em `Prioridade{ALTA,MEDIA,BAIXA}`.
  **É a peça que o redesenho condena** (ver §3.1). Já produz o dado bruto
  (`frequencia`, `obrigatoriaEm`, `percentual`) — o que sai é só o *bucketing*.
- **`GapAnalyzer`** — match/gap/extra por conjunto (case-insensitive) + sinergia
  por vaga (% das skills da vaga cobertas pelo CV) + média. Reaproveitável quase
  inteiro. **Limitação:** `gap`/`match`/`extra` são `List<String>` — perdem o
  tipo (obrigatória/diferencial), que a opção 1 e 3 precisam.
- Records: `SkillExtraida{nome,tipo}`, `TipoSkill{OBRIGATORIA,DIFERENCIAL}`,
  `SkillPrioridade`, `ResultadoGap`, `SinergiaVaga`, `FeedbackAts`,
  `PontoDeAtencao{categoria,descricao,impacto}`, enums `CategoriaAtencao`,
  `ImpactoAtencao`.

### IA

- **`SkillExtractionService`** (Haiku) — extrai/normaliza skills com tipo.
  Vocabulário canônico consistente entre vaga e CV. Base de tudo.
- **`VagaUnicaAiAnalyzer`** (Sonnet, 1 chamada) — roadmap direcionado + ATS numa
  saída estruturada. Prompt já honra **suficiência** e **honestidade** (lista de
  atenção vazia = elogio, não fabricar problema).
- **`RoadmapService`** (Sonnet) — roadmap agrupando por `Prioridade`. Depende do
  modelo que vai mudar (§3.1).

### Frontend

- F0–F2 prontos: API client central (`ApiError`), tema claro/escuro, design
  system (tokens/cards/fontes/botão — recém-refinado, **permanece válido**), e a
  página atual = **efetivamente a opção 2** (colar N vagas → `analyze` →
  visualização por prioridade). `GrupoDeSkills`/`VisualizacaoDeSkills` serão
  repensados (§3.1); o design system não.
- Hook `useAnaliseDeVagas`: `removerVaga` **já reseta** `resultado`/estado — o
  bug do "resultado órfão" (§6) provavelmente já está mitigado; confirmar.

### Mapa fluxo → esforço

| Fluxo | Pergunta | Backend | Esforço |
|---|---|---|---|
| **1.** 1 vaga + CV | "Estou pronto?" | `vaga-unica` existe | Refinar |
| **2.** 3–8 vagas | "O que o mercado pede?" | `analyze` existe | Reformar apresentação |
| **3.** 3–8 vagas + CV | "Onde estou vs mercado?" | `completa` existe | Reformar + ATS novo |
| **4.** Só CV | "Meu CV é bom?" | — | Novo |
| **5.** Dois CVs | comparação | — | Novo |

---

## 2. Decisões técnicas transversais (novas)

### 2.1 Nova agregação de skills (substitui Alta/Média/Baixa)

O dado bruto já é calculado; muda a **apresentação e a ordenação**.

- **Sem bucket de prioridade como eixo.** A resposta expõe, por skill:
  `nome`, `frequencia` (em quantas vagas), `obrigatoriaEm`, `percentual`, e um
  **estrato de consenso** derivado por densidade.
- **Estrato nomeado por significado**, não por faixa numérica. Proposta de enum
  `EstratoConsenso`:
  - `PRATICAMENTE_TODAS` — "exigido em praticamente todas"
  - `FREQUENTE` — "aparece com frequência"
  - `PONTUAL` — "menos comum, mas presente"
  Derivado do `percentual` de presença (limiares a fixar na fatia). **Nada é
  escondido nem colapsado** — o estrato dá peso visual, não filtra.
- **Ordenação por frequência ponderada pelo tipo:** obrigatória pesa mais que
  diferencial. Ex.: `peso = obrigatoriaEm * 2 + (frequencia - obrigatoriaEm)`.
  Ordena desc por peso; desempate por `frequencia`, depois `obrigatoriaEm`,
  depois `nome`. O que se **exibe** é o fato ("Java · 4/4 vagas · sempre
  obrigatória"), não o score.
- **Rótulo de tipo por skill (derivado):** `obrigatoriaEm == frequencia` →
  "sempre obrigatória"; `obrigatoriaEm == 0` → "diferencial"; caso intermediário
  → "às vezes obrigatória". Determinístico, no front ou no back.
- Vaga que não distingue requisito de diferencial → tudo obrigatória (já é o
  comportamento; manter).

Impacto de código: `SkillPriorityCalculator` → reescrito como agregador do novo
modelo (novo record `SkillAgregada`; `Prioridade` sai do eixo de apresentação).
Ripples: `AnaliseVagasResponse`, `AnaliseCompletaResponse`, `RoadmapService`,
`SkillParaAprender`, e os testes correspondentes.

### 2.2 Gap com tipo (opções 1 e 3)

Enriquecer o gap para carregar se a skill em falta é **obrigatória ou
diferencial** na(s) vaga(s) — hoje é `List<String>`. Necessário para "separar o
que a vaga trata como obrigatório do que trata como diferencial". Provável novo
tipo `SkillNoGap{nome, tipo}` (ou reusar `SkillExtraida`) em `ResultadoGap`.

### 2.3 Limites 3–8 (opções 2 e 3)

- Validar no **backend** (não só no front): mínimo 3, máximo 8 vagas. Para
  `/api/skills/analyze` (bean validation no request) e `/api/analise/completa`
  (`@RequestParam List<String>` → validação manual no controller/serviço, com 400
  claro via handler global).
- Comunicar o limite **antes** de o usuário esbarrar (frontend).
- 1 vaga → direcionar para a opção 1.

### 2.4 CV obrigatório em 4 dos 5 fluxos

Só a opção 2 dispensa CV. A entrada deixa de ser genérica: cada fluxo pede
exatamente o que precisa. Isso é estrutural na tela de escolha (Fatia 2).

### 2.5 Roadmap salvável + variantes

- **Completo** (tudo, do zero, ignora CV) × **Direcionado** (só o gap; skills já
  cobertas aparecem **na sequência**, marcadas "você já tem", sem detalhe, **não
  expansíveis**). Escolha **só existe quando há CV**.
- **Salvável** — reabre a decisão "exportar PDF fora do escopo". Proposta de MVP:
  **download `.md` + copiar**; PDF fica como incremento posterior. (Sem login →
  salvar é client-side.)

### 2.6 ATS que explica o mecanismo

Cada apontamento traz **o que está no CV, por que atrapalha a leitura
automatizada, e o que fazer**. Direcionado (com vaga: vocabulário, termos
repetidos não cobertos) nas opções 1 e 3; estrutural (multi-coluna, texto em
imagem, PDF não extraível, seções não convencionais) nas opções 4 e 5. Sempre com
a ressalva de honestidade (não existe ATS único; não prometer aprovação).

---

## 3. Fatiamento

Cada fatia: código + testes (JUnit no back, Vitest/RTL no front) + tratamento de
erro, ponta a ponta. Critério de pronto explícito.

### Fase A — fluxos 1, 2, 3 (reaproveitam backend)

**Fatia 1 — Nova agregação e apresentação de skills** *(valor primeiro)*
- Back: novo agregador + `SkillAgregada` + `EstratoConsenso`; ordenação ponderada
  (§2.1). Atualiza `AnaliseVagasResponse` e `AnaliseCompletaResponse`. Endpoint
  `/api/skills/analyze` mantém rota, muda shape.
- Front: nova visualização por densidade decrescente (substitui `GrupoDeSkills`);
  cada skill mostra o fato bruto; estratos nomeados por significado; nada
  escondido. Reusa o design system atual.
- Valida na página atual (que já é a opção 2, sem CV).
- Testes: agregador (limiares, ordenação, empates), visualização.
- **Pronto quando:** colar N vagas mostra dado bruto com hierarquia por densidade,
  sem buckets Alta/Média/Baixa.

**Fatia 2 — Tela de escolha de fluxo + limites 3–8 + entrada por fluxo** *(descoberta)*
- Front: tela inicial com as 5 opções (nome + **descrição do que entrega e do que
  pede**); 4 e 5 marcadas "em breve" nesta fase. Rota/entrada dedicada por fluxo
  (1: 1 vaga + CV; 2: 3–8 vagas; 3: 3–8 vagas + CV). Limite comunicado antes.
- Back: validação 3–8 nos endpoints de 2 e 3 (§2.3).
- Testes: tela de escolha, validação de limites (back e front).
- **Pronto quando:** usuário escolhe o fluxo e entra só com o que ele exige;
  limites validados/comunicados.

**Fatia 3 — Opção 1 ponta a ponta (1 vaga + CV) refinada**
- Back `/api/analise/vaga-unica`: gap separando obrigatório/diferencial (§2.2);
  ATS direcionado reforçado (vocabulário/termos repetidos); **sugestões em duas
  frentes** (alinhamento com a vaga × qualidade do documento) — hoje
  `acoesPrioritarias` é lista única, separar.
- Front: tela da opção 1 — cobertura, gap por tipo, ATS explicando mecanismo,
  sugestões nas duas frentes, botão de roadmap (variantes ficam pra Fatia 5).
- Testes back + front.

**Fatia 4 — Opção 3 ponta a ponta (3–8 + CV) refinada**
- Back `/api/analise/completa`: reusa nova agregação (Fatia 1); **ATS por
  segmento** (novo aqui — hoje `completa` não tem ATS; IA candidata a Sonnet);
  gap por tipo; sinergia por vaga (já existe).
- Front: ordem de leitura do produto — gap → sinergia por vaga → panorama →
  ATS/sugestões.
- Testes back + front.

**Fatia 5 — Roadmap: variantes completo/direcionado + salvável**
- Back: variante no roadmap (§2.5). `RoadmapService` migra de `Prioridade` para o
  novo modelo/gap. Direcionado marca cobertas "você já tem" na sequência.
- Front: escolha (só com CV), render Markdown, **salvar** (download `.md` +
  copiar), etapas concluídas não expansíveis + aviso da saída (gerar completo).
- Testes back + front.

**Fatia 6 — Fechamento da Fase A**
- Confirmar/corrigir o "resultado órfão" (§6). Estados de erro/vazio (429/503) por
  fluxo. **Atualizar `CLAUDE.md` e `PROGRESS.md`** com as decisões novas (§4).

### Fase B — fluxos 4, 5 (novos)

**Fatia 7 — Opção 4 (só CV)**
- Back: novo `POST /api/analise/curriculo` (multipart, só `arquivo`). ATS
  estrutural (sem vaga) + posicionamento (segmento/dispersão) + solidez
  técnica/escrita + **nota com critérios visíveis** (ATS, clareza, completude,
  direcionamento). IA pesada → Sonnet. Novo domínio de resposta.
- Front: nova tela. Testes.

**Fatia 8 — Opção 5 (dois CVs)**
- Back: novo `POST /api/analise/comparar` (dois arquivos + flag opcional
  "evolução" + qual é qual). Comparação neutra (critérios da opção 4, aponta o
  mais forte e porquê). **Modo evolução: análise cega primeiro** (veredito sem
  saber qual é o recente), só então o percurso. Orquestração de prompt que **não
  vaza** qual é o novo. Sonnet.
- Front: nova tela; oferecer a comparação ao fim das opções 1, 3 e 4. Testes.

---

## 4. `CLAUDE.md` — decisões a revisar (parte do trabalho, não só consulta)

- **Classificação Alta/Média/Baixa** → substituída pela agregação de dado bruto +
  estratos de densidade nomeados por significado, ordenação por frequência
  ponderada pelo tipo. Registrar a nova regra determinística.
- **CV opcional** → obrigatório em 4 dos 5 fluxos (só a opção 2 dispensa).
- **Tela única de entrada** → escolha de fluxo (5 opções nomeadas pela pergunta).
- **"Exportar roadmap em PDF fora do escopo"** → roadmaps salváveis (Markdown no
  MVP; PDF a avaliar). Ajustar a seção "Fora de escopo".
- **Limites 3–8** (validados no backend) entram como decisão fixa.
- **Mantêm-se:** chave só no backend; CV em memória e descartado; Fable proibido;
  Java puro para o determinístico; Haiku para extração, Sonnet para
  geração/ATS/comparação; login e persistência seguem fora do MVP (por isso
  "salvável" é client-side).

---

## 5. Pontos abertos (decidir na fatia correspondente)

- **Aviso de dispersão de segmento** (opções 2, 3, 4): heurística Java (exige
  taxonomia de skills) × julgamento leve de IA. Recomendação inicial: IA no mesmo
  passo de análise, evitando taxonomia manual. Decidir na Fatia 1/4.
- **Forma de "salvável"**: Markdown download (recomendado MVP) × PDF.
- **Limiares do estrato de consenso** (§2.1): fixar os cortes de densidade.
- **Nota do CV (opção 4)**: a IA devolve subscores por critério e o Java compõe,
  ou a IA já devolve o número? (Determinístico onde der.)
- **Gap com tipo** (§2.2): novo record × reuso de `SkillExtraida` no
  `ResultadoGap`.

---

## 6. Comportamento observado a tratar

"Resultado órfão": ao excluir uma vaga durante/depois da análise, a tela mantinha
o resultado sem forma de removê-lo. O hook atual (`useAnaliseDeVagas.removerVaga`)
já zera `resultado` e volta ao estado ocioso — **provavelmente já resolvido**.
Confirmar na Fatia 6 (ou antes, se a tela da opção 2 mudar na Fatia 1/2), e cobrir
com teste para não regredir.
