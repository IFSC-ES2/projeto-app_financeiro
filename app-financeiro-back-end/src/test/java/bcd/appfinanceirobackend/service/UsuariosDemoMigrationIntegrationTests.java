package bcd.appfinanceirobackend.service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class UsuariosDemoMigrationIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("smartbudget_migration_test")
                    .withUsername("migration_test")
                    .withPassword(UUID.randomUUID().toString());

    @TempDir
    Path migrationsLegadas;

    @Test
    void bancoNovoNaoExecutaSeedDeUsuarios() {
        Flyway flyway = configurar(
                "novo",
                "classpath:db/migration"
        );

        flyway.migrate();
        flyway.validate();

        assertThat(contar(
                "SELECT count(*) FROM novo.usuario"
        )).isZero();

        assertThat(contar(
                "SELECT count(*) FROM novo.categoria WHERE padrao = true"
        )).isEqualTo(7);

        assertThat(contar(
                "SELECT count(*) FROM novo.flyway_schema_history WHERE version = '3'"
        )).isZero();

        assertThat(contar(
                "SELECT count(*) FROM novo.flyway_schema_history " +
                "WHERE script = 'B6__baseline_sem_usuarios_demo.sql'"
        )).isEqualTo(1);
    }

    @Test
    void bancoExistenteRemoveDemoEPreservaUsuariosReais() throws IOException {

        String[] arquivos = {
                "V1__create_tables.sql",
                "V2__seed_categorias.sql",
                "V3__seed_usuarios.sql",
                "V4__ajusta_tipo_transacao.sql",
                "V5__adiciona_contador_duplicidades_importacao.sql",
                "V6__unique_fatura_conta_mes.sql"
        };

        for (String arquivo : arquivos) {
            try (InputStream sql = new ClassPathResource(
                    "db/migration/" + arquivo
            ).getInputStream()) {

                Files.copy(
                        sql,
                        migrationsLegadas.resolve(arquivo)
                );
            }
        }

        configurar(
                "legado",
                "filesystem:" + migrationsLegadas.toAbsolutePath()
        ).migrate();

        assertThat(contar(
                "SELECT count(*) FROM legado.usuario"
        )).isEqualTo(2);

        UUID real = UUID.randomUUID();
        UUID contaDemo = UUID.randomUUID();
        UUID contaReal = UUID.randomUUID();

        UUID demo = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        jdbc().update(
                "INSERT INTO legado.usuario " +
                        "(id,nome,email,senha,cpf) VALUES (?,?,?,?,?)",
                real,
                "Real de teste",
                "real@example.invalid",
                "hash-ficticio",
                "52998224725"
        );

        jdbc().update(
                "INSERT INTO legado.contas " +
                        "(id,usuario_id,nome,tipo) VALUES (?,?,?,?)",
                contaDemo,
                demo,
                "Conta demo",
                "CORRENTE"
        );

        jdbc().update(
                "INSERT INTO legado.contas " +
                        "(id,usuario_id,nome,tipo) VALUES (?,?,?,?)",
                contaReal,
                real,
                "Conta real",
                "CORRENTE"
        );

        jdbc().update(
                "INSERT INTO legado.categoria " +
                        "(id,usuario_id,nome,padrao) VALUES (?,?,?,?)",
                UUID.randomUUID(),
                demo,
                "Categoria demo",
                false
        );

        Flyway flyway = configurar(
                "legado",
                "classpath:db/migration"
        );

        flyway.migrate();
        flyway.validate();

        assertThat(contar(
                "SELECT count(*) FROM legado.usuario"
        )).isEqualTo(1);

        assertThat(contar(
                "SELECT count(*) FROM legado.contas " +
                        "WHERE id = '" + contaReal + "'"
        )).isEqualTo(1);

        assertThat(contar(
                "SELECT count(*) FROM legado.contas " +
                        "WHERE id = '" + contaDemo + "'"
        )).isZero();

        assertThat(contar(
                "SELECT count(*) FROM legado.categoria " +
                        "WHERE nome = 'Categoria demo'"
        )).isZero();

        assertThat(contar(
                "SELECT count(*) FROM legado.categoria WHERE padrao = true"
        )).isEqualTo(7);

        assertThat(contar(
                "SELECT count(*) FROM legado.flyway_schema_history " +
                        "WHERE version = '3'"
        )).isEqualTo(1);

        assertThat(contar(
                "SELECT count(*) FROM legado.flyway_schema_history " +
                        "WHERE version = '7'"
        )).isEqualTo(1);
    }

    private Flyway configurar(String schema, String location) {
        return Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .schemas(schema)
                .defaultSchema(schema)
                .locations(location)
                .load();
    }

    private JdbcTemplate jdbc() {
        return new JdbcTemplate(
                new DriverManagerDataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
        );
    }

    private int contar(String sql) {
        return jdbc().queryForObject(sql, Integer.class);
    }
}