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

### Backend (perfil `prod`)

| Variável | Obrigatoriedade | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` no deploy | Sem perfil explícito, o backend assume `prod`. |
| `PORT` | Opcional (`8080`) | Porta HTTP. |
| `SPRING_DATASOURCE_URL` | Obrigatória ou alternativa abaixo | URL JDBC PostgreSQL (host, porta e banco). |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Alternativa à URL JDBC | Render injeta os três valores. |
| `SPRING_DATASOURCE_USERNAME` | Obrigatória | Usuário dedicado, diferente de `postgres`. |
| `SPRING_DATASOURCE_PASSWORD` | Obrigatória | Senha exclusiva de produção com pelo menos 16 caracteres. |
| `JWT_SECRET` | Obrigatória | Gere com `openssl rand -hex 32` (256 bits de entropia, 64 caracteres hexadecimais). |
| `CORS_ALLOWED_ORIGINS` | Obrigatória | Origens reais do frontend, separadas por vírgula. |

A aplicação falha antes da criação do DataSource e do JWT quando as configurações obrigatórias estão ausentes ou inválidas. Nunca combine perfis. `dev` utiliza valores exclusivamente locais, e `test` utiliza fixtures e Testcontainers isolados. Não reutilize credenciais entre ambientes.

### Frontend (build-time, Vite)

| Variável        | Default                 | Descrição |
|-----------------|-------------------------|-----------|
| `VITE_API_URL`  | `http://localhost:8080` | URL pública da API consumida pelo frontend. |

> Não há fallback para credenciais de produção. Use o perfil `dev` somente em sua máquina.

## Opção A — Docker Compose (recomendado)

Sobe banco + backend + frontend em containers, com migrations Flyway aplicadas automaticamente.

```bash
# 1. Configure as variáveis (não versionado)
cp .env.prod.example .env
# Edite .env: defina POSTGRES_USER, POSTGRES_PASSWORD,
# JWT_SECRET, CORS_ALLOWED_ORIGINS e VITE_API_URL

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

Se o volume de produção já foi inicializado com o usuário padrão `postgres`, não basta mudar `POSTGRES_USER` no `.env`: crie um usuário dedicado e transfira as permissões e a propriedade dos objetos com um administrador autorizado, ou restaure um backup em ambiente novo. Valide antes de desativar a credencial antiga.

Encerrar:

```bash
docker compose -f docker-compose.prod.yml down
```

O comando abaixo também apaga o volume do banco e deve ser utilizado apenas em ambientes descartáveis:

```bash
docker compose -f docker-compose.prod.yml down -v
```

## Opção B — Build e execução manual

### Banco

Utilize um PostgreSQL próprio, configurado com um usuário e uma senha exclusivos de produção, e exporte as variáveis de conexão apontando para ele.

As migrations Flyway rodam automaticamente no boot do backend (`spring.jpa.hibernate.ddl-auto=validate`; o Hibernate apenas valida o schema criado pelo Flyway).

### Backend

```bash
cd app-financeiro-back-end
./gradlew bootJar

export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL="jdbc:postgresql://<host>:5432/app_financeiro"
export SPRING_DATASOURCE_USERNAME="<usuario>"
export SPRING_DATASOURCE_PASSWORD="<senha>"
export JWT_SECRET="$(openssl rand -hex 32)"
export CORS_ALLOWED_ORIGINS="https://<dominio-do-frontend>"

java -jar build/libs/app-financeiro-back-end-*.jar
```

Os segredos devem ser fornecidos por meio de um gerenciador de segredos ou de outro mecanismo seguro do ambiente. Não coloque valores reais em comandos versionados ou no histórico do terminal.

### Frontend

```bash
cd app-financeiro-front-end
npm ci
VITE_API_URL="https://<dominio-da-api>" npm run build
```

Sirva o conteúdo de `dist/` em qualquer servidor de estáticos (nginx, Vercel, Netlify, etc.). Para SPA, configure o fallback de rotas para `index.html` (veja `app-financeiro-front-end/nginx.conf`).

## Validação do ambiente

Execute as verificações de cadastro abaixo em um ambiente isolado de validação, nunca contra uma produção com dados reais.

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

# 3. Endpoint autenticado (use o token do passo 1/2)
# Retorna 200
curl -s -o /dev/null -w "%{http_code}\n" $API/categorias \
  -H "Authorization: Bearer <TOKEN>"

# 4. Frontend servido
# Retorna 200, inclusive em rotas da SPA
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8081/login
```

Pela interface: acesse o frontend, **cadastre uma conta** (`/register`), faça login e valide os fluxos do MVP (contas, transações, listagem com filtros, categorização).

> Bancos novos não recebem usuários demonstrativos. Cadastre um usuário próprio com dados fictícios e senha exclusiva em um ambiente de validação, não em produção real.

## Segurança

- Nunca commite `.env`, segredos, backups ou valores reais. Os arquivos `*.example` são modelos sem segredos reais.
- Configure `prod` no Render e no Compose de produção.
- Restrinja o CORS ao frontend real.
- Gere o JWT com `openssl rand -hex 32`.
- Gere a senha do banco separadamente com `openssl rand -hex 24`.
- Guarde os valores no gerenciador de segredos do ambiente, não em tickets ou logs.
- Utilize HTTPS em ambiente publicado.
- O Compose de validação local com HTTP não substitui a configuração HTTPS pública.

### Render

1. No painel do backend, confirme `SPRING_PROFILES_ACTIVE=prod`, a conexão do banco e as origens CORS reais. O Blueprint mantém a conexão por `fromDatabase`.
2. Gere e informe manualmente um novo `JWT_SECRET` de 64 caracteres hexadecimais. O `sync: false` evita versionar a chave; um serviço existente deve ser atualizado no painel.
3. Confirme que o usuário e a senha do banco são exclusivos e atendem à validação. Não altere credenciais gerenciadas pelo provedor sem coordenar a atualização do serviço.
4. Faça backup seguro do banco, rode o deploy e confirme que `/health` e os fluxos autenticados funcionam. Não exponha os valores em screenshots.

### Migração dos usuários de demonstração (#299)

- Preserve V1 a V6 exatamente como foram aplicadas.
- A baseline B6 cria bancos vazios sem executar o seed V3.
- V7 remove os dois IDs reservados do seed em bancos existentes.
- V7 exclui dados vinculados às contas demo, inclusive transações, cartões, faturas, importações e categorias personalizadas.
- Antes do deploy, confirme que esses IDs são demonstrativos e que não há dados reais a preservar; faça backup protegido.
- Se houver dados reais, suspenda a implantação e planeje a transferência autorizada dos dados.
- A remoção vale também para ambientes locais antigos.
- Não são inseridos novos usuários demo em nenhum perfil.

Após a migration, execute:

```sql
SELECT id
FROM usuario
WHERE id IN (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222'
);
```

O resultado esperado é zero registros.

Confirme também que os usuários reais permanecem no banco.

Produção não adota automaticamente schemas sem histórico Flyway. Se existir um banco legado sem histórico, valide-o e planeje a adoção manual antes do deploy.

### Rotação de credenciais

1. Identifique o ambiente e as credenciais afetadas. Faça backup protegido e programe janela de manutenção quando houver risco de indisponibilidade.

2. Gere um novo segredo JWT com `openssl rand -hex 32` e uma nova senha de banco independente.

   No Render, use as opções de gerenciamento/rotação do banco disponíveis ao serviço.

   No PostgreSQL administrado pela equipe, altere a senha do usuário utilizando `\password` no `psql` por uma sessão autorizada.

3. Atualize as variáveis do backend no gerenciador de segredos.

   No Compose, altere o `.env` e recrie o serviço. Mudar somente `POSTGRES_PASSWORD` em um volume existente **não** muda a senha do usuário no banco.

4. Reinicie/reimplante todas as instâncias do backend.

   A troca do JWT invalida todos os tokens antigos; avise os usuários para entrar novamente.

   Revogue o acesso com a credencial anterior.

5. Valide `/health`, login com conta de teste autorizada, acesso autenticado, rejeição de tokens antigos e ausência de usuários demo.

   Não registre segredos em logs, commits ou evidências.

6. Se houver exposição no Git, trate o segredo como comprometido: revogue e substitua antes de avaliar a limpeza do histórico.

   Examine todas as referências, PRs e branches com scanner de segredos (por exemplo, Gitleaks).

   Coordene com a equipe e não confunda remoção de arquivo com revogação.

   Registre apenas a evidência sanitizada da rotação.

### Auditoria de credenciais versionadas

Para verificar o histórico do repositório, utilize uma ferramenta de detecção de segredos, como o Gitleaks.

Na raiz do repositório:

```bash
gitleaks git --redact --log-opts="--all"
```

Caso sejam encontradas credenciais reais:

1. Identifique o ambiente afetado.
2. Revogue as credenciais comprometidas.
3. Gere novas credenciais.
4. Atualize o ambiente correspondente.
5. Valide o funcionamento da aplicação.
6. Avalie com a equipe a necessidade de limpeza do histórico do Git.

A verificação do histórico e a rotação de valores reais devem ser feitas por quem administra cada ambiente, sem publicar seus valores.

## Troubleshooting

Cenários comuns (Connection refused, porta `5432` ocupada, erro de validação do Hibernate/Flyway, CORS, JWT inválido após login) estão documentados em [`como-rodar.md`](./como-rodar.md#6-problemas-comuns).