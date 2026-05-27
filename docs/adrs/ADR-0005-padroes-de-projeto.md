# ADR-0005 — Padrões de Projeto (Design Patterns)

## Status
Aceito

## Contexto
Com o avanço do desenvolvimento na Sprint 2 e novas integrações na Sprint 3, surgiu a necessidade de definir e documentar os padrões de projeto adotados na arquitetura do sistema para padronizar a comunicação entre camadas e a resolução de lógicas específicas (como leitura de diferentes tipos de arquivos).

## Decisão
A equipe decidiu formalizar o uso dos seguintes padrões de projeto no backend:
- **Service Layer e Repository Pattern:** Para isolar a lógica de negócio do acesso a dados.
- **DTO (Data Transfer Object):** Para trafegar dados de forma segura entre a API e o frontend.
- **Dependency Injection (Injeção de Dependência):** Gerenciada nativamente pelo Spring Boot.
- **Strategy Pattern:** Utilizado especificamente para orquestrar os parsers de importação de extratos, permitindo trocar o algoritmo de leitura dinamicamente de acordo com o tipo de arquivo.

## Alternativas consideradas
- Utilizar apenas a estrutura MVC básica sem padrões adicionais para regras de negócio (descartado por gerar alto acoplamento).

## Consequências
### Positivas
- Código altamente testável e desacoplado.
- Facilidade de expansão: o Strategy Pattern permite adicionar novos formatos de importação no futuro sem alterar o código principal.

### Negativas / trade-offs
- Aumento da complexidade estrutural e maior quantidade de classes/interfaces no projeto.