# Match_CV — Redesenho de produto

Este documento define **o que a aplicação deve entregar**, não como implementar.
O plano técnico (endpoints, fatias, o que muda no código existente) é o próximo
passo e deve ser produzido a partir daqui.

---

## Contexto: por que este redesenho

A implementação atual expõe uma tela única onde o usuário cola vagas e,
opcionalmente, anexa o currículo. Isso gerou dois problemas:

**1. O usuário não descobre o que a aplicação faz.** Existem várias combinações
possíveis (uma vaga ou várias, com currículo ou sem, só currículo), mas a
interface não comunica nenhuma delas. A pessoa chega, vê um campo de texto, e
supõe que o app faz uma coisa só.

**2. A análise entrega pouco valor.** A classificação em três níveis
(Alta/Média/Baixa) produz resultados sem informação: com 1 vaga, todas as skills
caem em ALTA; com 2 vagas, quase todas caem em MÉDIA. Nos dois casos o usuário vê
uma lista de chips sem frequência, sem contexto e sem hierarquia real. A
apresentação também descarta dados que o backend já extrai (frequência e se a
skill é obrigatória ou diferencial).

A conclusão é que a aplicação precisa ser organizada em **fluxos distintos**,
cada um respondendo uma pergunta concreta do usuário, com entrega própria.

---

## As cinco opções

O usuário escolhe o que quer fazer antes de fornecer os dados. As opções devem
ser nomeadas pela pergunta que respondem, não pela funcionalidade técnica.

**Cada opção precisa exibir, além do nome, uma descrição curta do que ela
entrega.** O nome sozinho não basta para alguém que chega pela primeira vez
decidir qual fluxo serve para o seu caso — e a descoberta das funcionalidades é
justamente um dos problemas que este redesenho existe para resolver. A descrição
deve dizer o que a pessoa vai receber e o que precisa fornecer (uma vaga? várias?
currículo?), em linguagem direta.

---

### 1. Uma vaga + currículo — "Estou pronto pra esta vaga?"

Provavelmente a porta de entrada da aplicação: responde uma pergunta urgente com
esforço mínimo — uma vaga, um currículo. A pessoa viu um anúncio e quer saber se
vale se candidatar, o que falta, e como ajustar o currículo para essa vaga
específica. Merece destaque na tela de escolha.

**Entrega:**

- O quanto o perfil cobre o que a vaga pede.
- O que já tem e o que falta, separando o que a vaga trata como **obrigatório**
  do que trata como **diferencial**. Com uma vaga só não existe frequência, então
  o tipo é o eixo principal de classificação.
- Avaliação de ATS **específica daquela vaga**: o casamento entre o vocabulário
  da vaga e o do currículo, termos que a vaga repete e o currículo não menciona,
  requisitos que o filtro provavelmente procura e não encontra.
- Sugestões de melhoria em duas frentes: **alinhamento com a vaga** (o que
  adicionar, remover ou reescrever) e **qualidade geral do documento para ATS**.
- Roadmap sob demanda, com escolha entre completo e direcionado (ver "Regras do
  roadmap").

---

### 2. Múltiplas vagas (3 a 8), sem currículo — "O que o mercado está pedindo?"

A pessoa quer entender o cenário do segmento que está mirando: o que se repete, o
que é consenso, o que é nicho. Não é sobre ela ainda — é sobre o mercado.

**Entrega:**

- Panorama do que o conjunto de vagas pede, com os dados brutos visíveis:
  frequência (em quantas vagas a skill apareceu) e como ela aparece (obrigatória
  ou diferencial).
- Ordenação por **frequência ponderada pelo tipo**: uma skill que aparece em 3 de
  4 vagas sempre como obrigatória é mais forte que outra em 3 de 4 sempre como
  diferencial. O peso ordena; o que se exibe é o dado bruto, não um score
  composto — a pessoa lê o fato ("Java · 4/4 vagas · sempre obrigatória") e a
  posição na lista já reflete a força.
- Quando a vaga não distingue requisitos de diferenciais, o padrão é tratar tudo
  como obrigatório (comportamento conservador).
- Aviso, baseado no conteúdo analisado, quando as vagas parecem ser de segmentos
  diferentes — misturar backend Java com frontend React dilui o resultado. O
  aviso é mais útil depois da análise (com evidência) do que como texto genérico
  antes.
- Roadmap geral sob demanda, priorizado pelo que mais se repete, incluindo os
  diferenciais mais pedidos. Precisa ser salvável pelo usuário — um roadmap que
  não pode ser guardado perde o sentido.

**Sobre a apresentação:** o desafio é mostrar muitas skills sem virar uma parede
de chips indistintos. **Nada deve ser escondido ou colapsado** — uma skill que
aparece em 1 de 6 vagas ainda é uma vaga real do mercado e pode ser um
diferencial para o usuário. A solução é hierarquia por densidade decrescente, não
ocultação: as skills de maior consenso aparecem com mais presença visual
(barra, número, destaque), as intermediárias em formato mais compacto, e as
pontuais em formato ainda mais enxuto — todas visíveis. Os estratos devem ser
nomeados pelo que significam ("exigido em praticamente todas" / "aparece com
frequência" / "menos comum, mas presente"), não por faixas numéricas.

---

### 3. Múltiplas vagas (3 a 8) + currículo — "Onde estou em relação ao mercado?"

A opção mais completa: junta a leitura de mercado da opção 2 com a análise de
perfil da opção 1, aplicada a um conjunto de vagas. É mais eficiente que a opção
1 para ler o mercado e a situação do usuário em relação ao que está sendo pedido.

**Entrega:**

- **O que falta** no currículo em relação ao que essas vagas pedem — foco
  principal da tela, ordenado pelo que é mais frequente e mais obrigatório. Uma
  skill que aparece em 5 de 6 vagas e não está no currículo é a informação mais
  valiosa do fluxo.
- O que a pessoa já cobre (útil para confiança, mas não acionável — merece pouco
  espaço).
- **Sinergia por vaga**: quanto o perfil atende cada vaga individualmente, para
  saber onde aplicar primeiro. É a parte mais única deste fluxo e merece bloco
  próprio.
- Panorama do que o conjunto pede (mesma lógica da opção 2).
- Avaliação de ATS do currículo com foco naquele segmento de vagas.
- Sugestões de melhoria: de skills e do documento em si.
- Roadmap sob demanda, com escolha entre completo e direcionado.

**Ordem de leitura sugerida:** gap → sinergia por vaga → panorama de mercado →
ATS e sugestões de currículo.

---

### 4. Só currículo — "Meu currículo é bom?"

Sem vaga como referência. Avalia o currículo em três frentes. Esta é a opção onde
a análise de ATS é o centro do valor.

**Compatibilidade com ATS (o núcleo):** não observações genéricas, mas problemas
concretos acompanhados do motivo pelo qual atrapalham — estrutura e seções,
formatação que quebra a leitura automatizada, texto não extraível, nomenclatura
de seções não convencional. Cada apontamento explica o mecanismo, para a pessoa
entender por que vinha sendo rejeitada.

**Posicionamento:** que perfil o currículo comunica e para que tipo de vaga ele
está afiado. Muita gente usa o mesmo currículo para segmentos diferentes e não
percebe o desalinhamento. Quando o currículo reúne tecnologias de áreas que não
conversam entre si — comum em quem tenta cobrir todas as vagas com um documento
só — o app aponta a dispersão e explica o efeito: em vez de parecer versátil, o
perfil fica indefinido e perde aderência em qualquer filtro. A sugestão nesse
caso é direcionamento: identificar o segmento mais forte e recomendar versões
separadas por área.

**Solidez técnica e de escrita:** o que está vago ou sem contexto, lacunas, e —
em relação ao que o mercado costuma pedir para o perfil identificado — o que
falta para ser um candidato mais forte. Sobre evidência prática, a leitura é do
conjunto e não item a item: é normal um currículo listar mais tecnologias do que
detalha em projetos, e apontar cada uma seria contraproducente. O apontamento só
se justifica quando há desequilíbrio claro entre o que é declarado e o que é
demonstrado, e é especialmente relevante quando **não há nenhum projeto ou
experiência** que mostre aplicação real — caso em que sugerir a inclusão de
projetos é uma das melhorias de maior impacto, sobretudo para quem busca a
primeira vaga.

**Nota:** o currículo recebe uma pontuação, com os critérios que a compõem
visíveis (compatibilidade ATS, clareza, completude, direcionamento). Sem os
critérios expostos o número vira arbitrário e a pessoa fixa nele em vez de ler o
feedback. Com eles, a nota funciona como índice do que ler primeiro — e permite
comparação na opção 5.

---

### 5. Dois currículos — comparação

A pessoa anexa dois currículos.

**Comportamento padrão — comparação neutra:** o app avalia ambos com os mesmos
critérios da opção 4 e aponta **qual é o mais forte e por quê**, cobrindo todas
as frentes (ATS, estrutura, clareza, solidez técnica, direcionamento), além de
indicar pontos de melhoria de cada um. Serve tanto para quem tem duas versões
guardadas quanto para quem quer saber qual usar em qual situação.

**Opção de comparação de evolução:** na tela de anexo, uma opção — descrita em
linguagem clara para o usuário, não pelo nome do recurso — para o caso de serem a
versão antiga e a nova do mesmo currículo, com indicação de qual é qual.

Quando essa opção é usada, **a ordem da análise é preservada**: o app avalia os
dois às cegas primeiro e forma o veredicto de qual é mais forte **sem saber qual
é o mais recente**; só então analisa o percurso — o que mudou e se cada mudança
ajudou ou prejudicou.

Isso é essencial: saber de antemão qual é a versão nova enviesa a análise na
direção de confirmar que ela melhorou. **Uma versão mais recente não é
automaticamente melhor.** Se a versão nova for pior, o app diz que é pior e
explica onde. Um teste válido do comportamento correto é inverter os anexos de
propósito — o app deve apontar a piora.

Esta opção deve ser oferecida ao fim das opções 1, 3 e 4: "aplicou as melhorias?
volte e compare as versões."

---

## Princípios transversais

### Análise de ATS (opções 1, 3, 4 e 5)

O ATS é a dor mais concreta do público-alvo: a pessoa se candidata, é rejeitada
automaticamente, e nunca descobre por quê. Por isso a análise não pode ser
observação superficial — precisa **explicar o mecanismo**. "Seu currículo tem
duas colunas" é constatação; "layouts de duas colunas costumam ser lidos fora de
ordem pelos parsers, o que embaralha a sequência das suas experiências" é o que
produz o entendimento.

Cada apontamento deve trazer: **o que está no currículo, por que isso atrapalha a
leitura automatizada, e o que fazer.**

A análise assume duas formas conforme haja vaga ou não:

- **Com vaga (opções 1 e 3)** — direcionada: casamento de vocabulário entre vaga
  e currículo, termos que a vaga repete e o currículo não menciona, requisitos
  que o filtro provavelmente procura.
- **Sem vaga (opções 4 e 5)** — estrutural: os problemas que quebram em
  praticamente qualquer sistema (layout multi-coluna, texto em imagem, PDF não
  extraível, seções com nomenclatura não convencional, ausência de seções
  esperadas).

**Ressalva de honestidade:** não existe um ATS único — cada empresa usa um sistema
com comportamento próprio. O app aponta os problemas de maior probabilidade de
impacto e explica o porquê, sem prometer garantia de aprovação em um filtro
específico. Prometer certeza que não se tem destrói a credibilidade da parte que
é genuinamente útil.

### Sugestões sobre lacunas do currículo (opções 1 e 3)

Apontar tudo que a vaga pede e o currículo não demonstra é **obrigatório** — é o
núcleo da análise e uma das entregas mais valiosas.

Ao sugerir o que fazer com a lacuna, apresentar os dois caminhos possíveis:

- Se a pessoa **tem** a competência mas não a explicitou (esqueceu de atualizar
  após novos estudos, ou o currículo está direcionado a outro segmento), a ação é
  ajustar o currículo.
- Se **não tem**, a ação é estudar — e o roadmap existe para isso.

O app apresenta as duas saídas sem presumir qual se aplica: a pessoa sabe.

### Suficiência das recomendações (todas as opções com currículo)

O objetivo é o currículo ficar bom, não gerar retorno infinito. Se o currículo já
está sólido nos critérios avaliados, **isso deve ser dito com clareza** — e as
recomendações se limitam ao que for pontual e realmente relevante, ou a nenhuma.

Fabricar melhorias para ter o que dizer degrada a confiança: o usuário aplica as
sugestões, volta, e recebe outra dezena de ajustes, percebendo que a análise não
tem critério de suficiência. A avaliação precisa ser capaz de concluir "este
currículo está bom".

### Regras do roadmap

Duas variantes, disponíveis conforme o fluxo:

- **Roadmap completo** — tudo que a vaga ou o conjunto de vagas pede, do zero,
  sem considerar o que a pessoa já tem.
- **Roadmap direcionado** — apenas o que falta em relação ao currículo. As skills
  já cobertas aparecem **na sequência correta**, marcadas como concluídas e sem
  detalhamento (ex.: "Java · você já tem"), para que a pessoa não perca a noção
  da trilha inteira.

**A escolha entre as duas só existe quando há currículo.** Nos fluxos sem
currículo (opção 2), só o roadmap completo faz sentido e não se oferece escolha.

**Motivo da escolha existir:** estar no currículo não significa domínio. É comum
alguém usar uma tecnologia em um projeto e listá-la no currículo. Quem sabe que
inflou escolhe o completo; quem confia no próprio currículo escolhe o
direcionado. O app não julga — a pessoa é a única que sabe.

**As etapas marcadas como concluídas não são expansíveis.** Se a pessoa perceber
que não domina algo marcado como concluído, o caminho é gerar o roadmap completo.
Permitir expandir transformaria o direcionado no completo e anularia a escolha.
Um aviso pode indicar essa saída.

**Roadmaps precisam ser salváveis pelo usuário.**

### Limites de entrada

- Opções 2 e 3: **mínimo de 3 vagas, máximo de 8.** Abaixo de 3 não há
  distribuição de frequência que sustente a leitura de mercado (com 2 vagas, tudo
  é 50% ou 100%); acima de 8, o custo e o tempo de análise crescem sem ganho
  proporcional, e a fricção de colar tantas descrições torna o limite
  aspiracional.
- O limite precisa ser validado no backend, não apenas na interface.
- O limite precisa ser comunicado **antes** do usuário esbarrar nele.
- Quando o usuário tem apenas uma vaga, o caminho é a opção 1.

### Consequência sobre o currículo

Com este recorte, **o currículo deixa de ser opcional**: apenas a opção 2
dispensa currículo; as outras quatro exigem. Isso muda o desenho da entrada — cada
fluxo pede exatamente o que precisa, em vez de uma tela genérica com tudo
opcional.

---

## Comportamento observado que precisa ser tratado

Na implementação atual, ao adicionar uma descrição de vaga e clicar para
analisar, se a vaga for excluída durante o processo de análise, a aplicação
remove a descrição mas mantém o resultado da análise na tela, sem forma de
removê-lo — os blocos de skills só saem junto com a exclusão da descrição
adicionada, e esse comportamento só ocorre depois que a análise é feita.

A solução fica em aberto para avaliação técnica.

---

## Como este trabalho deve ser conduzido

**Em fatias verticais**, seguindo o padrão já estabelecido no projeto: cada fatia
funcionando ponta a ponta, com testes, antes da próxima. O escopo aqui é grande
(cinco fluxos, revisão da lógica de classificação, tela de escolha nova, mudanças
no backend e no frontend) e executá-lo de uma vez arrisca quebrar o que já
funciona sem deixar claro onde. O fatiamento em si — quantas fatias, em que
ordem, o que dá para reaproveitar do que já existe — deve ser proposto na
análise técnica.

**O CLAUDE.md e o PROGRESS.md precisam ser atualizados como parte do trabalho**,
não apenas consultados. Várias decisões fixadas hoje entram em conflito com este
redesenho (ver seção seguinte) e devem ser revistas explicitamente, com as novas
decisões registradas e as justificativas preservadas.

---

## Decisões atuais que este redesenho coloca em questão

O plano técnico deve avaliar explicitamente o que da implementação e da
documentação atual deixa de fazer sentido. Pelo menos:

- **A classificação em Alta/Média/Baixa** como está definida no CLAUDE.md — ela é
  a causa direta do problema de valor descrito no contexto.
- **O currículo como anexo opcional** — passa a ser obrigatório em 4 dos 5 fluxos.
- **A tela única de entrada** — substituída por escolha de fluxo.
- **Exportar roadmap em PDF está listado como fora do escopo do MVP** — mas
  roadmaps precisam ser salváveis. A forma (PDF, Markdown, outra) fica em aberto.
- Endpoints existentes e o que o frontend já consome, à luz dos novos fluxos.
