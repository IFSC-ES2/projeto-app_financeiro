# Governança do Repositório

## Papéis e Responsabilidades
Com base na divisão de papéis da equipe, as responsabilidades são as seguintes:
- **Scrum Master** (@victorlcrd): Facilita cerimônias ágeis (daily, planning, review, retro), garante aderência ao Scrum, resolve impedimentos e coordena comunicação entre equipe e stakeholders.
- **Arquiteto** (@Victor3294): Define a arquitetura do sistema, padrões de código, revisa designs técnicos e garante consistência na estrutura do projeto.
- **Engenheiro de Qualidade** (@lucaslrc01): Gerencia testes (unitários, integração, e2e), cobertura de código, linting, formatação e valida qualidade em PRs.
- **Infra/DevOps** (@JPIFSC): Gerencia infraestrutura, CI/CD, deploys, monitoramento, segurança de infra e automações.

## Regras básicas
- Proibido commit direto na main (tudo entra por PR).
- Branches:
  - feature/<id>-<resumo>
  - fix/<id>-<resumo>
  - chore/<id>-<resumo>
- Commits devem seguir convenção: `<tipo>: descrição` (ex.: `feat: adiciona login`).

## DoD do PR (mínimo)
- Descrição: o que mudou, por quê e como testar.
- Auto-review: checklist + comentários técnicos no PR.
- Aprovação obrigatória dos code owners relevantes (ver CODEOWNERS).

## Review (critérios)
- Comentários devem explicar o motivo e sugerir alternativa quando possível.
- Evitar PR grande: se não revisa em ~10 min, dividir.
- Reviews obrigatórios: Arquiteto para mudanças estruturais, Engenheiro de Qualidade para testes.

## Controle de Qualidade (Engenheiro de Qualidade)
### Testes
- Todos os PRs devem incluir testes unitários e de integração.
- Cobertura mínima de 80% para código novo.
- Testes e2e para funcionalidades críticas.

### Linting e Formatação
- Usar ESLint para JavaScript/TypeScript.
- Formatação automática com Prettier.
- Commits devem passar no linting.

## CI/CD (Infra/DevOps)
- Integração contínua com GitHub Actions.
- Build automático em push para branches feature/*.
- Deploy automático para staging em merge para main (após aprovação).
- Monitoramento: Logs, alertas e rollback automático em falhas.

## Segurança (Infra/DevOps)
- Revisão de segurança em PRs que envolvem autenticação ou dados sensíveis.
- Uso de dependências atualizadas; scans de vulnerabilidades semanais.
- Não commitar secrets; usar variáveis de ambiente.
- Auditorias regulares de código e infra.

## Documentação (Arquiteto)
- README.md atualizado com setup, arquitetura e contribuição.
- Documentação de API se aplicável.
- Comentários no código para funções complexas.
- Diagramas de arquitetura versionados.

## Versionamento
- Seguir SemVer para releases.
- Tags automáticas via CI/CD.
- Releases aprovados pelo Scrum Master.
