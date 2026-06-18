package bcd.appfinanceirobackend.dto.conta;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartaoCreditoRequestDTO {
    private BigDecimal limite;
    private Integer diaFechamento;
    private Integer diaVencimento;
}
