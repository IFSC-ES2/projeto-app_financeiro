# Como rodar a aplicação

Guia de execução local do projeto SmartBudget.

O projeto possui:

- Backend em Spring Boot
- Frontend em React + Vite
- Banco de dados PostgreSQL
- Autenticação com JWT
- Versionamento de schema com Flyway

## Pré-requisitos

- Java JDK 21
- Node.js 20 ou mais
- npm 10 ou mais
- Docker (Docker Desktop no Windows/Mac, ou Docker Engine no Linux)

> O Gradle Wrapper (`./gradlew`) já está versionado — não precisa instalar Gradle.
>
> Não precisa instalar PostgreSQL na máquina: ele vai rodar dentro de um container Docker.

## 1. Banco de dados (via Docker Compose)

A ideia aqui é não poluir a sua máquina com uma instalação local do PostgreSQL. O projeto utiliza o arquivo `docker-compose.yml`, localizado na raiz do repositório, para subir um container PostgreSQL já configurado com as credenciais esperadas pelo backend.

O banco fica disponível em `localhost:5432` e os dados são mantidos em um volume Docker, então eles não somem ao parar o container.

### Passo 1: confirmar que o Docker está rodando

Antes de qualquer coisa, abra o terminal e rode:

```bash
docker --version
docker info
```

Se o `docker info` reclamar que não consegue conectar no daemon, abra o Docker Desktop ou inicie o serviço com:

```bash
sudo systemctl start docker
```

Depois tente novamente.

### Passo 2: subir o PostgreSQL

Na raiz do projeto, execute:

```bash
docker compose up -d
```

Esse comando baixa a imagem do PostgreSQL 16, cria o container definido no `docker-compose.yml` e sobe o banco com as configurações usadas pelo backend:

- Banco: `app_financeiro`
- Usuário: `postgres`
- Senha local de desenvolvimento: `smartbudget-local-only` (ou `DEV_DB_PASSWORD`)
- Porta: `5432`
- Container: `smartbudget-postgres`

Essas credenciais são destinadas exclusivamente ao ambiente de desenvolvimento.

Você pode confirmar que o container está rodando com:

```bash
docker compose ps
```

O serviço `postgres` deve aparecer como iniciado. Após alguns segundos, o status de saúde deve ficar como `healthy`.

### Atualização de volumes locais anteriores à issue #299

A senha do PostgreSQL não é alterada automaticamente ao modificar o Docker Compose.

Caso já exista um volume do banco criado antes da implementação da issue #299, faça um backup e execute:

```bash
docker compose exec postgres psql -U postgres -d app_financeiro
```

Dentro do PostgreSQL, execute:

```text
\password postgres
```

Informe duas vezes a nova senha local:

```text
smartbudget-local-only
```

Caso tenha definido uma senha personalizada através da variável `DEV_DB_PASSWORD`, utilize essa senha.

Para sair:

```text
\q
```

Não é necessário apagar o volume ou perder os dados locais para alterar a senha.

### Passo 3: testar a conexão opcionalmente

Se quiser entrar no banco e dar uma olhada:

```bash
docker exec -it smartbudget-postgres psql -U postgres -d app_financeiro
```

Comandos úteis dentro do `psql`:

```text
\dt
```

Lista as tabelas existentes no banco.

```text
\q
```

Sai do `psql`.

As tabelas aparecerão depois que o backend subir e o Flyway executar as migrations.

### Gerenciamento do banco no dia a dia

| O que você quer | Comando |
|---|---|
| Subir o banco | `docker compose up -d` |
| Verificar o status | `docker compose ps` |
| Ver logs | `docker compose logs -f postgres` |
| Parar o banco | `docker compose down` |
| Parar e apagar os dados locais | `docker compose down -v` |

Atenção: o comando `docker compose down -v` remove o volume Docker e apaga os dados locais do banco.

## 2. Versionamento de banco de dados com Flyway

O projeto utiliza Flyway para versionamento e controle do schema do banco de dados.

As tabelas não são mais criadas automaticamente pelo Hibernate. Toda estrutura do banco é criada através das migrations localizadas em:

```text
app-financeiro-back-end/src/main/resources/db/migration
```

Ao iniciar a aplicação:

- O Flyway executa automaticamente as migrations pendentes
- O Hibernate apenas valida o schema existente
- O parâmetro utilizado é:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

### Banco vazio

Ao subir a aplicação em um banco vazio, o Flyway utilizará a baseline:

```text
B6__baseline_sem_usuarios_demo.sql
```

E executará as migrations posteriores, como:

```text
V7__remove_usuarios_demonstrativos.sql
```

A baseline cria a estrutura do banco e as categorias padrão, sem inserir usuários demonstrativos.

Novos usuários devem ser cadastrados normalmente através do sistema.

### Banco já existente

Em bancos que já possuem histórico de migrations, o Flyway executará apenas as migrations pendentes.

A migration V7 remove os usuários demonstrativos criados anteriormente pela V3, incluindo os dados associados a esses usuários.

Faça backup antes de atualizar um banco existente e confirme que os usuários demonstrativos não possuem dados reais que precisem ser preservados.

Para bancos locais criados antes da adoção do Flyway, o perfil de desenvolvimento mantém a configuração:

```properties
spring.flyway.baseline-on-migrate=true
```

Essa configuração permite que o Flyway reconheça um banco já existente como uma versão inicial.

Mesmo com essa configuração, é necessário validar manualmente a subida da aplicação em um banco antigo já populado antes de considerar a migration finalizada.

O perfil de produção não realiza automaticamente essa adoção de bancos sem histórico Flyway.

### Criando novas migrations

Toda alteração futura no schema do banco deve ser feita por uma nova migration SQL.

As migrations devem ser criadas em:

```text
app-financeiro-back-end/src/main/resources/db/migration
```

O nome do arquivo deve seguir o padrão:

```text
V<numero>__descricao_da_migration.sql
```

Exemplos:

```text
V8__add_status_to_transacao.sql
V9__create_table_objetivo_financeiro.sql
```

Os exemplos acima são ilustrativos. Sempre utilize a próxima versão disponível no repositório.

Regras importantes:

- Não utilizar `spring.jpa.hibernate.ddl-auto=update`
- Não alterar tabelas manualmente no banco
- Não depender do Hibernate para criar tabelas, colunas, índices ou constraints
- Toda alteração de schema deve possuir uma migration versionada
- Migrations já aplicadas não devem ser editadas depois de enviadas ao repositório
- Não inserir usuários demonstrativos ou credenciais conhecidas nas migrations compartilhadas com produção

## 3. Backend (Spring Boot)

### Perfis de execução

O backend utiliza três perfis:

| Perfil | Finalidade |
|---|---|
| `dev` | Desenvolvimento local |
| `test` | Testes automatizados |
| `prod` | Ambiente de produção |

O perfil padrão é `prod`.

Caso nenhuma configuração de ambiente seja informada, a aplicação assumirá o perfil de produção e exigirá as credenciais obrigatórias.

Para executar localmente, utilize explicitamente o perfil `dev`.

Com o banco de dados rodando, suba o backend:

```bash
cd app-financeiro-back-end
./gradlew bootRun --args='--spring.profiles.active=dev'
```

- Porta: 8080
- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Ao iniciar, o backend executa as migrations pendentes do Flyway e depois o Hibernate valida se as entidades estão compatíveis com o schema do banco.

### Variáveis de ambiente opcionais

| Variável | Default | Para que serve |
|---|---|---|
| `DEV_DB_PASSWORD` | `smartbudget-local-only` | Senha exclusiva do banco de desenvolvimento |
| `JWT_SECRET` | Chave local em `application-dev.properties` | Segredo HMAC utilizado na assinatura dos tokens JWT |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/app_financeiro` | URL de conexão com o PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuário do banco local |
| `SPRING_DATASOURCE_PASSWORD` | Valor de `DEV_DB_PASSWORD` ou senha local padrão | Senha utilizada pelo backend |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Origens permitidas nas requisições ao backend |

Exemplo utilizando um segredo JWT personalizado:

```bash
JWT_SECRET="$(openssl rand -hex 32)" \
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Caso utilize uma senha personalizada no PostgreSQL, a mesma senha deve estar configurada no Docker Compose e no ambiente do backend.

O Spring Boot não carrega automaticamente o arquivo `.env` utilizado pelo Docker Compose. Exporte as variáveis necessárias no terminal antes de iniciar o backend.

Nunca reutilize credenciais de produção no ambiente local.

Para a configuração do ambiente de produção, consulte [DEPLOY.md](./DEPLOY.md).

### Endpoints de autenticação

O backend expõe duas rotas em `/auth`:

- `POST /auth/register` — cria a conta e já devolve o token (`201`)
- `POST /auth/login` — autentica e devolve o token (`200`)

Erros mapeados:

- `400` — payload inválido, como CPF reprovado
- `409` — e-mail ou CPF já cadastrado
- `401` — credenciais inválidas

Teste rápido via `curl` em ambiente local:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Alexandre","email":"alex@test.com","cpf":"12345678909","senha":"123456"}'
```

Os valores acima são exclusivamente ilustrativos para testes locais.

## 4. Frontend (React + Vite)

Em outro terminal, suba o frontend:

```bash
cd app-financeiro-front-end
npm install
npm run dev
```

- Porta: 5173 (default do Vite)
- URL: `http://localhost:5173`

### Rotas disponíveis

- `/login` - tela de login
- `/register` - tela de cadastro com máscara de CPF e validação no submit
- `/contas/registrar` - tela de cadastro de conta para o usuário autenticado

Mantenha o backend rodando em paralelo. O frontend faz chamadas para:

```text
http://localhost:8080
```

## 5. Comandos úteis

Backend:

```bash
./gradlew build
./gradlew test
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Frontend:

```bash
npm run dev
npm run build
npm run preview
npm run lint
npm test
npm run test:coverage
```

### Testes de configuração de segurança

Para executar especificamente os testes relacionados à segurança das credenciais e dos ambientes:

```bash
cd app-financeiro-back-end

./gradlew test \
  --tests "*ConfiguracaoProducaoTest" \
  --tests "*JwtUtilTest" \
  --tests "*UsuariosDemoMigrationIntegrationTests"
```

Os testes de integração utilizam PostgreSQL através do Testcontainers.

Certifique-se de que o Docker esteja em execução antes de rodar os testes de integração.

### Cobertura de testes do frontend

Para gerar o relatório de cobertura com Vitest (provider `v8`):

```bash
cd app-financeiro-front-end
npm run test:coverage
```

O resumo aparece no terminal. O relatório detalhado fica em:

```text
app-financeiro-front-end/coverage/index.html
```

No CI, o job **Frontend** executa apenas `npm run test:coverage` (sem duplicar `npm test`), e imprime o resumo a partir de `coverage/coverage-summary.json`, no mesmo espírito do JaCoCo do backend.

## 6. Problemas comuns

### Connection refused no boot do backend

O container do PostgreSQL provavelmente não está em execução ou ainda não terminou de inicializar.

Confira o status com:

```bash
docker compose ps
```

Se o serviço `postgres` não estiver rodando, suba o banco novamente pela raiz do projeto:

```bash
docker compose up -d
```

Se o container estiver rodando, mas ainda aparecer erro de conexão, aguarde alguns segundos e verifique os logs:

```bash
docker compose logs -f postgres
```

O backend deve conseguir conectar quando o PostgreSQL estiver pronto para aceitar conexões na porta `5432`.

### port is already allocated ao subir o container

Você já tem um PostgreSQL ocupando a porta `5432`, seja instalado na máquina ou em outro container.

Você pode parar o serviço que está usando a porta ou trocar o mapeamento no `docker-compose.yml` para:

```yaml
ports:
  - "127.0.0.1:5433:5432"
```

Nesse caso, informe a URL correspondente ao executar o backend:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/app_financeiro \
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Erro de autenticação no PostgreSQL

Se o PostgreSQL apresentar erro de autenticação após a atualização da issue #299, verifique se o volume existente ainda utiliza a senha anterior.

A alteração da variável `POSTGRES_PASSWORD` do Docker Compose não modifica automaticamente a senha de um usuário em um volume já inicializado.

Siga as instruções da seção de atualização de volumes locais.

### Configuração obrigatória ausente

Caso o backend apresente erros indicando ausência de configurações obrigatórias de produção, confirme se está executando com o perfil correto.

Para desenvolvimento local:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Não configure credenciais fictícias para contornar as validações do perfil `prod`.

### Erro de validação do Hibernate ao iniciar

Como o projeto usa:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

O Hibernate não cria nem altera tabelas automaticamente.

Se aparecer erro informando tabela, coluna ou constraint ausente, verifique:

- Se as migrations do Flyway foram executadas
- Se o banco usado está correto
- Se existe alguma alteração de entidade JPA sem migration correspondente
- Se a migration foi criada no diretório correto

```text
app-financeiro-back-end/src/main/resources/db/migration
```

### Erro relacionado ao Flyway

Se o Flyway falhar ao iniciar, verifique:

- Se o banco está acessível
- Se existe erro de SQL em alguma migration
- Se a ordem das versões está correta
- Se alguma migration já aplicada foi editada depois de executada

Migrations já aplicadas não devem ser modificadas. Para corrigir uma alteração de schema, crie uma nova migration com uma nova versão.

### CORS ao chamar do frontend

Confirme que:

- O backend está rodando em `8080`
- O frontend está rodando em `5173`

Se mudar de porta, atualize a configuração `CORS_ALLOWED_ORIGINS` no backend.

### JWT inválido logo após login

O `JWT_SECRET` pode ter mudado entre execuções.

Tokens emitidos antes deixam de valer quando o segredo muda.

Nesse caso, faça login novamente para obter um novo token.