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
                          id         UUID     NOT NULL,
                          nome       VARCHAR(255) NOT NULL,
                          email      VARCHAR(255) NOT NULL UNIQUE,
                          senha      VARCHAR(255) NOT NULL,
                          cpf        CHAR(11)     NOT NULL,
                          created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT pk_usuarios        PRIMARY KEY (id),
                          CONSTRAINT uk_usuarios_email  UNIQUE (email),
                          CONSTRAINT uk_usuarios_cpf    UNIQUE (cpf)
);

-- ─── CONTAS ─────────────────────────────────────────────────

CREATE TABLE contas (
                        id         UUID     NOT NULL,
                        usuario_id UUID     NOT NULL,
                        nome       VARCHAR(80)  NOT NULL,
                        banco      VARCHAR(80),
                        tipo       VARCHAR(20)  NOT NULL,
                        descricao  VARCHAR(255),

                        CONSTRAINT pk_contas        PRIMARY KEY (id),
                        CONSTRAINT fk_contas_usuario FOREIGN KEY (usuario_id)
                            REFERENCES usuario (id)
                            ON DELETE CASCADE
);

-- ─── CARTOES_CREDITO ────────────────────────────────────────
-- Existe apenas para contas do tipo CARTAO_CREDITO.
-- UNIQUE em conta_id garante relação 1:1 com contas.
-- limite é opcional — fora do escopo do MVP atual (R04).

CREATE TABLE cartao_credito (
                                 id              UUID       NOT NULL,
                                 conta_id        UUID       NOT NULL,
                                 dia_fechamento  INT            NOT NULL,
                                 dia_vencimento  INT            NOT NULL,
                                 limite          DECIMAL(10, 2),

                                 CONSTRAINT pk_cartoes_credito        PRIMARY KEY (id),
                                 CONSTRAINT uk_cartoes_credito_conta  UNIQUE (conta_id),
                                 CONSTRAINT fk_cartoes_credito_conta  FOREIGN KEY (conta_id)
                                     REFERENCES contas (id)
                                     ON DELETE CASCADE
);

-- ─── CATEGORIAS ─────────────────────────────────────────────
-- usuario_id é NULL para categorias padrão do sistema.

CREATE TABLE categoria (
                            id         UUID    NOT NULL,
                            usuario_id UUID,
                            nome       VARCHAR(60) NOT NULL,
                            icone      VARCHAR(10),
                            cor        VARCHAR(7),
                            padrao     BOOLEAN     NOT NULL DEFAULT FALSE,

                            CONSTRAINT pk_categorias          PRIMARY KEY (id),
                            CONSTRAINT fk_categorias_usuario  FOREIGN KEY (usuario_id)
                                REFERENCES usuario (id)
                                ON DELETE SET NULL
);

-- ─── IMPORTACOES ────────────────────────────────────────────

CREATE TABLE importacao (
                             id           UUID     NOT NULL,
                             usuario_id   UUID     NOT NULL,
                             nome_arquivo VARCHAR(255) NOT NULL,
                             formato      VARCHAR(10)  NOT NULL,
                             status       VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
                             importado_em TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             total_linhas INT  NOT NULL DEFAULT 0,
                             sucessos     INT  NOT NULL DEFAULT 0,
                             falhas       INT  NOT NULL DEFAULT 0,
                             mensagem_erro varchar(255),

                             CONSTRAINT pk_importacoes          PRIMARY KEY (id),
                             CONSTRAINT fk_importacoes_usuario  FOREIGN KEY (usuario_id)
                                 REFERENCES usuario (id)
                                 ON DELETE CASCADE
);

-- ─── FATURAS ────────────────────────────────────────────────

CREATE TABLE fatura (
                         id              UUID       NOT NULL,
                         conta_id        UUID       NOT NULL,
                         mes_referencia  VARCHAR(7)     NOT NULL,
                         data_vencimento DATE           NOT NULL,
                         valor_total     DECIMAL(10, 2) NOT NULL,
                         status          VARCHAR(10)    NOT NULL DEFAULT 'ABERTA',

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
                            id              UUID       NOT NULL,
                            conta_id        UUID       NOT NULL,
                            categoria_id    UUID,
                            importacao_id   UUID,
                            fatura_id       UUID,
                            valor           DECIMAL(10, 2) NOT NULL,
                            data            DATE           NOT NULL,
                            descricao       VARCHAR(255),
                            tipo            VARCHAR(20)    NOT NULL,
                            forma_pagamento VARCHAR(20),
                            categorizada    BOOLEAN        NOT NULL DEFAULT FALSE,
                            futura          BOOLEAN        NOT NULL DEFAULT FALSE,

                            CONSTRAINT pk_transacoes              PRIMARY KEY (id),
                            CONSTRAINT fk_transacoes_conta        FOREIGN KEY (conta_id)
                                REFERENCES contas (id)
                                ON DELETE CASCADE,
                            CONSTRAINT fk_transacoes_categoria    FOREIGN KEY (categoria_id)
                                REFERENCES categoria (id)
                                ON DELETE SET NULL,
                            CONSTRAINT fk_transacoes_importacao   FOREIGN KEY (importacao_id)
                                REFERENCES importacao (id)
                                ON DELETE SET NULL,
                            CONSTRAINT fk_transacoes_fatura       FOREIGN KEY (fatura_id)
                                REFERENCES fatura (id)
                                ON DELETE SET NULL
);