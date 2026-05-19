package bcd.appfinanceirobackend.dto.importacao;

import bcd.appfinanceirobackend.model.enums.StatusImportacao;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ImportacaoResponseDTO {
    private UUID id;
    private StatusImportacao status;
    private int sucessos;
    private int falhas;
    private LocalDateTime importadoEm;
    private String mensagemErro;
}
