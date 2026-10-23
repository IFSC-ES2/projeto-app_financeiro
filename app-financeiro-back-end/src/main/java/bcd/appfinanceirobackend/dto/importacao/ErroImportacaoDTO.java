package bcd.appfinanceirobackend.dto.importacao;

import lombok.Getter;
import lombok.Setter;

@Setter 
@Getter 
public class ErroImportacaoDTO {
    private Integer numeroRegistro;
    private String campo;
    private String mensagem;
}
