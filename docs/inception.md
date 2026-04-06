# Inception: SmartBudget

## 1. Visão do Projeto

O SmartBudget é um gerenciador financeiro pessoal que centraliza movimentações de múltiplas contas e cartões em uma única plataforma. O sistema elimina o preenchimento manual por meio da importação de extratos (CSV/XML) e leitura de notas fiscais, e se diferencia ao oferecer uma visão preditiva do saldo futuro considerando parcelas, com notificações/lembretes para o usuário.

**Problema que resolve:** fragmentação de informações financeiras em diversos apps bancários, reeducação financeira e esquecimento de prazos de vencimento.

**Usuários:** pessoas físicas que utilizam múltiplos métodos de pagamento e diversos aplicativos de banco e que precisam de uma visão consolidada das suas finanças.

**Proposta de valor:** o SmartBudget é o único gerenciador financeiro pessoal brasileiro que elimina o registro manual de gastos por meio da importação automática de extratos bancários e notas fiscais, e que entrega uma visão preditiva do saldo futuro considerando parcelas e boletos em aberto — sem depender de integração bancária direta nem de assinatura paga.

**Restrições conhecidas:**
- Leitura de extratos apenas via arquivos exportados pelo usuário
- Sistema operará exclusivamente em Real 
- MVP focado em uso individual (uma conta, um CPF)
- Sem suporte a PDF, Open Banking ou gestão de investimentos

## 2. MVP

**Objetivo:** permitir que o usuário importe seus extratos e notas fiscais, visualize gastos por categoria e projete seu saldo para os meses seguintes, com avisos de vencimentos.

### Funcionalidades essenciais
 
As funcionalidades abaixo foram consideradas essenciais por comporem o fluxo mínimo que entrega valor ao usuário — sem qualquer uma delas o produto não resolve o problema central:
 
1. **Autenticação e perfil pessoal** pré-requisito técnico para qualquer dado ser persistido com segurança por usuário.
2. **Importação de extratos (CSV/XML)** é o diferencial central do produto; sem ela o usuário volta ao registro manual, que é exatamente o que o SmartBudget se propõe a eliminar.
3. **Categorização de gastos** sem categorias, os dados importados não geram insight. É o que transforma um extrato bruto em informação útil.
4. **Dashboard de gastos do mês** é a entrega de valor visível ao usuário; sem visualização, os dados existem mas não são consumíveis.
5. **Extrato futuro (projeção de parcelas e vencimentos)** é o diferencial competitivo frente a todas as soluções existentes no mercado; sua ausência torna o produto equivalente a concorrentes já estabelecidos.
 
### Viabilidade para o semestre
 
 Com uma equipe de 4 pessoas em dedicação parcial, e também visando o fim de semestre para 1 de julho, cada membro conciliando outras disciplinas e compromissos, o tempo efetivo disponível para desenvolvimento é reduzido. As funcionalidades essenciais foram dimensionadas para caber nessa realidade pelos seguintes motivos:
 
- A stack escolhida (Spring Boot + React + MySQL) é familiar à equipe, evitando perda de tempo com curva de aprendizado em tecnologias novas.
- Funcionalidades de alta complexidade e baixo impacto no fluxo principal (IA, Open Banking, PDF, multiusuário) foram excluídas exatamente para proteger o prazo, dado que a dedicação parcial da equipe não comporta escopo aberto.

### Critérios de decisão do escopo
 
A equipe usou três perguntas para decidir o que entra no MVP:
 
1. Faz parte do fluxo principal? A funcionalidade é necessária para importar, categorizar e visualizar gastos? Se não, fica fora.
2. Cabe no prazo? A implementação é viável até 1 de julho, sem depender de certificações ou APIs externas fora do controle da equipe?

### Fora do escopo

- Open Banking: Requer certificação fora do escopo acadêmico
- Gestão de investimentos: Complexidade sem valor para o fluxo principal 
- Extratos em PDF: Falta de padronização inviabiliza o parser 
- Multiusuário: Aumenta complexidade sem impacto no MVP 
- Moedas estrangeiras: Fora do perfil do usuário alvo 