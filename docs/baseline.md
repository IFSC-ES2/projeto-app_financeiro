# Baseline

## 1. Recorte do backlog

O backlog atual conta com 11 issues abertas. As issues fechadas (#1 ao #12) cobrem as entregas de kickoff, governança, EAP e ADRs.

As issues selecionadas para o planejamento inicial são as que compõem o fluxo mínimo para que o desenvolvimento do MVP possa começar:

- #16. Definir dependências do Spring Boot (Alta)
- #17. Configuração do Spring Boot e estrutura de pastas (Alta)
- #18. Configuração do React com TypeScript (Alta)
- #19. Modelagem do banco de dados (ER) (Alta)
- #20. Criação de diagrama UML (Alta)
- #21. Configuração do banco de dados (Alta)
- #22. Configuração de CORS entre frontend e backend (Alta)
- #25. Cadastro e autenticação de usuário (Alta)
- #26. Tela de login (Alta)
- #27. Tela de cadastro (Alta)
- #13. Criação da Home com um Relatório resumido das finanças do usuário (Baixa)

 **Critério de priorização:** as issues de alta prioridade formam a fundação técnica e o primeiro fluxo funcional do MVP. Sem autenticação funcionando, nenhuma outra funcionalidade pode ser desenvolvida com segurança. 

---

## 2. Técnica de estimativa

T-shirt size com mapeamento para horas.

A equipe optou pelo T-shirt size por ser uma técnica simples, rápida e adequada para um backlog ainda sem histórico de velocidade. O mapeamento adotado foi:

- P: até 2h
- M: 3h a 5h
- G: 6h a 9h
- GG: 10h ou mais

A unidade adotada foi de horas de trabalho efetivo.

**Critérios usados para dimensionar os itens:**
- Familiaridade da equipe com a tecnologia envolvida
- Número de decisões técnicas ainda em aberto no item
- Dependência de outros itens para ser iniciado
- Complexidade de configuração e validação
- Os extratos (XML/CSV) não está neste recorte mas é o item de maior incerteza técnica do projeto, seu dimensionamento será feito na Sprint 2
- Disponibilidade parcial dos membros pode variar por demandas de outras disciplinas

---

## 3. Estimativas dos itens priorizados

- #16. Definir dependências do Spring Boot (P, 2h, responsável: **Victor Blum**)
- #17. Configuração do Spring Boot e estrutura de pastas (M, 4h, responsável: **Victor Blum**)
- #18. Configuração do React com TypeScript (M, 4h, responsável: **João Pedro**)
- #19. Modelagem do banco de dados (ER) (M, 5h, responsável: **Victor Blum**)
- #20. Criação de diagrama UML (M, 4h, responsável: **Victor Blum**)
- #21. Configuração do banco de dados (G, 8h, responsável: **João Pedro**)
- #22. Configuração de CORS entre frontend e backend (M, 3h, responsável: **João Pedro**)
- #25. Cadastro e autenticação de usuário (GG, 10h, responsável: Alexandre)
- #26. Tela de login (M, 4h, responsável: A definir)
- #27. Tela de cadastro (M, 4h, responsável: Alexandre)
- #13. Criação da Home com relatório resumido (G, 8h, responsável: A definir)
- Total estimado: 50h

---

## 4. Capacidade planejada da equipe

### Integrantes ativos

- Victor Gabriel Lacerda: Scrum Master (3h/semana, 6h por sprint)
- Lucas de Leon Rodrigues: Engenheiro de Qualidade (5h/semana, 10h por sprint)
- Victor Blum: Arquiteto de Software (3h/semana, 6h por sprint)
- João Pedro Callegaro: DevOps / Infra (5h/semana, 10h por sprint)
- Alexandre Vilela: DevOps / Infra (7h/semana, 14h por sprint)

### Capacidade total por sprint (5 membros confirmados)

- Sprint 1 (08/04 – 22/04/2026): 32h
- Sprint 2 (23/04 – 07/05/2026): 46h

Capacidade calculada com base nos 5 membros com disponibilidade confirmada. 

### Restrições 

- Todos os membros estão de dedicação parcial, conciliando outras disciplinas do semestre e trabalho
- Teoricamente, o semestre encerra em 1 de julho de 2026 restam aproximadamente 6 sprints de 2 semanas a partir desta data
- Provas e avaliações de outras disciplinas concentradas em determinadas semanas
- Possível retrabalho caso a modelagem do banco precise ser revisada após o início da implementação

---

## 5. Previsão do período (Sprint 1 e Sprint 2)

Com capacidade de 32h por sprint e total estimado de 50h para todas as issues abertas, a previsão é:

**Sprint 1 (22/04):** concluir #16, #17, #18, #19, #20 e #21, da fundação técnica completa (23h estimadas).

**Sprint 2 (07/05):** concluir #22, #25, #26 e #27, ambiente integrado e primeiro fluxo funcional de autenticação (19h estimadas).

Ao fim da Sprint 2, a equipe deve ter o ambiente configurado, frontend e backend comunicando, banco de dados rodando e o fluxo de cadastro e login funcionando. É pré-requisito para iniciar as funcionalidades de importação de extratos na Sprint 3. A issue #13 fica reservada para sprint posterior, quando houver dados reais disponíveis.

---