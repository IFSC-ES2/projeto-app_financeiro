package bcd.appfinanceirobackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida a regra da migration V4 com banco real: dados legados com
 * {@code tipo = 'BOLETO'} ou {@code tipo = 'PARCELAMENTO'} precisam ser
 * normalizados, porque o enum Java {@code TipoTransacao} deixou de mapear esses
 * valores e o Hibernate quebraria ao lê-los.
 *
 * As linhas legadas são inseridas via JDBC bruto (a coluna é VARCHAR sem CHECK,
 * então aceita o valor antigo que o enum já não tem). O SQL executado é o
 * próprio arquivo da V4 lido do classpath — o Flyway já a roda no startup, mas
 * sobre uma tabela vazia (no-op), então a reexecutamos sobre as linhas legadas.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("Migration V4 (ajuste TipoTransacao) - Integração com banco real")
class AjusteTipoTransacaoMigrationIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("app_financeiro_test")
            .withUsername("postgres")
            .withPassword("1234");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID contaId;

    @BeforeEach
    void seedConta() {
        UUID usuarioId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO usuario (id, nome, email, senha, cpf) VALUES (?, ?, ?, ?, ?)",
                usuarioId, "Dono Migration", "dono-migration@test.com", "hash", "12345678901");

        contaId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO contas (id, usuario_id, nome, tipo) VALUES (?, ?, ?, ?)",
                contaId, usuarioId, "Conta Corrente", "CORRENTE");
    }

    @Test
    @DisplayName("tipo='BOLETO' legado vira DEBITO e move BOLETO para forma_pagamento")
    void boletoLegado_viraDebito_eMoveFormaPagamento() throws IOException {
        UUID id = inserirTransacaoLegada("BOLETO", new BigDecimal("150.00"), null);

        aplicarMigracaoV4();

        assertThat(tipoDe(id)).isEqualTo("DEBITO");
        assertThat(formaPagamentoDe(id)).isEqualTo("BOLETO");
    }

    @Test
    @DisplayName("tipo='PARCELAMENTO' legado vira DEBITO sem inventar forma_pagamento")
    void parcelamentoLegado_viraDebito() throws IOException {
        UUID id = inserirTransacaoLegada("PARCELAMENTO", new BigDecimal("90.00"), null);

        aplicarMigracaoV4();

        assertThat(tipoDe(id)).isEqualTo("DEBITO");
        assertThat(formaPagamentoDe(id)).isNull();
    }

    @Test
    @DisplayName("Após a migração, nenhuma linha permanece com tipo fora de DEBITO/CREDITO")
    void aposMigracao_nenhumaLinhaForaDeDebitoOuCredito() throws IOException {
        inserirTransacaoLegada("BOLETO", new BigDecimal("150.00"), null);
        inserirTransacaoLegada("PARCELAMENTO", new BigDecimal("90.00"), null);
        inserirTransacaoLegada("DEBITO", new BigDecimal("30.00"), null);
        inserirTransacaoLegada("CREDITO", new BigDecimal("500.00"), null);

        aplicarMigracaoV4();

        Integer fora = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transacoes WHERE tipo NOT IN ('DEBITO', 'CREDITO')", Integer.class);
        assertThat(fora).isZero();
    }

    @Test
    @DisplayName("BOLETO com forma_pagamento já preenchida vira DEBITO e preserva a forma existente")
    void boletoComFormaPagamentoExistente_preservaForma() throws IOException {
        UUID id = inserirTransacaoLegada("BOLETO", new BigDecimal("150.00"), "PIX");

        aplicarMigracaoV4();

        assertThat(tipoDe(id)).isEqualTo("DEBITO");
        assertThat(formaPagamentoDe(id)).isEqualTo("PIX");
    }

    @Test
    @DisplayName("Linhas já corretas (DEBITO/CREDITO) não são alteradas pela migração")
    void debitoECreditoExistentes_naoSaoAlterados() throws IOException {
        UUID debito = inserirTransacaoLegada("DEBITO", new BigDecimal("30.00"), null);
        UUID credito = inserirTransacaoLegada("CREDITO", new BigDecimal("500.00"), null);

        aplicarMigracaoV4();

        assertThat(tipoDe(debito)).isEqualTo("DEBITO");
        assertThat(tipoDe(credito)).isEqualTo("CREDITO");
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private UUID inserirTransacaoLegada(String tipo, BigDecimal valor, String formaPagamento) {
        UUID id = UUID.randomUUID();
        if (formaPagamento == null) {
            jdbcTemplate.update(
                    "INSERT INTO transacoes (id, conta_id, valor, data, descricao, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                    id, contaId, valor, LocalDate.of(2024, 1, 1), "legado " + tipo, tipo);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO transacoes (id, conta_id, valor, data, descricao, tipo, forma_pagamento) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    id, contaId, valor, LocalDate.of(2024, 1, 1), "legado " + tipo, tipo, formaPagamento);
        }
        return id;
    }

    /** Executa o SQL real da V4 (lido do classpath), instrução a instrução. */
    private void aplicarMigracaoV4() throws IOException {
        String conteudo = new String(new ClassPathResource(
                "db/migration/V4__ajusta_tipo_transacao.sql").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        String semComentarios = conteudo.lines()
                .filter(linha -> !linha.stripLeading().startsWith("--"))
                .collect(Collectors.joining("\n"));
        for (String instrucao : semComentarios.split(";")) {
            if (!instrucao.isBlank()) {
                jdbcTemplate.execute(instrucao.strip());
            }
        }
    }

    private String tipoDe(UUID id) {
        return jdbcTemplate.queryForObject("SELECT tipo FROM transacoes WHERE id = ?", String.class, id);
    }

    private String formaPagamentoDe(UUID id) {
        return jdbcTemplate.queryForObject("SELECT forma_pagamento FROM transacoes WHERE id = ?", String.class, id);
    }
}
