-- Base de regras de categorização automática (termo -> categoria).
-- usuario_id nulo = regra global (base treinada, versionada por migration);
-- preenchido = regra aprendida com a categorização manual daquele usuário, que tem prioridade.
-- Os termos ficam normalizados: minúsculos e sem acento.

INSERT INTO categoria (id, nome, icone, cor, padrao, usuario_id)
VALUES
    (gen_random_uuid(), 'Receitas',    '💰', '#35B779', true, null),
    (gen_random_uuid(), 'Assinaturas', '🔁', '#7C5CFF', true, null);

CREATE TABLE regra_categorizacao (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    termo         VARCHAR(120) NOT NULL,
    categoria_id  UUID NOT NULL REFERENCES categoria (id) ON DELETE CASCADE,
    usuario_id    UUID REFERENCES usuario (id) ON DELETE CASCADE,
    criado_em     TIMESTAMP NOT NULL DEFAULT now()
);

-- Um termo aparece no máximo uma vez por usuário e uma vez na base global
CREATE UNIQUE INDEX ux_regra_categorizacao_usuario_termo
    ON regra_categorizacao (usuario_id, termo) WHERE usuario_id IS NOT NULL;
CREATE UNIQUE INDEX ux_regra_categorizacao_global_termo
    ON regra_categorizacao (termo) WHERE usuario_id IS NULL;

INSERT INTO regra_categorizacao (termo, categoria_id)
SELECT regra.termo, categoria.id
FROM (VALUES
    -- Alimentação
    ('mercado', 'Alimentação'), ('supermercado', 'Alimentação'), ('mercadinho', 'Alimentação'),
    ('mini mercad', 'Alimentação'), ('hortifruti', 'Alimentação'), ('direto do produto', 'Alimentação'),
    ('acougue', 'Alimentação'), ('padaria', 'Alimentação'), ('restaurante', 'Alimentação'),
    ('lanchonete', 'Alimentação'), ('lanches', 'Alimentação'), ('pizzaria', 'Alimentação'),
    ('cafeteria', 'Alimentação'), ('sorveteria', 'Alimentação'), ('subway', 'Alimentação'),
    ('ifood', 'Alimentação'), ('rappi', 'Alimentação'), ('99food', 'Alimentação'),
    ('sodexo', 'Alimentação'), ('+ natural', 'Alimentação'), ('novo e bom', 'Alimentação'),
    ('kazuaki', 'Alimentação'), ('mundo bebidas', 'Alimentação'), ('bebidas', 'Alimentação'),
    -- Transporte
    ('uber', 'Transporte'), ('99', 'Transporte'), ('99 tecnologia', 'Transporte'),
    ('combustivel', 'Transporte'), ('gasolina', 'Transporte'), ('etanol', 'Transporte'),
    ('posto', 'Transporte'), ('autoposto', 'Transporte'), ('onibus', 'Transporte'),
    ('metro', 'Transporte'), ('estacionamento', 'Transporte'), ('pedagio', 'Transporte'),
    -- Saúde
    ('farmacia', 'Saúde'), ('drogaria', 'Saúde'), ('raia', 'Saúde'), ('drogasil', 'Saúde'),
    ('panvel', 'Saúde'), ('hospital', 'Saúde'), ('clinica', 'Saúde'), ('medico', 'Saúde'),
    ('laboratorio', 'Saúde'), ('dentista', 'Saúde'), ('odonto', 'Saúde'),
    -- Lazer
    ('cinema', 'Lazer'), ('teatro', 'Lazer'), ('steam', 'Lazer'), ('jogos', 'Lazer'),
    ('bar', 'Lazer'), ('espaco recanto', 'Lazer'), ('ingresso', 'Lazer'),
    -- Assinaturas
    ('netflix', 'Assinaturas'), ('spotify', 'Assinaturas'), ('ifood club', 'Assinaturas'),
    ('amazon prime', 'Assinaturas'), ('prime video', 'Assinaturas'), ('disney', 'Assinaturas'),
    ('hbo', 'Assinaturas'), ('youtube premium', 'Assinaturas'), ('deezer', 'Assinaturas'),
    ('globoplay', 'Assinaturas'), ('icloud', 'Assinaturas'), ('assinatura', 'Assinaturas'),
    -- Habitação
    ('aluguel', 'Habitação'), ('condominio', 'Habitação'), ('agua', 'Habitação'),
    ('luz', 'Habitação'), ('energia', 'Habitação'), ('gas', 'Habitação'),
    -- Serviços
    ('internet', 'Serviços'), ('telefone', 'Serviços'), ('celular', 'Serviços'),
    ('tim', 'Serviços'), ('claro', 'Serviços'), ('vivo', 'Serviços'), ('lavanderia', 'Serviços'),
    -- Manutenção
    ('oficina', 'Manutenção'), ('reparo', 'Manutenção'), ('conserto', 'Manutenção'),
    ('ferragem', 'Manutenção'), ('material', 'Manutenção'),
    -- Receitas
    ('salario', 'Receitas'), ('liquido de vencimento', 'Receitas'), ('rendimento', 'Receitas')
) AS regra (termo, nome_categoria)
JOIN categoria ON categoria.nome = regra.nome_categoria AND categoria.padrao = true;
