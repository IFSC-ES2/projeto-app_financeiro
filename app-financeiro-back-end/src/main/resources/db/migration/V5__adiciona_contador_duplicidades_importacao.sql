ALTER TABLE importacao
    ADD COLUMN ignoradas_por_duplicidade INT NOT NULL DEFAULT 0;
