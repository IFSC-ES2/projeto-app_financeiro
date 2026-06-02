# ADR-0004 — Estratégia de Arquitetura, Camadas e Padrões de Projeto

## Status
Revisado — Decisão expandida e aceita (Atualizado na Sprint 3)

## Contexto
O projeto precisa de uma estratégia arquitetural para organizar o desenvolvimento, visando agilidade e padronização. Além disso, com a introdução de regras de negócio mais complexas (como o processamento de múltiplos formatos de extratos bancários e notas fiscais), a documentação anterior focava apenas em frameworks e camadas, carecendo de uma definição clara sobre Padrões de Projeto (Design Patterns) aplicados ao domínio.

## Decisão
A equipe decidiu utilizar uma combinação de Arquitetura em Camadas e Padrões de Projeto Comportamentais:

1. **Arquitetura em Camadas (MVC):** Utilizada como base estrutural (Model-View-Controller adaptado ao ecossistema Spring Boot), separando a aplicação em `Controllers` (rotas e HTTP), `Services` (regras de negócio), `Repositories` (acesso a dados) e `Models` (entidades).

2. **Padrão Strategy (Design Pattern GoF):** Aplicado no módulo de importação de arquivos para lidar com múltiplos formatos (CSV, TXT, XML, NF-e) de forma extensível e limpa, aplicando o princípio Open/Closed (SOLID).

## Alternativas consideradas
- **Microserviços:** Descartado pela alta complexidade inicial para o escopo atual (MVP).
- **Orientada a Eventos (Event-Driven):** Descartado pois o fluxo atual de dados é puramente síncrono.
- **Processamento Procedural (If/Else Switch):** Em vez do padrão Strategy, poderíamos ter usado múltiplos `if/else` no Service para ler os arquivos. Descartado pelo alto acoplamento e dificuldade de manutenção futura.

## Consequências

### Positivas
- Padrão do framework Spring respeitado.
- Separação de responsabilidades clara entre as camadas.
- **Alta Extensibilidade (Strategy):** O `ImportacaoService` não precisa ser alterado para suportar um novo formato de arquivo. Basta criar uma nova classe que implemente a interface `ParserExtrato`.

### Negativas / trade-offs
- Curva de aprendizagem maior para alguns membros, tanto no framework (Spring) quanto no entendimento de injeção de dependências para padrões de projeto (Strategy).
- Acoplamento estrutural natural com o framework Spring Boot.

## Revisão futura
A decisão poderá ser revista caso ocorra o crescimento da complexidade de negócio (exigindo mensageria/eventos), a necessidade de maior escalabilidade (migração para microserviços) ou a quebra do monólito.