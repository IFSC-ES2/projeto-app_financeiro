-- Categoria padrão Compras e termos levantados num extrato real do Banco do Brasil.
-- No BB os estabelecimentos vêm em maiúsculas ou grudados ("CantinhoDasOstras"); os termos ficam normalizados.

INSERT INTO categoria (id, nome, icone, cor, padrao, usuario_id)
VALUES (gen_random_uuid(), 'Compras', '🛍️', '#FF8F3D', true, null);

INSERT INTO regra_categorizacao (termo, categoria_id)
SELECT regra.termo, categoria.id
FROM (VALUES
    ('lar dos velhinhos', 'Compras'), ('brecho', 'Compras'), ('loja', 'Compras'),
    ('mais natural', 'Alimentação'), ('cantinho das ostras', 'Alimentação'),
    ('cantinhodasostras', 'Alimentação'), ('quiosque', 'Alimentação'),
    ('telefonica', 'Serviços')
) AS regra (termo, nome_categoria)
JOIN categoria ON categoria.nome = regra.nome_categoria AND categoria.padrao = true;
