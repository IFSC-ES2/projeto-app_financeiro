package bcd.appfinanceirobackend.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.security.SecureRandom;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoProducaoTest {

    private static final String SEGREDO = gerarSegredo();

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withInitializer(new ConfigDataApplicationContextInitializer())
                    .withUserConfiguration(ConfiguracaoProducao.class);

    private ApplicationContextRunner producaoValida() {
        return runner.withPropertyValues(
                "spring.profiles.active=prod",
                "spring.datasource.url=jdbc:postgresql://localhost:5432/smartbudget",
                "spring.datasource.username=smartbudget",
                "spring.datasource.password=senha-ficticia-exclusiva-testes",
                "jwt.secret=" + SEGREDO,
                "app.cors.allowed-origins=https://example.invalid"
        );
    }

    @Test
    void producaoComConfiguracoesValidasInicia() {
        producaoValida().run(context ->
                assertThat(context).hasNotFailed()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "spring.datasource.url",
            "spring.datasource.username",
            "spring.datasource.password",
            "jwt.secret",
            "app.cors.allowed-origins"
    })
    void configuracaoObrigatoriaVaziaImpedeInicializacao(String propriedade) {
        producaoValida()
                .withPropertyValues(propriedade + "=")
                .run(context -> {
                    assertThat(context).hasFailed();

                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining(propriedade);
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234",
            "smartbudget-local-only",
            "troque-esta-senha"
    })
    void senhaPadraoOuFracaImpedeInicializacao(String senha) {
        producaoValida()
                .withPropertyValues(
                        "spring.datasource.password=" + senha
                )
                .run(context ->
                        assertThat(context).hasFailed()
                );
    }

    @Test
    void usuarioPadraoImpedeInicializacao() {
        producaoValida()
                .withPropertyValues(
                        "spring.datasource.username=postgres"
                )
                .run(context ->
                        assertThat(context).hasFailed()
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "curto",
            "smartbudget-development-only-not-a-production-signing-key",
            "smartbudget-test-only-not-a-production-signing-key",
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcd",
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
    void segredoInseguroImpedeInicializacao(String segredo) {
        producaoValida()
                .withPropertyValues("jwt.secret=" + segredo)
                .run(context ->
                        assertThat(context).hasFailed()
                );
    }

    @Test
    void urlComHostAusenteImpedeInicializacao() {
        producaoValida()
                .withPropertyValues(
                        "spring.datasource.url=jdbc:postgresql://:5432/smartbudget"
                )
                .run(context ->
                        assertThat(context).hasFailed()
                );
    }

    @Test
    void configuracaoRenderComVariaveisSeparadasFunciona() {
        runner.withPropertyValues(
                "spring.profiles.active=prod",
                "DB_HOST=db.example.invalid",
                "DB_PORT=5432",
                "DB_NAME=smartbudget",
                "SPRING_DATASOURCE_USERNAME=smartbudget",
                "SPRING_DATASOURCE_PASSWORD=senha-ficticia-exclusiva-testes",
                "JWT_SECRET=" + SEGREDO,
                "CORS_ALLOWED_ORIGINS=https://example.invalid"
        ).run(context ->
                assertThat(context).hasNotFailed()
        );
    }

    @Test
    void perfilDefaultSemSegredosFalha() {
        runner.withPropertyValues(
                "spring.profiles.default=prod",
                "jwt.secret="
        ).run(context ->
                assertThat(context).hasFailed()
        );
    }

    @Test
    void perfisDeDesenvolvimentoETesteFuncionamIsoladamente() {
        runner.withPropertyValues(
                "spring.profiles.active=dev"
        ).run(context -> {
            assertThat(context).hasNotFailed();

            assertThat(context.getEnvironment()
                    .getProperty("jwt.secret"))
                    .isEqualTo(
                            "smartbudget-development-only-not-a-production-signing-key"
                    );
        });

        runner.withPropertyValues(
                "spring.profiles.active=test"
        ).run(context -> {
            assertThat(context).hasNotFailed();

            assertThat(context.getEnvironment()
                    .getProperty("jwt.secret"))
                    .isEqualTo(
                            "smartbudget-test-only-not-a-production-signing-key"
                    );
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "prod,dev",
            "prod,test",
            "dev,test",
            "staging"
    })
    void perfisMisturadosOuDesconhecidosFalham(String perfis) {
        producaoValida()
                .withPropertyValues(
                        "spring.profiles.active=" + perfis
                )
                .run(context ->
                        assertThat(context).hasFailed()
                );
    }

    private static String gerarSegredo() {
        byte[] bytes = new byte[32];

        new SecureRandom().nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }
}