package bcd.appfinanceirobackend.dto.resumo;

import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GrupoPagamentoDTO {
    private TipoPagamento formaPagamento;
    private String rotulo;
    private BigDecimal total;
    private int quantidade;
    private BigDecimal percentual;
}
