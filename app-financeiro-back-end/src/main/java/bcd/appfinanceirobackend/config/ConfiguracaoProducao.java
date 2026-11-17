package bcd.appfinanceirobackend.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

@Configuration(proxyBeanMethods = false)
public class ConfiguracaoProducao {

    // Executado antes da criacao do DataSource, Flyway e JwtUtil.
    @Bean
    public static BeanFactoryPostProcessor validarConfiguracao(Environment ambiente) {
        return beanFactory -> {
            String[] perfis = ambiente.getActiveProfiles();

            if (perfis.length == 0) {
                perfis = ambiente.getDefaultProfiles();
            }

            if (perfis.length == 1 &&
                    ("dev".equals(perfis[0]) || "test".equals(perfis[0]))) {
                return;
            }

            if (!Arrays.equals(perfis, new String[]{"prod"})) {
                throw new IllegalStateException(
                        "Use exatamente um perfil: dev, test ou prod."
                );
            }

            validarUrl(obrigatorio(ambiente, "spring.datasource.url"));

            String usuario = obrigatorio(
                    ambiente, "spring.datasource.username"
            );

            if ("postgres".equalsIgnoreCase(usuario)) {
                throw new IllegalStateException(
                        "Configure um usuario de banco exclusivo para producao."
                );
            }

            String senha = obrigatorio(
                    ambiente, "spring.datasource.password"
            );

            if (senha.length() < 16
                    || senha.equalsIgnoreCase("smartbudget-local-only")
                    || senha.equalsIgnoreCase("troque-esta-senha")) {

                throw new IllegalStateException(
                        "Configure uma senha de banco forte e exclusiva (minimo 16 caracteres)."
                );
            }

            String segredo = obrigatorio(ambiente, "jwt.secret");

            if (!segredo.matches("[0-9a-fA-F]{64}")
                    || segredo.toLowerCase(Locale.ROOT).matches("(.)\\1{63}")
                    || segredo.equalsIgnoreCase("0123456789abcdef".repeat(4))) {

                throw new IllegalStateException(
                        "JWT_SECRET invalido. Gere com: openssl rand -hex 32"
                );
            }

            obrigatorio(ambiente, "app.cors.allowed-origins");
        };
    }

    private static String obrigatorio(
            Environment ambiente,
            String propriedade
    ) {
        String valor = ambiente.getProperty(propriedade);

        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Configuracao obrigatoria ausente: " + propriedade
            );
        }

        return valor;
    }

    private static void validarUrl(String url) {
        boolean valida = false;

        if (url.startsWith("jdbc:postgresql://")) {
            try {
                URI uri = URI.create(url.substring(5));

                valida = uri.getHost() != null
                        && uri.getPort() > 0
                        && uri.getPort() <= 65535
                        && uri.getPath() != null
                        && uri.getPath().length() > 1;

            } catch (IllegalArgumentException ignored) {
                // Nao inclui a URL ou credenciais na mensagem de erro.
            }
        }

        if (!valida) {
            throw new IllegalStateException(
                    "Configure uma URL JDBC PostgreSQL valida (host, porta e banco)."
            );
        }
    }
}