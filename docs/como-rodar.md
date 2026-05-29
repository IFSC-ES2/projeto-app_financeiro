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

## 1. Banco de dados (via Docker)

A ideia aqui é não poluir a sua máquina com uma instalação local do PostgreSQL. O projeto utiliza um container que já vem com tudo configurado e expõe a porta `5432` no `localhost`, exatamente como o backend espera.

### Passo 1 — confirmar que o Docker está rodando

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

### Passo 2 — subir o container do PostgreSQL

Esse comando baixa a imagem do PostgreSQL 16, cria um container chamado `app-financeiro-db`, define usuário, senha e banco que o backend usa por padrão e mantém os dados persistidos em um volume chamado `app-financeiro-pgdata`.

Assim, os dados não somem se você derrubar o container.

```bash
docker run -d \
  --name app-financeiro-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=1234 \
  -e POSTGRES_DB=app_financeiro \
  -p 5432:5432 \
  -v app-financeiro-pgdata:/var/lib/postgresql/data \
  postgres:16
```

Na primeira vez ele vai baixar a imagem. Você pode confirmar que o container está rodando com:

```bash
docker ps
```

Deve aparecer uma linha do `app-financeiro-db` com a porta `0.0.0.0:5432->5432/tcp`.

### Passo 3 — testar a conexão (opcional)

Se quiser entrar no banco e dar uma olhada:

```bash
docker exec -it app-financeiro-db psql -U postgres -d app_financeiro
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

### Gerenciamento do container no dia a dia

| O que você quer | Comando |
|---|---|
| Parar o banco | `docker stop app-financeiro-db` |
| Voltar a subir | `docker start app-financeiro-db` |
| Ver logs | `docker logs -f app-financeiro-db` |
| Apagar o container, mantendo os dados | `docker rm -f app-financeiro-db` |
| Apagar tudo, inclusive os dados | `docker rm -f app-financeiro-db && docker volume rm app-financeiro-pgdata` |

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

Ao subir a aplicação em um banco vazio, o Flyway executará automaticamente as migrations existentes, como:

```text
V1__create_tables.sql
V2__seed_categorias.sql
V3__seed_usuarios.sql
```

Essas migrations criam a estrutura inicial do banco e inserem dados necessários para o funcionamento do sistema.

### Banco já existente

Para bancos criados em versões anteriores do projeto, antes da adoção do Flyway, o sistema utiliza:

```properties
spring.flyway.baseline-on-migrate=true
```

Essa configuração permite que o Flyway reconheça um banco já existente como uma versão inicial, evitando recriação de tabelas e perda de dados.

Mesmo com essa configuração, é necessário validar manualmente a subida da aplicação em um banco antigo já populado antes de considerar a migration finalizada.

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
V4__add_status_to_transacao.sql
V5__create_table_objetivo_financeiro.sql
```

Regras importantes:

- Não utilizar `spring.jpa.hibernate.ddl-auto=update`
- Não alterar tabelas manualmente no banco
- Não depender do Hibernate para criar tabelas, colunas, índices ou constraints
- Toda alteração de schema deve possuir uma migration versionada
- Migrations já aplicadas não devem ser editadas depois de enviadas ao repositório

### Usando credenciais diferentes

Se preferir outro usuário, senha ou nome de banco, troque as três variáveis `POSTGRES_*` no `docker run` e reflita as mesmas escolhas em `app-financeiro-back-end/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/app_financeiro
spring.datasource.username=postgres
spring.datasource.password=1234
```

## 3. Backend (Spring Boot)

Com o banco de dados rodando, suba o backend:

```bash
cd app-financeiro-back-end
./gradlew bootRun
```

- Porta: 8080
- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Ao iniciar, o backend executa as migrations pendentes do Flyway e depois o Hibernate valida se as entidades estão compatíveis com o schema do banco.

### Variáveis de ambiente opcionais

| Variável | Default | Para que serve |
|---|---|---|
| `JWT_SECRET` | chave de dev em `application.properties` | Segredo HMAC do JWT. Em produção, sobrescrever com no mínimo 32 bytes. |

Exemplo:

```bash
JWT_SECRET="$(openssl rand -hex 32)" ./gradlew bootRun
```

### Endpoints de autenticação

O backend expõe duas rotas em `/auth`:

- `POST /auth/register` — cria a conta e já devolve o token (`201`)
- `POST /auth/login` — autentica e devolve o token (`200`)

Erros mapeados:

- `400` — payload inválido, como CPF reprovado
- `409` — e-mail ou CPF já cadastrado
- `401` — credenciais inválidas

Teste rápido via `curl`:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Alexandre","email":"alex@test.com","cpf":"12345678909","senha":"123456"}'
```

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
- `/categorias` - lista todas as categorias do sistema (avalia o usuário logado para mostrar categorias de usuários)
- `/contas` - lista todas as contas do usuário autenticado
- `/contas/registrar` - tela de cadastro de conta para o usuário autenticado
- `/importacoes` - tela de importações de extratos bancarios e notas fiscais
- `/importacoes/{id}/status` - tela que verifica o status de uma importação, valida se ela pertence ao usuário autenticado
- `/transacoes/manual` - tela de registro manual de uma transação
- `/transacoes/{transacaoId}/categoria` - tela de categorização de uma transação que pertence ao usuário autenticado.

Mantenha o backend rodando em paralelo. O frontend faz chamadas para:

```text
http://localhost:8080
```

## 5. Comandos úteis

Backend:

```bash
./gradlew build
./gradlew test
./gradlew bootRun
```

Frontend:

```bash
npm run dev
npm run build
npm run preview
npm run lint
npm test
```

## 6. Problemas comuns

### Connection refused no boot do backend

O container do PostgreSQL provavelmente não está de pé.

Confira com:

```bash
docker ps
```

Se não aparecer, rode:

```bash
docker start app-financeiro-db
```

Ou refaça o comando `docker run` do passo 2.

### port is already allocated ao subir o container

Você já tem um PostgreSQL ocupando a porta `5432`, seja instalado na máquina ou em outro container.

Você pode parar o serviço que está usando a porta ou trocar o mapeamento para `5433`:

```bash
-p 5433:5432
```

Nesse caso, ajuste também a URL em `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/app_financeiro
```

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

Se mudar de porta, atualize a configuração de CORS no backend.

### JWT inválido logo após login

O `JWT_SECRET` pode ter mudado entre execuções.

Tokens emitidos antes deixam de valer quando o segredo muda.