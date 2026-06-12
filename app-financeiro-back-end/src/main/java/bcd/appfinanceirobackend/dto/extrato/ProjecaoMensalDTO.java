package bcd.appfinanceirobackend.dto.extrato;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ProjecaoMensalDTO {
    private Integer ano;
    private Integer mes;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private BigDecimal saldoPrevisto;
    private BigDecimal totalDebitos;
    private BigDecimal totalCreditos;
    private List<FaturaResumoDTO> faturas;
    private List<TransacaoResponseDTO> transacoes;
}
