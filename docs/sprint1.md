# Sprint 1: SmartBudget v0.1.0

O foco principal dessa Sprint foi a tela de cadastro e autenticação de usuários.

#### Escolha do vertical slice:

Fluxo de cadastro e autenticação de usuário, porque envolve:

- Tela no frontend;
- Envio de dados para o backend;
- Validação das informações;
- Persistência no banco de dados;
- Retorno de sucesso ou erro para o usuário.

Assim, a equipe consegue testar a base do MVP logo no início, reduzir riscos técnicos e garantir que as próximas funcionalidades sejam construídas sobre uma estrutura já validada.

As issues para essa sprint foram:

- #13: Criação da Home com um Relatório resumido das finanças do usuário (Alexandre Vilella)
- #16: Definir dependências do Sprint Boot (Victor Blum)
- #17: Configuração do Spring Boot e estrutura de pastas (Victor Blum)
- #18: Configuração do React com TypeScript (João Pedro)
- #19: Modelagem do banco de dados Entidade Relacionamento (Victor Blum)
- #20: Criação do diagrama UML (Victor Blum)
- #21: Configuração do banco de dados (João Pedro)
- #22: Configuração de CORS entre frontend e backend (João Pedro)
- #25: Cadastro e autenticação de usuário (Alexandre Vilella)
- #26: Tela de login (João Pedro)
- #27: Tela de cadastro (Alexandre Vilella)
- #40: Criação do docs/sprint1.md (Victor Lacerda)
- #41: Vertical slice: estruturação básica do backend para a Sprint 1 (João Pedro)
- #42: Criar arquivo de configuração para testes (Lucas de Leon)
- #43: Infraestrutura de execução do Docker (Alexandre Vilella)
- #44: Vertical slice: implementar a tela correspondente à entrega da Sprint 1 (João Pedro)
- #53: Implementar entidades JPA e enums do diagrama de classes (Victor Blum)


## Contribuições em Pull Requests (Reviews)

| Integrante | PR contribuído | Comentários | Resultado | Reviews |
|---|---|---|---|---|
| Alexandre Vilella | [feat: configuracao de CORS entre frontend e backend #22 #56](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56) | Questionou a alteração da estrutura do backend do banco de dados feita anteriormente, pois, caso avançasse, poderia sobrescrever arquivos em todas as pastas.<br><br>Também questionou a alteração do `gradle.properties` e a substituição pelo Maven. | O proprietário do PR, João, restaurou os arquivos da estrutura do backend com banco de dados. O Maven foi removido. Erros de lint/compilação no `WebConfig` e no `App.tsx` foram corrigidos.<br><br>Evidência: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56#issuecomment-4366968779> | 1. [Alteração indevida da estrutura](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56#issuecomment-4362635794)<br>2. [Alteração indevida para Maven](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56#issuecomment-4362643208) |
| Alexandre Vilella | [feat(#18): configuracao inicial do front-end com React e Vite #54](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/54) | Indicou que a `baseURL` estava definida diretamente como `localhost` no arquivo `api.ts`.<br><br>Sugeriu adicionar um TODO para, futuramente, substituir esse valor por uma variável de ambiente `.env`. | O proprietário do PR adicionou o TODO sugerido.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/54/commits/f69871d13ae845236dd1351fc30f06b09ebc9a4e> | 1. [Comentário sobre baseURL hardcoded](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/54#issuecomment-4356153204) |
| Alexandre Vilella | [Feat/#53 implementando models #55](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55) | Pontuou que a descrição do PR informava o uso de getters e setters, porém os arquivos estavam sem esses métodos.<br><br>Questionou se as entidades ficariam dessa forma no início do projeto ou se isso seria tratado em outra issue. | O proprietário do PR, Victor Blum, alterou as entidades adicionando getters e setters.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55/commits/a87852eb65f9f598937a979b9997889d2ea67155> | 1. [Comentário sobre ausência de getters e setters](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55#issuecomment-4356597951) |
| Alexandre Vilella | [Docs/12 uml e banco de dados #46](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46) | Pontuou a prática inadequada de commitar arquivos gerados automaticamente, como `.gradle` e `.idea`, recomendando atualização do `.gitignore`. Também apontou inconsistência relacionada à ausência do campo `limite` em `cartao_credito`. | O proprietário do PR, Victor Blum, ajustou a inconsistência sobre o limite no cartão de crédito, deixando o campo como opcional por não estar no MVP.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46/commits/fbb4b38ba9f4aade388c758a3fc0dea57b500106> | 1. [Comentário sobre arquivos gerados, gitignore e campo limite](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46#issuecomment-4322699073) |
| Lucas de Leon | [feat: configuracao de CORS entre frontend e backend #22 #56](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56) | Questionou a alteração da estrutura do backend do banco de dados feita anteriormente, pois, caso avançasse, poderia sobrescrever arquivos em todas as pastas.<br><br>Também questionou a alteração do `gradle.properties` e a substituição pelo Maven. | O proprietário do PR, João, restaurou os arquivos da estrutura do backend com banco de dados. O Maven foi removido. Erros de lint/compilação no `WebConfig` e no `App.tsx` foram corrigidos.<br><br>Evidência: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56#issuecomment-4366968779> | 1. [Alteração indevida da estrutura](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56#issuecomment-4362635794)<br>2. [Alteração indevida para Maven](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56#issuecomment-4362643208) |
| Lucas de Leon | [Feat/#53 implementando models #55](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55) | Pontuou um erro gramatical no arquivo `StatusImportacao.java`. | O proprietário do PR, Victor Blum, corrigiu o enum, alterando `CONCLUINDO` para `CONCLUIDO`.<br><br>Evidência: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55#discussion_r3171593154> | 1. [Correção gramatical em StatusImportacao.java](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55#discussion_r3171543186) |
| Lucas de Leon | [feat: cria projeto base do backend e configura conexao com PostgreSQL (Issue #21) #50](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/50) | Pontuou que a estrutura do backend já havia sido feita e que essa implementação não correspondia ao escopo da issue `#21`, nem à tarefa atribuída ao João Pedro. | O proprietário do PR, João Pedro, refatorou a estrutura.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/50/commits/01643a184b05790811286b75b8c2fa414748793d> | 1. [Review sobre escopo e estrutura do backend](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/50#pullrequestreview-4192821638) |
| Victor Blum | [feat: configuracao de CORS entre frontend e backend #22 #56](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56) | Apontou erro de versão e inconsistências em arquivos que continuaram mesmo após alteração do proprietário do PR. | O Pull Request foi encerrado após o review por inconsistências. | 1. [Comentário sobre inconsistências de versão e arquivos](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/56#issuecomment-4367277886) |
| Victor Blum | [feat(#18): configuracao inicial do front-end com React e Vite #54](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/54) | Solicitou a remoção das imagens padrão do React e dos arquivos CSS não utilizados. | O proprietário do Pull Request removeu as imagens padrão.<br><br>Commits:<br>1. <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/54/commits/2e83bdddcc4a640e8f38648365b93490582cc699><br>2. <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/54/commits/9fe7932e4b944530b72d8a37f924e1a515ac2bee> | 1. [Comentário para remover imagens default do React e CSS](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/54#issuecomment-4357185101) |
| Victor Blum | [feat: cria projeto base do backend e configura conexao com PostgreSQL (Issue #21) #50](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/50) | Pontuou que a correção feita pelo proprietário do PR para resolver a duplicação de estruturas foi realizada incorretamente, pois excluiu pastas essenciais do projeto no commit de correção. | O Pull Request foi fechado por complicações no processo de desenvolvimento e pela inconsistência com o que havia sido solicitado na issue `#21`. Foi orientada a abertura de um novo PR. | 1. [Review sobre correção incorreta da estrutura](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/50#pullrequestreview-4193258027)<br>2. [Comentário complementar sobre inconsistências](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/50#issuecomment-4339911415) |
| Victor Lacerda | [Feat/#53 implementando models #55](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55) | Identificou uma possível falha de segurança relacionada a senha exposta ou informação sensível no código/configuração. | A senha foi alterada pelo proprietário do PR, Victor Blum.<br><br>Evidência: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55#discussion_r3171584613><br><br>O PR foi aprovado por Victor Gabriel e posteriormente mergeado.<br><br>Aprovação: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55#pullrequestreview-4209401024> | 1. [Possível falha de segurança identificada](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/55#discussion_r3171554305) |
| Victor Lacerda | [Docs/12 uml e banco de dados #46](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46) | Pontuou que havia um erro de sintaxe em `docs/diagramaUML.md`, na linha 266. | O proprietário do PR, Victor Blum, corrigiu o erro de sintaxe.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46/commits/d6171776dce55c8393027cf50611f04225071b6a> | 1. [Review sobre erro de sintaxe no diagrama UML](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46#pullrequestreview-4173679390) |
| Victor Lacerda | [Docs/12 uml e banco de dados #46](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46) | Sugeriu alterar `dia_fechamento` e `dia_vencimento` para campos opcionais, pois esses campos não funcionariam adequadamente para todos os tipos de conta. Também sugeriu como alternativa separar esses dados em uma entidade específica para cartão de crédito. | O proprietário do PR, Victor Blum, seguiu a solução alternativa e criou uma entidade separada para cartão de crédito.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46/commits/0b5254ec3a482011de82a03bcd8b53d563acd410> | 1. [Sugestão sobre campos opcionais e entidade de cartão de crédito](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46#discussion_r3140565663) |
| Victor Lacerda | [Docs/12 uml e banco de dados #46](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46) | Sugeriu definir precisão para campos monetários usando `DECIMAL(10,2)`, garantindo escala adequada para valores financeiros. | O proprietário do PR tentou aplicar a sugestão, porém o Mermaid não aceitou a notação diretamente. Como alternativa, Victor Blum adicionou um comentário no próprio diagrama indicando o limite de decimais.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46/commits/c9ffa8d7d8be7fa72678a54bf4c3130627cf47ce> | 1. [Sugestão sobre precisão de campos monetários](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/46#discussion_r3140627087) |
| Victor Lacerda | [chore: Criando o projeto springboot e adicionando as dependencias #47](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/47) | Pontuou o uso do Java 25 e questionou se não seria melhor utilizar Java 21 por questões de compatibilidade geral. | O proprietário do PR, Victor Blum, alterou a versão do Java de 25 para 21, concordando com o ponto levantado.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/47/commits/f6a51918a7c9ee27c4710031b54703359d9d5946> | 1. [Discussão sobre versão do Java](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/47#discussion_r3140826685) |
| Victor Lacerda | [chore: Criando o projeto springboot e adicionando as dependencias #47](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/47) | Pontuou a falta do driver do banco de dados e orientou a equipe a revisar a ADR para validação do modelo escolhido. | O driver do PostgreSQL foi incluído nas dependências pelo proprietário do PR, Victor Blum.<br><br>Commit: <https://github.com/IFSC-ES2/projeto-app_financeiro/pull/47/commits/2f7d7875569f355c14b7106c0d425159f070da72> | 1. [Comentário sobre driver do banco e revisão da ADR](https://github.com/IFSC-ES2/projeto-app_financeiro/pull/47#issuecomment-4317143777) |
 

### Pontos de notoriedade

Essa Sprint consistiu em 11 pull requests, sendo:
- 7 aprovados;
- 4 recusados por inconsistências. 

8 desses 11 PRs foram revisados por no mínimo 2 pessoas, demonstrando o esforço colaborativo na revisão dos PRs. 
- Em sua maioria, eles foram aprovados/mergeados pelo Scrum Master;
- Em casos pontuais, o Scrum Master participou do review, porém outro membro fez a aprovação do merge.

Será revisada a disponibilidade de tempo de cada membro novamente, visto que, na prática, nem toda a carga que foi atribuída a cada um foi realizada dentro do prazo, impactando o planejamento inicial.

A partir dessa análise, será conversado novamente com cada membro para reorganizar tempo e interesse individual para a realização das issues propostas na Sprint 2. 


**Obs.:** Esse documento está incompleto. Será finalizado quando o elemento principal dessa sprint estiver pronto, o front-end, garantindo que o fluxo mínimo do MVP funcione.