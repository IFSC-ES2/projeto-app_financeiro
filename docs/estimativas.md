# Estimativas 

## 1. Técnica adotada

A equipe utilizou T-shirt size com mapeamento para horas de trabalho efetivo.

A técnica foi escolhida por ser simples, rápida e adequada para um backlog sem histórico de velocidade.

A equipe primeiro classificou cada item por tamanho relativo e depois converteu para horas usando uma tabela de referência.

| Tamanho | Horas estimadas |
|-|--|
| P | até 2h |
| M | 3h a 5h |
| G | 6h a 9h |
| GG | 10h ou mais |


## 2. Quem participou

A sessão de estimativas foi conduzida pelo Scrum Master e contou com a participação de todos os integrantes ativos da equipe:

- Victor Gabriel Lacerda: Scrum Master
- Victor Blum: Arquiteto de Software
- Lucas de Leon Rodrigues:Engenheiro de Qualidade
- João Pedro Callegaro: DevOps / Infra
- Alexandre Vilela: DevOps / Infra


## 3. Critérios de dimensionamento

Cada item foi dimensionado considerando:

- **Familiaridade com a tecnologia**: itens envolvendo tecnologias já conhecidas pela equipe receberam tamanhos menores
- **Decisões técnicas em aberto**: itens com muitas incertezas técnicas foram dimensionados para cima
- **Dependência de outros itens**: itens que só podem começar após outro ser concluído receberam estimativa conservadora
- **Complexidade de configuração e validação**: itens que exigem configuração de ambiente, integração entre camadas ou testes específicos foram tratados como maiores


## 4. Estimativas por item

| Issue | Descrição | Tamanho | Estimativa | Responsável |
|-------|-----------|---------|------------|-------------|
| #16 | Definir dependências do Spring Boot | P | 2h | Victor Blum |
| #17 | Configuração do Spring Boot e estrutura de pastas | P | 1h | Victor Blum |
| #18 | Configuração do React com TypeScript | P | 1h | João Pedro Callegaro |
| #19 | Modelagem do banco de dados (ER) | M | 5h | Victor Blum |
| #20 | Criação de diagrama UML | M | 4h | Victor Blum |
| #21 | Configuração do banco de dados | G | 8h | Alexandre Pereira Villela |
| #22 | Configuração de CORS entre frontend e backend | M | 3h | Victor Blum |
| #25 | Cadastro e autenticação de usuário | GG | 10h | Victor Blum |
| #26 | Tela de login | M | 4h | João Pedro Callegaro |
| #27 | Tela de cadastro | M | 4h | João Pedro Callegaro |
| #13 | Criação da Home com relatório resumido | G | 8h | João Pedro Callegaro |
| **Total** | | | **50h** | |


## 5. Hipóteses assumidas

As estimativas foram construídas sobre as seguintes premissas. Caso alguma mude, as estimativas devem ser revisadas:

- Todos os integrantes permanecerão ativos no período
- O escopo do MVP não sofrerá mudanças significativas até o próximo marco
- A stack definida nas ADRs (Spring Boot, React, TypeScript, MySQL) será mantida
- Cada membro conseguirá cumprir sua disponibilidade declarada na maioria das semanas
- As horas estimadas refletem trabalho efetivo. Estudo, deslocamento e cerimônias não estão incluídos
- O parser de extratos (XML/CSV) não está neste recorte, seu dimensionamento será feito na Sprint 2, após validação técnica inicial


## 6. Limitações e incertezas

- **Sem histórico de velocidade:** esta é a primeira sprint de desenvolvimento real da equipe. As estimativas são hipóteses iniciais e devem ser calibradas a partir da Sprint 2, quando houver dados reais de entrega.
- **Parser de extratos não estimado:** o item de maior incerteza técnica do projeto foi intencionalmente deixado fora deste recorte. Será dimensionado somente após a equipe ter contato com arquivos reais de extrato bancário.
- **Disponibilidade parcial variável:** a capacidade declarada pode não se confirmar em todas as semanas, especialmente em períodos com avaliações de outras disciplinas.
- **Integração frontend-backend:** o esforço real de integração entre as camadas tende a ser subestimado em estimativas iniciais. A issue #22 foi dimensionada de forma conservadora justamente por isso.