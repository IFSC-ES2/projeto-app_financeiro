package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.comum.PaginaDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Importacao;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService - listagem paginada e filtrada")
class ListarTransacoesPaginadoTests {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    private Usuario usuario;
    private Conta conta;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setUsuario(usuario);
    }

    private Transacao transacao() {
        Transacao t = new Transacao();
        t.setId(UUID.randomUUID());
        t.setConta(conta);
        t.setValor(new BigDecimal("10.00"));
        t.setData(LocalDate.of(2024, 1, 10));
        t.setTipo(TipoTransacao.DEBITO);
        return t;
    }

    @Test
    @DisplayName("Mapeia Page<Transacao> para o envelope PaginaDTO")
    void deveMapearPageParaPaginaDTO() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "data"));
        Page<Transacao> page = new PageImpl<>(List.of(transacao()), pageable, 1);
        when(transacaoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PaginaDTO<TransacaoResponseDTO> resultado =
                transacaoService.listarTransacoesPorUsuario(usuario, null, null, null, null, null, pageable);

        assertThat(resultado.conteudo()).hasSize(1);
        assertThat(resultado.totalElementos()).isEqualTo(1);
        assertThat(resultado.pagina()).isZero();
        assertThat(resultado.tamanho()).isEqualTo(20);
        assertThat(resultado.totalPaginas()).isEqualTo(1);
        assertThat(resultado.primeira()).isTrue();
        assertThat(resultado.ultima()).isTrue();
    }

    @Test
    @DisplayName("Calcula metadados de página intermediária corretamente")
    void deveCalcularMetadadosDePaginaIntermediaria() {
        Pageable pageable = PageRequest.of(1, 10);
        // total de 25 elementos, página 1 (segunda de três)
        Page<Transacao> page = new PageImpl<>(List.of(transacao(), transacao()), pageable, 25);
        when(transacaoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PaginaDTO<TransacaoResponseDTO> resultado =
                transacaoService.listarTransacoesPorUsuario(usuario, null, null, null, null, null, pageable);

        assertThat(resultado.totalElementos()).isEqualTo(25);
        assertThat(resultado.totalPaginas()).isEqualTo(3);
        assertThat(resultado.pagina()).isEqualTo(1);
        assertThat(resultado.primeira()).isFalse();
        assertThat(resultado.ultima()).isFalse();
    }

    @Test
    @DisplayName("Repassa o Pageable e uma Specification não nula ao repositório")
    void deveRepassarPageableESpecificationAoRepositorio() {
        Pageable pageable = PageRequest.of(2, 5);
        when(transacaoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        transacaoService.listarTransacoesPorUsuario(
                usuario, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                UUID.randomUUID(), TipoTransacao.CREDITO, null, pageable);

        ArgumentCaptor<Specification<Transacao>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transacaoRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

        assertThat(specCaptor.getValue()).isNotNull();
        assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
    }

    @Test
    @DisplayName("Mapeia todos os campos da Transacao para o TransacaoResponseDTO")
    void deveMapearCamposDaTransacaoParaDTO() {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.setNome("Alimentação");
        categoria.setPadrao(true);

        Importacao importacao = new Importacao();
        importacao.setId(UUID.randomUUID());

        Transacao t = transacao();
        t.setCategoria(categoria);
        t.setImportacao(importacao);
        t.setValor(new BigDecimal("120.50"));
        t.setData(LocalDate.of(2026, 5, 30));
        t.setDescricao("Mercado");
        t.setTipo(TipoTransacao.DEBITO);
        t.setFormaPagamento(TipoPagamento.PIX);

        Pageable pageable = PageRequest.of(0, 20);
        when(transacaoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(t), pageable, 1));

        PaginaDTO<TransacaoResponseDTO> resultado =
                transacaoService.listarTransacoesPorUsuario(usuario, null, null, null, null, null, pageable);

        assertThat(resultado.conteudo()).hasSize(1);
        TransacaoResponseDTO dto = resultado.conteudo().getFirst();
        assertThat(dto.getTransacaoId()).isEqualTo(t.getId());
        assertThat(dto.getValor()).isEqualByComparingTo(t.getValor());
        assertThat(dto.getData()).isEqualTo(t.getData());
        assertThat(dto.getDescricao()).isEqualTo(t.getDescricao());
        assertThat(dto.getTipoTransacao()).isEqualTo(t.getTipo());
        assertThat(dto.getFormaPagamento()).isEqualTo(t.getFormaPagamento());
        assertThat(dto.getContaId()).isEqualTo(conta.getId());
        assertThat(dto.getCategoriaId()).isEqualTo(categoria.getId());
        assertThat(dto.getImportacaoId()).isEqualTo(importacao.getId());
    }

    @Test
    @DisplayName("Mapeia categoriaId e importacaoId como nulos quando não existem")
    void deveMapearCategoriaIdEImportacaoIdNulosQuandoNaoExistem() {
        Transacao t = transacao(); // sem categoria nem importação
        t.setDescricao("Lanche");
        t.setFormaPagamento(TipoPagamento.DINHEIRO);

        Pageable pageable = PageRequest.of(0, 20);
        when(transacaoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(t), pageable, 1));

        PaginaDTO<TransacaoResponseDTO> resultado =
                transacaoService.listarTransacoesPorUsuario(usuario, null, null, null, null, null, pageable);

        TransacaoResponseDTO dto = resultado.conteudo().getFirst();
        assertThat(dto.getCategoriaId()).isNull();
        assertThat(dto.getImportacaoId()).isNull();
    }
}
