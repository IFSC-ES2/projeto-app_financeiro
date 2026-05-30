# SmartBudget front-end pós-login

## O que foi implementado

A pasta `src` contém a área privada pós-login em React, Vite e TypeScript, mantendo o login como primeira tela pública da aplicação.

Rotas implementadas:

- `/login`: tela pública de login existente, preservada.
- `/cadastro`: tela pública de cadastro existente, preservada.
- `/contas/nova`: cadastro da primeira conta bancária, protegido por autenticação.
- `/dashboard`: primeira seção após login, exibindo `Em construção.`.
- `/transacoes`: seção funcional de transações, com filtros visuais e resumo.
- `/transacoes/nova`: formulário protegido para criação de transação manual.
- `/categorias`: seção protegida exibindo `Em construção.`.
- `/parcelamentos`: seção protegida exibindo `Em construção.`.

## Menu lateral

O layout privado permite recolher e expandir o menu lateral em telas grandes. A própria logo do SmartBudget alterna entre os dois estados. Em telas menores, o menu fica oculto por padrão e pode ser aberto pelo botão no topo da página.

## Rotas privadas e autenticação

As rotas privadas passam pelo componente `RotaPrivada`. Ele valida o estado de autenticação em memória antes de renderizar a área interna. Usuários sem sessão autenticada no fluxo atual da aplicação são redirecionados para `/login`.

A rota `/` sempre redireciona para `/login`. As rotas públicas não redirecionam automaticamente para a área privada só porque existe algum token antigo no navegador. Isso evita que a aplicação abra diretamente na tela pós-login ao iniciar o front-end.

A autenticação respeita o fluxo atual do backend:

- `POST /auth/login` retorna `TokenDTO`.
- `POST /auth/register` retorna `TokenDTO`.
- O token JWT é enviado no header `Authorization: Bearer <token>` pelo interceptor do Axios.
- O token é mantido em `sessionStorage` somente depois de login ou cadastro bem-sucedido.
- Ao recarregar a página com F5, uma sessão válida em `sessionStorage` é reutilizada na mesma aba.
- Ao abrir a aplicação diretamente em `/`, a rota inicial continua redirecionando para `/login`.
- Tokens antigos em `localStorage.token` são removidos e não autenticam o usuário.
- Em respostas `401` ou `403`, a sessão local é removida e a área privada deixa de ficar acessível.

Nenhum token, senha ou CPF é enviado para console, URL ou mensagens de erro exibidas ao usuário.

## Layout pós-login

Foi criado o componente `LayoutPrivado`, com:

- navegação entre Dashboard, Transações, Categorias e Parcelamentos;
- indicação visual da seção ativa;
- tema claro baseado na paleta existente do projeto;
- perfil circular no canto superior direito;
- dropdown de perfil com opção `Sair`;
- encerramento local da sessão ao sair e redirecionamento para `/login`.

## Endpoints usados

Endpoints integrados no front-end:

- `POST /auth/login`
- `POST /auth/register`
- `GET /contas`
- `POST /contas/registrar`
- `GET /categorias`
- `GET /transacoes`
- `POST /transacoes/manual`

A URL base permanece `http://localhost:8080`, centralizada em `src/services/api.ts`.

## Transações

A seção `/transacoes` contém:

- estado de carregamento;
- estado vazio;
- tratamento de erro crítico de API;
- carregamento real por `GET /transacoes`;
- filtros por período, tipo, conta e categoria;
- cards de resumo com total, receitas, despesas e saldo;
- tabela com descrição, categoria, conta, data e valor formatados.

Os filtros são funcionais no front-end e aplicam-se sobre a lista retornada por `GET /transacoes`. O endpoint atual não informa suporte a query params para período, tipo, conta ou categoria, então a filtragem é feita no cliente sobre os dados autenticados já retornados pelo backend.

A tela não exibe banners amarelos de aviso para falhas não críticas de enriquecimento da listagem, como indisponibilidade de contas ou categorias. Quando esses dados não estão disponíveis, a interface permanece limpa e mostra a listagem possível ou o estado vazio.

## Criação de nova transação

A rota `/transacoes/nova` usa o endpoint real:

```http
POST /transacoes/manual
```

Campos enviados conforme contrato atual:

- `valor`
- `data`
- `descricao`
- `tipoTransacao`
- `formaPagamento`
- `categoriaId`
- `contaId`

Validações no front-end:

- valor obrigatório e maior que zero;
- data obrigatória;
- conta obrigatória quando a forma de pagamento não for `DINHEIRO`.

Após sucesso, o usuário é redirecionado para `/transacoes`. A listagem é recarregada por `GET /transacoes`, refletindo o histórico retornado pelo backend.

## Redirecionamento após cadastro

Após cadastro de usuário, o usuário continua autenticado porque o backend retorna `TokenDTO`. O fluxo redireciona para `/contas/nova`.

Após cadastro da conta bancária, o usuário é redirecionado para `/dashboard`, sem encerrar a sessão.

## Limitações documentadas do backend atual

### Categoria em transação manual

O DTO de request aceita `categoriaId`, mas o serviço de registro manual observado não associa a categoria à transação antes de salvar.

Impacto:

- a tela envia `categoriaId` quando selecionado;
- o backend pode retornar `categoriaId` nulo mesmo após seleção no formulário.

Necessário no backend:

- validar se a categoria é padrão ou pertence ao usuário autenticado;
- associar a categoria à transação no registro manual;
- retornar o `categoriaId` persistido em `TransacaoResponseDTO`.

Fallback implementado:

- o front-end não força a categoria selecionada na resposta;
- a listagem mostra `Sem categoria` quando o backend retorna nulo.

## Validação recomendada

Na pasta do front-end:

```bash
npm install
npm run build
npm run lint
npm test
```

Para integração manual, subir o backend em `localhost:8080` e acessar o front-end em `localhost:5173`.
