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

## Valores observados — Sprint 2


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

**O que foi planejado:** O [baseline](baseline.md) previa para a Sprint 2 o fechamento de **#22** (CORS / integração), **#25** (cadastro e autenticação no backend), **#26** e **#27** (telas de login e cadastro), totalizando **19 h** estimadas em camisetas, com o ambiente integrado e o primeiro fluxo de autenticação funcional como resultado esperado. A mitigação do risco **R01** (parser) mencionava iniciar trabalho na Sprint 2, em paralelo à priorização de formatos — isso **não** estava listado como entrega obrigatória das quatro issues acima.

**O que foi executado:** As quatro issues planejadas foram **concluídas** (fechamento em **06/05** e **11/05/2026**). Houve entrega **extra** de infraestrutura de qualidade com a issue **#74** (CI no GitHub Actions, **12/05**), alinhada ao papel de Engenheiro de Qualidade. Testes automatizados do backend foram ampliados em torno de registro/login (`UsuarioService`); o front evoluiu com telas e validação de CPF, sem ainda incorporar testes no pipeline.

**Fatores que influenciaram o resultado:** (1) **Dedicação parcial** e outras disciplinas (risco **R03**) continuam a moldar o ritmo, embora a taxa de conclusão da sprint tenha sido total para o **escopo planejado**. (2) **Rotação do Scrum Master** para Lucas de Leon, registrada ao final da Sprint 1 em [sprint1.md](sprint1.md), reorganizou a governança das cerimônias sem bloquear as entregas técnicas. (3) **Dependência de PostgreSQL** para o *context load* do Spring elevou o alinhamento com o CI (serviço `postgres` no workflow), reduzindo divergência ambiente local vs. pipeline. (4) **Escopo do MVP** permanece concentrado na fundação: alta conclusão da sprint **não** implica alto percentual do MVP, pois as próximas funcionalidades essenciais (importação, categorias, dashboards, projeção) ainda estão no backlog. (5) **Cobertura de testes** reflete prioridade na camada de serviço de autenticação; DTOs e demais pacotes puxam a média global para baixo — decisão consciente ou dívida técnica a tratar nas próximas sprints se a meta for o agregado do backend.

**Referência de cobertura (reprodutibilidade):** executar `./gradlew test` em `app-financeiro-back-end/` com PostgreSQL acessível em `localhost:5432` (mesmos parâmetros do [README](../README.md) / CI); relatório HTML/XML em `app-financeiro-back-end/build/reports/jacoco/test/`.

### Dívidas técnicas registradas na Sprint 2

- **Cobertura de testes no frontend = 0% (dívida técnica).** O projeto front-end (`app-financeiro-front-end/`) não possui framework de testes configurado no `package.json` nem step de execução real no CI; o passo `npm test --if-present` apenas é ignorado quando o script não existe. Isso significa que a métrica de cobertura agregada do produto **subestima o risco real**, pois mede apenas o backend. **Ação planejada para a Sprint 3:** abrir issue dedicada para introduzir Vitest + React Testing Library, configurar o script `test` no `package.json`, adicionar geração de cobertura (`--coverage`) e incorporar o resumo no workflow de CI ao lado do JaCoCo. Responsável: Lucas de Leon (Engenheiro de Qualidade).

