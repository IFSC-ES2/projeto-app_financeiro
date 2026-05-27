# ADR-0007 — Documentação de API com Swagger / OpenAPI

## Status
Aceito

## Contexto
Para que a equipe encarregada do frontend (React) consiga consumir de forma eficiente e sem erros as rotas e endpoints criados no backend (Spring Boot), é fundamental ter uma documentação clara, atualizada e centralizada. A comunicação verbal ou por texto informal sobre os parâmetros e payloads das rotas estava gerando retrabalho.

## Decisão
A equipe decidiu adotar o **Swagger (OpenAPI 3)** através da biblioteca `springdoc-openapi-ui` para gerar automaticamente a documentação interativa da API do projeto.

## Alternativas consideradas
- Criação e manutenção manual de uma coleção no Postman ou Insomnia.
- Documentação manual escrita em arquivos Markdown no repositório ou páginas no Notion.

## Consequências
### Positivas
- Geração automatizada da documentação baseada nas próprias classes `Controller` do Spring Boot, reduzindo o esforço de manutenção.
- Interface gráfica amigável (Swagger UI) que permite testar as rotas e requisições diretamente pelo navegador sem ferramentas externas.
- Redução drástica de erros de integração entre o Frontend e o Backend.

### Negativas / trade-offs
- Inclusão de anotações adicionais no código java dos Controllers (como `@Operation`, `@ApiResponse`), o que pode deixar o código um pouco mais poluído visualmente.

## Revisão futura
A decisão será mantida por todo o desenvolvimento do projeto como padrão oficial de entrega das rotas do backend.