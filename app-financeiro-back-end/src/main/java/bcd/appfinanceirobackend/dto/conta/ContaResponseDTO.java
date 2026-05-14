package bcd.appfinanceirobackend.dto.conta;

import bcd.appfinanceirobackend.model.enums.TipoConta;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ContaResponseDTO {
    private UUID contaId;
    private String nome;
    private TipoConta tipoConta;
    private String banco;
    private String descricao;
}
