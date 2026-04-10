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



