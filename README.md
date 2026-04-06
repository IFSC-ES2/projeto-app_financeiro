# Projeto de App de Gerenciamento Financeiro - SmartBudget

Projeto proposto pelo Clayrton em Extensão II:

> Desenvolvimento de um gerenciador financeiro pessoal que permita a integração de notas fiscais (NF-e), extratos bancários (xml, csv, txt), extratos de cartão (xml, csv, txt), e que permita gerenciar as contas do usuário de modo a prover relatórios de gastos e despesas mensais por categorias (habitação, saúde, serviços, lazer, manutenção, transportes, etc.).

## 1. Equipe Formada

- Alexandre Pereira Villela -> DevOPS / Infra
- João Pedro Callegaro -> DevOPS / Infra
- Lucas de Leon Rodrigues -> Engenheiro de Qualidade
- Victor Blum -> Arquiteto de Software
- Victor Gabriel Lacerda -> Scrum Master

## 2. Tema Definido

### a) qual problema o sistema pretende resolver?

O sistema pretende resolver a falta de organização financeira e a falta de controle sobre o pŕoprio dinheiro do usuario. Um possível usuário pode usar diferentes bancos, diferentes cartões de crédito, mas falta um sistema central, que una e mostre os seus gastos totais em todos os aspectos da sua vida, com o SmartBudget o usuário vai poder organizar e visualizar melhor sua vida financeira.

### b) quem são os usuários?

O sistema é focado em pessoas físicas, não empresas. Os usuários são pessoas que querem ter um maior controle de gastos, pode ser uma pessoa que usa muito cartão de crédito e quer informações mais práticas dos gastos, ou um chefe de família que quer ver em quais aspectos da sua vida o dinheiro está sendo mais utilizado (lazer, alimentação, saúde e entre outros).

### c) qual a proposta do sistema para resolver esse problema?

A proposta é ser um assistente financeiro prático e inteligente que elimina o trabalho de anotar gastos na mão, sendo necessário apenas enviar extratos bancários e notas fiscais para leitura automática. Ele resolve a falta de controle e vizualização de gastos centralizando tudo em um único lugar, separando despesas por categoria e criando um 'extrato futuro', que avisa com antecedência o valor das faturas do cartão e o vencimento de boletos.

## 3. MVP

### a) o que o MVP fará?

O sistema vai permitir a leitura de extratos bancários e notas fiscais (.xml, .csv, .txt), separando os gastos em categorias (automaticamente, ou manualmente se necessário). Informará o 'extrato futuro', baseando-se nos extratos bancários enviados (irá considerar vencimento de boletos, parcelamentos, financiamentos e entre outros).

### b) quais são as funcionalidades principais?

1. Criação de perfil pessoal, com autenticação;
1. Leitura de extratos bancários e notas fiscais (xml, csv, txt);
1. Adicionar gastos manualmente;
1. Categorizar os gastos em subdivisões (lazer, alimentação etc);
1. Categorizar os gastos a partir de como o dinheiro foi utilizado (cartão, pix, dinheiro, boleto);
1. Categorizar os gastos a partir do cartão e banco utilizado;
1. Visualização de gastos do mês em texto, gráficos e dashboards;
1. Visualição do extrato dos próximos meses em texto, gráficos e dashboards;


### c) o que ficará fora do escopo no momento

1. Integração direta com a conta bancária;
1. Inteligência Artificial para organizar a categorização;
1. Investimentos e Criptomoedas;
1. Conversão para outras moedas (dólar, euro, libras etc), será apenas o Real.

-------
### Documentos 

- [Visão do Produto e MVP](docs/inception.md) 
- [Definition of Done](docs/dod.md) 
- [ADR-0001 — Stack Frontend](docs/adrs/ADR-0001-Tipo-de-Aplicacao.md) 
- [ADR-0002 — Stack Backend](docs/adrs/ADR-0002-Linguagem-e-Framework.md) 
- [ADR-0003 — Banco de Dados](docs/adrs/ADR-0003-Abordagem-de-persistência-de-dados.md) 
- [ADR-0004 — Arquitetura Geral](docs/adrs/ADR-0004-Estrategia-de-arquiteturas-e-camadas.md)
- [Board](https://github.com/orgs/IFSC-ES2/projects/20)
- [Backlog](https://github.com/IFSC-ES2/projeto-app_financeiro/issues)