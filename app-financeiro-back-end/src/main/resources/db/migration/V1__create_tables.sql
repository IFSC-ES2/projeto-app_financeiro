-- ============================================================
-- SmartBudget - Migration inicial
-- V1__create_tables.sql
-- ============================================================
-- Ordem de criação respeita dependências de FK:
-- usuarios → contas → cartoes_credito
--          → categorias
--          → importacoes → transacoes
--                        ↑
--          → faturas ────┘
-- ============================================================

-- ─── USUARIOS ───────────────────────────────────────────────

CREATE TABLE usuario(
                          id         CHAR(36)     NOT NULL,
                          nome       VARCHAR(100) NOT NULL,
                          email      VARCHAR(150) NOT NULL,
                          senha_hash VARCHAR(255) NOT NULL,
                          cpf        CHAR(11)     NOT NULL,
                          criado_em  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT pk_usuarios        PRIMARY KEY (id),
                          CONSTRAINT uk_usuarios_email  UNIQUE (email),
                          CONSTRAINT uk_usuarios_cpf    UNIQUE (cpf)
);

-- ─── CONTAS ─────────────────────────────────────────────────

CREATE TABLE contas (
                        id         CHAR(36)     NOT NULL,
                        usuario_id CHAR(36)     NOT NULL,
                        nome       VARCHAR(80)  NOT NULL,
                        banco      VARCHAR(80)      NULL,
                        tipo       ENUM(
                   'CORRENTE',
                   'POUPANCA',
                   'CARTAO_CREDITO',
                   'CARTEIRA'
               )            NOT NULL,
                        descricao  VARCHAR(255)     NULL,

                        CONSTRAINT pk_contas        PRIMARY KEY (id),
                        CONSTRAINT fk_contas_usuario FOREIGN KEY (usuario_id)
                            REFERENCES usuarios (id)
                            ON DELETE CASCADE
);

-- ─── CARTOES_CREDITO ────────────────────────────────────────
-- Existe apenas para contas do tipo CARTAO_CREDITO.
-- UNIQUE em conta_id garante relação 1:1 com contas.
-- limite é opcional — fora do escopo do MVP atual (R04).

CREATE TABLE cartao_credito (
                                 id              CHAR(36)       NOT NULL,
                                 conta_id        CHAR(36)       NOT NULL,
                                 dia_fechamento  INT            NOT NULL,
                                 dia_vencimento  INT            NOT NULL,
                                 limite          DECIMAL(10, 2)     NULL,

                                 CONSTRAINT pk_cartoes_credito        PRIMARY KEY (id),
                                 CONSTRAINT uk_cartoes_credito_conta  UNIQUE (conta_id),
                                 CONSTRAINT fk_cartoes_credito_conta  FOREIGN KEY (conta_id)
                                     REFERENCES contas (id)
                                     ON DELETE CASCADE
);

-- ─── CATEGORIAS ─────────────────────────────────────────────
-- usuario_id é NULL para categorias padrão do sistema.

CREATE TABLE categoria (
                            id         CHAR(36)    NOT NULL,
                            usuario_id CHAR(36)        NULL,
                            nome       VARCHAR(60) NOT NULL,
                            icone      VARCHAR(10)     NULL,
                            cor        VARCHAR(7)      NULL,
                            padrao     BOOLEAN     NOT NULL DEFAULT FALSE,

                            CONSTRAINT pk_categorias          PRIMARY KEY (id),
                            CONSTRAINT fk_categorias_usuario  FOREIGN KEY (usuario_id)
                                REFERENCES usuarios (id)
                                ON DELETE SET NULL
);

-- ─── IMPORTACOES ────────────────────────────────────────────

CREATE TABLE importacao (
                             id           CHAR(36)     NOT NULL,
                             usuario_id   CHAR(36)     NOT NULL,
                             nome_arquivo VARCHAR(255) NOT NULL,
                             formato      ENUM(
                     'CSV',
                     'XML',
                     'TXT',
                     'NFE'
                 )            NOT NULL,
                             status       ENUM(
                     'PENDENTE',
                     'PROCESSANDO',
                     'CONCLUIDO',
                     'ERRO'
                 )            NOT NULL DEFAULT 'PENDENTE',
                             importado_em DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             total_linhas INT  NOT NULL DEFAULT 0,
                             sucessos     INT  NOT NULL DEFAULT 0,
                             falhas       INT  NOT NULL DEFAULT 0,
                             mensagem_erro varchar(255),

                             CONSTRAINT pk_importacoes          PRIMARY KEY (id),
                             CONSTRAINT fk_importacoes_usuario  FOREIGN KEY (usuario_id)
                                 REFERENCES usuarios (id)
                                 ON DELETE CASCADE
);

-- ─── FATURAS ────────────────────────────────────────────────

CREATE TABLE fatura (
                         id              CHAR(36)       NOT NULL,
                         conta_id        CHAR(36)       NOT NULL,
                         mes_referencia  VARCHAR(7)     NOT NULL,
                         data_vencimento DATE           NOT NULL,
                         valor_total     DECIMAL(10, 2) NOT NULL,
                         status          ENUM(
                        'ABERTA',
                        'FECHADA',
                        'PAGA'
                    )              NOT NULL DEFAULT 'ABERTA',

                         CONSTRAINT pk_faturas        PRIMARY KEY (id),
                         CONSTRAINT fk_faturas_conta  FOREIGN KEY (conta_id)
                             REFERENCES contas (id)
                             ON DELETE CASCADE
);

-- ─── TRANSACOES ─────────────────────────────────────────────
-- categoria_id: NULL enquanto aguarda categorização.
-- importacao_id: NULL para transações adicionadas manualmente.
-- fatura_id: NULL para transações que não são de cartão de crédito.

CREATE TABLE transacoes (
                            id              CHAR(36)       NOT NULL,
                            conta_id        CHAR(36)       NOT NULL,
                            categoria_id    CHAR(36)           NULL,
                            importacao_id   CHAR(36)           NULL,
                            fatura_id       CHAR(36)           NULL,
                            valor           DECIMAL(10, 2) NOT NULL,
                            data            DATE           NOT NULL,
                            descricao       VARCHAR(255)       NULL,
                            tipo            ENUM(
                        'DEBITO',
                        'CREDITO',
                        'PARCELAMENTO',
                        'BOLETO'
                    )              NOT NULL,
                            forma_pagamento ENUM(
                        'PIX',
                        'CARTAO_DEBITO',
                        'CARTAO_CREDITO',
                        'DINHEIRO',
                        'BOLETO',
                        'TED_DOC'
                    )                  NULL,
                            categorizada    BOOLEAN        NOT NULL DEFAULT FALSE,
                            futura          BOOLEAN        NOT NULL DEFAULT FALSE,

                            CONSTRAINT pk_transacoes              PRIMARY KEY (id),
                            CONSTRAINT fk_transacoes_conta        FOREIGN KEY (conta_id)
                                REFERENCES contas (id)
                                ON DELETE CASCADE,
                            CONSTRAINT fk_transacoes_categoria    FOREIGN KEY (categoria_id)
                                REFERENCES categorias (id)
                                ON DELETE SET NULL,
                            CONSTRAINT fk_transacoes_importacao   FOREIGN KEY (importacao_id)
                                REFERENCES importacoes (id)
                                ON DELETE SET NULL,
                            CONSTRAINT fk_transacoes_fatura       FOREIGN KEY (fatura_id)
                                REFERENCES faturas (id)
                                ON DELETE SET NULL
);