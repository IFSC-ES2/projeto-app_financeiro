# ADR-0001 — Escolha da stack principal

## Status
Aceita

## Contexto
O projeto precisa de uma stack que permita desenvolvimento rápido,
boa organização do código e facilidade de colaboração entre os membros
da equipe ao longo do semestre.

## Decisão
A equipe decidiu utilizar Java com Spring Boot no backend e PostgreSQL
como banco de dados principal.

## Alternativas consideradas
- Node.js com Express
- Python com Django
- Java com Quarkus

## Consequências
### Positivas
- Boa organização para aplicações em camadas
- Ecossistema consolidado
- Facilidade para testes e manutenção

### Negativas / trade-offs
- Curva de aprendizagem maior para alguns membros
- Maior configuração inicial em comparação a opções mais simples

## Revisão futura
A decisão poderá ser revista caso surjam restrições técnicas relevantes
ou dificuldades excessivas de implementação.