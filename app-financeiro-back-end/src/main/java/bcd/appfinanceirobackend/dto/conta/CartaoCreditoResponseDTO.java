package bcd.appfinanceirobackend.dto.conta;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CartaoCreditoResponseDTO {
    private UUID cartaoId;
    private UUID contaId;
    private BigDecimal limite;
    private int diaFechamento;
    private int diaVencimento;
}
