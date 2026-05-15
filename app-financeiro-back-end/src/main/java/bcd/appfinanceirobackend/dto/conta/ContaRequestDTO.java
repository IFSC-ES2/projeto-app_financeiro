package bcd.appfinanceirobackend.dto.conta;

import bcd.appfinanceirobackend.model.enums.TipoConta;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContaRequestDTO {
    private String nome;
    private TipoConta tipoConta;
    private String banco;
    private String descricao;
}
