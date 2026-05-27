# ADR-0005 — Padrões de Projeto (Design Patterns) na Camada de Negócio

## Status
Aceito

## Contexto
Com o avanço do desenvolvimento na Sprint 2, surgiu a necessidade de padronizar a forma como os dados trafegam entre o frontend (React) e o backend (Spring Boot), além de isolar as regras de negócio das operações diretas de banco de dados para manter o código limpo e testável.

## Decisão
A equipe decidiu adotar o padrão **Camada de Serviço (Service Layer)** combinado com o **Repository Pattern** e o uso de **DTOs (Data Transfer Objects)** para a transferência de dados.

## Alternativas consideradas
- Manipulação de entidades diretamente nos Controllers.
- Uso de queries SQL brutas direto nas rotas sem camada de abstração.

## Consequências
### Positivas
- Isolamento total da lógica de negócio nos Services.
- Segurança aprimorada, expondo apenas os dados necessários através de DTOs, evitando expor entidades do banco de dados diretamente para o cliente React.
- Facilidade para criação de testes unitários mockando os Repositories.

### Negativas / trade-offs
- Aumento da quantidade de arquivos e classes no projeto (necessidade de criar classes DTO e mapeadores).
- Pequena verbosidade inicial para converter Entidade em DTO e vice-versa.

## Revisão futura
A decisão poderá ser revista caso a complexidade de mapeamento de objetos se torne um gargalo de performance ou caso surjam padrões mais eficientes com a evolução do ecossistema.