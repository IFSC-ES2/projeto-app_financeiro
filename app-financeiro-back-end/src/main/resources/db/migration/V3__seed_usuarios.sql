INSERT INTO usuario (id, nome, email, senha, cpf, created_at)
SELECT
    '11111111-1111-1111-1111-111111111111',
    'Usuário Demo',
    'demo@smartbudget.com',
    '$2y$10$t3HG4B2VNjI4k5zpfQSLEeNk/jmBRmiHTbpWrjePfulzdmXX/47O6',
    '12345678909',
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1
    FROM usuario
    WHERE email = 'demo@smartbudget.com'
       OR cpf = '12345678909'
);

INSERT INTO usuario (id, nome, email, senha, cpf, created_at)
SELECT
    '22222222-2222-2222-2222-222222222222',
    'Usuário Teste',
    'teste@smartbudget.com',
    '$2y$10$t3HG4B2VNjI4k5zpfQSLEeNk/jmBRmiHTbpWrjePfulzdmXX/47O6',
    '11144477735',
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1
    FROM usuario
    WHERE email = 'teste@smartbudget.com'
       OR cpf = '11144477735'
);