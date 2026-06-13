package bcd.appfinanceirobackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Valida a restrição única da migration V5 com banco real: o banco precisa
 * rejeitar duas faturas com o mesmo {@code conta_id} e {@code mes_referencia},
 * porque o get-or-create de {@code gerarFatura} não cobre chamadas concorrentes
 * (ambas podem passar pelo SELECT antes de qualquer INSERT).
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("Migration V5 (fatura única por conta e mês) - Integração com banco real")
class FaturaUnicidadeIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("app_financeiro_test")
            .withUsername("postgres")
            .withPassword("1234");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID contaId;
    private UUID usuarioId;

    @BeforeEach
    void seedConta() {
        usuarioId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO usuario (id, nome, email, senha, cpf) VALUES (?, ?, ?, ?, ?)",
                usuarioId, "Dono Fatura", "dono-fatura@test.com", "hash", "98765432100");

        contaId = criarConta("Cartão Nubank");
    }

    @Test
    @DisplayName("rejeita segunda fatura com mesmo conta_id e mes_referencia")
    void rejeitaFaturaDuplicadaParaMesmaContaEMes() {
        inserirFatura(contaId, "2026-07");

        assertThatThrownBy(() -> inserirFatura(contaId, "2026-07"))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("uk_fatura_conta_mes");
    }

    @Test
    @DisplayName("permite faturas da mesma conta em meses diferentes")
    void permiteFaturasDaMesmaContaEmMesesDiferentes() {
        inserirFatura(contaId, "2026-07");
        inserirFatura(contaId, "2026-08");

        assertThat(contarFaturas(contaId)).isEqualTo(2);
    }

    @Test
    @DisplayName("permite faturas de contas diferentes no mesmo mês")
    void permiteFaturasDeContasDiferentesNoMesmoMes() {
        UUID outraContaId = criarConta("Cartão Inter");

        inserirFatura(contaId, "2026-07");
        inserirFatura(outraContaId, "2026-07");

        assertThat(contarFaturas(contaId) + contarFaturas(outraContaId)).isEqualTo(2);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private UUID criarConta(String nome) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO contas (id, usuario_id, nome, tipo) VALUES (?, ?, ?, ?)",
                id, usuarioId, nome, "CARTAO_CREDITO");
        return id;
    }

    private void inserirFatura(UUID conta, String mesReferencia) {
        jdbcTemplate.update(
                "INSERT INTO fatura (id, conta_id, mes_referencia, data_vencimento, valor_total, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), conta, mesReferencia, LocalDate.of(2026, 7, 28), BigDecimal.ZERO, "ABERTA");
    }

    private Integer contarFaturas(UUID conta) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM fatura WHERE conta_id = ?", Integer.class, conta);
    }
}
