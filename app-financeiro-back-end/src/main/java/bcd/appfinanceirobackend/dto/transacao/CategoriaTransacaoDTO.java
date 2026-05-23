package bcd.appfinanceirobackend.dto.transacao;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class CategoriaTransacaoDTO {

    private UUID categoriaId;
    private String nome;
    private String icone;
    private String cor;
    private boolean padrao;

}