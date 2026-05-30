## 10. Padrões de Projeto (Design Patterns) Aplicados

Para resolver problemas de acoplamento e regras de negócio complexas, o backend do SmartBudget utiliza padrões de projeto clássicos (GoF). O destaque principal no MVP é o uso do padrão **Strategy**.

### Padrão Strategy: Módulo de Importação
A funcionalidade de importação permite que o usuário envie extratos de diferentes fontes e formatos (CSV, TXT, XML, NF-e). Em vez de centralizar a lógica de interpretação de todos os arquivos em um único serviço monolítico e cheio de condicionais (`if/else`), foi aplicado o padrão **Strategy**.

**Como funciona no código:**
1. **A Interface (A Estratégia):** Foi criada a interface `ParserExtrato` contendo os contratos `aceita(MultipartFile arquivo)` e `parsear(MultipartFile arquivo, Conta conta)`.
2. **As Implementações (Estratégias Concretas):** Classes como `ParserCSV`, `ParserXML` e `ParserNFe` implementam a interface, contendo a lógica específica para traduzir bytes daquele formato específico em objetos `Transacao`.
3. **O Contexto:** A classe `ImportacaoService` recebe via injeção de dependência do Spring uma lista de todas as estratégias disponíveis (`List<ParserExtrato> parsers`).

**Fluxo de Execução:**
Quando um arquivo chega via requisição, o `ImportacaoService` itera sobre as estratégias chamando o método `aceita()`. O primeiro parser que retornar `true` é escolhido dinamicamente e acionado via método `parsear()`.

**Benefício (Open/Closed Principle):**
Se o SmartBudget precisar suportar arquivos PDF ou OFX no futuro, a equipe precisará apenas criar uma nova classe `ParserOFX` que implemente `ParserExtrato`. O `ImportacaoService` não sofrerá nenhuma alteração em seu código, garantindo segurança contra regressões.