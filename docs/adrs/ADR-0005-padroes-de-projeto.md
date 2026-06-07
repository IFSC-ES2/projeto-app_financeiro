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
- **Strategy Pattern:** Utilizado especificamente para orquestrar os parsers de importação de extratos. A interface `ParserExtrato` define o contrato comum entre o `ImportacaoService` e os parsers concretos, padronizando os métodos `aceita()` e `parsear()`, o retorno por meio de `ResultadoParser`, o tratamento de sucesso parcial e a separação de responsabilidades entre parsing e persistência.

## Alternativas consideradas
- Utilizar apenas a estrutura MVC básica sem padrões adicionais para regras de negócio (descartado por gerar alto acoplamento).

## Consequências
### Positivas
- Código altamente testável e desacoplado.
- Facilidade de expansão: o Strategy Pattern permite adicionar novos formatos de importação no futuro sem alterar o código principal.
- Contrato explícito para importação: todos os parsers seguem a mesma regra para aceitar arquivos, retornar transações válidas, contabilizar registros inválidos e delegar persistência/categorização ao `ImportacaoService`.

### Negativas / trade-offs
- Aumento da complexidade estrutural e maior quantidade de classes/interfaces no projeto.

## Justificativa
A adoção destes padrões foi considerada superior a uma implementação direta nos controllers porque garante a separação de responsabilidades, facilita a criação de testes unitários isolados e permite que a aplicação escale de forma mais sustentável. O Strategy Pattern, em particular, evita o uso de múltiplos "if/else" para verificar o tipo de ficheiro, tornando o código mais limpo e preparado para novos formatos no futuro.
No módulo de importação, o contrato comum dos parsers evita que cada implementação adote regras próprias de retorno ou tratamento de erro. Com isso, o `ImportacaoService` consegue tratar CSV, TXT, XML e NF-e de maneira uniforme, mantendo o fluxo de importação previsível e facilitando a inclusão futura de novos formatos.
