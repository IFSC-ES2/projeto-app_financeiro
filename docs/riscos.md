# Registro de Riscos

Este documento deve ser revisitado e atualizado ao final de cada sprint.    

## 1. Critério de classificação

### Probabilidade

| Nível | Descrição |
|-|-|
| Baixa | Improvável de ocorrer na situação atual da equipe |
| Média | Pode ocorrer dependendo das circunstâncias |
| Alta | Pode acontecer se não tiver contenção |

### Impacto

| Nível | Descrição |
|-|-|
| Baixo | Atraso ou retrabalho pequeno que pode ser resolvido na sprint |
| Médio | Compromete uma ou mais funcionalidades do MVP |
| Alto | Compromete o prazo final |

### Prioridade 

Baseado em probabilidade × impacto em níveis de prioridade 

| | Impacto Baixo | Impacto Médio | Impacto Alto |
|-|-|-|-|
| **Prob. Alta** | Média | Alta | Crítica |
| **Prob. Média** | Baixa | Média | Alta |
| **Prob. Baixa** | Baixa | Baixa | Média |

---

## 2. Matriz de riscos

| ID | Risco | Probabilidade | Impacto | Prioridade |
|-|-|-|-|-|
| R01 | Parser de extratos com formatos incompatíveis | Alta | Alto | **Crítica** |
| R02 | Atraso no setup do ambiente de desenvolvimento | Alta | Médio | **Alta** |
| R03 | Sobrecarga dos membros com outras disciplinas | Alta | Médio | **Alta** |
| R04 | Escopo crescendo além da capacidade da equipe | Média | Alto | **Alta** |
| R05 | Falha na integração entre frontend e backend | Média | Médio | **Média** |
| R06 | Saída ou inatividade de membro da equipe | Baixa | Alto | **Média** |
| R07 | Vulnerabilidade de segurança nos dados financeiros | Baixa | Alto | **Média** |

---

## 3. Detalhamento dos riscos

---

### R01 — Parser de extratos com formatos incompatíveis

- **Natureza:** Tecnologia
- **Descrição:** Cada banco exporta CSV e XML em formato proprietário sem padronização nacional. O parser pode funcionar para um banco e falhar completamente para outro.
- **Causa:** Ausência de padrão obrigatório de exportação de extratos no Brasil. Cada instituição define sua própria estrutura de arquivo.
- **Consequência:** O diferencial central do SmartBudget é eliminar o registro manual, pode deixar de funcionar para uma parcela significativa dos usuários. Retrabalho alto e prazo comprometido.
- **Probabilidade:** Alta
- **Impacto:** Alto
- **Prioridade:** Crítica
- **Estratégia de mitigação:** Iniciar o desenvolvimento do parser na Sprint 2, antes das demais funcionalidades de produto. Priorizar os formatos do Nubank (CSV) e Itaú (XML) por serem os bancos mais usados pelas personas. Modelar o parser com camada de adaptadores para facilitar adição de novos formatos sem reescrever o núcleo.
- **Plano de contingência:** Caso o parser não esteja estável até a Sprint 4, priorizar o cadastro manual de transações como fallback funcional para o MVP.
- **Responsável:** Victor Blum (Arquiteto de Software)

---

### R02 — Atraso no setup do ambiente de desenvolvimento

- **Natureza:** Tecnologia / Prazo
- **Descrição:** O ambiente local integrado (Spring Boot + React + MySQL) ainda não está configurado. Nenhuma issue de desenvolvimento pode avançar sem ele.
- **Causa:** A equipe está em fase de planejamento e documentação. A configuração do ambiente está prevista para a Sprint 1, mas é o gargalo imediato para o início do desenvolvimento real.
- **Consequência:** Cada semana de atraso no ambiente comprime o tempo disponível para implementação, impactando diretamente a viabilidade do MVP até 1 de julho.
- **Probabilidade:** Alta
- **Impacto:** Médio
- **Prioridade:** Alta
- **Estratégia de mitigação:** Tratar as issues #16, #17, #18 e #21 como prioridade máxima da Sprint 1. João Pedro e Alexandre ficam responsáveis por garantir que o ambiente esteja funcional e documentado até o fim da primeira sprint. Utilizar Docker Compose para padronizar o ambiente entre todos os membros.
- **Plano de contingência:** Caso surjam bloqueios técnicos, o Arquiteto assume a triagem e define um ambiente mínimo viável para desbloquear o restante da equipe.
- **Responsável:** João Pedro Callegaro (DevOps / Infra)

---

### R03 — Sobrecarga dos membros com outras disciplinas

- **Natureza:** Equipe
- **Descrição:** Todos os membros estão em regime de dedicação parcial, conciliando este projeto com outras disciplinas do semestre. Picos de avaliações em outras matérias podem reduzir a disponibilidade efetiva abaixo do declarado no baseline.
- **Causa:** Contexto acadêmico com múltiplas disciplinas simultâneas e capacidade declarada já baixa (3h a 5h semanais por membro).
- **Consequência:** Sprints não concluídas, acúmulo de dívida técnica e pressão crescente nas sprints finais próximas ao prazo de 1 de julho.
- **Probabilidade:** Alta
- **Impacto:** Médio
- **Prioridade:** Alta
- **Estratégia de mitigação:** Revisar a capacidade da equipe no início de cada sprint durante o planejamento. Priorizar sempre as issues de maior valor do MVP para garantir que, se o ritmo cair, o essencial já esteja feito. Manter o board atualizado para o Scrum Master identificar bloqueios cedo.
- **Plano de contingência:** Reduzir o escopo das sprints afetadas, preservando as funcionalidades essenciais e adiando as de menor prioridade.
- **Responsável:** Victor Gabriel Lacerda (Scrum Master)

---

### R04 — Escopo crescendo além da capacidade da equipe

- **Natureza:** Escopo
- **Descrição:** Novas funcionalidades podem ser sugeridas ao longo do semestre e adicionadas ao backlog sem avaliação criteriosa de impacto no prazo, inflando o escopo além do que a equipe consegue entregar até 1 de julho.
- **Causa:** Entusiasmo natural com o produto, feedback de entregas anteriores e pressão por nota podem levar a decisões de escopo sem análise de custo.
- **Consequência:** MVP incompleto ao final do semestre. Funcionalidades essenciais podem ser comprometidas em favor de funcionalidades secundárias adicionadas no caminho.
- **Probabilidade:** Média
- **Impacto:** Alto
- **Prioridade:** Alta
- **Estratégia de mitigação:** Qualquer nova funcionalidade deve passar pelo crivo do Scrum Master e do Arquiteto antes de entrar no backlog ativo. Utilizar os critérios de escopo definidos no inception.md como filtro. Manter o foco nas 5 funcionalidades essenciais definidas no MVP.
- **Plano de contingência:** Se o escopo já tiver crescido, realizar uma sessão de repriorização com a equipe para cortar funcionalidades de menor impacto antes que comprometam o prazo.
- **Responsável:** Victor Gabriel Lacerda (Scrum Master)

---

### R05 — Falha na integração entre frontend e backend

- **Natureza:** Tecnologia
- **Descrição:** A comunicação entre o frontend React e o backend Spring Boot pode apresentar problemas de CORS, contratos de API divergentes ou autenticação JWT mal implementada, gerando retrabalho de integração.
- **Causa:** Frontend e backend sendo desenvolvidos em paralelo por membros diferentes, sem contrato de API formalizado previamente.
- **Consequência:** Funcionalidades implementadas individualmente não funcionam quando integradas, gerando retrabalho e atraso.
- **Probabilidade:** Média
- **Impacto:** Médio
- **Prioridade:** Média
- **Estratégia de mitigação:** Definir o contrato da API (endpoints, formatos de requisição e resposta) antes de iniciar o desenvolvimento paralelo. A issue #22 (CORS) deve ser validada na Sprint 1. O Arquiteto revisa os contratos antes da implementação.
- **Plano de contingência:** Reservar ao menos 1 sprint para testes de integração antes da entrega final do MVP.
- **Responsável:** Alexandre Vilela (DevOps / Infra)

---

### R06 — Saída ou inatividade de membro da equipe

- **Natureza:** Equipe
- **Descrição:** Um ou mais membros podem se tornar inativos ao longo do semestre por motivos pessoais, acadêmicos ou profissionais, reduzindo a capacidade efetiva da equipe.
- **Causa:** Imprevistos pessoais ou sobrecarga acadêmica que tornem inviável a continuidade no projeto.
- **Consequência:** Redistribuição de tarefas, redução de capacidade e risco de funcionalidades do MVP não serem concluídas.
- **Probabilidade:** Baixa
- **Impacto:** Alto
- **Prioridade:** Média
- **Estratégia de mitigação:** Documentar bem o código e as decisões técnicas para facilitar a transferência de conhecimento. Evitar que funcionalidades críticas dependam de um único membro. O Scrum Master monitora o engajamento da equipe a cada sprint.
- **Plano de contingência:** Redistribuir as tarefas do membro inativo entre os demais, priorizando as funcionalidades essenciais do MVP e descartando as de menor prioridade.
- **Responsável:** Victor Gabriel Lacerda (Scrum Master)

---

### R07 — Vulnerabilidade de segurança nos dados financeiros

- **Natureza:** Qualidade / Processo
- **Descrição:** O sistema manipula dados financeiros sensíveis (extratos bancários, notas fiscais, histórico de gastos). Uma implementação insegura de autenticação, armazenamento ou transmissão de dados pode expor informações dos usuários.
- **Causa:** Pressão por prazo pode levar a atalhos na implementação de segurança, como senhas armazenadas sem hash, tokens JWT sem expiração ou dados trafegando sem HTTPS.
- **Consequência:** Exposição de dados financeiros dos usuários, comprometimento da confiança no produto e eventual penalização acadêmica por entrega com falhas críticas de segurança.
- **Probabilidade:** Baixa
- **Impacto:** Alto
- **Prioridade:** Média
- **Estratégia de mitigação:** Incluir requisitos mínimos de segurança no DoD: senhas com BCrypt, JWT com expiração, HTTPS no ambiente de staging, variáveis sensíveis em .env. O Engenheiro de Qualidade valida esses requisitos nos PRs de autenticação.
- **Plano de contingência:** Realizar revisão de segurança dedicada antes da entrega final, com foco nas rotas de autenticação e no armazenamento de dados do usuário.
- **Responsável:** Lucas de Leon Rodrigues (Engenheiro de Qualidade)


## 4. Riscos críticos no momento

Os riscos **R01** e **R02** são os mais críticos no estado atual do projeto:

- **R01** porque o parser de extratos é o diferencial central do produto e sua viabilidade técnica ainda é uma incógnita — nenhuma linha de código foi escrita para validá-la.
- **R02** porque sem ambiente local configurado nenhum desenvolvimento pode avançar, e o prazo já está em curso.

Ambos devem ser os primeiros a receber ação concreta na Sprint 1.

## 5. Relação entre Riscos e Atributos de Qualidade

Esta seção conecta cada risco com os atributos de qualidade que ele ameaça, e indica como a mitigação protege esses atributos.

### R01 — Parser de extratos com formatos incompatíveis

Atributos afetados: Desempenho, Confiabilidade

Um parser que falha para certos bancos gera dados incompletos ou incorretos, comprometendo a confiabilidade. Processar formatos variados sem uma estrutura adequada também tende a ser mais lento, afetando o desempenho.

A camada de adaptadores nos permite isolar os formatos entre si, protegendo a estabilidade e facilitando otimizações independentes.

---

### R02 — Atraso no setup do ambiente de desenvolvimento

Atributos afetados: Manutenibilidade

Sem um ambiente padronizado, cada membro trabalha em condições diferentes, o que dificulta rodar testes e identificar problemas. Isso compromete diretamente a manutenibilidade.

O Docker Compose resolve isso: todos passam a trabalhar no mesmo ambiente, o que torna os testes mais confiáveis e o desenvolvimento mais consistente.

---

### R03 — Sobrecarga dos membros com outras disciplinas

Atributos afetados: Manutenibilidade, Confiabilidade

Com menos tempo disponível, testes são deixados de lado e o código é entregue com menos cuidado, reduzindo a cobertura e aumentando a chance de falhas não tratadas.

Revisando a capacidade no início de cada sprint e mantendo o foco nas funcionalidades mais críticas, conseguimos preservar a qualidade do que é entregue mesmo nos períodos de menor disponibilidade.

---

### R04 — Escopo crescendo além da capacidade da equipe

Atributos afetados: Manutenibilidade, Confiabilidade, Desempenho

Funcionalidades adicionadas sem planejamento tendem a ser implementadas com pressa, acumulando dívida técnica e aumentando a chance de regressões e problemas de desempenho.

Por isso filtramos novas funcionalidades antes de entrarem no backlog ativo, evitando que o código cresça além da nossa capacidade de manutenção e teste.

---

### R05 — Falha na integração entre frontend e backend

Atributos afetados: Confiabilidade

Problemas de integração como contratos de API divergentes ou autenticação mal implementada elevam a taxa de erros e reduzem a disponibilidade do sistema, afetando diretamente a confiabilidade.

Definir o contrato da API antes do desenvolvimento paralelo alinha as expectativas das duas camadas desde o início, e é o que nos protege dessas falhas de comunicação.

---

### R06 — Saída ou inatividade de membro da equipe

Atributos afetados: Manutenibilidade

A saída de um membro sem documentação adequada torna difícil para os demais assumirem suas tarefas, aumentando o tempo de correção de bugs e reduzindo a capacidade de manutenção.

Manter o código e as decisões técnicas bem documentados reduz essa dependência de conhecimento individual e garante que qualquer membro consiga assumir uma área sem partir do zero.

---

### R07 — Vulnerabilidade de segurança nos dados financeiros

Atributos afetados: Segurança, Confiabilidade

Falhas como senhas sem hash ou tokens sem expiração expõem dados financeiros dos usuários e comprometem a confiança no sistema, afetando tanto a segurança quanto a confiabilidade percebida.

Incluir esses requisitos no DoD e validar nos PRs faz com que a segurança seja tratada como parte do fluxo normal de entrega, e não como uma etapa deixada para o final.