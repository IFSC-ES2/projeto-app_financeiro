-- Remove exclusivamente as identidades criadas pela migration V3.
-- Contas, cartoes, faturas e transacoes dependentes sao removidos em cascata.
-- Categorias personalizadas do demo nao devem se tornar categorias orfas.

DELETE FROM categoria
WHERE usuario_id IN (
    SELECT id
    FROM usuario
    WHERE id IN (
        '11111111-1111-1111-1111-111111111111',
        '22222222-2222-2222-2222-222222222222'
    )
);

DELETE FROM usuario
WHERE id IN (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222'
);