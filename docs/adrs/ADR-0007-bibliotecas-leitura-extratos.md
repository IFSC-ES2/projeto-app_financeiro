# ADR-0007 — Bibliotecas de Leitura de Extratos (CSV e XML)

## Status
Aceito

## Contexto
Na Sprint 3, o projeto passou a contemplar a funcionalidade de importação de extratos bancários. Foi necessário definir as ferramentas adequadas no backend (Java/Spring Boot) para realizar o *parsing* (leitura e extração de dados) de arquivos contendo o histórico financeiro dos usuários.

## Decisão
A equipe decidiu adotar as seguintes bibliotecas para apoiar a importação de extratos:
- **OpenCSV:** Para leitura e processamento de arquivos `.csv`.
- **Jackson XML:** Para leitura e processamento de arquivos `.xml`.

## Alternativas consideradas
- Criar rotinas nativas usando `java.io` e manipulação manual de strings (descartado pelo alto risco de erros e complexidade).

## Consequências
### Positivas
- Processamento robusto, rápido e maduro de formatos padrão do mercado.
- Integração facilitada com o padrão Strategy adotado pela equipe.

### Negativas / trade-offs
- Inclusão de novas dependências externas no `pom.xml` (ou `build.gradle`), aumentando ligeiramente o tamanho do build da aplicação.