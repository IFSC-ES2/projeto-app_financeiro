# Avaliação - Engenharia de Software II

| entrega | aluno                   | commit  | data     | correção | nota | peso |
| ------- | ----------------------- | ------- | -------- | -------- | ---- | ---- |
| 1       | equipe                  | c83abb3 | 16/03/26 | 20/03/26 | 9,8  | 2    |
| 2       | equipe                  | e428721 | 27/03/26 | 29/03/26 | 7    | 2    |
| 3       | equipe                  | 96b6f65 | 09/04/26 | 22/04/26 | 9,4  | 3    |
| 4       | equipe                  | fe9a0a2 | 17/04/26 | 09/05/26 | 8,1  | 3    |
| 5       | Alexandre Villela       | dd7c536 | 10/05/26 | 24/05/26 | 7,4  | 10   |
| 5       | João Pedro Callegaro    | dd7c536 | 10/05/26 | 24/05/26 | 6,8  | 10   |
| 5       | Lucas de Leon Rodrigues | dd7c536 | 10/05/26 | 24/05/26 | 7,5  | 10   |
| 5       | Victor Blum             | dd7c536 | 10/05/26 | 24/05/26 | 7,2  | 10   |
| 5       | Victor Gabriel Lacerda  | dd7c536 | 10/05/26 | 24/05/26 | 7,0  | 10   |

## Nota parcial

| aluno                   | nota parcial |
| ----------------------- | ------------ |
| Alexandre Villela       | 8,0          |
| João Pedro Callegaro    | 7,7          |
| Lucas de Leon Rodrigues | 8,1          |
| Victor Blum             | 7,9          |
| Victor Gabriel Lacerda  | 7,8          |

## Comentários

### Entrega 1

- `README.md` gerado por IA.

#### Recuperação

1. Equipe formada: parcialmente corrigido.
   - Há um novo membro na equipe que não é mencionado.
2. Tema definido: corrigido.
3. MVP: corrigido.
4. Governança mínima: corrigido.

### Entrega 2

1. Visão do produto: parcialmente atendido.
   - Não informaram qual é a proposta de valor do sistema.
2. Definição do MVP: parcialmente atendido.
   - Não informaram quais funcionalidades são consideradas essenciais.
   - Não informaram por que esse recorte é viável para o semestre.
   - Não informaram quais critérios foram usados pela equipe para decidir o que entra e o que fica de fora
3. Backlog inicial com critérios de aceitação: não atendido
4. Definition of Done (DoD): atendido
5. ADRs iniciais: atendido
6. Atualização do README: atendido

### Entrega 3

1. Planejamento inicial e baseline: parcialmente
   - T-shirt size é uma técnica de estimativa relativa, onde se avalia o esforço demandado por cada uma das tarefas/funcionalidades em comparação às outras; por isso, não faz sentido limitar cada um dos tamanhos em horas, o que a tornaria uma estimativa absoluta.
   - A priorização deve estar também no acompanhamento do projeto do Github, não apenas na descrição das _issues_
   - Informar a data de registro do baseline
2. Registro da abordagem de estimativa: atendido
3. Capacidade planejada da equipe: atendido
4. Definição das métricas que serão acompanhadas: atendido
5. Ficha de cada métrica: parcialmente atendido
   - Separar as fichas de cada métrica em arquivos diferentes
     - Em cada uma adicionar data do acompanhamento e valor coletado
   - Numerar métricas para facilitar a referenciação

### Entrega 4

1. Registro inicial de riscos do projeto: atendido
2. Análise e priorização dos riscos: parcial
   - A matriz de riscos deve ter como eixos probabilidade x impacto para apresentar visualmente os riscos
3. Plano de resposta aos riscos: atendido
4. Consolidação do fluxo de trabalho no repositório: atendido
5. Definição inicial de critérios de qualidade do projeto: atendido
6. Relação entre riscos e qualidade: atendido
7. Definição preliminar de avaliação da qualidade: não atendido
8. Atualização da documentação do projeto: atendido

### Entrega 5

1. Primeiro incremento funcional do sistema: parcial.
   - O vertical slice escolhido foi cadastro e autenticação de usuário, alinhado ao MVP revisado em `inception.md`, em que autenticação/perfil pessoal aparece como funcionalidade essencial e pré-requisito técnico.
   - Há implementação atravessando frontend, backend e persistência: telas de login/cadastro em React, endpoints `POST /auth/register` e `POST /auth/login`, `UsuarioService`, `UsuarioRepository`, entidade `Usuario`, PostgreSQL e JWT.
   - O backend não executa com o comando documentado `./gradlew bootRun` porque o `gradle-wrapper.jar` não está versionado.
   - Usando Gradle instalado no sistema, o backend iniciou quando havia PostgreSQL acessível em `localhost:5432`; porém a reprodutibilidade da instrução oficial fica comprometida porque o Docker indicado na documentação não pôde ser usado sem permissão ao daemon e o wrapper Gradle está incompleto.
2. Testes de unidade automatizados: parcial.
   - Há testes versionados em `RegistrarLoginTests.java`, cobrindo regras de cadastro, validação de CPF/e-mail, duplicidade, autenticação e credenciais inválidas.
   - Os testes da classe passam com Gradle instalado e o nome correto da classe: `gradle test --tests "bcd.appfinanceirobackend.RegistrarLoginTests"`.
   - O comando documentado no README usa pacote incorreto (`bcd.appfinanceirobackend.service.RegistrarLoginTests`) e, além disso, `./gradlew` falha por ausência do wrapper jar.
   - A suíte completa `gradle test` passou no ambiente usado nesta reexecução, mas depende de um PostgreSQL real em `localhost:5432` por causa de `AppFinanceiroBackEndApplicationTests.contextLoads()`, em vez de usar configuração isolada de teste.
3. Escopo da Sprint 1 explicitado e justificado: atendido.
   - `sprint1.md` informa o vertical slice, as issues planejadas, concluídas e não atendidas, além de justificar a escolha da autenticação como base técnica do MVP.
   - O documento explicita replanejamento de tarefas (`#44`, `#26` e `#27`) por atraso e redistribuição para outros membros.
4. Backlog e board atualizados: parcial.
   - As issues da sprint possuem critérios de aceitação em geral e foram vinculadas ao board indicado no README.
   - Algumas issues ficaram com critérios genéricos ou incompletos, por exemplo `#40`, `#43` e `#44`.
   - A documentação da sprint lista as issues, mas a vinculação entre cada issue, commits e PRs não está completa no próprio documento.
5. Fluxo de trabalho evidenciado no repositório: parcial.
   - Há forte evidência de trabalho por branches e PRs, com vários PRs aprovados e mesclados (`#46`, `#47`, `#48`, `#52`, `#54`, `#55`, `#57`, `#58`, `#59`, `#69`, `#70`, `#72`, `#73`) e alguns PRs fechados por inconsistências.
   - `sprint1.md` registra reviews relevantes feitas pelos integrantes.
6. Registro das contribuições individuais: atendido.
   - `sprint1.md` registra contribuições por integrante, especialmente em reviews e PRs.
   - Os commits confirmam, em linhas gerais, as contribuições descritas, embora algumas tarefas tenham sido reatribuídas durante a sprint.
   - Contribuições individuais:
     - Alexandre: contribuiu diretamente com a funcionalidade central de autenticação/cadastro no backend, JWT, DTOs, segurança, validação de CPF e tela de cadastro inicial, além de reviews relevantes.
     - João Pedro: contribuiu com frontend inicial, configuração React/Vite, PostgreSQL/CORS e tela de login inicial, mas parte das tarefas atribuídas foi replanejada para outros membros por atraso, conforme `sprint1.md`. A nota é menor que a dos demais participantes ativos por essa redistribuição e pelas inconsistências de ambiente deixadas na entrega.
     - Lucas: contribuiu fortemente com testes automatizados, merges/reviews e assumiu parte das telas finais de login/cadastro após replanejamento. A atuação é coerente com o papel de qualidade, mas a suíte completa ainda ficou dependente de banco real e o lint do frontend falha.
     - Victor Blum: contribuiu de forma relevante na estrutura backend, modelos JPA, enums, diagramas e reviews de arquitetura. A nota reflete participação forte em base técnica/arquitetura, mas menor protagonismo no fluxo final de autenticação.
     - Victor Gabriel: atuou como Scrum Master, documentação da Sprint 1, release/tag, revisões e ajustes pontuais no frontend. A participação é rastreável e importante para processo, mas com menor volume de implementação da funcionalidade principal.
7. Documentação atualizada: parcial.
   - README e `como-rodar.md` descrevem o estado atual, execução do banco, backend, frontend e testes.
   - As instruções de Gradle não funcionam no checkout da entrega devido à ausência de `gradle-wrapper.jar`.
   - O comando de teste do README referencia o pacote errado da classe de teste.
   - A documentação usa `/register` como rota de frontend em `como-rodar.md`, mas a aplicação expõe a tela de cadastro em `/cadastro`.
   - A documentação e decisões anteriores ainda carregam inconsistências residuais de banco/ambiente, apesar da execução real estar em PostgreSQL.
8. Release do marco: atendido com atraso.
   - A tag anotada `v0.1.0` existe e aponta para o commit `dd7c536`.
   - A release `v0.1.0` foi publicada com notas da Sprint 1.
