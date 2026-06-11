package bcd.appfinanceirobackend.dto.resumo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
public class GrupoCategoriaDTO {
    private UUID categoriaID;
    private String nome;
    private String icone;
    private String cor;
    private BigDecimal total;
    private int quantidade;
    private BigDecimal percentual;
}
