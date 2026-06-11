# Sprint 4

A Sprint 4 representou uma etapa de consolidação do MVP do SmartBudget. Enquanto a Sprint 3 havia avançado na base de importação, categorização, autenticação, listagem e testes iniciais, a Sprint 4 focou em transformar essas bases em fluxos mais completos para o usuário, com manutenção de transações, gestão de contas, melhoria de listagens, ampliação de testes, refatoração técnica e preparação do projeto para publicação estável.

No recorte original da Sprint 4 foram consideradas 30 issues únicas. Após a consolidação inicial da entrega, 28 foram feitas na 0.4.0 e todas as 30 finalizadas na 0.4.1.

Após a publicação inicial da versão `v0.4.0`, a equipe realizou uma rodada complementar de estabilização e fechamento de pendências, consolidada na versão `v0.4.1`. Essa versão incorporou correções, testes adicionais, ajustes de modelagem e a finalização de funcionalidades que haviam ficado pendentes no recorte inicial da Sprint 4.

## Decisões durante a Sprint 4

A equipe decidiu manter a evolução do MVP em torno de fluxos reais de uso da aplicação, priorizando funcionalidades que melhoram a experiência do usuário autenticado.

A tela de conta bancária deixou de ser apenas um formulário isolado de cadastro e passou a representar uma área de **Contas**, com listagem das contas do usuário e cadastro de novas contas por modal. Também foi mantido um fluxo específico de primeira conta após o cadastro inicial do usuário.

No backend, o `TransacaoService` foi refatorado para reduzir a concentração de responsabilidades. Regras auxiliares foram separadas em componentes mais específicos, como:

- `ContaUsuarioService`;
- `SugestaoCategoriaService`;
- `TransacaoMapper`;
- `TransacaoSpecs`.

Essa decisão foi registrada na ADR `docs/adrs/ADR-0008-decomposicao-transacao-service.md`.

Também foi reforçada a decisão de tornar a cobertura frontend mensurável no pipeline, com Vitest e geração de relatório de cobertura. A partir da Sprint 4, o frontend passou a ter acompanhamento formal de cobertura, complementando o JaCoCo já usado no backend.

## Incremento funcional da Sprint 4

### Gestão de contas bancárias

A Sprint 4 evoluiu o fluxo de contas bancárias. A tela `NovaConta.tsx` passou a funcionar visualmente como uma área de **Contas**, permitindo listar contas cadastradas, exibir estado vazio e abrir um modal para cadastrar uma nova conta.

Também foi criado o fluxo de primeira conta após cadastro, representado por:

- `app-financeiro-front-end/src/pages/PrimeiraConta.tsx`;
- `app-financeiro-front-end/src/pages/PrimeiraConta.test.tsx`.

Com isso, o fluxo ficou dividido em duas experiências:

- usuário recém-cadastrado segue para cadastrar a primeira conta;
- usuário já logado usa a área **Contas** para gerenciar contas existentes e adicionar novas.

No backend, a gestão de contas também evoluiu com suporte a edição e validação de propriedade, incluindo:

- `ContaEdicaoRequestDTO`;
- alterações em `ContaController`;
- alterações em `ContaService`;
- `ContaUsuarioService`.

### Manutenção de transações

A Sprint 4 adicionou suporte mais completo para manutenção de transações. A aplicação passou a contar com recursos de edição e exclusão de transações no backend e no frontend.

As principais evidências são:

- `app-financeiro-front-end/src/pages/EditarTransacao.tsx`;
- alterações em `Transacoes.tsx`;
- alterações em `NovaTransacao.tsx`;
- alterações em `TransacaoController`;
- alterações em `TransacaoService`;
- `TransacaoEdicaoExclusaoControllerTest`;
- `TransacaoEdicaoExclusaoServiceTest`.

Com isso, o histórico de transações deixou de ser apenas uma visualização e passou a permitir manutenção pelo usuário autenticado.

### Listagem, filtros e paginação de transações

A listagem de transações foi evoluída com suporte a filtros e paginação server-side. A Sprint 4 introduziu estrutura para filtros no backend, incluindo:

- `PaginaDTO`;
- `TransacaoSpecs`;
- alterações em `TransacaoRepository`;
- alterações em `TransacaoController`;
- testes de listagem paginada.

No frontend, a tela de transações também foi ajustada para consumir e testar melhor esse fluxo.

### Categorização de transações pela interface

A categorização, que já havia sido iniciada no backend na Sprint 3, avançou na Sprint 4 com integração pela interface e reforço de testes.

As principais evidências são:

- alterações em `TransacaoController`;
- alterações em `CategoriaService`;
- `CategorizarTransacaoIntegrationTests`;
- `CategorizarTransacaoTests`;
- `CategorizacaoNaImportacaoTests`;
- testes e ajustes na tela de transações.

Também houve avanço na sugestão automática de categoria por palavras-chave com `SugestaoCategoriaService`.

### Resumo por forma de pagamento

A Sprint 4 avançou no resumo financeiro por forma de pagamento. No frontend, foram adicionados componentes específicos para visualização em lista e gráfico:

- `ResumoFormaPagamento.tsx`;
- `ResumoFormaPagamentoPizza.tsx`;
- `ResumoFormaPagamento.test.tsx`;
- `ResumoFormaPagamentoPizza.test.tsx`.

No backend, houve ajustes em `ResumoController`, `ResumoService` e `GrupoPagamentoDTO`.

Essa entrega contribui para a funcionalidade do MVP relacionada à categorização e visualização dos gastos por forma de pagamento, como PIX, cartão, dinheiro e boleto.

### Importação de extratos e contrato dos parsers

O fluxo de importação continuou sendo trabalhado na Sprint 4. Houve reorganização do contrato dos parsers e ajustes nos arquivos relacionados à importação:

- `ParserExtrato`;
- `ParserCSV`;
- `ParserTXT`;
- `ParserXML`;
- `ParserNFe`;
- `ResultadoParser`;
- `ImportacaoService`;
- `ImportarExtrato.tsx`;
- `ImportarExtrato.test.tsx`.

A issue `#174` melhorou a organização do contrato comum dos parsers.

Ao mesmo tempo, a Sprint 4 identificou a issue `#170`, relacionada a bug no fluxo de importação de extratos. Esse bug indicou que o usuário podia ser redirecionado para login em cenários de erro que não deveriam encerrar a sessão. Por isso, na versão `v0.4.0`, a importação foi considerada funcionalidade existente, mas com ressalva operacional.

Na versão complementar `v0.4.1`, essa pendência foi tratada com ajustes no fluxo de importação, incluindo correção do envio de arquivos via `FormData` e melhoria do comportamento da interface para evitar redirecionamentos indevidos.

### Refatoração do `TransacaoService`

A Sprint 4 teve uma frente técnica importante de reengenharia do `TransacaoService`.

Antes da refatoração, o serviço concentrava responsabilidades de:

- registrar transação manual;
- editar transação;
- excluir transação;
- listar transações;
- categorizar transação;
- buscar transação do usuário;
- validar conta;
- sugerir categoria;
- converter entidades em DTOs.

Após a refatoração, parte dessas responsabilidades foi distribuída para componentes mais coesos:

- `ContaUsuarioService`: resolução e validação de contas do usuário;
- `SugestaoCategoriaService`: sugestão automática de categoria;
- `CategoriaService`: validação de categoria permitida ao usuário;
- `TransacaoMapper`: conversão de entidade para DTO;
- `TransacaoSpecs`: composição de filtros para listagem.

Essa mudança não alterou o contrato externo da API, mas reduziu acoplamento, melhorou testabilidade e facilitou evolução futura.

### Testes automatizados

A Sprint 4 ampliou significativamente a cobertura de testes no backend e no frontend.

No backend, foram adicionados ou ajustados testes como:

- `CategorizarTransacaoIntegrationTests`;
- `ListarCategoriasIntegrationTests`;
- `ListarTransacoesIntegrationTests`;
- `TransacaoEdicaoExclusaoControllerTest`;
- `TransacaoMapperTest`;
- `CategoriaSeedIntegrationTests`;
- `CategorizacaoNaImportacaoTests`;
- `CategorizarTransacaoTests`;
- `ListarTransacoesPaginadoIntegrationTests`;
- `ListarTransacoesPaginadoTests`;
- `ResumoServiceTest`;
- `SugestaoCategoriaServiceTest`;
- `TransacaoEdicaoExclusaoServiceTest`.

No frontend, foram adicionados ou ampliados testes como:

- `NovaConta.test.tsx`;
- `NovaTransacao.test.tsx`;
- `ImportarExtrato.test.tsx`;
- `Transacoes.test.tsx`;
- `PrimeiraConta.test.tsx`;
- `ResumoFormaPagamento.test.tsx`;
- `ResumoFormaPagamentoPizza.test.tsx`.

Também foi consolidado o relatório de cobertura frontend com Vitest.

## Complementos incorporados na versão 0.4.1

Após a consolidação inicial da Sprint 4 na versão `v0.4.0`, a equipe realizou uma nova rodada de merges para fechar pendências e estabilizar a entrega. Esses ajustes foram incorporados à versão `v0.4.1`.

Os principais complementos foram:

| PR | Responsável pela abertura | Entrega |
|----|---------------------------|---------|
| `#212` - Feat/67 resumo mensal categorias | `Victor3294` | Finalização do backend do resumo mensal do dashboard, incluindo total recebido, total gasto, saldo, categoria com maior gasto, gastos agrupados por categoria e variação percentual em relação ao mês anterior. |
| `#208` - Modelagem: `TipoTransacao` apenas com sentido financeiro (`#176`) | `Alexandre Villela` | Ajuste de modelagem para deixar `TipoTransacao` com sentido financeiro mais claro, separando melhor a ideia de crédito e débito das demais classificações do domínio. |
| `#211` - Fix: importação + modificações pequenas na UX (`#170`) | `lucaslrc01` | Correção complementar do fluxo de importação, com ajuste no envio de arquivos via `FormData`, remoção do `Content-Type` global no Axios e melhorias de navegação e textos na interface. |
| `#209` - Testes de contrato dos parsers de importação (`#175`) | `Alexandre Villela` | Inclusão de testes de contrato para parsers de importação, reforçando o comportamento esperado na leitura de extratos e reduzindo risco de regressões. |
| `#210` - Fix: corrige CORS para frontend em produção | `victorlcrd` | Correção de CORS para permitir integração adequada entre frontend e backend no ambiente publicado. |

Com esses merges, a versão `v0.4.1` passou a representar uma versão mais estável da Sprint 4, com fechamento das principais pendências que ainda estavam abertas na `v0.4.0`.

Em especial, a issue `#67`, que inicialmente havia ficado pendente, foi finalizada com a entrega do backend de resumo mensal e agrupamento por categorias. A issue `#170`, que estava como correção parcial no recorte inicial, também recebeu ajuste complementar no fluxo de importação.

A versão `v0.4.1` também reforçou a confiabilidade da importação com testes de contrato dos parsers e corrigiu a comunicação entre frontend e backend no ambiente de produção.

## Deploy e versão estável publicada

A Sprint 4 incorporou a publicação da versão estável na branch `main`.

Embora parte do desenvolvimento tenha ocorrido na branch `dev`, o estado estável considerado para a entrega foi publicado diretamente na `main`, que representa a versão estável atual do projeto. Para o contexto deste documento, considera-se que o conteúdo desenvolvido na `dev` foi incorporado à entrega da Sprint 4 e consolidado na `main`.

A versão estável publicada incluiu arquivos específicos de deploy e execução reprodutível, como:

- `.env.prod.example`;
- `docker-compose.prod.yml`;
- `render.yaml`;
- `docs/DEPLOY.md`;
- `app-financeiro-back-end/Dockerfile`;
- `app-financeiro-back-end/.dockerignore`;
- `app-financeiro-front-end/Dockerfile`;
- `app-financeiro-front-end/.dockerignore`;
- `app-financeiro-front-end/nginx.conf`;
- `app-financeiro-front-end/.env.example`.

O arquivo `docs/DEPLOY.md` documenta a execução reprodutível do MVP com backend, frontend e banco. O `render.yaml` registra uma alternativa de publicação usando Render, com banco, backend e frontend.

Essa entrega atende à necessidade de disponibilizar uma versão estável e validável do MVP, além de melhorar a rastreabilidade do processo de implantação.

A partir da versão `v0.4.1`, os ambientes publicados considerados para validação são:

- API: `https://smartbudget-api-kbze.onrender.com`;
- Web: `https://smartbudget-web-0sic.onrender.com`.

## Itens considerados na Sprint 4

| Item | Situação | Observação |
|------|----------|------------|
| #65 - Interface de categorização de transações | Concluído | Fecha o fluxo de categorização manual pela interface. |
| #66 - Resumo por forma de pagamento e gestão de contas | Concluído | Avança visualização por forma de pagamento e complementa regras de contas. |
| #67 - Resumo mensal/backend do dashboard | Concluído na v0.4.1 | Inicialmente replanejado na v0.4.0, foi finalizado no PR #212. |
| #106 - Filtros/listagem paginada de transações | Concluído | Melhora consulta de movimentações com filtros e paginação. |
| #107 - Testes de categorização de transações | Concluído | Reforça confiabilidade das regras de categorização. |
| #122 - Manutenção do CI com Gradle Wrapper e lint frontend | Concluído | Melhora reprodutibilidade do pipeline. |
| #124 - Documentação de padrões arquiteturais aplicados | Concluído | Corrige e detalha arquitetura e padrões. |
| #125 - Ambiente de staging ou alternativa reprodutível | Concluído | Permite validação do MVP em ambiente acessível ou reproduzível. |
| #126 - Deploy/documentação de deploy | Concluído | Registra instruções de implantação. |
| #127 - Atualização de métricas da Sprint 4 | Concluído | Atualiza acompanhamento quantitativo e qualitativo. |
| #128 - Refatoração/reengenharia do `TransacaoService` | Concluído | Melhora manutenibilidade. |
| #129 - Comparação de métrica antes/depois da refatoração | Concluído | Registra impacto da reengenharia. |
| #130 - ADR da refatoração da Sprint 4 | Concluído | Documenta decisão de decomposição do serviço. |
| #134 - Backend para editar e excluir transações | Concluído | Implementa manutenção de transações no backend. |
| #135 - Frontend para editar e excluir transações | Concluído | Implementa manutenção de transações na interface. |
| #136 - Tela para cadastro de nova conta bancária | Concluído | Evolui fluxo de contas no frontend. |
| #146 - Testes da tela de registro manual de transações | Concluído | Amplia cobertura do fluxo manual. |
| #147 - Testes da tela de cadastro de nova conta bancária | Concluído | Amplia cobertura do fluxo de contas. |
| #151 - Testes da tela de importação de extratos | Concluído | Reforça cobertura da importação no frontend. |
| #155 - Testes da tela de listagem de transações | Concluído | Reforça cobertura do histórico. |
| #157 - Frontend do resumo por forma de pagamento | Concluído | Complementa o resumo por pagamento. |
| #158 - Testes do resumo por pagamento e contas | Concluído | Amplia cobertura do resumo e contas. |
| #162 - Testes para edição e exclusão de transações | Concluído | Reforça manutenção de transações. |
| #165 - Tela de gerenciamento de contas bancárias | Concluído | Consolida gestão de contas no frontend. |
| #170 - Bug no fluxo de importação de extrato | Concluído na v0.4.1 | Inicialmente parcial na v0.4.0, recebeu correção complementar no PR #211. |
| #174 - Contrato comum dos parsers de importação | Concluído | Melhora organização dos parsers. |
| #175 - Testes de contrato dos parsers de importação | Concluído na v0.4.1 | Reforça validação dos parsers de importação. |
| #176 - Ajuste de modelagem de `TipoTransacao` | Concluído na v0.4.1 | Simplifica o sentido financeiro de crédito e débito. |
| #177 - Relatório de cobertura dos testes frontend | Concluído | Torna cobertura frontend mensurável no CI. |
| #184 - Testes da tela Primeira Conta | Concluído | Amplia cobertura do onboarding após cadastro. |
| #194 - Backend para editar conta bancária | Concluído | Permite edição de contas com validação de propriedade. |
| #195 - Frontend para editar e deletar contas bancárias | Concluído | Completa manutenção de contas na interface. |
| Correção de CORS em produção | Concluído na v0.4.1 | Ajusta integração entre frontend e backend publicados. |

## Registro de contribuição individual

### Lucas de Leon Rodrigues Coelho

#### Contribuições principais

Lucas de Leon atuou principalmente em funcionalidades de transações, manutenção de contas bancárias e revisões técnicas de frontend e backend.

Durante a Sprint 4 e a estabilização da `v0.4.1`, abriu PRs relacionados a:

- CRUD de transações;
- testes backend para edição e exclusão de transações;
- implementação de funções de editar e excluir transações no menu;
- edição de conta bancária;
- correção complementar do fluxo de importação para a versão `v0.4.1`.

Também contribuiu em reviews importantes, apontando erros de lógica, inconsistências no backend e problemas de tipagem.

#### Evidências associadas

- PR relacionado à issue `#134`, CRUD de transações;
- PR relacionado à issue `#162`, testes de edição e exclusão de transações;
- PR relacionado à issue `#194`, edição de conta bancária;
- PR `#211`, correção de importação e melhorias pequenas de UX para a `v0.4.1`;
- PR `#160`, revisão com apontamentos sobre lógica backend e tipos;
- PR `#168`, revisão sobre erro na refatoração;
- PR `#164`, revisão apontando inconsistência no backend.

#### Observações

A atuação de Lucas foi relevante para a estabilização das funcionalidades de manutenção de transações e contas, além de contribuir para a qualidade técnica das entregas revisadas. Na versão `v0.4.1`, também contribuiu para estabilizar o fluxo de importação de extratos.

---

### Victor Gabriel Lacerda

#### Contribuições principais

Victor Gabriel atuou principalmente no frontend, na experiência do usuário, na gestão de contas bancárias, no resumo por forma de pagamento e em revisões gerais do projeto.

Durante a Sprint 4, abriu PRs relacionados a:

- tela de cadastro de conta bancária para usuários logados;
- refatoração da tela para gerenciamento de contas;
- tela de resumo por forma de pagamento;
- edição e exclusão de conta bancária;
- correção de CORS para integração do frontend em produção.

Também contribuiu com reviews em backend, frontend, testes e documentação, participando ativamente da aprovação de PRs antes do merge.

#### Evidências associadas

- PR `feat/#136-tela-cadastro-banco-logged-users`;
- PR `refactor/tela gerenciamento contas`, relacionado à issue `#165`;
- PR `feat/(closes #157)tela-resumo-forma-pagamento`;
- PR `feat/(closes #195) editar e deletar conta bancaria`;
- PR `#210`, correção de CORS para frontend em produção;
- PR `#212`, review em cálculos do resumo mensal, ordem de parâmetros e tratamento de categoria sem nome;
- PR `#211`, review e aprovação da correção de importação;
- PR `#198`, review para evitar `NullPointerException` em requisições sem body e orientação sobre retorno de erro;
- PR `#145`, review sobre mensagem de sucesso permanecendo indefinidamente e análise de possível erro de lógica;
- PR `#163`, review e aprovação;
- PR `#192`, review e aprovação em conversa com o responsável;
- PR `#143`, apontamento de inconsistências pontuais na lógica;
- PR `#183`, review e aprovação;
- PR `#180`, review e aprovação;
- PR `#188`, review e aprovação;
- PR `#190`, review e aprovação;
- PR `#187`, sugestão para documentar ajuste na regra de negócio e posterior aprovação;
- PR `#191`, apontamento de que o PR não corrigia completamente a issue mencionada e sugestão para ajustar a descrição.

#### Observações

A contribuição de Victor Gabriel foi importante para transformar o cadastro de conta em um fluxo mais completo de gerenciamento, além de reforçar a experiência do usuário e a consistência dos PRs revisados. Na `v0.4.1`, também participou da estabilização do ambiente publicado e da revisão do backend de resumo mensal.

---

### Victor Blum

#### Contribuições principais

Victor Blum atuou principalmente em backend, arquitetura, categorização, refatoração do `TransacaoService`, contrato de parsers, métricas técnicas e dashboard mensal.

Durante a Sprint 4, abriu PRs relacionados a:

- interface de categorização;
- categorização por forma de pagamento e conta;
- correção do contrato dos parsers de importação;
- atualização do documento de métricas com dados de refatoração;
- refatoração do design do `TransacaoService`;
- backend de resumo mensal e gastos por categoria para a versão `v0.4.1`.

Também contribuiu em muitas revisões técnicas, com foco em testes, controller, backend, documentação e qualidade das entregas.

#### Evidências associadas

- PR `Feat/#65 interface de categorizacao`;
- PR `Feat/#66 categorizacao por forma de pagamento e conta`;
- PR `Fix/174 contrato parsers importacao`;
- PR de atualização das métricas de refatoração;
- PR `Refactor/#128 transacao service design`;
- PR `#212`, finalização do backend do resumo mensal e gastos por categoria;
- PR `#141`, revisão de documentação desatualizada;
- PR `#151`, apontamento de testes faltantes e ajustes na lógica dos testes;
- PR `#152`, revisão de testes faltantes, descrição do PR e correções pontuais;
- PR `#153`, revisão de testes e correções nos testes montados;
- PR `#159`, contribuição em correção backend para funcionamento da tela e review técnico;
- PR `#163`, sugestão de novos testes;
- PR `#142`, sugestão de novo teste;
- PR `#143`, sugestão de novo teste e inconsistência no controller;
- PR `#181`, review e aprovação;
- PR `#164`, ajustes apontados por Lucas de Leon e aprovação;
- PR `#178`, sugestão de novo teste;
- PR `#156`, sugestão de dois novos testes;
- PR `#185`, sugestão de novo teste;
- PR `#186`, review e aprovação;
- PR `#189`, review e aprovação;
- PR `#166`, apontamentos sobre controller, service e cobertura;
- PR `#167`, revisão de testes e validações;
- PR `#169`, revisão de pontos de manutenção e consistência;
- PR `#172`, acompanhamento da issue de testes do resumo mensal;
- PR `#174`, revisão e correção do contrato dos parsers;
- PR `#197`, documentação de métricas da Sprint 4.

#### Observações

A contribuição de Victor Blum foi central para a evolução técnica do backend e para a redução de complexidade do `TransacaoService`. Na versão `v0.4.1`, também foi responsável por finalizar o backend do resumo mensal e gastos por categoria, fechando uma das principais pendências da Sprint 4.

---

### Alexandre Villela

#### Contribuições principais

Alexandre Villela atuou principalmente em testes, deploy, paginação, filtros server-side, categorização, documentação de publicação, modelagem e confiabilidade da importação.

Durante a Sprint 4 e a estabilização da `v0.4.1`, abriu PRs relacionados a:

- testes automatizados de categorização de transações;
- deploy no Render com blueprint e datasource por variáveis de ambiente;
- documentação de deploy;
- testes de resumo por pagamento e gestão de contas;
- paginação e filtros server-side nas transações;
- testes de contrato dos parsers de importação;
- ajuste de modelagem de `TipoTransacao`;
- validações de comportamento esperado na importação.

Também contribuiu em reviews de frontend e apontou melhorias de experiência e manutenção, incluindo confirmação antes de exclusões e redução de duplicação de código.

#### Evidências associadas

- PR `test(#107): testes automatizados de categorização de transações`;
- PR `Deploy no Render: blueprint + datasource via env (#126)`;
- PR `docs(#126): documentação de deploy + configuração por variáveis de ambiente`;
- PR `test(#158): testes de resumo por pagamento e gestão de contas`;
- PR `feat(#106): paginação e filtros server-side nas transações`;
- PR `#209`, testes de contrato dos parsers de importação;
- PR `#208`, modelagem de `TipoTransacao` apenas com sentido financeiro;
- issue `#175`, testes de contrato dos parsers;
- issue `#176`, ajuste de modelagem de `TipoTransacao`;
- PR `#145`, review e aprovação;
- PR `#180`, review com sugestão de ajuste visual;
- contribuição em review da issue `#195`, sugerindo confirmação por `window` antes da exclusão de conta bancária;
- apontamentos de duplicação de código em PRs da equipe.

#### Observações

A atuação de Alexandre Villela foi importante para o avanço do deploy, da documentação de execução, dos testes automatizados, da evolução da listagem de transações com filtros e paginação, da redução de ambiguidades no domínio financeiro e da confiabilidade da importação de extratos. Parte dessas entregas foi incorporada na versão complementar `v0.4.1`.

---

### João Pedro Callegaro Guimarães

#### Contribuições principais

João Pedro atuou principalmente em testes frontend, documentação arquitetural, cobertura de testes, importação de extratos e registro de ADR.

Durante a Sprint 4, abriu PRs relacionados a:

- atualização da documentação de padrões arquiteturais com Strategy;
- testes automatizados da tela de importação de extratos;
- testes automatizados da tela de registro manual de transações;
- testes unitários da tela de nova conta bancária;
- testes da listagem de transações;
- testes da tela Primeira Conta;
- relatório de cobertura dos testes frontend;
- ADR de decomposição do `TransacaoService`;
- correção para importação não deslogar em erro `403 Forbidden`.

Também registrou que a issue `#97` foi transferida para Lucas de Leon Rodrigues Coelho.

#### Evidências associadas

- PR `docs: atualiza documentação de padrões arquiteturais (Strategy)`;
- PR `test: adiciona testes automatizados da tela de importação de extratos (#151)`;
- PR `test: adiciona testes automatizados para a tela de registro manual de transações`;
- PR `test: adiciona testes unitários para a tela de nova conta bancária (#147)`;
- PR `test: adiciona testes da listagem de transações (#155)`;
- PR `test: adiciona testes da tela Primeira Conta (#184)`;
- PR `fix: adiciona relatório de cobertura dos testes frontend (#177)`;
- PR `docs(adr): registra decomposicao do TransacaoService na sprint 4 (#130)`;
- PR `fix(frontend): importacao nao desloga em 403 Forbidden (#170)`;
- PR `#143`, review concordando com a aprovação;
- PR `#188`, review com sugestões para segurança no deploy;
- PR `#190`, review com sugestão de ordem de merges;
- PR `#187`, review sobre regra de negócio refatorada e posterior aprovação.

#### Observações

A contribuição de João Pedro foi importante para consolidar a qualidade automatizada do frontend, documentar decisões arquiteturais e registrar a cobertura de testes como métrica acompanhada pela equipe. Também contribuiu para a estabilidade do fluxo de importação ao tratar o comportamento de logout indevido em erro `403 Forbidden`.

## PRs e reviews relevantes

Durante a Sprint 4, os PRs tiveram papel importante não apenas para entrega de funcionalidades, mas também para discussão técnica, revisão de regras de negócio e melhoria da qualidade geral do projeto.

Os PRs revisados envolveram principalmente:

- frontend de contas, transações, resumo e importação;
- backend de categorização, transações, contas e parsers;
- testes automatizados frontend e backend;
- documentação arquitetural;
- deploy e configuração por variáveis de ambiente;
- métricas e cobertura de testes.

Alguns PRs se destacaram pela quantidade de comentários e discussões técnicas:

### PR `Feat/#60 processamento extrato endpoint upload`

Esse PR recebeu grande volume de comentários e serviu como ponto importante de discussão sobre o fluxo de importação. O PR avançou tecnicamente, mas os comentários indicaram melhorias necessárias para evitar retrabalho futuro. Victor Blum e Victor Gabriel participaram ativamente com respostas e reviews.

### PR `test: Implementa testes unitários para parsers de extratos bancários (#97)`

Esse PR também recebeu muitos comentários. Devido à quantidade de problemas encontrados e ao impacto no andamento da issue, a equipe decidiu fechar o PR e transferir a issue `#97` para Lucas de Leon Rodrigues Coelho, que posteriormente ficou responsável pelo encaminhamento.

### PR `Feat/#64 categorizacao transacoes`

Esse PR teve discussão relevante sobre categorização de transações. Victor Blum e Victor Gabriel participaram ativamente para melhorar a implementação, com reviews válidos e comunicação clara.

### Reviews durante a Sprint

As reviews da Sprint 4 ajudaram a:

- identificar inconsistências em controllers e serviços;
- apontar testes ausentes;
- corrigir descrições de PRs que não representavam corretamente o escopo entregue;
- sugerir melhorias de UX, como mensagens temporárias e confirmação antes de exclusões;
- evitar links quebrados após mudança de rotas;
- reforçar documentação de regras de negócio;
- revisar deploy e segurança da configuração;
- manter a branch `dev` mais estável antes da consolidação na `main`.

Esse processo de review foi essencial para que a Sprint 4 entregasse funcionalidades novas sem perder o controle de qualidade.

## Pontos adicionais de documentação

A Sprint 4 também atualizou documentação técnica e de processo:

- `README.md`;
- `docs/como-rodar.md`;
- `docs/metricas.md`;
- `docs/arquitetura.md`;
- `docs/adrs/ADR-0004-Estrategia-de-arquiteturas-e-camadas.md`;
- `docs/adrs/ADR-0005-padroes-de-projeto.md`;
- `docs/adrs/ADR-0008-decomposicao-transacao-service.md`;
- `docs/DEPLOY.md`.

Essas atualizações melhoram a rastreabilidade do projeto, a execução local, a compreensão da arquitetura e a reprodução do ambiente de entrega.

## Observação sobre a versão 0.4.1

A versão `v0.4.1` não substitui o escopo principal da Sprint 4, mas registra uma rodada complementar de estabilização feita após a `v0.4.0`.

Essa versão foi criada para:

- finalizar pendências que ainda estavam abertas no recorte inicial;
- corrigir problemas identificados no fluxo de importação;
- reforçar testes de contrato dos parsers;
- ajustar a modelagem de tipo de transação;
- corrigir CORS para o ambiente publicado;
- consolidar o backend do resumo mensal do dashboard.

Com isso, a Sprint 4 passa a ter uma entrega mais completa e alinhada ao MVP validável do SmartBudget.
