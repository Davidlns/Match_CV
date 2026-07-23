# CLAUDE.md

## Visão geral

Ferramenta para quem busca emprego. O usuário cola descrições de uma ou mais
vagas e (opcionalmente) anexa o próprio currículo. A aplicação:

1. Extrai e normaliza as skills das vagas, agrega por frequência e classifica
   por prioridade (Alta/Média/Baixa).
2. Se houver currículo, extrai as skills dele e faz um "gap analysis":
   o que o usuário tem (match), o que falta (gap) e o que tem mas nenhuma vaga
   pediu. Também calcula uma **porcentagem de sinergia de cada vaga** com o
   currículo (quanto das skills daquela vaga o usuário já cobre), para ele saber
   pra quais vagas está mais pronto.
3. Sob demanda (botão), gera um roadmap de estudo focado nos gaps.

Público-alvo: comunidade de quem busca a primeira vaga. Será publicado
gratuitamente.

## Stack

- Backend: Spring Boot (Java). Expõe a API REST e faz as chamadas de IA.
- Frontend: Next.js + React. Só consome a API do backend.
- IA: API da Anthropic. Modelo Haiku para extração/normalização de skills
  (tarefa barata e bem servida pelo modelo mais econômico). O roadmap pode
  testar um modelo melhor se a qualidade exigir.

## Decisões fixas (não reabrir sem eu pedir)

- A chave da API da Anthropic fica APENAS no backend. Nunca no cliente.
- O currículo é processado EM MEMÓRIA e descartado. Nada de currículo é
  persistido em disco ou banco. (Evita responsabilidade de LGPD; é ponto de
  venda: "seu currículo não é armazenado".)
- Extração de texto do PDF do currículo no backend com Apache PDFBox.
- Prioridade das skills é calculada no backend por regra determinística,
  combinando o TIPO (obrigatória/diferencial, extraído da vaga pela IA) com a
  frequência entre vagas: ALTA se obrigatória em >=70% das vagas; senão MEDIA se
  aparece (obrigatória ou diferencial) em >=40%; senão BAIXA. Com 1 vaga:
  obrigatória=ALTA, diferencial=MEDIA. Se a vaga não distingue requisitos de
  diferenciais, tudo é tratado como obrigatória. A IA extrai/normaliza/classifica
  o tipo; o código conta e classifica a prioridade.
- O gap analysis (cruzar skills da vaga x do CV) é lógica de conjunto em Java
  puro, sem IA.
- Chamadas de IA separadas por etapa (extração barata x roadmap caro).
- Normalização de nomes de skill é crítica ("React"="ReactJS"="React.js").
  Extração das vagas e do CV deve usar vocabulário consistente entre si.
- Rate limiting básico por IP nos endpoints que chamam a IA (proteção contra
  abuso, já que o app é público).

## Estratégia de modelos e uso de tokens

Princípio: gastar o mínimo de tokens em cada tarefa, sem perder qualidade.
Começar sempre no modelo mais básico que resolve; só subir de nível com
evidência de que a qualidade não atende.

Ressalva importante: eficiência NÃO é economia a qualquer custo. A prioridade é
entregar o que a aplicação promete, com qualidade e valor real para o usuário.
Escolher o modelo e o `max_tokens` mais baratos que atingem esse nível de
qualidade — não os mais baratos em absoluto. Se economizar degrada o resultado,
subir de nível sem hesitar.

Escolha de modelo (do mais barato ao mais caro):

- **Sem IA (Java puro)** sempre que a lógica for determinística — é o mais
  barato: zero tokens. Ex.: contagem/prioridade das skills, gap analysis. Já é
  decisão fixa.
- **Haiku 4.5** (`claude-haiku-4-5`) — padrão para tarefas mecânicas e bem
  definidas: extração e normalização de skills (vagas e CV). Barato e suficiente.
- **Sonnet 4.6** (`claude-sonnet-4-6`) — só para tarefas que exigem raciocínio
  de verdade ou geração de qualidade, e só depois de confirmar que o Haiku não
  dá conta. Candidato: o roadmap de estudo (Fatia 6).
- **Opus** — não usar por padrão; este app não precisa. Reservado para o caso
  raro de algo ser complexo demais até para o Sonnet.
- **Fable 5 (`claude-fable-5`) — PROIBIDO em qualquer hipótese.** É o modelo mais
  caro e não tem nenhum uso neste projeto; nunca selecionar, sob nenhuma condição.

Técnicas de economia de tokens:

- **`max_tokens` justo** — dimensionar a saída à tarefa (lista de skills = poucas
  centenas, não milhares). Nunca superdimensionar.
- **Saída estruturada** — restringe a resposta ao formato, corta preâmbulo e
  divagação → menos tokens de saída.
- **Sem "thinking" em tarefa mecânica** — não pagar tokens de raciocínio em
  extração/classificação.
- **Prompt de instrução enxuto** — a descrição da vaga vem do usuário (não dá
  para encurtar), mas o nosso prompt de sistema deve ser curto e direto.
- **Prompt caching** — quando o mesmo prompt for reusado em lote (ex.: extrair de
  N vagas), avaliar cachear o prefixo estável (há um mínimo de tokens para valer).
- **Menos chamadas** — nunca chamar a IA para o que é lógica de código.

## Frontend — UX e design

Objetivo de experiência: ao usar o app, a pessoa deve pensar "UAU, que design
criativo — é bonito, divertido e simples de navegar". O "uau" vem de detalhes
bem-feitos, não de excesso.

Público e tom: quem busca a primeira vaga de dev. A estética é voltada para
devs — pode abraçar uma linguagem visual "de dev" (tema escuro como base,
acentos em fonte monoespaçada, motivos sutis de código/terminal), com bom gosto
e sem exagero.

Princípios:

- **Moderno e com identidade própria** — evitar aparência genérica de template;
  buscar personalidade. Nada de "AI slop".
- **Interativo e vivo** — micro-interações e animações sutis que dão feedback e
  fluidez (ao colar vagas, ao subir o CV, transições entre estados). Estados de
  carregamento com personalidade, já que há espera nas chamadas de IA.
- **Simples e eficiente** — navegação óbvia, poucos passos, hierarquia visual
  clara. Bonito não pode custar usabilidade: fácil de entender de primeira.
- **Legível e acessível** — tipografia limpa, bom contraste, responsivo.
- **Leve e rápido** — Next.js/React bem usados; nada de travar ou pesar.
- **Tema claro/escuro** — seletor dinâmico de modo claro/escuro na interface,
  com o **modo escuro como padrão** (combina com a estética dev). A troca deve
  ser suave e a preferência do usuário, lembrada.

Momentos de destaque (onde caprichar no "uau"): a visualização das skills por
prioridade (Alta/Média/Baixa) e do gap analysis (tem / falta / sobra) devem ser
visuais e agradáveis, não uma tabela crua.

Escopo: decisões finas (paleta exata, fontes, biblioteca de componentes/animação)
ficam para a fase de frontend — construímos o backend primeiro. Este bloco é a
direção, não a especificação final.

## Frontend — decisões técnicas fixas (não reabrir sem eu pedir)

Travadas ao iniciar a fase de frontend. A direção de UX acima continua valendo;
estas são as escolhas de ferramenta que a implementavam.

- **Stack:** Next.js (App Router) + React + TypeScript, na subpasta `frontend/`.
  Só consome a API do backend (decisão fixa: nada de IA nem segredos no cliente).
- **Integração front↔back: chamada direta + CORS.** O browser chama o Spring
  diretamente (`http://localhost:8080` em dev). O backend expõe uma config de
  CORS liberando a origem do front. Escolhido em vez de um BFF/proxy no Next
  porque preserva o **IP real do usuário** — o rate limiting do backend é por IP
  (um proxy colapsaria todos num IP só, salvo repasse cuidadoso de
  `X-Forwarded-For`). Base URL da API via variável de ambiente
  (`NEXT_PUBLIC_API_BASE_URL`).
- **Estilo e componentes: Tailwind CSS + shadcn/ui.** Componentes são copiados
  para o nosso código (somos donos e customizamos), fugindo da cara genérica de
  template e do "AI slop".
- **Animação: Framer Motion (Motion).** Para as micro-interações e transições
  ("interativo e vivo", loading com personalidade).
- **Testes: Vitest + React Testing Library, em toda fatia.** Mesma régua do
  backend — uma fatia de front não está pronta sem testes de componente/lógica.
- **Cliente de API único + tratamento de erro central.** Uma camada de acesso à
  API traduz status HTTP + corpo `{"error": "..."}` do backend em mensagens de
  UI (429 rate limit, 503 IA indisponível, 400 validação/PDF). Sem `fetch` solto
  espalhado; error boundaries para falhas inesperadas.
- **Acessibilidade e responsividade** são requisito permanente de cada fatia, não
  uma etapa final.

## Frontend — organização e granularidade (obrigatório)

- **Um componente por arquivo.** Nada de vários componentes empilhados
  no mesmo arquivo.
- **Componentes pequenos.** Se um arquivo passa de ~150 linhas, é sinal
  de que precisa ser quebrado. Extrair subcomponentes e hooks. (O limite
  é um gatilho de revisão, não uma lei — 160 linhas num componente coeso
  é melhor que quebrar artificialmente.)
- **Lógica fora do componente.** Chamadas de API, transformação de dados
  e estado complexo vivem em hooks ou módulos próprios — o componente
  cuida de renderizar.
- **Sem CSS gigante.** Estilo via Tailwind nas classes; CSS global só
  para tokens e reset. Nada de arquivos `.css` longos por componente.
- **Sem duplicação de markup.** Se um bloco visual aparece duas vezes,
  vira componente.
- **Convenção de idioma:** todo o código deste projeto usa **português**
  para identificadores — nomes de classes, métodos, variáveis, rotas e
  propriedades de resposta. Exceções aceitáveis: termos técnicos
  consagrados em inglês na indústria sem tradução natural (ex.: `fetch`,
  sufixos de padrão como `Controller`, `Service`, `Hook`). A
  inconsistência do backend (`AnalysisController` × `AnaliseController`)
  é um legado que não vale refatorar agora, mas não deve se repetir —
  qualquer código novo segue português.

## Fora de escopo no MVP (fica pra v2)

- Login / contas de usuário.
- Persistência (banco, histórico de análises).
- Exportar roadmap em PDF.

## Estrutura do repositório (monorepo)

O projeto é um monorepo. A raiz guarda apenas o que é comum ao repositório
inteiro; cada aplicação vive na sua subpasta:

```
Match_CV/                 # raiz do repositório
├── CLAUDE.md             # instruções do projeto (backend + frontend)
├── PROGRESS.md           # rastreio de progresso geral
├── .gitignore            # ignore único, cobre o monorepo todo
├── .gitattributes        # atributos do repo (eol do mvnw etc.)
├── backend/              # aplicação Spring Boot (pom.xml, mvnw, src/)
└── frontend/             # aplicação Next.js (ainda não criada)
```

O backend roda a partir de `backend/` (é onde estão o `pom.xml` e o wrapper):

```powershell
cd backend
.\mvnw.cmd spring-boot:run   # sobe a app (porta 8080)
.\mvnw.cmd test              # roda a suíte
```

A chave da Anthropic continua em `backend/src/main/resources/application-local.properties`
(fora do Git). O frontend, quando existir, só consome a API do backend.

## Como trabalhamos

- Construção em fatias verticais, uma de cada vez, cada fatia funcionando ponta
  a ponta antes da próxima.
- Escopo apertado nas tasks: aponte os arquivos/diretórios exatos.
- Nas partes de Spring Boot, explicar o que cada camada faz e por quê
  (estou aprendendo Spring construindo este projeto).

## Padrões de engenharia (obrigatórios em toda fatia)

Aplicar sempre, junto com o código da própria fatia — não são etapa opcional
nem "pra depois":

- **Testes automatizados (JUnit).** Cada fatia entrega testes junto com o
  código. Lógica pura (prioridade, gap analysis) → testes unitários; camadas
  web/serviço → testes com o suporte do Spring Boot (@WebMvcTest, MockMvc,
  mocks). Uma fatia não está pronta sem testes.
- **Controle de exceções centralizado.** Sem try/catch ad-hoc espalhado nem
  stack trace vazando pro cliente. Usar @RestControllerAdvice como handler
  global, exceções de domínio próprias, e códigos HTTP adequados com mensagens
  claras.
- **Clean Code.** Nomes significativos, métodos curtos com responsabilidade
  única, sem duplicação, camadas bem separadas (Controller fino → Service →
  Adapter/lógica).

Motivo: o projeto é aprendizado de Spring E peça de portfólio; qualidade,
testes e robustez são parte do valor, não um extra.

## Roadmap de fatias (verticais) — Backend

Cada fatia sobe ponta a ponta — com testes e tratamento de erro — antes da
próxima. O **estado atual** (qual fatia foi concluída, onde estamos, decisões já
tomadas) é rastreado em `PROGRESS.md` na raiz do projeto. **Consulte o
PROGRESS.md ao começar uma sessão e atualize-o sempre que concluir uma fatia ou
fizer uma mudança relevante.**

**Todas as fatias de backend (0–7 + rate limiting) estão concluídas.**

0. `GET /api/hello` — app sobe e responde JSON (fumaça).
1. Config da chave (env) + `AnthropicClient` + ping na IA (`GET /api/ai/ping`).
2. Extrair skills de 1 vaga (Haiku + saída estruturada tipada).
3. N vagas + agregação por frequência + prioridade Alta/Média/Baixa (Java puro).
4. Upload do CV em PDF → PDFBox em memória → skills do CV.
5. Gap analysis (match / gap / extra) + **sinergia por vaga** (% das skills de
   cada vaga cobertas pelo CV) — lógica de conjunto em Java puro, sem IA.
6. Roadmap de estudo sob demanda (IA; testar modelo melhor se a qualidade exigir).
7. Experiência de vaga única (pós-núcleo): reúne o alinhamento (sinergia) com
   AQUELA vaga, o roadmap direcionado, um feedback de como o CV se sairia num
   filtro de currículos (ATS) e sugestões de melhoria do CV direcionadas à vaga.
   As partes de IA (feedback ATS e sugestões) são candidatas a Sonnet
   (geração/raciocínio de qualidade); o alinhamento é Java puro.
* Rate limiting por IP nos endpoints de IA — encaixar após a fatia 2.

## Roadmap de fatias (verticais) — Frontend

Mesma filosofia do backend: cada fatia sobe ponta a ponta (input → API → algo
visível funcionando), com testes (Vitest + RTL) e tratamento de erro. Consome os
endpoints já prontos do backend. Estado rastreado no mesmo `PROGRESS.md`.

0. **Scaffold + fumaça** — criar `frontend/` (Next App Router + TS), shell de
   layout, e provar o encaixe chamando `GET /api/ai/ping` e exibindo o resultado.
   Inclui a config de **CORS no backend** liberando a origem do front.
1. **Design system + tema claro/escuro** — tokens (paleta, tipografia com acento
   monoespaçado), theme provider com **escuro padrão** + seletor com preferência
   lembrada e troca suave, primitivos base (Button, Card, Input, Textarea) e
   header com identidade "de dev".
2. **Entrada de vagas + skills por prioridade** — colar N vagas
   (adicionar/remover), `POST /api/skills/analyze`, e a **visualização de skills
   por prioridade** (Alta/Média/Baixa) — momento de destaque, visual e não tabela.
   Loading com personalidade.
3. **Upload do CV + gap analysis + sinergia** — drag-drop de PDF, troca para
   `POST /api/analise/completa`, e o **gap analysis** (tem/falta/sobra) + sinergia
   por vaga — o outro momento de destaque. Reforço do "seu CV não é armazenado".
4. **Roadmap sob demanda** — botão que chama `POST /api/roadmap` com as skills do
   gap e renderiza o Markdown, com estado de espera caprichado (Sonnet é lento).
5. **Experiência de vaga única** — rota dedicada consumindo
   `POST /api/analise/vaga-unica`: alinhamento + roadmap dirigido + card de
   feedback ATS (pontuação, favoráveis, pontos de atenção com categoria/impacto,
   ações prioritárias).
6. **Polimento** — responsividade mobile, passe de acessibilidade
   (contraste/foco/aria), micro-interações finais, estados de erro/vazio (incl.
   429/503) bem apresentados, performance.
* **Transversal** — cliente de API + tratamento de erro central: encaixa já na
  fatia 0 e amadurece a cada fatia (como o exception handler no backend).