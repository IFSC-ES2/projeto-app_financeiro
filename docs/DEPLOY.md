# Deploy — SmartBudget

Guia de **deploy / execução reprodutível** do MVP (backend + frontend + banco).

Para desenvolvimento local (rodar com `bootRun`/`npm run dev`), veja [`como-rodar.md`](./como-rodar.md). Este documento foca em publicar o sistema de forma parametrizável por variáveis de ambiente.

## Visão geral

| Serviço  | Stack                     | Porta (default) |
|----------|---------------------------|-----------------|
| Frontend | React + Vite (nginx)      | `8081` → `80`   |
| Backend  | Spring Boot (Java 21)     | `8080`          |
| Banco    | PostgreSQL 16 + Flyway    | `5432`          |

O frontend é uma SPA estática que chama a API pelo **navegador**, então a URL da API (`VITE_API_URL`) precisa ser acessível pelo cliente e é **embutida no build** do Vite.

Ambientes publicados atuais:

- Web: `https://smartbudget-web-0sic.onrender.com`
- API: `https://smartbudget-api-kbze.onrender.com`

## Pré-requisitos

- **Docker** + **Docker Compose v2** (caminho recomendado), ou
- **Java JDK 21**, **Node.js 20+** e **PostgreSQL 16** (build/execução manual).

## Variáveis de ambiente

### Backend (`application.properties`)

| Variável                     | Default                                          | Descrição |
|------------------------------|--------------------------------------------------|-----------|
| `PORT`                       | `8080`                                           | Porta HTTP do backend. |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://localhost:5432/app_financeiro`| URL JDBC do PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` | `postgres`                                       | Usuário do banco. |
| `SPRING_DATASOURCE_PASSWORD` | `1234`                                           | Senha do banco. |
| `JWT_SECRET`                 | chave de dev (apenas local)                      | Segredo HMAC do JWT. **Em produção use ≥ 32 bytes.** |
| `CORS_ALLOWED_ORIGINS`       | `http://localhost:5173`                          | Origem(ns) do frontend permitida(s) no CORS, separadas por vírgula. |

### Frontend (build-time, Vite)

| Variável        | Default                 | Descrição |
|-----------------|-------------------------|-----------|
| `VITE_API_URL`  | `http://localhost:8080` | URL pública da API consumida pelo frontend. |

> Os defaults preservam a execução local. Em produção, **sempre** sobrescreva `JWT_SECRET`, as credenciais do banco e o `CORS_ALLOWED_ORIGINS`.

## Opção A — Docker Compose (recomendado)

Sobe banco + backend + frontend em containers, com migrations Flyway aplicadas automaticamente.

```bash
# 1. Configure as variáveis (não versionado)
cp .env.prod.example .env
#    edite .env: defina POSTGRES_PASSWORD, JWT_SECRET (>=32 bytes), CORS_ALLOWED_ORIGINS, VITE_API_URL

# 2. Suba a stack (build das imagens incluído)
docker compose -f docker-compose.prod.yml up -d --build

# 3. Acompanhe os logs do backend (migrations + boot)
docker compose -f docker-compose.prod.yml logs -f backend
```

Acesso:

- Frontend: `http://localhost:8081`
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Gere um `JWT_SECRET` forte com:

```bash
openssl rand -hex 32
```

Encerrar:

```bash
docker compose -f docker-compose.prod.yml down      # mantém os dados
docker compose -f docker-compose.prod.yml down -v   # apaga o volume do banco
```

## Opção B — Build e execução manual

### Banco

Use o `docker-compose.yml` da raiz (Postgres local) ou um PostgreSQL próprio, e exporte as variáveis de conexão apontando para ele. As migrations Flyway rodam automaticamente no boot do backend (`spring.jpa.hibernate.ddl-auto=validate`; o Hibernate apenas valida o schema criado pelo Flyway).

### Backend

```bash
cd app-financeiro-back-end
./gradlew bootJar                       # gera build/libs/app-financeiro-back-end-*.jar

export SPRING_DATASOURCE_URL="jdbc:postgresql://<host>:5432/app_financeiro"
export SPRING_DATASOURCE_USERNAME="<usuario>"
export SPRING_DATASOURCE_PASSWORD="<senha>"
export JWT_SECRET="$(openssl rand -hex 32)"
export CORS_ALLOWED_ORIGINS="https://<dominio-do-frontend>"
java -jar build/libs/app-financeiro-back-end-*.jar
```

### Frontend

```bash
cd app-financeiro-front-end
npm ci
VITE_API_URL="https://<dominio-da-api>" npm run build   # gera dist/
```

Sirva o conteúdo de `dist/` em qualquer servidor de estáticos (nginx, Vercel, Netlify, etc.). Para SPA, configure o fallback de rotas para `index.html` (veja `app-financeiro-front-end/nginx.conf`).

## Validação do ambiente

Substitua as portas se necessário. Com a stack no ar:

```bash
API=http://localhost:8080

# 1. Cria um usuário de teste (retorna 201 + token)
curl -s -X POST $API/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Validacao","email":"validacao@teste.com","cpf":"52998224725","senha":"123456"}'

# 2. Login (retorna 200 + token)
curl -s -X POST $API/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"validacao@teste.com","senha":"123456"}'

# 3. Endpoint autenticado (use o token do passo 1/2) — retorna 200
curl -s -o /dev/null -w "%{http_code}\n" $API/categorias -H "Authorization: Bearer <TOKEN>"

# 4. Frontend servido (retorna 200, inclusive em rotas da SPA)
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8081/login
```

Pela interface: acesse o frontend, **cadastre uma conta** (`/register`), faça login e valide os fluxos do MVP (contas, transações, listagem com filtros, categorização).

> O banco já vem com usuários demo do seed (`demo@smartbudget.com`, `teste@smartbudget.com`). Para validação, prefira **registrar um novo usuário** pela tela/`/auth/register`. Os CPFs `12345678909` e `11144477735` já estão em uso pelo seed.

## Segurança

- **Nunca** commite o arquivo `.env` — ele está no `.gitignore` (apenas `*.example` são versionados).
- Defina um `JWT_SECRET` forte e exclusivo do ambiente (≥ 32 bytes). Trocar o segredo invalida tokens já emitidos.
- Use senhas de banco fortes; não reutilize as senhas de exemplo (`1234`, `smoketest123`).
- Restrinja `CORS_ALLOWED_ORIGINS` ao(s) domínio(s) reais do frontend.

## Troubleshooting

Cenários comuns (Connection refused, porta `5432` ocupada, erro de validação do Hibernate/Flyway, CORS, JWT inválido após login) estão documentados em [`como-rodar.md`](./como-rodar.md#6-problemas-comuns).
