# Projeto de App de Gerenciamento Financeiro - SmartBudget

[Acesse web pelo Render](https://smartbudget-web-0sic.onrender.com) e a  [ API](https://smartbudget-api-kbze.onrender.com).

Projeto proposto pelo Clayrton em Extensão II:

> Desenvolvimento de um gerenciador financeiro pessoal que permita a integração de notas fiscais (NF-e), extratos bancários (xml, csv, txt), extratos de cartão (xml, csv, txt), e que permita gerenciar as contas do usuário de modo a prover relatórios de gastos e despesas mensais por categorias (habitação, saúde, serviços, lazer, manutenção, transportes, etc.).

## 1. Equipe Formada

- Alexandre Pereira Villela -> DevOPS / Infra
- João Pedro Callegaro -> DevOPS / Infra
- Lucas de Leon Rodrigues -> Engenheiro de Qualidade
- Victor Blum -> Arquiteto de Software
- Victor Gabriel Lacerda -> Scrum Master

## 2. Tema Definido

### a) qual problema o sistema pretende resolver?

O sistema pretende resolver a falta de organização financeira e a falta de controle sobre o próprio dinheiro do usuário. Um possível usuário pode usar diferentes bancos, diferentes cartões de crédito, mas falta um sistema central, que una e mostre os seus gastos totais em todos os aspectos da sua vida. Com o SmartBudget, o usuário vai poder organizar e visualizar melhor sua vida financeira.

### b) quem são os usuários?

O sistema é focado em pessoas físicas, não empresas. Os usuários são pessoas que querem ter um maior controle de gastos, pode ser uma pessoa que usa muito cartão de crédito e quer informações mais práticas dos gastos, ou um chefe de família que quer ver em quais aspectos da sua vida o dinheiro está sendo mais utilizado, como lazer, alimentação, saúde, entre outros.

### c) qual a proposta do sistema para resolver esse problema?

A proposta é ser um assistente financeiro prático e inteligente que elimina o trabalho de anotar gastos na mão, sendo necessário apenas enviar extratos bancários e notas fiscais para leitura automática. Ele resolve a falta de controle e visualização de gastos centralizando tudo em um único lugar, separando despesas por categoria e criando um "extrato futuro", que avisa com antecedência o valor das faturas do cartão e o vencimento de boletos.

## 3. MVP planejado inicialmente

Esta seção registra o escopo inicialmente planejado para o MVP. O estado real do RC, com funcionalidades concluídas, concluídas com ressalva e pendentes, está detalhado nas seções 4 e 5.

### a) o que o MVP pretendia fazer?

O sistema vai permitir a leitura de extratos bancários e notas fiscais (.xml, .csv, .txt), separando os gastos em categorias automaticamente ou manualmente, se necessário. Também informará o "extrato futuro", baseando-se nos extratos bancários enviados, considerando vencimento de boletos, parcelamentos, financiamentos, entre outros.

### b) quais eram as funcionalidades principais previstas?

1. Criação de perfil pessoal, com autenticação;
1. Adicionar gastos manualmente;
1. Leitura de extratos bancários e notas fiscais (xml, csv, txt);
1. Categorizar os gastos em subdivisões, como lazer, alimentação etc.;
1. Categorizar os gastos a partir de como o dinheiro foi utilizado, como cartão, pix, dinheiro e boleto;
1. Categorizar os gastos a partir do cartão e banco utilizado;
1. Visualização de gastos do mês em texto, gráficos e dashboards;
1. Visualização do extrato dos próximos meses em texto, gráficos e dashboards.

### c) o que ficará fora do escopo no momento

1. Integração direta com a conta bancária;
1. Inteligência Artificial para organizar a categorização;
1. Investimentos e criptomoedas;
1. Conversão para outras moedas, como dólar, euro e libra. O sistema será apenas em Real.

## 4. Situação atual do projeto

O projeto possui backend em Spring Boot, frontend em React + Vite, autenticação com JWT, banco PostgreSQL, versionamento de schema com Flyway e ambiente local padronizado com Docker Compose.

Na versão RC, o projeto consolidou os fluxos principais de autenticação, contas, transações, categorias, importação de arquivos, resumos de dados, testes automatizados, documentação de deploy e ambiente de execução. A lista detalhada do que foi entregue, do que foi entregue com ressalva e do que ficou fora do RC está registrada na seção seguinte.

Algumas telas ainda estão em evolução, como dashboard visual completo, categorias, parcelamentos, faturas de cartão de crédito e extrato futuro. Elas podem existir como parte da navegação, modelo ou base técnica do sistema, mas ainda não representam a versão final dessas funcionalidades.

## 5. Pontos que foram feitos

### Funcionalidades concluídas

Funcionalidades entregues no RC:

- cadastro de usuário;
- login;
- autenticação JWT;
- rotas públicas e privadas;
- cadastro de contas;
- listagem de contas;
- edição de contas;
- exclusão de contas sem transações vinculadas;
- fluxo de primeira conta;
- cadastro manual de transações;
- edição de transações;
- exclusão de transações;
- listagem paginada de transações;
- filtros de transações por período, tipo, conta e categoria;
- listagem de categorias;
- categorização manual de transações;
- importação de arquivos pela tela;
- envio de arquivo via `multipart/form-data`;
- consulta de status da importação;
- parsers para CSV, TXT, XML genérico e NF-e;
- resumo por forma de pagamento;
- componente visual de resumo por forma de pagamento;
- backend de resumo mensal;
- backend de agrupamento por categoria;
- testes automatizados no backend;
- testes automatizados no frontend;
- documentação de deploy;
- ambiente com PostgreSQL, Flyway, Docker Compose e Render.

### Funcionalidades com ressalva

Funcionalidades existentes ou parcialmente atendidas, mas com limitação de escopo, validação ou completude no RC:

- importação de extratos reais;
- categorização automática por regras de palavras-chave;
- dashboard mensal;
- categorização por cartão/banco quando envolver faturas ou cartão de crédito completo.

### Funcionalidades pendentes ou fora do RC

Funcionalidades que não devem ser consideradas concluídas nesta versão:

- extrato futuro;
- projeção de saldo dos próximos meses;
- avisos de vencimentos;
- parcelamentos;
- faturas de cartão de crédito como fluxo funcional;
- dashboard visual completo de gastos do mês;
- gestão visual completa de categorias;
- suporte universal a qualquer layout real de extrato bancário;
- integração bancária direta;
- Open Finance;
- PDF;
- investimentos;
- múltiplas moedas;
- multiusuário familiar/empresarial.

## 6. Fechamento do Release Candidate

O Release Candidate do SmartBudget entrega os fluxos principais de autenticação, gestão de contas, cadastro manual de transações, listagem com filtros, categorização de transações, importação de arquivos financeiros, resumo por forma de pagamento e backend do resumo mensal.

Algumas funcionalidades previstas no inception foram entregues com ressalvas. A importação de arquivos está implementada para CSV, TXT, XML e NF-e, mas extratos reais podem variar por banco e exigir ajustes específicos de parser. O dashboard mensal possui backend implementado, mas a visualização completa no frontend ainda não está finalizada. A categorização de transações está funcional, mas a tela própria de gestão de categorias permanece em construção.

O extrato futuro, projeções de próximos meses, parcelamentos e avisos de vencimento não fazem parte do escopo funcional entregue neste RC e devem ser tratados como evolução futura.

### Resumo em relação ao inception

Não entregue:

- extrato futuro;
- projeção de saldo dos próximos meses;
- avisos/lembretes de vencimento;
- parcelamentos funcionais;
- faturas funcionais.

Entregue parcialmente:

- dashboard de gastos do mês: backend entregue e frontend completo pendente;
- importação de extratos reais: fluxo e parsers entregues, mas suporte real ainda limitado por layout;
- categorização: transações categorizáveis, mas tela própria de categorias ainda não finalizada;
- cartão/conta/banco: conta e banco entregues, mas cartão, fatura e parcelamento não finalizados.

Entregue além ou como complemento do inception:

- cadastro manual de transações;
- edição e exclusão de transações;
- edição e exclusão de contas;
- filtros e paginação;
- resumo por forma de pagamento;
- testes automatizados;
- cobertura frontend/backend;
- CI;
- deploy;
- documentação de arquitetura, métricas e ADRs;
- refatoração do `TransacaoService`.

### Classificação final do RC

Concluído:

- autenticação e perfil pessoal;
- cadastro manual de transações;
- listagem, edição e exclusão de transações;
- gestão de contas;
- filtros e paginação;
- categorização manual de transações;
- resumo por forma de pagamento;
- deploy e documentação de execução.

Concluído com ressalva:

- importação de extratos e NF-e;
- categorização automática;
- categorização por conta/banco/cartão;
- backend do dashboard mensal;
- dashboard enquanto página, pois ainda não exibe todo o resumo financeiro planejado.

Pendente ou fora do RC:

- extrato futuro;
- parcelamentos;
- faturas;
- avisos de vencimento;
- dashboard visual completo;
- gestão visual completa de categorias;
- suporte universal a extratos reais de bancos diferentes.

## 7. Execução local

### Banco de dados local

Para subir o banco de dados:

```bash
docker compose up -d
```

O banco PostgreSQL será iniciado com as configurações definidas no `docker-compose.yml` da raiz do projeto:

- Banco: `app_financeiro`
- Usuário: `postgres`
- Senha: `1234`
- Porta: `5432`
- Container: `smartbudget-postgres`

Para parar o banco:

```bash
docker compose down
```

Para apagar os dados locais do banco:

```bash
docker compose down -v
```

Atenção: o comando `docker compose down -v` remove o volume Docker e apaga os dados locais do banco.

### Backend

Para rodar o backend:

```bash
cd app-financeiro-back-end
./gradlew bootRun
```

- Porta: 8080
- Base URL: `http://localhost:8080`

Ao iniciar, o backend executa as migrations pendentes do Flyway e valida o schema do banco com Hibernate.

### Frontend

Para rodar o frontend:

```bash
cd app-financeiro-front-end
npm install
npm run dev
```

- Porta: 5173
- URL: `http://localhost:5173`

Rotas principais disponíveis no frontend:

- `/login` - tela de login
- `/cadastro` - tela de cadastro
- `/dashboard` - painel inicial autenticado
- `/contas` - gestão de contas
- `/contas/primeira` - fluxo de primeira conta
- `/transacoes` - listagem de transações
- `/transacoes/nova` - cadastro manual de transação
- `/transacoes/:transacaoId/editar` - edição de transação
- `/categorias` - tela de categorias
- `/parcelamentos` - tela de parcelamentos
- `/importacoes/nova` - importação de extratos

### Testes

Para rodar os testes do backend:

```bash
cd app-financeiro-back-end
./gradlew test
```

Para rodar os testes do frontend:

```bash
cd app-financeiro-front-end
npm install
npm test
```

Também é recomendado rodar o lint e o build do frontend antes de abrir PR:

```bash
npm run lint
npm run build
```

### Teste rápido de autenticação via curl

Com o backend rodando, é possível testar o cadastro de usuário com:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Alexandre","email":"alex@test.com","cpf":"12345678909","senha":"123456"}'
```

Informações mais detalhadas sobre execução local estão disponíveis em [docs/como-rodar.md](docs/como-rodar.md).

Documentação de deploy: [docs/DEPLOY.md](docs/DEPLOY.md).

## Migrations e versionamento do banco

O projeto utiliza Flyway para controlar as versões do banco de dados.

A partir da adoção do Flyway, o Hibernate não deve mais criar ou alterar tabelas automaticamente. O Hibernate deve apenas validar se as entidades JPA estão compatíveis com o schema existente no banco.

A configuração esperada é:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

As migrations ficam no diretório:

```text
app-financeiro-back-end/src/main/resources/db/migration
```

### Criando uma nova migration

Toda alteração estrutural no banco deve ser feita através de uma nova migration SQL.

O nome do arquivo deve seguir o padrão:

```text
V<numero>__descricao_da_migration.sql
```

Exemplos:

```text
V3__add_status_to_transacao.sql
V4__create_table_objetivo_financeiro.sql
V5__add_indice_data_transacao.sql
```

### Regras para alterações de schema

- Não utilizar `spring.jpa.hibernate.ddl-auto=update`
- Não alterar tabelas manualmente no banco de dados
- Não depender do Hibernate para criar tabelas, colunas, índices ou constraints
- Toda alteração de tabela, coluna, índice ou constraint deve possuir uma migration versionada
- Migrations já aplicadas não devem ser editadas depois de enviadas para o repositório

### Fluxo recomendado para novas alterações no banco

1. Criar uma nova migration em `app-financeiro-back-end/src/main/resources/db/migration`
2. Executar a aplicação localmente
3. Validar se o Flyway executou a migration com sucesso
4. Validar se o Hibernate iniciou corretamente com `ddl-auto=validate`
5. Conferir se a alteração não causa perda de dados em bancos já existentes

## Rodando os testes localmente

Os testes automatizados estão versionados no repositório e devem passar antes de qualquer merge — o pipeline de CI executa exatamente os mesmos comandos descritos abaixo a cada pull request.

Backend (JUnit + Spring Boot):

```bash
cd app-financeiro-back-end
./gradlew test
```

O relatório HTML fica em `app-financeiro-back-end/build/reports/tests/test/index.html` e o relatório de cobertura JaCoCo em `app-financeiro-back-end/build/reports/jacoco/test/html/index.html`.

Para rodar apenas uma classe específica:

```bash
./gradlew test --tests "bcd.appfinanceirobackend.RegistrarLoginTests"
```

Frontend (React + Vite):

```bash
cd app-financeiro-front-end
npm ci
npm run lint
npm test --if-present
```

O frontend deve passar pelo lint antes do merge. O pipeline de CI executa `npm run lint`, `npm run build` e `npm test --if-present`, garantindo que problemas de padronização e erros estáticos sejam detectados automaticamente.

## Integração contínua (CI)

O workflow `.github/workflows/ci.yml` é disparado em cada pull request (`opened`, `synchronize`, `reopened`) e roda quatro jobs: build e testes do backend com Postgres efêmero usando Gradle Wrapper, lint/build/testes do frontend, validação de YAML e verificação de arquivos obrigatórios.

Qualquer falha derruba o check do PR e impede o merge.

A proteção da branch `main` no GitHub já exige os quatro jobs (`Backend (build & test)`, `Frontend (build & test)`, `YAML syntax validation` e `Required files check`) como required status checks e exige ao menos 1 review aprovador, garantindo que nenhum PR seja mergeado com testes vermelhos.

---

## Documentos

- [Visão do Produto e MVP](docs/inception.md)
- [Arquitetura C4](docs/arquitetura.md)
- [Definition of Done](docs/dod.md) 
- [ADR-0001 — Stack Frontend](docs/adrs/ADR-0001-Tipo-de-Aplicacao.md) 
- [ADR-0002 — Stack Backend](docs/adrs/ADR-0002-Linguagem-e-Framework.md) 
- [ADR-0003 — Banco de Dados](docs/adrs/ADR-0003-Abordagem-de-persistência-de-dados.md) 
- [ADR-0004 — Arquitetura Geral](docs/adrs/ADR-0004-Estrategia-de-arquiteturas-e-camadas.md)
- [ADR-0005 — Padrões de Projeto](docs/adrs/ADR-0005-padroes-de-projeto.md)
- [ADR-0006 — Versionamento de Banco de Dados com Flyway](docs/adrs/ADR-0006-migracoes-de-banco-de-dados-com-flyway.md)
- [ADR-0007 — Bibliotecas de Leitura de Extratos](docs/adrs/ADR-0007-bibliotecas-leitura-extratos.md)
- [ADR-0008 — Decomposição do TransacaoService](docs/adrs/ADR-0008-decomposicao-transacao-service.md)
- [Board](https://github.com/orgs/IFSC-ES2/projects/20)
- [Backlog](https://github.com/IFSC-ES2/projeto-app_financeiro/issues)
- [Estimativas](docs/estimativas.md)
- [Métricas](docs/metricas.md)
- [Baseline](docs/baseline.md)
- [Como rodar](docs/como-rodar.md)
- [Deploy](docs/DEPLOY.md)
