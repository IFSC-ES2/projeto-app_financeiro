package bcd.appfinanceirobackend.dto.categoria;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategorizarTransacaoRequestDTO {
    private UUID categoriaId;
}
