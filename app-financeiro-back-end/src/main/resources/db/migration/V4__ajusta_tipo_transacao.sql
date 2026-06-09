-- ============================================================
-- SmartBudget - Ajuste de modelagem de tipo de transação
-- V4__ajusta_tipo_transacao.sql
-- ============================================================
-- TipoTransacao passa a representar apenas o sentido financeiro
-- (DEBITO / CREDITO). Valores que descrevem forma/condição de
-- pagamento saem da coluna `tipo`:
--   - BOLETO       -> tipo DEBITO + forma_pagamento BOLETO
--   - PARCELAMENTO -> tipo DEBITO (parcela paga é uma saída)
--
-- Necessário porque `tipo` é mapeado como @Enumerated(STRING); uma linha
-- legada com 'BOLETO'/'PARCELAMENTO' quebraria a leitura do Hibernate.
-- Em banco novo nenhuma linha é afetada (no-op seguro).
-- ============================================================

UPDATE transacoes
   SET forma_pagamento = 'BOLETO'
 WHERE tipo = 'BOLETO'
   AND (forma_pagamento IS NULL OR forma_pagamento = '');

UPDATE transacoes
   SET tipo = 'DEBITO'
 WHERE tipo IN ('BOLETO', 'PARCELAMENTO');
