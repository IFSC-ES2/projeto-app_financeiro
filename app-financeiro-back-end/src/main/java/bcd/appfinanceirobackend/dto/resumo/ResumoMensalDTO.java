package bcd.appfinanceirobackend.dto.resumo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ResumoMensalDTO {
    private Integer ano;
    private Integer mes;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private BigDecimal totalRecebido;
    private BigDecimal totalGasto;
    private BigDecimal saldo;
    private UUID categoriaMaiorGastoId;
    private String categoriaMaiorGastoNome;
    private BigDecimal categoriaMaiorGastoTotal;
    private BigDecimal variacaoPercentualGastos;
    private Boolean possuiTransacoes;
}
