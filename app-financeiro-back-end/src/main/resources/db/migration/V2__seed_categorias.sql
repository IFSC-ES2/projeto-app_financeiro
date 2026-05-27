INSERT INTO categoria (id, nome, icone, cor, padrao, usuario_id)
VALUES
    (gen_random_uuid(), 'Alimentação',  '🍔', '#FF6B6B', true, null),
    (gen_random_uuid(), 'Transporte',   '🚗', '#4ECDC4', true, null),
    (gen_random_uuid(), 'Saúde',        '💊', '#45B7D1', true, null),
    (gen_random_uuid(), 'Lazer',        '🎬', '#96CEB4', true, null),
    (gen_random_uuid(), 'Habitação',    '🏠', '#FFEAA7', true, null),
    (gen_random_uuid(), 'Serviços',     '📱', '#DDA0DD', true, null),
    (gen_random_uuid(), 'Manutenção',   '🔧', '#98D8C8', true, null);