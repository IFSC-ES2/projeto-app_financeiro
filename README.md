# Projeto de App de Gerenciamento Financeiro - SmartBudget

Projeto proposto pelo Clayrton em Extensão II:

Desenvolvimento de um gerenciador financeiro pessoal que permita a integração de notas fiscais (NF-e), extratos bancários (xml, csv, txt), extratos de cartão (xml, csv, txt), e que permita gerenciar as contas do usuário de modo a prover relatórios de gastos e despesas mensais por categorias (habitação, saúde, serviços, lazer, manutenção, transportes, etc.).


## Equipe
| Nome | Função |
| :--- | :--- |
| João Pedro | DevOPS |
| Lucas de Leon | Eng. Qualidade |
| Victor Lacerda | Scrum Master |
| Victor Blum | Arquiteto de Software |

## 2. Respostas ao Questionário

### (a) Qual problema o sistema pretende resolver?
O sistema busca solucionar a **fragmentação de informações financeiras** e a **falta de previsibilidade**. Atualmente, o usuário médio possui contas em diferentes bancos e diversos cartões de crédito, o que gera:
* **Dificuldade de previsão:** Incerteza sobre o valor real comprometido nos meses futuros devido a parcelamentos.
* **Esquecimento de prazos:** Pagamento de multas e juros por falta de alertas centralizados de vencimento.
* **Fadiga de dados:** Desistência do controle financeiro pelo esforço manual de digitar cada gasto ou nota fiscal.

### (b) Quem são os usuários?
* **Pessoas Físicas:** Indivíduos que utilizam múltiplos métodos de pagamento (PIX, cartões, dinheiro) e precisam de uma visão consolidada.
* **Gestores Domésticos:** Pessoas que precisam categorizar gastos familiares (Habitação, Saúde, Lazer) para otimizar o orçamento.
* **Usuários de Crédito:** Consumidores que realizam compras parceladas e precisam visualizar o impacto dessas parcelas no saldo dos meses seguintes.

### (c) Qual é a proposta do sistema para resolver o problema?
A solução baseia-se em três pilares principais:
1.  **Automação e Integração:** Elimina o preenchimento manual através da importação de arquivos bancários e leitura de QR Code de Notas Fiscais (NF-e), garantindo precisão nos dados.
2.  **Visão Preditiva (Extrato Futuro):** Diferente de apps comuns, o sistema projeta o saldo das contas considerando as faturas de cartão de crédito e lançamentos futuros.
3.  **Comunicação Ativa:** Utiliza notificações em tempo real (push e e-mail) para avisar sobre vencimentos iminentes, garantindo que o usuário mantenha sua saúde financeira sem precisar abrir o app o tempo todo.

## Definição de MVP

### (a) O que o MVP fará
O sistema permitirá que o usuário centralize suas movimentações financeiras através do upload de arquivos de extrato e notas fiscais. Ele consolidará esses dados em um dashboard único, separando gastos por categoria e, principalmente, projetando o saldo disponível para os meses seguintes com base nas parcelas de cartão de crédito e contas fixas cadastradas.



### (b) Funcionalidades Principais (Incluso)

### 1. Autenticação e Perfil
* Criação de conta com login/senha e recuperação de acesso.
* Cadastro de múltiplas "Contas" (Ex: Banco X, Banco Y, Carteira).

### 2. Importação e Integração de Dados
* **Upload de Extratos:** Processamento de arquivos .CSV e .XML de bancos e cartões.
* **Leitor de NF-e:** Campo para inserir a chave de acesso ou upload do XML da nota fiscal para detalhamento automático de itens de compra.
* **Lançamento Manual:** Registro de gastos em dinheiro ou PIX que não constam em extratos importados.

### 3. Gestão e Categorização
* Categorização automática baseada em palavras-chave do extrato (ex: "Posto" -> Transporte).
* Interface para edição manual de categorias e nomes de lançamentos.

### 4. Visão de Futuro e Alertas
* **Dashboard de Extrato Futuro:** Gráfico de linha mostrando a previsão de saldo para os próximos 3 a 6 meses.
* **Notificações:** Alertas simples no sistema e envio de e-mail lembrando de vencimentos de faturas cadastradas.


### (c) Fora do Escopo (Não incluso neste momento)

* **Conexão Direta via Open Banking:** Não haverá login direto em contas bancárias (leitura será feita apenas via arquivos exportados pelo usuário).
* **Investimentos e Corretoras:** O sistema não fará gestão de carteira de ações ou criptoativos.
* **Leitura de PDF:** Devido à falta de padronização, extratos em PDF não serão suportados (apenas CSV/XML).
* **Multiusuário/Contas Conjuntas:** O MVP será focado apenas no uso individual (uma conta, um CPF).
* **Suporte a moedas estrangeiras:** O sistema operará exclusivamente em Real (BRL).

---


[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/aY1Nu9LV)
