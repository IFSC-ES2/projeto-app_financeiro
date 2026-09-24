package bcd.appfinanceirobackend.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaRequestDTO {
    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(max = 60, message = "O nome da categoria deve ter no máximo 60 caracteres")
    private String nome;

    @Size(max = 10, message = "O ícone da categoria deve ter no máximo 10 caracteres")
    private String icone;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "A cor da categoria deve estar no formato hexadecimal #RRGGBB")
    private String cor;
}
