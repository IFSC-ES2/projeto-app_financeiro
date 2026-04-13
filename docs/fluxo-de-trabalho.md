#  Fluxo de Trabalho - SmartBudget

##  Objetivo
Este documento descreve o fluxo de trabalho adotado pela equipe para organização do desenvolvimento, utilização de branches, abertura de Pull Requests (PR) e processo de revisão.

---

##  Estratégia de Branches

A equipe adota um modelo simples baseado em branches temáticas, sem utilização de GitFlow completo.

### Branch principal
- `main`: contém a versão estável do projeto

### Branches de trabalho
As branches seguem um padrão baseado no tipo de atividade:

- `docs/...` → documentação
- `chore/...` → tarefas técnicas, organização e infraestrutura

### Exemplos reais utilizados:
- `docs/07-baseline`
- `docs/06-metricas`
- `chore/04-estrutura-entrega-03`
- `chore/03-readme-refs-links`

---

##  Processo de Desenvolvimento

1. Criar uma branch a partir da `main`
2. Nomear a branch seguindo o padrão definido
3. Realizar as alterações necessárias
4. Fazer commits descritivos
5. Abrir um Pull Request (PR)

---

##  Pull Requests (PR)

- Todo desenvolvimento é integrado via Pull Request
- O PR deve conter descrição clara das alterações
- O template de PR é utilizado para padronização

### Template

O repositório possui o arquivo:
- `.github/PULL_REQUEST_TEMPLATE.md`


Este template garante:
- descrição do que foi feito
- contexto da alteração
- checklist básico

---

##  Revisão de Código

- Todo PR deve ser revisado por pelo menos um membro da equipe
- A revisão garante:
  - consistência do código
  - aderência ao padrão do projeto
  - qualidade das entregas

- Apenas após aprovação o PR pode ser integrado à `main`

---

##  Evidências

A equipe utiliza Pull Requests como evidência do processo de desenvolvimento.

Exemplos observados no repositório incluem PRs associados às branches:
- `docs/...`
- `chore/...`

Esses PRs demonstram:
- uso contínuo do fluxo de branches
- revisão antes de merge
- histórico de contribuições

---

##  Considerações

- O fluxo adotado é simples e adequado ao contexto acadêmico do projeto
- Prioriza organização, rastreabilidade e colaboração
- Pode evoluir conforme a complexidade do projeto aumentar