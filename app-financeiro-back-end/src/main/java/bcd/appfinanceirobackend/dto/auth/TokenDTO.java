package bcd.appfinanceirobackend.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TokenDTO {
    private String accessToken;
    private String tipo;
    private LocalDateTime expiracao;
}
