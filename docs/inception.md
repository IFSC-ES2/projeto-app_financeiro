# Inception: SmartBudget

## 1. Visão do Projeto

O SmartBudget é um gerenciador financeiro pessoal que centraliza movimentações de múltiplas contas e cartões em uma única plataforma. O sistema elimina o preenchimento manual por meio da importação de extratos (CSV/XML) e leitura de notas fiscais, e se diferencia ao oferecer uma visão preditiva do saldo futuro considerando parcelas, com notificações/lembretes para o usuário.

**Problema que resolve:** fragmentação de informações financeiras em diversos apps bancários, reeducação financeira e esquecimento de prazos de vencimento.

**Usuários:** pessoas físicas que utilizam múltiplos métodos de pagamento e diversos aplicativos de banco e que precisam de uma visão consolidada das suas finanças.

**Restrições conhecidas:**
- Leitura de extratos apenas via arquivos exportados pelo usuário
- Sistema operará exclusivamente em Real 
- MVP focado em uso individual (uma conta, um CPF)
- Sem suporte a PDF, Open Banking ou gestão de investimentos

## 2. MVP

**Objetivo:** permitir que o usuário importe seus extratos e notas fiscais, visualize gastos por categoria e projete seu saldo para os meses seguintes, com avisos de vencimentos.

### Fora do escopo

- Open Banking: Requer certificação fora do escopo acadêmico
- Gestão de investimentos: Complexidade sem valor para o fluxo principal 
- Extratos em PDF: Falta de padronização inviabiliza o parser 
- Multiusuário: Aumenta complexidade sem impacto no MVP 
- Moedas estrangeiras: Fora do perfil do usuário alvo 