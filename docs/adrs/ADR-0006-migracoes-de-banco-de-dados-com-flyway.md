# ADR-0006 — Gerenciamento de Migrações de Banco de Dados com Flyway

## Status
Aceito

## Contexto
Com o avanço do projeto na Sprint 3 e a criação de novas tabelas (como a tabela de usuários, extratos e parcelas), tornou-se difícil sincronizar manualmente a estrutura do banco de dados Postgres entre todos os desenvolvedores. Scripts SQL manuais geravam conflitos e inconsistências nos ambientes locais de cada membro.

## Decisão
A equipe decidiu adotar o **Flyway** integrado ao Spring Boot para realizar o controle de versão e a evolução automatizada do esquema do banco de dados através de arquivos de *migration*.

## Alternativas consideradas
- Execução manual de scripts de criação (`schema.sql`).
- Uso do Liquibase.

## Consequências
### Positivas
- Automação completa: ao rodar o projeto localmente, o banco de dados é atualizado automaticamente para a última versão estável.
- Histórico confiável e rastreável de todas as alterações de banco diretamente no Git.
- Garantia de que todos os membros do grupo estão testando a aplicação exatamente com a mesma estrutura de tabelas.

### Negativas / trade-offs
- Exigência de disciplina rigorosa na nomenclatura dos arquivos (`V1__...`, `V2__...`).
- Impossibilidade de alterar um arquivo de migração que já foi executado; qualquer correção exige a criação de um novo arquivo de migração (ex: `V3__correcao...`).

## Revisão futura
A decisão é definitiva para o ciclo de vida atual do projeto, a menos que ocorram problemas críticos de compatibilidade com versões futuras do Spring Boot ou do PostgreSQL.