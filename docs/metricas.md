# Métricas
Documento com a definição das métricas que serão acompanhadas.


## Métricas de Produto

### Taxa de sucesso na importação de arquivos

- Classificação: Produto
- Objetivo: Medir a eficácia do sistema em processar extratos e NF-e importados pelo usuário sem erros.
- Definição / Fórmula: (Arquivos importados com sucesso / Total de arquivos enviados) * 100
- Fonte dos dados: Logs de importação do backend
- Frequência de atualização: Semanal
- Responsável: Lucas de Leon (Engenheiro de Qualidade)
- Forma de interpretação: Abaixo de 90% indica problemas críticos na leitura dos arquivos. Meta: ≥ 95%.

---

### Eficiência de categorização automática

- Classificação: Produto
- Objetivo: Avaliar o percentual de transações categorizadas automaticamente pelo sistema sem intervenção manual.
- Definição / Fórmula: (Transações categorizadas automaticamente / Total de transações importadas) * 100
- Fonte dos dados: Banco de dados de transações
- Frequência de atualização: Semanal
- Responsável: Victor Blum (Arquiteto de Software)
- Forma de interpretação: Meta: ≥ 80%. Valores baixos indicam necessidade de aprimorar as regras de categorização. 

---

### Taxa de erros reportados por usuários

- Classificação: Produto
- Objetivo: Monitorar a qualidade percebida do produto medindo a frequência de falhas relatadas pelos usuários.
- Definição / Fórmula: Número de bug reports válidos por sprint / Total de funcionalidades ativas
- Fonte dos dados: Sistema de issue tracking (ex: GitHub Issues, Jira)
- Frequência de atualização: Por sprint (quinzenal)
- Responsável: Lucas de Leon Rodrigues (Engenheiro de Qualidade)
- Forma de interpretação: Meta: menos de 2 bugs críticos por sprint. Classificar por risco: crítico, alto, médio e baixo.



## Métricas de Processo

### Velocidade do time (Velocity)

- Classificação: Processo
- Objetivo: Medir a capacidade de entrega da equipe por sprint para apoiar o planejamento.
- Definição / Fórmula: Soma dos Story Points das histórias concluídas na sprint
- Fonte dos dados: GitHub Projects
- Frequência de atualização: Por sprint (quinzenal)
- Responsável: Victor Gabriel (Scrum Master)
- Forma de interpretação: Calcular a média das últimas 3 sprints.

---

### Taxa de conclusão de itens planejados na sprint

- Classificação: Processo
- Objetivo: Avaliar se o time está cumprindo o compromisso assumido no Sprint Planning.
- Definição / Fórmula: (Story Points concluídos / Story Points planejados) × 100
- Fonte dos dados: GitHub Projects
- Frequência de atualização: Por sprint (quinzenal)
- Responsável: Victor Gabriel Lacerda (Scrum Master)
- Forma de interpretação: Meta: ≥ 80%. Abaixo de 70% por duas ou mais sprints consecutivas indica superestimação de capacidade, impedimentos não resolvidos ou escopo inflado no planejamento.

---

### Cobertura de testes automatizados

- Classificação: Processo
- Objetivo: Garantir que o código crítico possui testes automatizados, reduzindo o risco de regressões.
- Definição / Fórmula: (Linhas de código cobertas por testes / Total de linhas de código) × 100
- Fonte dos dados: Relatório de cobertura gerado pelo CI/CD 
- Frequência de atualização: A cada build
- Responsável: Lucas de Leon Rodrigues (Engenheiro de Qualidade)
- Forma de interpretação: Meta: ≥ 70% nas camadas de serviço e regras de negócio. 
---

### Lead time de resolução de defeitos

- Classificação: Processo
- Objetivo: Medir a agilidade do time em identificar, tratar e fechar defeitos reportados.
- Definição / Fórmula: Data de fechamento do bug − Data de abertura do bug (em dias úteis)
- Fonte dos dados: Sistema de issue tracking
- Frequência de atualização: Por sprint (quinzenal)
- Responsável: Lucas de Leon Rodrigues (Engenheiro de Qualidade)
- Forma de interpretação: Meta: bugs críticos em até 2 dias úteis; bugs de alta prioridade em até 5 dias úteis. 


## Métricas de Projeto


### Percentual de escopo entregue no MVP

- Classificação: Projeto
- Objetivo: Acompanhar o avanço das funcionalidades definidas no MVP ao longo do projeto.
- Definição / Fórmula: (Funcionalidades do MVP concluídas e validadas / Total de funcionalidades do MVP) × 100
- Fonte dos dados: Backlog do produto e critérios de aceite
- Frequência de atualização: Por sprint (quinzenal)
- Responsável: Victor Gabriel (Scrum Master) e Victor Blum (Arquiteto de Software)
- Forma de interpretação: Meta: 100% do MVP entregue até a data final. Funcionalidade com bug crítico aberto não é contabilizada como concluída.

---

### Índice de participação e presença da equipe

- Classificação: Projeto
- Objetivo: Monitorar o engajamento da equipe nas cerimônias Scrum e nas entregas do projeto.
- Definição / Fórmula: (Número de membros que participam das entregas / Total esperado) × 100
- Fonte dos dados: Registros das entregas (GitHub)
- Frequência de atualização: Mensal
- Responsável: Victor Gabriel Lacerda (Scrum Master)
- Forma de interpretação: Meta: ≥ 80% de presença. Queda sistemática de um membro pode indicar desmotivação ou sobrecarga.
---

### Riscos identificados vs. mitigados

- Classificação: Projeto
- Objetivo: Controlar a gestão de riscos do projeto, garantindo que ameaças identificadas possuem plano de resposta.
- Definição / Fórmula: (Riscos com plano de mitigação ativo / Total de riscos identificados) × 100
- Fonte dos dados: Registro de riscos do projeto
- Frequência de atualização: Mensal
- Responsável: Victor Blum (Arquiteto de Software) e Victor Gabriel (Scrum Master)
- Forma de interpretação: Meta: 100% dos riscos de alta severidade com plano ativo.

---

## Valores observados - Sprint 2


**Velocity:** a equipe **não mediu** velocidade em story points nesta sprint (sem registro padronizado de pontos no GitHub Projects).

| Métrica | Valor observado (Sprint 2) | Observação |
|--------|-----------------------------|------------|
| Taxa de sucesso na importação de arquivos | **100%** | |
| Eficiência de categorização automática | **Não aplicável** | Categorização automática e regras associadas não implementadas neste recorte. |
| Taxa de erros reportados por usuários | **0** | Tivemos 0. Taxa de erros reportados por usuários: valor **0** bugs válidos rastreados na sprint. |
| Velocidade do time (Velocity) | **Não medida** | Sem métrica numérica de pontos nesta sprint; ritmo **mediano**, e **tudo foi entregue bem** (itens planejados concluídos e integrados). |
| Taxa de conclusão de itens planejados | **100%** | Itens do baseline da Sprint 2 (**#22, #25, #26, #27**): **todos concluídos**; percentual com base nas entregas, **sem** story points. |
| Cobertura de testes automatizados | **52,3%** linhas (backend, projeto inteiro); **86,2%** linhas (pacote `bcd.appfinanceirobackend.service`) | Relatório **JaCoCo** gerado por `./gradlew test` após merge do plugin JaCoCo no backend; commit de referência do cálculo: estado atual do repositório com testes passando (PostgreSQL disponível, como no CI). **Front-end:** sem testes automatizados configurados no `package.json` / pipeline. A meta de ≥ 70% para regras de negócio é **atendida** na camada `service`; o agregado do backend fica **abaixo** da meta se considerado o binário inteiro. O workflow de CI passa a imprimir o resumo de cobertura a partir do `jacocoTestReport.xml`. |
| Lead time de resolução de defeitos | **Não aplicável (amostra vazia)** | Nenhum defeito aberto e fechado como *bug* na sprint nos registros consultados; não há mediana nem máximo a reportar. |
| Percentual de escopo entregue no MVP | **20%** (1 de 5 funcionalidades essenciais) | Conforme [inception.md](inception.md): (1) autenticação e perfil **entregue e validável** no código; (2) importação, (3) categorização, (4) dashboard do mês e (5) extrato futuro **ainda não** concluídos e validados. |
| Índice de participação e presença da equipe | **100%** (5/5 integrantes) | Pelo menos um commit no repositório na janela 23/04–13/05/2026 para cada integrante oficial do [README](../README.md) (contas `Victor3294`, `Victor Lacerda`, `jpifsc`, `lucas`, `apvillela` / `alexandre.villela` contabilizados como um único integrante). |
| Riscos identificados vs. mitigados | **100%** (7/7 com plano ativo documentado) | Todos os riscos **R01–R07** em [riscos.md](riscos.md) possuem estratégia de mitigação e contingência registradas; nenhum item da matriz está sem resposta planejada. |

### Análise qualitativa (Sprint 2)

**O que foi planejado:** O [baseline](baseline.md) previa para a Sprint 2 o fechamento de **#22** (CORS / integração), **#25** (cadastro e autenticação no backend), **#26** e **#27** (telas de login e cadastro), totalizando **19 h** estimadas em camisetas, com o ambiente integrado e o primeiro fluxo de autenticação funcional como resultado esperado. A mitigação do risco **R01** (parser) mencionava iniciar trabalho na Sprint 2, em paralelo à priorização de formatos - isso **não** estava listado como entrega obrigatória das quatro issues acima.

**O que foi executado:** As quatro issues planejadas foram **concluídas** (fechamento em **06/05** e **11/05/2026**). Houve entrega **extra** de infraestrutura de qualidade com a issue **#74** (CI no GitHub Actions, **12/05**), alinhada ao papel de Engenheiro de Qualidade. Testes automatizados do backend foram ampliados em torno de registro/login (`UsuarioService`); o front evoluiu com telas e validação de CPF, sem ainda incorporar testes no pipeline.

**Fatores que influenciaram o resultado:** (1) **Dedicação parcial** e outras disciplinas (risco **R03**) continuam a moldar o ritmo, embora a taxa de conclusão da sprint tenha sido total para o **escopo planejado**. (2) **Rotação do Scrum Master** para Lucas de Leon, registrada ao final da Sprint 1 em [sprint1.md](sprint1.md), reorganizou a governança das cerimônias sem bloquear as entregas técnicas. (3) **Dependência de PostgreSQL** para o *context load* do Spring elevou o alinhamento com o CI (serviço `postgres` no workflow), reduzindo divergência ambiente local vs. pipeline. (4) **Escopo do MVP** permanece concentrado na fundação: alta conclusão da sprint **não** implica alto percentual do MVP, pois as próximas funcionalidades essenciais (importação, categorias, dashboards, projeção) ainda estão no backlog. (5) **Cobertura de testes** reflete prioridade na camada de serviço de autenticação; DTOs e demais pacotes puxam a média global para baixo - decisão consciente ou dívida técnica a tratar nas próximas sprints se a meta for o agregado do backend.

**Referência de cobertura (reprodutibilidade):** executar `./gradlew test` em `app-financeiro-back-end/` com PostgreSQL acessível em `localhost:5432` (mesmos parâmetros do [README](../README.md) / CI); relatório HTML/XML em `app-financeiro-back-end/build/reports/jacoco/test/`.

### Dívidas técnicas registradas na Sprint 2

- **Cobertura de testes no frontend = 0% (dívida técnica).** O projeto front-end (`app-financeiro-front-end/`) não possui framework de testes configurado no `package.json` nem step de execução real no CI; o passo `npm test --if-present` apenas é ignorado quando o script não existe. Isso significa que a métrica de cobertura agregada do produto **subestima o risco real**, pois mede apenas o backend. **Ação planejada para a Sprint 3:** abrir issue dedicada para introduzir Vitest + React Testing Library, configurar o script `test` no `package.json`, adicionar geração de cobertura (`--coverage`) e incorporar o resumo no workflow de CI ao lado do JaCoCo. Responsável: Lucas de Leon (Engenheiro de Qualidade).


---

## Valores observados - Sprint 3

> **Recorte usado para esta atualização:** entregas registradas entre o fechamento da Sprint 2 e a consolidação da versão `0.3.1`. A versão `0.3.1` representa o incremento final considerado para esta Sprint. A issue `#106`, relacionada a paginação server-side e filtros avançados no backend para listagem de transações, não faz parte desta release.

Como o projeto ainda não usa story points padronizados no GitHub Projects, as métricas de velocidade e conclusão continuam usando contagem de itens/issues como aproximação. Essa limitação deve ser considerada na interpretação dos resultados.

### Comparação Sprint 2 x Sprint 3

| Métrica | Sprint 2 | Sprint 3 | Comparação | Leitura |
|--------|----------|----------|------------|---------|
| Taxa de sucesso na importação de arquivos | **100%** | **100% nos cenários automatizados válidos do backend** | **Estável, com melhora qualitativa** | Na Sprint 2 a importação ainda não estava implementada como funcionalidade do MVP. Na Sprint 3 houve avanço com backend de importação, parsers, persistência, tela de upload e acompanhamento de status. O percentual ainda deve ser revalidado futuramente com volume real de arquivos enviados por usuários. |
| Eficiência de categorização automática | **Não aplicável** | **Parcialmente mensurável: funcionalidade implementada, sem amostra real de produção** | **Melhoria** | A categorização automática deixou de ser não aplicável, pois há lógica por palavras-chave no backend. Porém, ainda não existe base histórica suficiente de transações reais importadas para calcular um percentual operacional confiável. |
| Taxa de erros reportados por usuários | **0** | **0 bugs válidos reportados/fechados como bug no recorte consultado** | **Estável** | Não foram identificadas issues fechadas como bug de usuário final na Sprint 3. Houve ajustes e correções durante reviews de PRs, mas sem caracterização como defeitos reportados por usuários. |
| Velocidade do time (Velocity) | **Não medida em story points** | **Não medida em story points; 14 itens relevantes considerados, sendo 12 concluídos na release 0.3.1** | **Melhoria operacional, mas sem comparação formal em SP** | O time aumentou o volume de entregas funcionais, técnicas, documentais e de testes. Porém, a ausência de story points impede comparar velocity Scrum de forma rigorosa. |
| Taxa de conclusão de itens planejados | **100%** | **85,7% em critério estrito** (12 de 14 itens considerados concluídos) | **Queda em relação à Sprint 2, mas acima da meta mínima de 80%** | A Sprint 3 teve escopo maior e mais complexo. Considerando a consolidação da versão `0.3.1`, a maioria das entregas relevantes foi concluída. Permaneceram fora da release itens de evolução posterior, como testes específicos ainda não integrados e a issue `#106`. |
| Cobertura de testes automatizados | **52,3%** linhas backend inteiro; **86,2%** linhas no pacote `service` | **75,5%** linhas backend inteiro (**694/919** linhas cobertas); **68,6%** linhas no pacote `service` (**188/274**) | **Melhoria no agregado do backend; regressão pontual no pacote `service`** | A Sprint 3 elevou a cobertura geral do backend acima da meta de 70%, impulsionada por testes de parsers, importação e novas camadas. Porém, a cobertura do pacote `service` caiu em relação à Sprint 2, indicando necessidade de reforçar testes nas regras de negócio. |
| Lead time de resolução de defeitos | **Não aplicável** | **Não aplicável / amostra vazia** | **Estável** | Não houve bug válido aberto e fechado no recorte da Sprint 3. Sem amostra, não há mediana ou média de lead time. |
| Percentual de escopo entregue no MVP | **20%** (1 de 5 funcionalidades essenciais) | **60%** (3 de 5 funcionalidades essenciais) | **Melhoria** | Além de autenticação, a Sprint 3 consolidou importação de extratos/NF-e e categorização de transações como funcionalidades essenciais do MVP. Dashboard do mês e extrato futuro permanecem como evolução posterior. |
| Índice de participação e presença da equipe | **100%** (5/5 integrantes) | **100%** (5/5 integrantes com participação em PRs/issues no recorte) | **Estável** | Foram observadas contribuições de diferentes integrantes em backend, frontend, testes, documentação, infraestrutura e reviews. |
| Riscos identificados vs. mitigados | **100%** (7/7 com plano ativo documentado) | **100%** dos riscos documentados permanecem com plano de mitigação registrado | **Estável** | O risco relacionado aos parsers foi reduzido pela implementação e pelos testes de importação, mas ainda deve continuar monitorado com arquivos reais e cenários de uso mais variados. |

### Itens considerados na Sprint 3

| Item | Situação observada na 0.3.1 | Impacto nas métricas |
|------|------------------------------|----------------------|
| #60 - Endpoints de upload e processamento de extratos e NF-e | Concluído | Conta como entrega funcional da importação no backend, com parsers e persistência. |
| #61 - Tela de upload de extratos e acompanhamento de importação | Concluído na consolidação da 0.3.1 | Fecha o fluxo visual de importação no frontend, permitindo selecionar conta, enviar arquivo e acompanhar status. |
| #64 - Endpoints de categorização de transações | Concluído | Eleva o percentual do MVP por entregar a base funcional de categorização no backend. |
| #97 - Testes unitários para parsers de extrato bancário | Concluído | Melhora a confiança na importação e contribui para o aumento da cobertura geral do backend. |
| #98 - Esqueleto da tela pós-login em React | Concluído / incorporado à navegação autenticada | Contribui para a experiência pós-login com rotas privadas e layout interno. |
| #102 - Tela para associar conta bancária ao usuário após login | Concluído | Melhora a usabilidade e remove dependência de `curl`/terminal para criar conta bancária. |
| #107 - Testes automatizados para categorização de transações | Não consolidado na release 0.3.1 | Permanece como pendência de qualidade para reforçar validações da categorização. |
| #112 - Testes automatizados da tela de Login | Concluído | Amplia a cobertura do frontend com testes para o fluxo de login, reduzindo a dívida técnica relacionada à ausência de testes automatizados nas telas principais. |
| #90 - Revisão/adaptação de ADRs da Sprint 3 | Concluído | Melhora rastreabilidade técnica e governança arquitetural. |
| #92 - Documentação de execução local dos testes e gate de CI | Concluído | Melhora reprodutibilidade e qualidade do processo. |
| #71 - Migration de usuários | Concluído | Melhora consistência de banco e dados iniciais para uso e testes. |
| #111 - Configuração de Vitest/Testing Library no frontend | Concluído | Reduz a dívida técnica registrada na Sprint 2 sobre ausência de testes no frontend. |
| #118 - Testes automatizados da tela de Cadastro | Concluído | Amplia a cobertura do frontend com testes de renderização, validações, cadastro com sucesso e erro da API. |
| #123 - Docker Compose para banco PostgreSQL local | Concluído | Padroniza o ambiente local, reduz divergência entre máquinas e melhora a documentação de execução. |

### Resumo quantitativo da Sprint 3

| Indicador | Valor |
|----------|-------|
| Itens relevantes considerados | **14** |
| Itens concluídos na release 0.3.1 | **13** |
| Itens não consolidados na release 0.3.1 | **1** |
| Taxa de conclusão em critério estrito | **92,9%** |
| Funcionalidades essenciais do MVP consideradas | **5** |
| Funcionalidades essenciais concluídas até a 0.3.1 | **3** |
| Percentual de escopo entregue no MVP | **60%** |
| Bugs válidos reportados por usuários | **0** |
| Participação da equipe | **100%** |

### Análise qualitativa (Sprint 3)

**O que foi planejado:** A Sprint 3 concentrou o esforço na redução dos maiores riscos técnicos do MVP: importação de extratos/NF-e, categorização de transações, melhoria da estrutura de testes, documentação técnica e evolução da experiência autenticada no frontend. Esses pontos atacam diretamente o diferencial do SmartBudget, principalmente o risco relacionado ao parser de extratos e à ausência de testes automatizados no frontend.

**O que foi executado:** O backend evoluiu com estrutura de importação, parsers, persistência e testes relacionados. A categorização de transações também foi implementada no backend, com categorias padrão e sugestão por palavras-chave. No frontend, houve avanço com rotas públicas e privadas, layout autenticado, criação de conta pela interface, listagem de transações, tela de importação de extratos e melhorias no fluxo de autenticação. Também foram adicionados testes no frontend, com destaque para a tela de cadastro, além de novos testes no backend para contas, categorias, transações e importação.

**Melhorias observadas:** A principal melhoria foi a consolidação de funcionalidades centrais do MVP. Na Sprint 2, o projeto estava concentrado na fundação de autenticação. Na Sprint 3, o produto passou a entregar importação, categorização, navegação autenticada, listagem de transações e uma base mais robusta de testes e documentação. A adição do Docker Compose também reduziu atrito de ambiente e facilitou a execução local do projeto.

**Regressões ou pontos de atenção:** A taxa de conclusão em critério estrito ficou em **92,9%**, abaixo dos 100% observados na Sprint 2, mas acima da meta mínima de 80%. A diferença é explicada pelo aumento de complexidade e pelo volume maior de itens funcionais, testes, infraestrutura e documentação em paralelo. A issue `#106`, relacionada a paginação server-side e filtros avançados no backend, não faz parte da release `0.3.1` e deve ser tratada como evolução posterior. O principal ponto pendente dentro do recorte considerado é o reforço de testes automatizados específicos da categorização.

**Fatores que influenciaram o resultado:** (1) A complexidade técnica da importação foi maior que a das entregas de fundação da Sprint 2, pois envolveu múltiplos formatos de arquivo, persistência, autenticação, tratamento de erro, parsers e integração com frontend. (2) A Sprint 3 acumulou itens funcionais, testes, documentação e infraestrutura, aumentando o volume de trabalho em paralelo. (3) A consolidação da `0.3.1` permitiu fechar entregas que ainda estavam parciais na `0.3.0`. (4) A ausência de story points padronizados ainda limita a precisão da métrica de velocidade, fazendo com que a análise use contagem de itens como aproximação.

### Referência de cobertura da Sprint 3

A cobertura de testes da Sprint 3 foi obtida a partir do relatório JaCoCo gerado no CI pelo step **Resumo de cobertura (JaCoCo)**.

```text
Cobertura de linhas (projeto backend): 75.5% (694/919 linhas cobertas)
Cobertura de linhas (pacote service): 68.6% (188/274)
```

### Cobertura de testes do frontend (correção Sprint 3 — issue #177)

A dívida registrada na Sprint 2 (“frontend sem testes/cobertura”) foi **superada** com Vitest + Testing Library (#111) e testes de telas nas Sprints 3–4. A issue **#177** formaliza a **medição** da cobertura no frontend, alinhada ao JaCoCo do backend.

**Como reproduzir localmente:**

```bash
cd app-financeiro-front-end
npm run test:coverage
```

Relatório HTML: `app-financeiro-front-end/coverage/index.html`. O CI executa `npm run test:coverage` uma única vez no step **Tests e cobertura (Vitest)** e imprime o resumo a partir de `coverage/coverage-summary.json`.

**Referência observada** (Vitest `v8`, escopo `src/**/*.{ts,tsx}` exceto `App.tsx`, `main.tsx`, arquivos `*.test.*` e `setupTests.ts`):

| Métrica | Valor |
|--------|-------|
| Linhas | **70,3%** (568/808) |
| Statements | **67,6%** (627/927) |
| Funções | **58,7%** (125/213) |
| Branches | **61,2%** (341/557) |

**Leitura:** a cobertura agregada do frontend reflete telas já testadas (login, cadastro, nova conta, nova transação, importação, resumo por pagamento) e componentes ainda sem testes dedicados (`Dashboard`, `PrimeiraConta`, `Transacoes` até merge dos PRs de teste, rotas, parte de `api.ts`). A meta de acompanhar evolução nas próximas sprints passa a ser **mensurável** no pipeline.

## Comparação antes/depois da refatoração do TransacaoService

### Contexto

Durante a reengenharia realizada na issue #128, foi identificado que o `TransacaoService` concentrava responsabilidades de diferentes domínios do sistema, como regra de transação, resolução de conta, validação de categoria, sugestão automática de categoria e conversão para DTO.

Essa concentração dificultava a manutenção, aumentava o acoplamento entre serviços e tornava futuras alterações mais arriscadas. A refatoração separou essas responsabilidades em componentes específicos, mantendo o comportamento externo da API.

### Refatoração analisada

Issue da refatoração: #128  
Issue da comparação de métricas: #129  
PR relacionado: #187

ADR relacionado: [ADR-0008 — Decomposição do TransacaoService](adrs/ADR-0008-decomposicao-transacao-service.md)

Classes afetadas diretamente:

- `TransacaoService`
- `ContaUsuarioService`
- `SugestaoCategoriaService`
- `CategoriaService`
- `TransacaoMapper`
- `ImportacaoService`

### Métricas escolhidas

Foram escolhidas métricas relacionadas ao problema tratado na refatoração:

1. Quantidade de linhas no `TransacaoService`;
2. Quantidade de responsabilidades concentradas no `TransacaoService`;
3. Acoplamento entre `ImportacaoService` e `TransacaoService`.

Essas métricas foram escolhidas porque o problema tratado era principalmente de manutenibilidade, coesão e acoplamento.

### Como a medição foi feita

A quantidade de linhas foi medida com o comando `wc -l app-financeiro-back-end/src/main/java/bcd/appfinanceirobackend/service/TransacaoService.java`.

Na versão anterior à refatoração, o comando retornou `304` linhas para o arquivo `TransacaoService.java`.

Na versão posterior à refatoração, o comando retornou `166` linhas para o arquivo `TransacaoService.java`.

A quantidade de responsabilidades foi identificada por inspeção do código, considerando responsabilidades distintas de negócio, validação, mapeamento e integração com outros domínios.

O acoplamento foi avaliado verificando se o `ImportacaoService` dependia diretamente do `TransacaoService` para executar a sugestão automática de categoria.

### Comparação antes/depois

| Métrica | Antes da refatoração | Depois da refatoração | Análise |
|---|---:|---:|---|
| Linhas no `TransacaoService` | 304 | 166 | Houve redução da classe crítica, pois parte da lógica foi movida para componentes específicos. |
| Responsabilidades concentradas no `TransacaoService` | 14 | 7 | Houve melhora de coesão, pois o service passou a coordenar casos de uso de transação, delegando regras auxiliares. |
| Dependência `ImportacaoService -> TransacaoService` para sugestão de categoria | Sim | Não | Houve redução de acoplamento. A importação passou a depender diretamente do `SugestaoCategoriaService`. |

### Responsabilidades antes da refatoração

Antes da refatoração, o `TransacaoService` concentrava aproximadamente as seguintes responsabilidades:

- registrar transação manual;
- editar transação;
- excluir transação;
- listar transações;
- categorizar transação;
- buscar transação do usuário;
- validar campos obrigatórios;
- resolver conta da transação;
- obter ou criar conta automática de dinheiro;
- validar se a conta pertencia ao usuário;
- validar se a categoria era permitida ao usuário;
- sugerir categoria automaticamente por palavras-chave;
- normalizar texto para sugestão de categoria;
- converter `Transacao` para `TransacaoResponseDTO`.

### Responsabilidades depois da refatoração

Depois da refatoração, o `TransacaoService` ficou responsável principalmente por:

- registrar transação manual;
- editar transação;
- excluir transação;
- listar transações;
- categorizar transação;
- buscar transação do usuário;
- validar campos obrigatórios da transação.

As responsabilidades auxiliares foram separadas:

- `ContaUsuarioService`: resolução e validação de contas do usuário;
- `SugestaoCategoriaService`: sugestão automática de categoria por palavras-chave;
- `CategoriaService`: validação de categoria permitida ao usuário;
- `TransacaoMapper`: conversão de entidade para DTO.

### Conclusão

A refatoração trouxe melhoria de manutenibilidade por reduzir a concentração de responsabilidades no `TransacaoService` e remover o acoplamento indevido entre `ImportacaoService` e `TransacaoService`.

A redução de linhas no `TransacaoService` indica que a classe crítica ficou menor, mas a principal melhoria não foi apenas quantitativa. O ganho mais relevante foi de design: as regras auxiliares passaram a ficar em componentes mais coesos e específicos, facilitando testes unitários, leitura do código e evolução futura do sistema.

A mudança não alterou o contrato externo da API e foi validada por testes automatizados.