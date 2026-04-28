# ADR-0002 — Linguagem e Framework Principal

## Status
Aceito

## Contexto
O projeto precisa de uma linguagem principal definida
para uma boa organização do código e facilidade de colaboração entre os membros
da equipe ao longo do semestre.

## Decisão
A equipe decidiu utilizar Java com Spring Boot no backend e postgres como banco de dados principal.
A equipe decidiu utilizar React e Typescript no frontend.

## Alternativas consideradas
- Node.js com Express
- Vue com Typescript
- Python com MongoDB

## Consequências
### Positivas
- Boa organização para aplicações em camadas
- Ecossistema consolidado
- Facilidade para testes e manutenção
- Consolidação do conhecimento adquirido no curso
- Tipagem fortemente definida em ambas as partes do projeto

### Negativas / trade-offs
- Curva de aprendizagem maior para alguns membros
- Maior configuração inicial em comparação a opções mais simples
- Sintaxe mais robusta

## Revisão futura
A decisão poderá ser revista caso surjam restrições técnicas relevantes
ou dificuldades excessivas de implementação.
Caso seja identificado uma linguagem que se encaixe melhor no modelo do produto.