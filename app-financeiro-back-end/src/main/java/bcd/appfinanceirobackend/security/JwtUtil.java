package bcd.appfinanceirobackend.security;

import bcd.appfinanceirobackend.dto.auth.TokenDTO;
import bcd.appfinanceirobackend.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtUtil(@Value("${jwt.secret}") String segredo,
                   @Value("${jwt.expiration-ms}") long expiracaoMs) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public TokenDTO gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date validade = new Date(agora.getTime() + expiracaoMs);

        String jwt = Jwts.builder()
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId() == null ? null : usuario.getId().toString())
                .claim("nome", usuario.getNome())
                .issuedAt(agora)
                .expiration(validade)
                .signWith(chave)
                .compact();

        TokenDTO dto = new TokenDTO();
        dto.setAccessToken(jwt);
        dto.setTipo("Bearer");
        dto.setExpiracao(validade.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return dto;
    }

    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validar(String token) {
        try {
            Jwts.parser().verifyWith(chave).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
