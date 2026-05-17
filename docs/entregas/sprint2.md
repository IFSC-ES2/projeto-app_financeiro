# Registro de Contribuições Individuais — Sprint 2

O principal objetivo da Sprint 2 foi implementar a função de registro manual de transações, cobrindo tanto o frontend quanto o backend da funcionalidade. Além disso, a sprint teve melhorias na infraestrutura de CI, atualização da documentação de riscos e métricas, e maior cobertura de testes.

## Alexandre Vilela (DevOps)

Alexandre criou o workflow de integração contínua (PR #79, issue #74), que passa a rodar automaticamente em todo pull request, verificando o build e os testes do backend e do frontend, a sintaxe dos arquivos YAML e a presença de arquivos obrigatórios no repositório. Ele também abriu o PR #83 (issue #75) com o registro das métricas da Sprint 2, adicionando os valores observados e uma análise comparando o que foi planejado com o que foi executado, além de configurar o plugin de cobertura de código no backend.

Contribuições associadas: PR #79 (issue #74), PR #83 (issue #75).

---

## Victor Blum (Arquiteto de Software)

Victor foi um dos desenvolvedores principais da sprint, sendo responsável pela maior parte do backend do registro manual de transações. No PR #81, implementou a autenticação por token JWT nas requisições e garantiu que cada usuário só consiga registrar transações em contas que pertencem a ele. No PR #80, melhorou a documentação do projeto, adicionando uma matriz visual de riscos e seções sobre o modelo de qualidade adotado e o escopo da avaliação nos arquivos riscos.md e qualidade.md.

Contribuições associadas: PR #81 (backend, segurança), PR #80 (documentação de riscos e qualidade).

---

## João Pedro (DevOps)

João Pedro atualizou a documentação de riscos ao final da Sprint 2 (PR #84, issue #76). Ele registrou o status de cada risco identificado anteriormente (R01 a R07), incluiu um novo risco que surgiu durante a sprint — R08, relacionado à ausência de testes automatizados — e adicionou uma seção de encerramento resumindo o que se materializou e o que foi mitigado. O objetivo foi deixar o documento refletindo o que de fato aconteceu na sprint.

Contribuições associadas: PR #84 (issue #76), documentação docs/riscos.md.

---

## Victor Gabriel (Desenvolvedor)

Victor Gabriel foi o desenvolvedor principal da sprint. No PR #86, implementou o formulário de registro manual de transações no frontend, com campos para valor, data, descrição, tipo, forma de pagamento, categoria e conta. Integrou o formulário ao backend, adicionou o carregamento de contas e categorias do usuário logado e criou os endpoints necessários para isso no backend. Também foi responsável pelo merge dos PRs ao longo da sprint: #86, #84, #83, #80 e #79.

Contribuições associadas: PR #86 (issue #63), merges dos PRs #84, #83, #80 e #79.

---

## Lucas (Scrum Master e Engenheiro de Qualidade)

Lucas fez o review e merge dos PRs #81 e #86. Como Engenheiro de Qualidade, abriu o PR #85 com testes de unidade para o registro manual de transações, verificando se o método registroManual no TransacaoService está funcionando corretamente, o PR ainda está aberto aguardando revisão. Também analisou as contribuições da equipe na Sprint 2, resultando na criação desse documento. 

Contribuições associadas: PR #85 (testes, branch test/sprint-2), merge dos PRs #81 e #86.