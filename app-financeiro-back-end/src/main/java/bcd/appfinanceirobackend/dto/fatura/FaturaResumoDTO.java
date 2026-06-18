package bcd.appfinanceirobackend.dto.fatura;

import bcd.appfinanceirobackend.model.enums.StatusFatura;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class FaturaResumoDTO {
    private UUID faturaId;
    private UUID contaId;
    private String contaNome;
    private String mesReferencia;
    private LocalDate dataVencimento;
    private BigDecimal valorTotal;
    private StatusFatura status;
}
