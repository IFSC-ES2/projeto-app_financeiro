package bcd.appfinanceirobackend.dto.resumo;

import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GrupoPagamentoDTO {
    private TipoPagamento formaPagamento;
    private BigDecimal total;
    private String rotulo;
    private int quantidade;
    private BigDecimal percentual;
}
