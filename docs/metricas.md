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

> **Recorte usado para esta atualização:** entregas registradas entre o fechamento da Sprint 2 e o final da Sprint 3, considerando os PRs e issues consultados no GitHub e o estado atual do projeto. Como o projeto ainda não usa story points padronizados no GitHub Projects, as métricas de velocidade e conclusão continuam usando contagem de itens/PRs como aproximação, mantendo a mesma limitação registrada na Sprint 2.

### Comparação Sprint 2 x Sprint 3

| Métrica | Sprint 2 | Sprint 3 | Comparação | Leitura |
|--------|----------|----------|------------|---------|
| Taxa de sucesso na importação de arquivos | **100%** | **100% nos cenários automatizados válidos do backend** | **Estável**, com melhora qualitativa | Na Sprint 2 a métrica ainda era limitada, pois a importação não estava implementada. Na Sprint 3 houve avanço técnico relevante com endpoint, parsers e testes para CSV, XML, TXT e NF-e. O percentual deve ser revalidado futuramente com dados reais de uso. |
| Eficiência de categorização automática | **Não aplicável** | **Parcialmente mensurável: funcionalidade implementada, sem amostra real de produção** | **Melhoria** | A categorização automática deixou de ser não aplicável, pois há lógica por palavras-chave no backend. Porém, ainda não existe base histórica de transações reais importadas para calcular um percentual operacional confiável. |
| Taxa de erros reportados por usuários | **0** | **0 bugs válidos reportados/fechados como bug no recorte consultado** | **Estável** | Não foram identificadas issues fechadas como bug de usuário final na Sprint 3. Houve ajustes e reviews em PRs, mas sem caracterização como defeitos reportados por usuários. |
| Velocidade do time (Velocity) | **Não medida em story points** | **Não medida em story points; 12 itens relevantes considerados no recorte, sendo 8 concluídos em critério estrito e 11 com algum avanço verificável** | **Melhoria operacional, mas sem comparação formal em SP** | O time aumentou o volume de entregas técnicas, funcionais, de testes e documentais. Porém, a ausência de story points impede comparar velocity Scrum de forma rigorosa. |
| Taxa de conclusão de itens planejados | **100%** | **67% em critério estrito** (8 de 12 itens considerados concluídos); **91,7% com avanço parcial/review** (11 de 12 itens com entrega, PR, implementação parcial ou avanço verificável) | **Regressão no fechamento estrito, mas com alto avanço operacional** | A Sprint 3 teve escopo maior e mais complexo. Considerando apenas itens concluídos, a taxa cai porque parte das entregas ficou em andamento, em review ou parcialmente implementada, como importação no frontend, testes de login e testes de categorização. Porém, a maior parte dos itens teve avanço concreto durante a sprint. |
| Cobertura de testes automatizados | **52,3%** linhas backend inteiro; **86,2%** linhas no pacote `service` | **75,5%** linhas backend inteiro (**694/919** linhas cobertas); **68,6%** linhas no pacote `service` (**188/274**) | **Melhoria no agregado do backend; regressão pontual no pacote `service`** | A Sprint 3 elevou a cobertura geral do backend acima da meta de 70%, impulsionada por testes de parsers/importação. Porém, a cobertura do pacote `service` caiu em relação à Sprint 2, indicando necessidade de reforçar testes nas regras de negócio adicionadas ou alteradas. O valor foi obtido pelo step “Resumo de cobertura (JaCoCo)” do CI da Sprint 3. |
| Lead time de resolução de defeitos | **Não aplicável** | **Não aplicável / amostra vazia** | **Estável** | Não houve bug válido aberto e fechado no recorte da Sprint 3. Sem amostra, não há mediana ou média de lead time. |
| Percentual de escopo entregue no MVP | **20%** (1 de 5 funcionalidades essenciais) | **40%** (2 de 5 funcionalidades essenciais) | **Melhoria** | A Sprint 3 concluiu a base de categorização de gastos no backend e a Importação de extratos e NFe. |
| Índice de participação e presença da equipe | **100%** (5/5 integrantes) | **100%** (5/5 integrantes com participação em PRs/issues no recorte) | **Estável** | Foram observadas contribuições de diferentes integrantes em backend, frontend, testes, documentação e reviews. |
| Riscos identificados vs. mitigados | **100%** (7/7 com plano ativo documentado) | **100%** dos riscos documentados permanecem com plano de mitigação registrado | **Estável** | O risco R01 foi reduzido pela implementação dos parsers, mas ainda não pode ser considerado totalmente encerrado enquanto o fluxo completo de importação não estiver fechado e validado ponta a ponta. |

### Itens considerados na Sprint 3

| Item | Situação observada | Impacto nas métricas |
|------|--------------------|----------------------|
| #60 - Endpoints de upload e processamento de extratos e NF-e | Avançou tecnicamente com implementação de backend, parsers e processamento, mas a funcionalidade de importação ainda dependia de fechamento ponta a ponta | Conta como avanço relevante da importação, porém não como funcionalidade 100% concluída do MVP. |
| #61 - Tela de upload de extratos e acompanhamento de importação | Em andamento / PR em draft para tela de importação no frontend | Deve ser considerada no planejamento da Sprint 3, mas não deve contar como concluída em critério estrito enquanto estiver em draft ou sem merge. Impacta diretamente o fechamento da funcionalidade de importação no MVP. |
| #64 - Endpoints de categorização de transações | Concluído | Eleva o percentual do MVP de 20% para 40% no critério conservador, pois entrega a base funcional de categorização no backend. |
| #97 - Testes unitários para parsers de extrato bancário | Concluído no recorte | Melhora a confiança na importação e contribui para o aumento da cobertura geral do backend. |
| #98 - Esqueleto da tela pós-login em React | Em desenvolvimento | Deve ser considerado como item da Sprint 3 por evoluir a experiência pós-login e o ponto inicial do usuário autenticado. Não altera diretamente o percentual do MVP funcional caso não esteja integrado à main. |
| #102 - Tela para associar conta bancária ao usuário após login | Concluído | Melhora a usabilidade e remove dependência de `curl`/terminal para criar conta bancária. Também prepara o fluxo necessário para importação de extratos vinculados a uma conta. |
| #107 - Testes automatizados para categorização de transações | Planejado / em aberto no recorte | Deve ser considerado como pendência de qualidade da Sprint 3. Impacta a confiança sobre regras de negócio de categorização, principalmente sugestão automática, atualização manual e validações de segurança. |
| #112 - Testes automatizados da tela de Login | Concluído | Deve ser considerado como avanço de qualidade do frontend, mas não como concluído em critério estrito até aprovação e merge. Ajuda a reduzir a dívida técnica registrada na Sprint 2 sobre ausência de testes no frontend. |
| #90 - Revisão/adaptação de ADRs da Sprint 3 | Concluído | Melhora rastreabilidade técnica e governança arquitetural. |
| #92 - Documentação de execução local dos testes e gate de CI | Concluído | Melhora reprodutibilidade e qualidade do processo. |
| #71 - Migration de usuários | Concluído | Melhora consistência de banco e dados iniciais para uso/testes. |
| #111 - Configuração de Vitest/Testing Library no frontend | Concluída | Reduz dívida técnica registrada na Sprint 2 sobre ausência de testes no frontend, mas ainda requer ampliação da suíte para gerar cobertura significativa. |

### Análise qualitativa (Sprint 3)

**O que foi planejado:** A Sprint 3 concentrou o esforço na redução dos maiores riscos técnicos do MVP: importação de extratos/NF-e, categorização de transações, melhoria da estrutura de testes e atualização das decisões/documentações técnicas. Esses pontos atacam diretamente o diferencial do SmartBudget, principalmente o risco R01, relacionado ao parser de extratos, e o risco R08, relacionado à ausência de testes automatizados.

**O que foi executado:** O backend evoluiu com estrutura de importação, parsers, persistência e testes relacionados. A categorização de transações também foi implementada no backend, com categorias padrão e sugestão por palavras-chave. No frontend, houve avanço na criação de conta bancária pela interface, no esqueleto da tela pós-login e na tela de importação de extratos/NF-e, ainda que parte dessas entregas tenha permanecido em validação, review ou draft no fechamento da sprint. Também houve evolução na infraestrutura de testes com Vitest/Testing Library e avanço em testes específicos, como tela de login e categorização de transações. Além disso, foram atualizadas documentações técnicas, ADRs e orientações de execução de testes/CI.

**Melhorias observadas:** A principal melhoria foi a redução da incerteza técnica sobre importação e categorização. Na Sprint 2, o MVP estava concentrado na fundação de autenticação e registro manual; na Sprint 3, o projeto passou a atacar funcionalidades centrais do produto. Também houve melhoria no processo de qualidade, pois o frontend deixou de estar completamente sem base de testes configurada e o backend recebeu testes adicionais para parsers/importação.

**Regressões ou pontos de atenção:** A taxa de conclusão caiu de 100% na Sprint 2 para **58,3% em critério estrito** na Sprint 3, considerando 7 itens concluídos entre 12 itens planejados/considerados. Essa regressão é explicável pelo aumento de complexidade e pela quantidade de entregas que avançaram, mas permaneceram em andamento, em review ou parcialmente integradas, como #61, #98, #107 e #112. Ainda assim, quando considerados itens com avanço parcial, PR aberto, PR fechado ou implementação em validação, a sprint teve **91,7% de avanço operacional**. O principal ponto de atenção é diferenciar claramente “trabalho iniciado/avançado” de “item concluído e integrado”.

**Fatores que influenciaram o resultado:** (1) A complexidade técnica da importação foi maior que a das entregas de fundação da Sprint 2, pois envolveu múltiplos formatos de arquivo, persistência, autenticação, tratamento de erro, parsers e integração com frontend. (2) A Sprint 3 acumulou itens funcionais, testes e documentação, aumentando o volume de trabalho em paralelo. (3) Parte importante do esforço ficou em review, draft ou validação no fechamento da sprint, especialmente nas issues #61, #107 e #112. (4) A ausência de story points padronizados ainda limita a precisão da métrica de velocidade, fazendo com que a análise use contagem de itens como aproximação.

### Referência de cobertura da Sprint 3

A cobertura de testes da Sprint 3 foi obtida a partir do relatório JaCoCo gerado no CI pelo step **Resumo de cobertura (JaCoCo)**.

```text
Cobertura de linhas (projeto backend): 75.5% (694/919 linhas cobertas)
Cobertura de linhas (pacote service): 68.6% (188/274)