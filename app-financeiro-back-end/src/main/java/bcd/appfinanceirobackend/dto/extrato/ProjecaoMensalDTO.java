package bcd.appfinanceirobackend.dto.extrato;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import lombok.Getter;
import lombok.Setter;


@Getter 
@Setter 
public class ProjecaoMensalDTO {
    private YearMonth mes;
    private BigDecimal saldoPrevisto;
    private BigDecimal totalDebitos;
    private BigDecimal totalCreditos;
    private int quantidadeVencimentos;
    private List<TransacaoResponseDTO> transacoes;
    private List<FaturaResumoDTO> faturas;
}
