# Sprint 3

A Sprint 3 representou um avanço importante na consolidação técnica do MVP. O projeto deixou de estar concentrado apenas na fundação de autenticação e registro manual e passou a atacar funcionalidades centrais do que queremos propor para o SmartBudget, como importação de arquivos financeiros e categorização de transações.

Das 12 issues/PRs previstas, 8 foram feitas complemente feitas e mais 3 estão, atualmente, mapeadas como parcialmente implementadas. Totalizando 11/12 completas ou parcialmente completas.

## Decisões durante a Sprint 3

A equipe adotou migrations versionadas para evitar que o Hibernate altere automaticamente a estrutura do banco. Com isso, o banco passa a ter histórico rastreável e reproduzível.

- `app-financeiro-back-end/src/main/resources/db/migration/V1__create_tables.sql`;
- `app-financeiro-back-end/src/main/resources/db/migration/V2__seed_categorias.sql`;
- `app-financeiro-back-end/src/main/resources/db/migration/V3__seed_usuarios.sql`

Com Flyway para evitar inconsistências entre ambientes locais e manter o schema versionado no Git.

Também adotamos OpenCSV para arquivos CSV e Jackson XML para arquivos XML. Arquivos TXT e NF-e são processados com lógica própria da aplicação. Essa decisão reduz o risco técnico de implementar parsers frágeis do zero.

## Incremento funcional da Sprint 3

A importação de extratos e NF-e teve avanço técnico relevante no backend. Essa funcionalidade está evidenciada pela presença do `ImportacaoController`, do `ImportacaoService` e dos parsers para arquivos CSV, TXT, XML e NF-e.

A categorização de transações foi implementada no backend. O recurso está representado pelo `CategoriaController`, pelo `CategoriaService`, pelos DTOs relacionados à categorização e pelo endpoint de atualização de categoria nas transações.

A criação de conta bancária pelo frontend também foi implementada. A funcionalidade está presente na tela `src/pages/NovaConta.tsx`, com integração ao backend.

O registro manual de transações foi mantido e integrado ao fluxo autenticado da aplicação. Essa parte envolve o `TransacaoController`, o `TransacaoService` e a tela `src/pages/NovaTransacao.tsx`.

A autenticação com JWT foi mantida e utilizada pelas rotas protegidas do sistema. A estrutura está evidenciada pelo `JwtAuthFilter`, pelo `JwtUtil`, pelo `SecurityConfig` e pelo `ContextoAutenticacao.tsx` no frontend.

As migrations de banco com Flyway foram implementadas para organizar a criação e preparação inicial da base de dados. As evidências principais são os arquivos `V1__create_tables.sql`, `V2__seed_categorias.sql` e `V3__seed_usuarios.sql`.

Os testes de parsers e importação foram implementados no backend. Essa base de testes inclui `ParserCSVTest`, `ParserTXTTest`, `ParserXMLTest`, `ParserNFETest`, `ImportacaoServiceTest` e `ImportacaoControllerTest`.

No frontend, os testes foram configurados e iniciados. Essa estrutura está evidenciada pelos arquivos `vitest.config.ts`, `setupTests.ts`, `ambiente.test.ts` e `Login.test.tsx`.

## Registro de contribuição individual

### Lucas de Leon Rodrigues Coelho

#### Abertura de PRs

- `test/#97 importacao`
- `feat: Tela inicial (preliminar) (CLOSES #98)`
- `feat/#98 esqueleto tela` (0.3.1)
- `test/120 conta categorias transacoes` (0.3.1)

#### Observações

- O PR que fechava a issue `#98` foi fechado para evitar retrabalho, pois a branch principal avançou em pontos que impactavam diretamente esse PR.
- A resolução da issue `#98` ficou parcial para a versão `0.3.0` e planejada para ser entregue em 0.3.1.
- Contribuiu diretamente com commits no PR `test/#121-isolar-ambiente` aberto pelo Victor Gabriel.
- Incrementou o dashboard inicial para melhora de fluxo.

#### Contribuições em reviews

- Contribuiu em revisões de frontend e backend.

---

### Victor Gabriel Lacerda

#### Abertura de PRs

- `feat/#102 tela para associar conta bancaria ao registrar usuario`
- `docs/#94-sprint3` 
- `test/#121-isolar-ambiente` (0.3.1)
- `chore/#123-docker-compose-postgres` (0.3.1)

#### Contribuições em reviews

- Atuou diretamente com reviews para evitar retrabalho, especialmente no PR relacionado à issue `#60`, mantendo comunicação direta com Victor Blum para melhoria da implementação.
- Participou ativamente com reviews na implementação relacionada à issue `#64`.
- Validou que o PR `test: Implementa testes unitários para parsers de extratos bancários (#97)` apresentava complicações que impediam o avanço da solução, contribuindo para que a issue fosse remanejada.
- Contribuiu na revisão de PRs relacionados ao backend e à documentação.
- Contribuiu junto com Victor Blum para a elaboração e fechamento do documento `sprint3.md`.

---

### Victor Blum

#### Abertura de PRs

- `docs/#91 Atualizando e comparando as metricas da sprint 3`
- `docs/#89 arquitetura c4`
- `feat/#71 migration de usuarios`
- `feat/#64 categorizacao transacoes`
- `feat/#60 processamento extrato endpoint upload`
- `feat/#122 corrigir ci` (0.3.1)

#### Contribuições em reviews

- Contribuiu diretamente no PR relacionado à issue `#97`, aberto por Lucas de Leon, não apenas com reviews, mas também com commits junto ao proprietário.
- Revisou o backend para que os testes desse PR funcionassem corretamente, considerando sua participação ativa nessa parte do projeto.
- Contribuiu em PRs de backend, testes, documentação e frontend, participando do fluxo geral do projeto.

---

### Alexandre Vilella

#### Abertura de PRs

- `docs: documenta execução local dos testes e gate de CI (closes #92)`
- `feat(#61): Tela de importação de extratos e NF-e` (0.3.1)

#### Contribuições em reviews

- Contribuiu diretamente no PR que fecha a issue `#64`, realizando um commit importante para corrigir a execução dos testes do backend no GitHub.
- Contribuiu em revisões durante a Sprint 3.

---

### João Pedro Callegaro Guimarães

#### Abertura de PRs

- `test(login): implementa testes automatizados de comportamento da tela de login`
- `chore(test): configura ambiente do vitest e testing library no frontend`
- `docs(adr): revisa adrs antigos e adiciona novas decisoes da sprint 3 (#90)`
- `test: Adiciona testes unitários e validação de segurança XXE para ParserXML (#97)` - Fechada
- `test: Implementa testes unitários para parsers de extratos bancários (#97)` - Fechada
- `test(#118): Criar testes unitários para a tela de Cadastro de Usuário` (0.3.1)

#### Observações

- Os PRs relacionados à issue `#97` foram fechados.
- A issue `#97` foi posteriormente transferida para Lucas de Leon Rodrigues Coelho.

#### Contribuições em reviews

- Contribuiu em reviews de documentação.

---

## PRs com maior volume de reviews

### `feat/#60 processamento extrato endpoint upload`

- Recebeu 38 comentários.
- O PR avançou bem, e os comentários foram voltados principalmente para melhorias.
- No final, o PR foi fechado com algumas alterações planejadas para evitar retrabalho futuro.
- Victor Blum e Victor Gabriel trabalharam ativamente com respostas constantes e reviews válidas.

### `test: Implementa testes unitários para parsers de extratos bancários (#97)`

- Recebeu 33 comentários.
- O PR foi fechado devido a muitos erros que atrasaram o avanço da issue.
- A issue `#97` foi remanejada para outra pessoa e posteriormente fechada por Lucas de Leon Rodrigues Coelho.

### `feat/#64 categorizacao transacoes`

- Recebeu 33 comentários.
- Victor Blum e Victor Gabriel trabalharam ativamente para melhorar essa implementação.
- As reviews foram válidas e houve comunicação clara durante o processo.

## Incremento adicional da versão 0.3.1

Após a publicação da versão `0.3.0`, a equipe realizou um incremento complementar para consolidar entregas que ficaram parciais ou que precisavam de ajustes finais. Esse incremento resultou na versão `0.3.1`.

A versão `0.3.1` não substitui o escopo principal da Sprint 3, mas registra avanços adicionais feitos sobre a base da `0.3.0`, principalmente em execução local, navegação do frontend, listagem de transações, importação de extratos e testes.

## Incremento adicional da versão 0.3.1

Após a publicação da versão `0.3.0`, a equipe realizou um incremento complementar para consolidar entregas que ficaram parciais ou que precisavam de ajustes finais. Esse incremento resultou na versão `0.3.1`.

A versão `0.3.1` não substitui o escopo principal da Sprint 3, mas registra avanços adicionais feitos sobre a base da `0.3.0`, principalmente em execução local, navegação do frontend, listagem de transações, importação de extratos e testes.

### Principais avanços da 0.3.1

Foi adicionado um `docker-compose.yml` na raiz do projeto para padronizar a execução local do PostgreSQL. Com isso, o banco pode ser iniciado com `docker compose up -d`, reduzindo diferenças de configuração entre os ambientes dos integrantes.

A documentação de execução local foi atualizada no `README.md` e em `docs/como-rodar.md`, incluindo instruções para subir, parar e recriar o banco local com Docker Compose.

O frontend passou a contar com uma estrutura mais completa de rotas públicas e privadas. Também foi adicionado um layout privado com navegação interna, permitindo organizar melhor as telas autenticadas da aplicação.

A tela de listagem de transações foi adicionada ao frontend. Nessa versão, a listagem exibe transações do usuário autenticado e permite filtros no frontend por período, tipo, conta e categoria.

O backend passou a disponibilizar a listagem de transações do usuário autenticado, permitindo que a tela de transações consuma dados reais da aplicação.

A importação de extratos avançou também no frontend, com uma tela para selecionar conta, enviar arquivo e acompanhar o status da importação.

O fluxo de autenticação no frontend foi reorganizado com melhorias no armazenamento da sessão, uso do token JWT e interceptors da API.

Também foram adicionados testes automatizados no frontend, com destaque para a tela de cadastro, cobrindo renderização, validações, cadastro com sucesso e tratamento de erro da API.

No backend, a cobertura de testes foi ampliada com novos testes relacionados a contas, categorias e listagem de transações.

### Situação da 0.3.1

Com esse incremento, a versão `0.3.1` consolida uma base mais estável para o MVP, com melhor experiência de execução local, navegação autenticada no frontend, listagem de transações, importação de extratos e maior cobertura de testes.

### Pontos adicionais

- Arquitetura foi criado em [docs/arquitetura.md](../arquitetura.md);
- ADRs podem ser localizadas em [docs/adrs](../adrs);
- Métricas atualizadas em [docs/metricas.md](../metricas.md);
- Workflow valida pontos mínimos:
    - instala dependências do frontend com `npm ci`;
    - configura Java e Gradle para o backend;
    - executa build do backend com `gradle build -x test`;
    - executa testes do backend com `gradle test`;
    - gera resumo de cobertura via JaCoCo;
    - executa build do frontend com `npm run build`;
    - executa testes do frontend com `npm test --if-present`;
    - valida sintaxe YAML;
- Testes identificados no backend:
    - `AppFinanceiroBackEndApplicationTests`;
    - `ImportacaoControllerTest`;
    - `RegistrarManualIntegrationTests`;
    - `ParserCSVTest`;
    - `ParserNFETest`;
    - `ParserTXTTest`;
    - `ParserXMLTest`;
    - `ImportacaoServiceTest`;
    - `RegistrarLoginTests`;
    - `RegistrarManualTransacaoTests`.
- Frontend:
    - `vitest.config.ts`;
    - `src/setupTests.ts`;
    - `src/ambiente.test.ts`;
    - `src/pages/Login.test.tsx`.