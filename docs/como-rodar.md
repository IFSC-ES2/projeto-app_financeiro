# Como rodar a aplicação

Guia de execução local após a branch `feat/tela-de-cadstro`, que adiciona cadastro/login com JWT, validação de CPF (front + back) e a rota `/register` no SPA.

## Pré-requisitos

Java JDK 21
Node.js 20 ou mais
npm 10 ou mais
Docker (Docker Desktop no Windows/Mac, ou Docker Engine no Linux)

> O Gradle Wrapper (`./gradlew`) já está versionado — não precisa instalar Gradle.
> Não precisa instalar PostgreSQL na máquina: ele vai rodar dentro de um container.

## 1. Banco de dados (via Docker)

A ideia aqui é não poluir a sua máquina com uma instalação do Postgres: a gente sobe um container que já vem com tudo configurado e expõe a porta `5432` no `localhost`, exatamente como o backend espera.

### Passo 1 — confirmar que o Docker está rodando

Antes de qualquer coisa, abra o terminal e rode:

```bash
docker --version
docker info
```

Se o `docker info` reclamar que não consegue conectar no daemon, abra o Docker Desktop (ou inicie o serviço com `sudo systemctl start docker` no Linux) e tente de novo.

### Passo 2 — subir o container do Postgres

Esse comando baixa a imagem do Postgres 16, cria um container chamado `app-financeiro-db`, define usuário/senha/banco que o backend usa por padrão e mantém os dados persistidos num volume chamado `app-financeiro-pgdata` (assim os dados não somem se você derrubar o container):

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

Na primeira vez ele vai baixar a imagem (alguns segundos). Você pode confirmar que está de pé com:

```bash
docker ps
```

Deve aparecer uma linha do `app-financeiro-db` com a porta `0.0.0.0:5432->5432/tcp`.

### Passo 3 — testar a conexão (opcional)

Se quiser entrar no banco e dar uma olhada:

```bash
docker exec -it app-financeiro-db psql -U postgres -d app_financeiro
```

Comandos úteis dentro do `psql`: `\dt` lista tabelas (depois que o backend subir e o Hibernate criar elas), `\q` sai.

### Gerenciamento do container no dia a dia

| O que você quer | Comando |
|---|---|
| Parar o banco | `docker stop app-financeiro-db` |
| Voltar a subir | `docker start app-financeiro-db` |
| Ver logs | `docker logs -f app-financeiro-db` |
| Apagar o container (mantém os dados) | `docker rm -f app-financeiro-db` |
| Apagar TUDO, inclusive os dados | `docker rm -f app-financeiro-db && docker volume rm app-financeiro-pgdata` |

As tabelas (incluindo `usuario`, usada pelo cadastro) são criadas automaticamente pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update`) na primeira vez que o backend subir.

### Usando credenciais diferentes

Se preferir outro usuário/senha/nome de banco, troque as três variáveis `POSTGRES_*` no `docker run` e reflita as mesmas escolhas em `app-financeiro-back-end/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/app_financeiro
spring.datasource.username=postgres
spring.datasource.password=1234
```

## 2. Backend (Spring Boot)

```bash
cd app-financeiro-back-end
./gradlew bootRun
```

- Porta: 8080
- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Variáveis de ambiente opcionais

| Variável | Default | Para que serve                                                                        |
|---|---|---------------------------------------------------------------------------------------|
| `JWT_SECRET` | chave de dev em `application.properties` | importante-->> segredo HMAC do JWT — em produção, sobrescrever com no mínimo 32 bytes |

Exemplo:

```bash
JWT_SECRET="$(openssl rand -hex 32)" ./gradlew bootRun
```

### Endpoints de autenticação

Esta branch expõe duas rotas em `/auth`:

- `POST /auth/register` — cria a conta e já devolve o token (`201`)
- `POST /auth/login` — autentica e devolve o token (`200`)

Erros mapeados: `400` payload inválido (ex.: CPF reprovado), `409` e-mail/CPF já cadastrado, `401` credenciais inválidas.

Teste rápido via `curl`:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Alexandre","email":"alex@test.com","cpf":"12345678909","senha":"123456"}'
```

## 3. Frontend (React + Vite)

```bash
cd app-financeiro-front-end
npm install
npm run dev
```

- Porta: 5173 (default do Vite)
- URL: `http://localhost:5173`

### Rotas disponíveis

- `/login` — tela de login (lê `accessToken` do backend e tem link para cadastro)
- `/register` — tela de cadastro com máscara de CPF e validação no submit

Mantenha o backend rodando em paralelo: o front faz as chamadas para `http://localhost:8080`.

## 4. Comandos úteis

Backend:
```bash
./gradlew build          # compila + roda testes
./gradlew test           # apenas testes
./gradlew bootRun        # sobe a aplicação
```

Frontend:
```bash
npm run dev              # dev server com HMR
npm run build            # build de produção (tsc + vite build)
npm run preview          # serve o build
npm run lint             # ESLint
```

## 5. Problemas comuns

- Connection refused no boot do backend - o container do Postgres não está de pé. Confira com `docker ps`; se não aparecer, rode `docker start app-financeiro-db` (ou refaça o `docker run` do passo 2).
- port is already allocated ao subir o container - você já tem um Postgres ocupando a `5432` (instalado na máquina ou outro container). Pare o que estiver usando a porta ou troque para `-p 5433:5432` e ajuste a URL em `application.properties`.
- relation "usuario" does not exist - o backend ainda não subiu uma vez para o Hibernate criar as tabelas. Sobe o backend e tenta de novo.
- CORS ao chamar do front - confirme que o backend está em `8080` e o front em `5173`; se mudar de porta, atualize a `SecurityConfig`/CORS.
- JWT inválido logo após login - `JWT_SECRET` mudou entre execuções; tokens emitidos antes deixam de valer.
