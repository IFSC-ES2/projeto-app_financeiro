package bcd.appfinanceirobackend.mapper;

import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Importacao;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransacaoMapper")
class TransacaoMapperTest {

    private TransacaoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransacaoMapper();
    }

    @Test
    @DisplayName("Mapeia todos os campos da entidade para o DTO")
    void deveMapearTodosOsCampos() {
        Conta conta = new Conta();
        conta.setId(UUID.randomUUID());

        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());

        Importacao importacao = new Importacao();
        importacao.setId(UUID.randomUUID());

        Transacao transacao = new Transacao();
        transacao.setId(UUID.randomUUID());
        transacao.setConta(conta);
        transacao.setCategoria(categoria);
        transacao.setImportacao(importacao);
        transacao.setValor(new BigDecimal("120.50"));
        transacao.setData(LocalDate.of(2026, 5, 30));
        transacao.setDescricao("Mercado");
        transacao.setTipo(TipoTransacao.DEBITO);
        transacao.setFormaPagamento(TipoPagamento.PIX);
        transacao.setCategorizada(true);

        TransacaoResponseDTO dto = mapper.toResponse(transacao);

        assertThat(dto.getTransacaoId()).isEqualTo(transacao.getId());
        assertThat(dto.getValor()).isEqualByComparingTo(transacao.getValor());
        assertThat(dto.getData()).isEqualTo(transacao.getData());
        assertThat(dto.getDescricao()).isEqualTo(transacao.getDescricao());
        assertThat(dto.getTipoTransacao()).isEqualTo(transacao.getTipo());
        assertThat(dto.getFormaPagamento()).isEqualTo(transacao.getFormaPagamento());
        assertThat(dto.getContaId()).isEqualTo(conta.getId());
        assertThat(dto.getCategoriaId()).isEqualTo(categoria.getId());
        assertThat(dto.getImportacaoId()).isEqualTo(importacao.getId());
        assertThat(dto.isCategorizada()).isTrue();
    }

    @Test
    @DisplayName("Mapeia categoriaId e importacaoId como null quando não existem")
    void deveMapearRelacionamentosNulos() {
        Conta conta = new Conta();
        conta.setId(UUID.randomUUID());

        Transacao transacao = new Transacao();
        transacao.setId(UUID.randomUUID());
        transacao.setConta(conta);
        transacao.setValor(BigDecimal.TEN);
        transacao.setData(LocalDate.of(2026, 5, 30));
        transacao.setTipo(TipoTransacao.DEBITO);
        transacao.setFormaPagamento(TipoPagamento.DINHEIRO);
        transacao.setCategorizada(false);

        TransacaoResponseDTO dto = mapper.toResponse(transacao);

        assertThat(dto.getCategoriaId()).isNull();
        assertThat(dto.getImportacaoId()).isNull();
        assertThat(dto.getContaId()).isEqualTo(conta.getId());
        assertThat(dto.isCategorizada()).isFalse();
    }
}