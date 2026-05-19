package bcd.appfinanceirobackend.dto.transacao;

import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TransacaoResponseDTO {
    private UUID transacaoId;
    private BigDecimal valor;
    private LocalDate data;
    private String descricao;
    private TipoTransacao tipoTransacao;
    private TipoPagamento formaPagamento;
    private UUID importacaoId;
    private UUID categoriaId;
    private UUID contaId;
}
