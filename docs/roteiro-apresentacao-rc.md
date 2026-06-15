# Roteiro de apresentacao do Release Candidate

Este roteiro descreve o cenario de demonstracao do MVP do SmartBudget para validacao do Release Candidate.

O foco da apresentacao e demonstrar um fluxo realista de uso:

1. acessar o sistema com usuario de teste;
2. importar um extrato bancario no formato Nubank;
3. revisar e editar transacoes importadas;
4. ajustar formas de pagamento, como Pix e cartao de debito;
5. acompanhar o resumo visual por forma de pagamento;
6. criar uma nova conta bancaria;
7. cadastrar uma transacao manual associada a essa nova conta.

## 1. Dados do ambiente

Ambiente publicado:

- Web: `https://smartbudget-web-0sic.onrender.com`
- API: `https://smartbudget-api-kbze.onrender.com`

Ambiente local, se a apresentacao for feita pela maquina da equipe:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Banco: PostgreSQL via Docker Compose

## 2. Usuario de teste

Usuario recomendado para a apresentacao:

| Campo | Valor |
|-------|-------|
| Nome | Adriano |
| E-mail | `adrianopingpong@gmail.com` |
| Senha | `123456` |
| CPF | `410.100.960-01` |

Será criado no momento da apresentação com esses dados para que tenha a demonstração da tela de cadastro + criação da conta bancária.

## 3. Arquivo de extrato para demonstracao

Usar um arquivo CSV de demonstracao com layout compativel com extrato de conta Nubank.

Nome sugerido do arquivo:

```text
extrato-nubank-demo.csv
```

Conteudo sugerido:

```csv
Data,Valor,Identificador,Descricao
2026-06-01,-32.90,nu-001,Padaria Centro
2026-06-02,-89.90,nu-002,Mercado Avenida
2026-06-03,-18.50,nu-003,Uber
2026-06-04,-120.00,nu-004,Farmacia Drogasil
2026-06-05,2500.00,nu-005,Salario
2026-06-06,-45.00,nu-006,Netflix
2026-06-07,-64.80,nu-007,Restaurante
2026-06-08,-150.00,nu-008,Conta de luz
```

## 4. Preparacao do cenario antes da apresentacao

Antes de iniciar a demonstracao:

1. Confirmar que o frontend e a API estao acessiveis.
2. Confirmar que o e-mail e CPF do usuario de teste ainda nao foram usados no ambiente escolhido.
3. Se o usuario ja existir, trocar e-mail/CPF antes da apresentacao para conseguir demonstrar o cadastro.
4. Deixar o arquivo `extrato-nubank-demo.csv` separado e facil de selecionar.
5. Usar um usuario novo para evitar dados antigos confundindo a demonstracao.

## 5. Fluxo 1 - cadastro do usuario e primeira conta

Objetivo: mostrar a criacao de perfil, autenticacao automatica apos cadastro e o fluxo inicial de criacao de conta bancaria.

Passos:

1. Abrir a URL do frontend.
2. Acessar `/cadastro`.
3. Preencher os dados do usuario de teste:
   - nome: `Adriano`;
   - e-mail: `adrianopingpong@gmail.com`;
   - CPF: `410.100.960-01`;
   - senha: `123456`;
   - confirmacao de senha: `123456`.
4. Confirmar o cadastro.
5. Mostrar que o sistema direciona o usuario para o fluxo privado.
6. Criar a primeira conta bancaria que sera usada na importacao:
   - nome: `Nubank Principal`;
   - banco: `Nubank`;
   - tipo: `Conta corrente`;
   - descricao: `Conta usada para importar o extrato Nubank na apresentacao`.
7. Salvar a conta.
8. Mostrar rapidamente que o menu privado esta disponivel.

> Comecamos pela criacao do perfil para demonstrar que cada usuario acessa seus dados de forma autenticada. Depois do cadastro, o sistema ja permite criar a primeira conta bancaria, que sera usada como destino das transacoes importadas do extrato Nubank.

Pontos a mostrar:

- tela de cadastro;
- validacao de dados obrigatorios;
- entrada no ambiente privado apos cadastro;
- fluxo de primeira conta;
- conta `Nubank Principal` pronta para receber a importacao.

## 6. Fluxo 2 - importar extrato Nubank

Objetivo: demonstrar o diferencial do MVP, que e importar um arquivo financeiro e transformar linhas do extrato em transacoes.

Passos:

1. Pelo menu, acessar `Importacoes` ou abrir `/importacoes/nova`.
2. Selecionar a conta `Nubank Principal`.
3. Selecionar o arquivo `extrato-nubank-demo.csv`.
4. Enviar a importacao.
5. Aguardar o status da importacao ate aparecer como concluida.
6. Ir para a tela de transacoes.

> Nesta etapa, o usuario envia um arquivo CSV semelhante ao extrato de conta do Nubank. O backend recebe o arquivo por `multipart/form-data`, identifica o parser CSV, processa as linhas validas e grava as transacoes associadas a conta selecionada.

Pontos a mostrar:

- selecao da conta de destino;
- upload do arquivo;
- acompanhamento do status da importacao;
- transacoes importadas aparecendo na listagem.

Limite que deve ser citado:

> A importacao esta implementada, mas extratos reais podem variar por banco e layout. O RC possui parser para CSV, TXT, XML e NF-e, mas nao garante suporte universal para qualquer extrato real.

## 7. Fluxo 3 - revisar e editar transacoes importadas

Objetivo: mostrar que as transacoes importadas podem ser revisadas, categorizadas e ajustadas.

Passos:

1. Acessar `/transacoes`.
2. Localizar as transacoes importadas do arquivo Nubank.
3. Para algumas transacoes, clicar em editar.
4. Ajustar a forma de pagamento:
   - `Padaria Centro`: Pix;
   - `Mercado Avenida`: Cartao de debito;
   - `Uber`: Cartao de debito;
   - `Farmacia Drogasil`: Pix;
   - `Netflix`: Cartao de credito, se quiser demonstrar a classificacao, deixando claro que fatura completa nao esta implementada;
   - `Conta de luz`: Boleto.
5. Ajustar a categoria quando fizer sentido, por exemplo:
   - alimentacao;
   - transporte;
   - saude;
   - servicos;
   - lazer.
6. Salvar cada edicao e retornar para a listagem.

> Depois da importacao, o usuario ainda consegue revisar as transacoes. Isso e importante porque extratos bancarios nem sempre trazem a categoria ou a forma de pagamento do jeito que o usuario quer acompanhar. Aqui ajustamos as transacoes para Pix, cartao de debito, boleto ou outras formas, e isso alimenta o resumo visual.

Pontos a mostrar:

- edicao de transacao importada;
- alteracao de forma de pagamento;
- categorizacao manual;
- retorno da transacao atualizada para a listagem.

Limite que deve ser citado:

> A categorizacao manual esta funcional. A sugestao automatica por palavras-chave existe como apoio, mas ainda depende de uma base maior de transacoes reais para ser medida com confianca.

## 8. Fluxo 4 - acompanhar resumo por forma de pagamento

Objetivo: demonstrar a visualizacao inicial de resumo, especialmente a divisao em pizza por forma de pagamento.

Passos:

1. Voltar para a tela de transacoes ou dashboard inicial, conforme a versao em uso na apresentacao.
2. Mostrar o componente de resumo por forma de pagamento.
3. Destacar que as formas alteradas nas transacoes aparecem agrupadas, como:
   - Pix;
   - Cartao de debito;
   - Cartao de credito;
   - Boleto;
   - Dinheiro;
   - TED/DOC.
4. Explicar que a pizza/resumo ajuda a entender como o dinheiro foi movimentado.

> Este resumo mostra a distribuicao das movimentacoes por forma de pagamento. Ao editar as transacoes importadas e marcar Pix, cartao de debito ou boleto, o dashboard inicial passa a refletir essa divisao visualmente.

Limite que deve ser citado:

> O backend do resumo mensal e os agrupamentos existem, mas o dashboard visual completo planejado no inception ainda nao esta finalizado no frontend.

## 9. Fluxo 5 - criar nova conta bancaria

Objetivo: demonstrar gestao de contas.

Passos:

1. Pelo menu, acessar `Contas` ou abrir `/contas`.
2. Criar uma nova conta bancaria.
3. Dados sugeridos:
   - nome: `Banco do Brasil Reserva`;
   - banco: `Banco do Brasil`;
   - tipo: `Conta corrente`;
   - descricao: `Conta criada durante a demonstracao`.
4. Salvar.
5. Mostrar a nova conta na listagem.

> O usuario pode cadastrar mais de uma conta para organizar movimentacoes por origem. Isso permite separar dados importados de um banco das transacoes manuais de outro banco.

Pontos a mostrar:

- cadastro de conta;
- listagem da conta criada;
- possibilidade de edicao e exclusao quando permitido.

## 10. Fluxo 6 - criar transacao manual associada a nova conta

Objetivo: demonstrar o fallback manual e o registro de movimentacoes que nao vieram por extrato.

Passos:

1. Acessar `/transacoes/nova`.
2. Preencher uma transacao manual associada a `Banco do Brasil Reserva`.
3. Dados sugeridos:
   - tipo: `Saida / despesa`;
   - valor: `75.00`;
   - data: data do dia da apresentacao;
   - descricao: `Almoco demonstracao`;
   - forma de pagamento: `Pix`;
   - categoria: `Alimentacao`;
   - conta: `Banco do Brasil Reserva`.
4. Salvar.
5. Voltar para `/transacoes`.
6. Usar filtros por conta para mostrar apenas as transacoes da nova conta.
7. Mostrar que a nova transacao tambem impacta os resumos.

> Mesmo com importacao de extratos, o cadastro manual continua importante. Ele funciona como fallback para bancos ou layouts ainda nao suportados e tambem para movimentacoes que o usuario quer registrar imediatamente.

## 11. Fluxo completo a ser demonstrado

Ordem recomendada na apresentacao:

1. Criar o usuario pela tela de cadastro.
2. Criar a primeira conta `Nubank Principal`.
3. Importar `extrato-nubank-demo.csv`.
4. Consultar status da importacao.
5. Ir para transacoes.
6. Editar transacoes importadas, ajustando forma de pagamento e categoria.
7. Mostrar resumo/pizza por forma de pagamento.
8. Criar nova conta bancaria pelo menu.
9. Criar transacao manual associada a nova conta.
10. Mostrar listagem, filtros e impacto nos resumos.

## 12. O que não deve ser apresentado como concluído

Durante a apresentacao, evitar dizer que as funcionalidades abaixo estao prontas:

- extrato futuro;
- projecao de saldo dos proximos meses;
- avisos ou lembretes de vencimento;
- parcelamentos funcionais;
- faturas funcionais de cartao de credito;
- dashboard visual completo do mes;
- gestao visual completa de categorias;
- suporte universal a qualquer layout real de extrato bancario.


> O RC demonstra o fluxo principal de controle financeiro com importacao, edicao, categorizacao, contas, transacoes manuais e resumo por forma de pagamento. Funcionalidades como extrato futuro, faturas, parcelamentos e dashboard completo ficam registradas como evolucao futura.

