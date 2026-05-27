# ADR-0006 — Versionamento de Banco de Dados com Flyway

## Status
Aceito

## Contexto
Anteriormente, o projeto utilizava a estratégia do Hibernate para criar ou atualizar tabelas automaticamente no PostgreSQL (auto-ddl). No entanto, com a evolução da Sprint 3, essa abordagem começou a gerar inconsistências no schema de banco de dados entre os ambientes locais dos desenvolvedores.

## Decisão
A equipe decidiu adotar o **Flyway** integrado ao Spring Boot para realizar o versionamento e a evolução automatizada do esquema do banco de dados através de arquivos de *migration* explícitos.

## Alternativas consideradas
- Manter o Hibernate criando as tabelas automaticamente.
- Uso do Liquibase.

## Consequências
### Positivas
- Histórico confiável e rastreável de todas as alterações de banco diretamente no Git.
- Garantia de que todos os membros do grupo testam a aplicação com a mesma estrutura.

### Negativas / trade-offs
- Exigência de disciplina rigorosa na nomenclatura dos arquivos (`V1__...`).
- Arquivos de migração já executados não podem ser alterados; correções exigem novas migrações.

## Justificativa
O uso do Flyway foi escolhido em detrimento do auto-ddl do Hibernate porque o auto-ddl é voltado para ambientes de desenvolvimento iniciais e não oferece controlo seguro sobre mudanças de estrutura, podendo levar a perda de dados ou conflitos entre a equipa. O Flyway garante que todos tenham exatamente a mesma base de dados, passo a passo.