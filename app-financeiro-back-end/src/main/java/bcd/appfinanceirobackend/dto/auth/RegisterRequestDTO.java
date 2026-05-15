package bcd.appfinanceirobackend.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    private String nome;
    private String email;
    private String senha;
    private String cpf;
}
