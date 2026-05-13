package bcd.appfinanceirobackend.dto.transacao;

import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransacaoRequestDTO {
    private BigDecimal valor;
    private LocalDateTime data;
    private String descricao;
    private TipoTransacao tipoTransacao;
    private TipoPagamento formaPagamento;
    private UUID categoriaId;
    private UUID contaId;
}