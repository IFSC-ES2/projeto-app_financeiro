# ADR-0003 — Abordagem de persistência de dados

## Status
Em espera

## Contexto
O projeto precisa de uma maneira de persistir os dados que permita a equipe guardar os dados dos usuários e de definições do funcionamento do projeto.

## Decisão
A equipe decidiu utilizar o modelo relacional SQL.

## Alternativas consideradas
- Documento
- Chave-valor

## Consequências
### Positivas
- Dados organizados em tabelas com esquema fixo e relações entre entidades
- Conhecimento já adquirido da equipe em linguagens SQL
- Dados têm estrutura bem definida.

### Negativas / trade-offs
- Rigidez do modelo relacional, qualquer alteração na estrutura é muito trabalhos
- Curva de aprendizado na modelagem

## Revisão futura
A decisão poderá ser revista caso surjam restrições técnicas relevantes ou dificuldades excessivas de implementação.