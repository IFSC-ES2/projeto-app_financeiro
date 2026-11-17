package bcd.appfinanceirobackend.security;

import bcd.appfinanceirobackend.model.Usuario;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void rotacaoDeSegredoInvalidaTokensAnteriores() {
        JwtUtil anterior = new JwtUtil(
                gerarSegredo(), 3600000
        );

        JwtUtil novo = new JwtUtil(
                gerarSegredo(), 3600000
        );

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Usuario de teste");
        usuario.setEmail("usuario@example.invalid");

        String tokenAntigo = anterior
                .gerarToken(usuario)
                .getAccessToken();

        String tokenNovo = novo
                .gerarToken(usuario)
                .getAccessToken();

        assertThat(anterior.validar(tokenAntigo)).isTrue();
        assertThat(novo.validar(tokenAntigo)).isFalse();
        assertThat(novo.validar(tokenNovo)).isTrue();

        assertThat(novo.extrairEmail(tokenNovo))
                .isEqualTo(usuario.getEmail());
    }

    private String gerarSegredo() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }
}