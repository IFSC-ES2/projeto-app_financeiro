# ADR-0008 — Decomposição do `TransacaoService` (Sprint 4)

## Status
Aceito

## Contexto
Na Sprint 4, a issue #128 exigiu refatoração/reengenharia orientada a design no backend. O `TransacaoService` concentrou, ao longo das Sprints 2 e 3, responsabilidades de domínios diferentes: registro e edição de transações, resolução de conta do usuário (incluindo conta automática de dinheiro), validação de categoria permitida, sugestão automática de categoria por palavras-chave e conversão de entidade para DTO.

Esse acúmulo gerou efeitos colaterais práticos:

- **Alto acoplamento:** o `ImportacaoService` dependia do `TransacaoService` apenas para chamar `sugerirCategoria()`, misturando o fluxo de importação com o módulo de transações manuais.
- **Baixa coesão:** alterações em regra de conta, categoria ou mapeamento exigiam editar a mesma classe usada por criação, edição, listagem e categorização.
- **Testes mais difíceis:** cenários unitários precisavam mockar dependências que não eram centrais ao caso de uso testado.

A refatoração foi conduzida no PR #187 (`refactor/128-transacao-service-design`) e motivou a necessidade de registrar formalmente a decisão arquitetural, conforme a issue #130.

## Decisão
A equipe decidiu decompor responsabilidades extraídas do `TransacaoService` em componentes especializados, mantendo o `TransacaoService` como coordenador dos casos de uso de transação.

Componentes criados ou reforçados:

| Componente | Pacote | Responsabilidade |
| --- | --- | --- |
| `ContaUsuarioService` | `service` | Resolver a conta usada na transação, validar propriedade do usuário autenticado e criar/obter a conta automática `Dinheiro / Carteira` quando a forma de pagamento for dinheiro. |
| `SugestaoCategoriaService` | `service` | Sugerir categoria padrão a partir da descrição da transação, com base em palavras-chave. |
| `TransacaoMapper` | `mapper` | Converter `Transacao` em `TransacaoResponseDTO`, isolando a montagem de resposta da API. |
| `CategoriaService` (extensão) | `service` | Centralizar a busca de categoria por ID e a validação de categoria permitida ao usuário (`buscarCategoriaPermitida`). |

Papéis após a mudança:

- **`TransacaoService`:** orquestra `registrarManual`, `editar`, `excluir`, `listarTransacoesPorUsuario` e `categorizar`, delegando regras auxiliares aos serviços acima.
- **`ImportacaoService`:** passa a depender diretamente de `SugestaoCategoriaService`, sem acoplamento ao `TransacaoService`.

A API pública dos endpoints de transação e importação permanece a mesma; a mudança é interna à organização do backend.

## Alternativas consideradas

- **Manter o `TransacaoService` monolítico:** Descartado por perpetuar acoplamento entre importação e transações, dificultar testes isolados e aumentar o risco de regressão em refatorações futuras.
- **Extrair apenas métodos privados dentro da mesma classe:** Descartado por não reduzir dependências entre módulos nem permitir reutilização independente (como no caso da importação).
- **Separar em microserviços por domínio:** Descartado pelo escopo do MVP monolítico e pelo custo operacional desproporcional à necessidade atual.

## Consequências

### Positivas
- Melhor aderência ao **Single Responsibility Principle (SRP)** dentro do monólito Spring.
- Testes unitários mais focados: cada serviço pode ser testado com mocks mínimos.
- Redução de acoplamento entre importação e transações manuais.
- Evolução mais segura: mudanças em sugestão de categoria ou resolução de conta não exigem alterar todo o fluxo de transação.

### Negativas / trade-offs
- Maior quantidade de classes e injeções no construtor dos serviços.
- Curva de leitura inicial: o desenvolvedor precisa saber qual serviço consultar para cada regra auxiliar.
- A refatoração exige atualização coordenada de testes existentes (`RegistrarManualTransacaoTests`, `TransacaoEdicaoExclusaoServiceTest`, `CategorizacaoNaImportacaoTests`, entre outros).

## Justificativa
A decomposição foi considerada superior à manutenção de um service único porque preserva o comportamento externo da API enquanto melhora a estrutura interna exigida pela Sprint 4. O padrão segue a estratégia de camadas já definida na ADR-0004 e complementa os padrões formalizados na ADR-0005, aplicando separação de responsabilidades também dentro da camada de serviços — não apenas entre controller, service e repository.

## Referências
- Issue #128 — Refatoração/reengenharia orientada a design
- Issue #130 — ADR da refatoração da Sprint 4
- PR #187 — `refactor/128-transacao-service-design`
- Classes afetadas: `TransacaoService`, `ContaUsuarioService`, `SugestaoCategoriaService`, `TransacaoMapper`, `CategoriaService`, `ImportacaoService`

## Revisão futura
A decisão poderá ser revista se o backend evoluir para arquitetura distribuída ou se novos domínios (dashboard, extrato futuro, parcelamentos) exigirem uma reorganização mais ampla dos bounded contexts. Enquanto o sistema permanecer monolítico, a decomposição por serviços especializados deve ser o padrão para novos módulos que acumulem responsabilidades semelhantes.
