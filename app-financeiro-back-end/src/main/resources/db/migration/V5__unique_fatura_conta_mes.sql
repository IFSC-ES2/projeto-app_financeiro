-- ============================================================
-- SmartBudget - Unicidade de fatura por conta e mês
-- V5__unique_fatura_conta_mes.sql
-- ============================================================
-- `gerarFatura` é get-or-create: duas chamadas concorrentes podem
-- passar pelo SELECT antes de qualquer INSERT e duplicar a fatura
-- do mês. A consulta prévia no service não garante unicidade; a
-- restrição precisa morar no banco.
-- ============================================================

ALTER TABLE fatura
    ADD CONSTRAINT uk_fatura_conta_mes UNIQUE (conta_id, mes_referencia);
