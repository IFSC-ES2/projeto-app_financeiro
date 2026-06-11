# Sprint 4

A Sprint 4 representou uma etapa de consolidação do MVP do SmartBudget. Enquanto a Sprint 3 havia avançado na base de importação, categorização, autenticação, listagem e testes iniciais, a Sprint 4 focou em transformar essas bases em fluxos mais completos para o usuário, com manutenção de transações, gestão de contas, melhoria de listagens, ampliação de testes, refatoração técnica e preparação do projeto para publicação estável.

No recorte original da Sprint 4 foram consideradas 30 issues únicas. Após a consolidação inicial da entrega, 28 foram consideradas concluídas e 2 permaneceram como pendência ou entrega parcial: `#67` e `#170`.

Após a publicação inicial da versão `v0.4.0`, a equipe realizou uma rodada complementar de estabilização e fechamento de pendências, consolidada na versão `v0.4.1`. Essa versão incorporou correções, testes adicionais, ajustes de modelagem e a finalização de funcionalidades que haviam ficado pendentes no recorte inicial da Sprint 4.

## Complementos incorporados na versão 0.4.1

Após a consolidação inicial da Sprint 4 na versão `v0.4.0`, a equipe realizou uma nova rodada de merges para fechar pendências e estabilizar a entrega. Esses ajustes foram incorporados à versão `v0.4.1`.

Os principais complementos foram:

| PR | Responsável pela abertura | Entrega |
|----|---------------------------|---------|
| `#212` - Feat/67 resumo mensal categorias | `Victor3294` | Finalização do backend do resumo mensal do dashboard, incluindo total recebido, total gasto, saldo, categoria com maior gasto, gastos agrupados por categoria e variação percentual em relação ao mês anterior. |
| `#208` - Modelagem: `TipoTransacao` apenas com sentido financeiro (`#176`) | `apvillela` | Ajuste de modelagem para deixar `TipoTransacao` com sentido financeiro mais claro, separando melhor a ideia de crédito e débito das demais classificações do domínio. |
| `#211` - Fix: importação + modificações pequenas na UX (`#170`) | `lucaslrc01` | Correção complementar do fluxo de importação, com ajuste no envio de arquivos via `FormData`, remoção do `Content-Type` global no Axios e melhorias de navegação e textos na interface. |
| `#209` - Testes de contrato dos parsers de importação (`#175`) | `apvillela` | Inclusão de testes de contrato para parsers de importação, reforçando o comportamento esperado na leitura de extratos e reduzindo risco de regressões. |
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