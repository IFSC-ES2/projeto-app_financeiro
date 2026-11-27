ALTER TABLE fatura
ADD CONSTRAINT uk_fatura_conta_mes
UNIQUE (conta_id, mes_referencia);