# CLAUDE.md

## Visão geral

Ferramenta para quem busca a primeira vaga de dev. Em vez de uma tela única, a
aplicação oferece **cinco fluxos**, cada um respondendo uma pergunta concreta —
o usuário escolhe o que quer fazer antes de fornecer os dados:

1. **Uma vaga + currículo** — "Estou pronto pra esta vaga?" Cobertura do perfil,
   o que falta (separando obrigatório de diferencial), ATS direcionado à vaga e
   sugestões de melhoria do currículo.
2. **Múltiplas vagas (3–8), sem currículo** — "O que o mercado está pedindo?"
   Panorama das skills do conjunto, com o dado bruto visível.
3. **Múltiplas vagas (3–8) + currículo** — "Onde estou em relação ao mercado?"
   Gap contra o conjunto, sinergia por vaga, panorama de mercado e ATS por segmento.
4. **Só currículo** — "Meu currículo é bom?" ATS estrutural, posicionamento/
   direcionamento e solidez técnica, com nota de critérios visíveis.
5. **Dois currículos** — comparação neutra (qual é mais forte e por quê), com modo
   opcional de evolução (versão antiga × nova, avaliado às cegas primeiro).

A **agregação de skills** expõe o dado bruto (frequência entre vagas, se é
obrigatória ou diferencial), ordenado por frequência ponderada pelo tipo, com as
skills agrupadas em **estratos de consenso nomeados por significado** — sem buckets
Alta/Média/Baixa. O **gap analysis** (match/gap/extra) e a **sinergia por vaga** são
lógica de conjunto em Java puro. O **roadmap** de estudo é gerado sob demanda.

Público-alvo: comunidade de quem busca a primeira vaga. Será publicado
gratuitamente.

O produto e sua motivação estão em `redesenho-produto.md`; o plano de execução
(contratos, fatias) em `plano-tecnico.md`.

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
- Agregação de skills calculada no backend por regra determinística (Java puro),
  a partir do TIPO (obrigatória/diferencial, extraído da vaga pela IA) e da
  frequência entre vagas. Expõe o **dado bruto** por skill: frequência (em quantas
  vagas aparece), obrigatoriaEm (em quantas é obrigatória) e percentual de presença.
  **Ordenação por frequência ponderada pelo tipo** (obrigatória pesa mais que
  diferencial). Skills agrupadas em **estratos de consenso nomeados por significado**
  ("exigido em praticamente todas" / "aparece com frequência" / "menos comum, mas
  presente") — hierarquia por densidade, **nada é escondido ou colapsado**. Sem os
  buckets Alta/Média/Baixa. Se a vaga não distingue requisito de diferencial, tudo é
  tratado como obrigatória. A IA extrai/normaliza/classifica o tipo; o código agrega,
  ordena e estratifica.
- O gap analysis (cruzar skills da vaga x do CV) é lógica de conjunto em Java
  puro, sem IA.
- Chamadas de IA separadas por etapa (extração barata x roadmap caro).
- Normalização de nomes de skill é crítica ("React"="ReactJS"="React.js").
  Extração das vagas e do CV deve usar vocabulário consistente entre si.
- Rate limiting básico por IP nos endpoints que chamam a IA (proteção contra
  abuso, já que o app é público).
- O **currículo é obrigatório em 4 dos 5 fluxos** — só o fluxo de múltiplas vagas
  sem currículo (opção 2) dispensa. A entrada é por fluxo: cada um pede exatamente o
  que precisa, não uma tela genérica com tudo opcional.
- **Limites de entrada: 3 a 8 vagas** nos fluxos de múltiplas vagas (2 e 3),
  validados no backend (não só na interface) e comunicados antes de o usuário
  esbarrar. Com 1 vaga, o caminho é a opção 1.

## Princípios de análise (transversais)

Valem para todos os fluxos que os envolvem. Detalhamento em `redesenho-produto.md`.

- **Análise de ATS que explica o mecanismo.** Cada apontamento traz: o que está no
  currículo, **por que** atrapalha a leitura automatizada, e o que fazer.
  Direcionada quando há vaga (casamento de vocabulário, termos que a vaga repete e o
  CV não menciona); estrutural quando não há (multi-coluna, texto em imagem, PDF não
  extraível, seções não convencionais). **Honestidade:** não existe ATS único;
  apontar os problemas de maior impacto provável sem prometer aprovação em filtro
  específico.
- **Sugestões sobre lacunas — os dois caminhos.** Apontar tudo que a vaga pede e o
  CV não demonstra é obrigatório. Para cada lacuna, as duas saídas sem presumir qual
  se aplica: se a pessoa **tem** e não explicitou → ajustar o currículo; se **não
  tem** → estudar (roadmap).
- **Suficiência das recomendações.** Se o currículo já está sólido, **dizer isso** e
  limitar as recomendações ao pontual e relevante — ou a nenhuma. Fabricar melhorias
  destrói a confiança. A avaliação precisa poder concluir "está bom".
- **Regras do roadmap.** Duas variantes: **completo** (tudo, do zero, ignora o CV) e
  **direcionado** (só o que falta; cobertas aparecem na sequência, marcadas como
  concluídas, sem detalhe, **não expansíveis**). A **escolha só existe quando há
  currículo** — sem CV (opção 2), só o completo. Roadmaps precisam ser **salváveis**.

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
  dá conta. Candidatos: o roadmap de estudo, o feedback de ATS e a comparação de
  currículos.
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

Momentos de destaque (onde caprichar no "uau"): a **apresentação das skills por
densidade de consenso** (o dado bruto — frequência e tipo — com hierarquia visual,
sem tabela crua nem faixas Alta/Média/Baixa) e o **gap analysis** (tem / falta /
sobra) devem ser visuais e agradáveis.

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
- Exportar roadmap **em PDF** — o roadmap precisa ser **salvável**, mas no MVP isso
  é client-side em **Markdown** (download `.md` + copiar); PDF fica para v2.

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
└── frontend/             # aplicação Next.js (Next 16 + TS)
```

O backend roda a partir de `backend/` (é onde estão o `pom.xml` e o wrapper):

```powershell
cd backend
.\mvnw.cmd spring-boot:run   # sobe a app (porta 8080)
.\mvnw.cmd test              # roda a suíte
```

O frontend roda a partir de `frontend/`:

```powershell
cd frontend
npm run dev    # sobe o Next (porta 3000)
npm test       # roda a suíte (Vitest)
```

A chave da Anthropic continua em `backend/src/main/resources/application-local.properties`
(fora do Git). O frontend (em `frontend/`) só consome a API do backend.

## Como trabalhamos

- Construção em fatias verticais, uma de cada vez, cada fatia funcionando ponta
  a ponta antes da próxima.
- Escopo apertado nas tasks: aponte os arquivos/diretórios exatos.
- Nas partes de Spring Boot, explicar o que cada camada faz e por quê
  (estou aprendendo Spring construindo este projeto).
- O estado atual (fatia concluída, decisões tomadas) é rastreado em `PROGRESS.md`.
  Consulte-o ao começar uma sessão e atualize-o ao concluir uma fatia ou fazer uma
  mudança relevante.

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

## Roadmap e estado das fatias

O roadmap completo do redesenho (Fase A: fluxos 1–3; Fase B: fluxos 4–5) está em
`plano-tecnico.md`, e o **estado atual** (fatias concluídas, decisões tomadas) em
`PROGRESS.md`. O CLAUDE.md guarda as regras vigentes, não o histórico do que já
foi feito.