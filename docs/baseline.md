# Baseline

## 1. Recorte do backlog

Visualização geral das implementações planejadas para o projeto ao decorrer do semestre.

1. Criação de perfil pessoal, com autenticação;
2. Adicionar gastos manualmente;
3. Leitura de extratos bancários e notas fiscais (xml, csv, txt);
4. Categorizar os gastos em subdivisões (lazer, alimentação etc);
5. Categorizar os gastos a partir de como o dinheiro foi utilizado (cartão, pix, dinheiro, boleto);
6. Categorizar os gastos a partir do cartão e banco utilizado;
7. Visualização de gastos do mês em texto, gráficos e dashboards;
8. Visualição do extrato dos próximos meses em texto, gráficos e dashboards;

Por ordem do que queremos implementar. Esse documento poderá ser alterado após decisão em conjunto dos membros para definir complexidade das funcionalidades em P, M, G ou GG,

---

## 2. Técnica de estimativa

T-shirt size com mapeamento para horas.

A equipe optou pelo T-shirt size por ser uma técnica simples, rápida e adequada para um backlog ainda sem histórico de velocidade. O mapeamento adotado foi:

- P
- M
- G
- GG

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

- #16. Definir dependências do Spring Boot (P, responsável: **Victor Blum**)
- #17. Configuração do Spring Boot e estrutura de pastas (M, responsável: **Victor Blum**)
- #18. Configuração do React com TypeScript (M, responsável: **João Pedro**)
- #19. Modelagem do banco de dados (ER) (M, responsável: **Victor Blum**)
- #20. Criação de diagrama UML (M, responsável: **Victor Blum**)
- #21. Configuração do banco de dados (G, responsável: **João Pedro**)
- #22. Configuração de CORS entre frontend e backend (M, responsável: **João Pedro**)
- #25. Cadastro e autenticação de usuário (GG, responsável: Alexandre)
- #26. Tela de login (M, responsável: A definir)
- #27. Tela de cadastro (M, responsável: Alexandre)
- #13. Criação da Home com relatório resumido (G, responsável: A definir)

---

## 4. Capacidade planejada da equipe

### Integrantes ativos

- Victor Gabriel Lacerda: Scrum Master (3h/semana, 6h por sprint)
- Lucas de Leon Rodrigues: Engenheiro de Qualidade (5h/semana, 10h por sprint)
- Victor Blum: Arquiteto de Software (3h/semana, 6h por sprint)
- João Pedro Callegaro: DevOps / Infra (5h/semana, 10h por sprint)
- Alexandre Vilela: DevOps / Infra (7h/semana, 14h por sprint)


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