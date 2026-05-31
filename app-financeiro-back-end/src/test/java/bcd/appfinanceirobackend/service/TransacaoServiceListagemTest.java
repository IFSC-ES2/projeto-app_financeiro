package bcd.appfinanceirobackend.service;

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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService - listarTransacoesPorUsuario")
class TransacaoServiceListagemTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    private Usuario usuarioAutenticado;
    private Conta conta;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setNome("Conta Corrente");
        conta.setUsuario(usuarioAutenticado);
    }

    @Nested
    @DisplayName("Listagem de transações")
    class ListagemTransacoes {

        @Test
        @DisplayName("Busca apenas transações do usuário autenticado")
        void deveBuscarApenasTransacoesDoUsuarioAutenticado() {
            when(transacaoRepository.findAllByContaUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of());

            transacaoService.listarTransacoesPorUsuario(usuarioAutenticado);

            verify(transacaoRepository).findAllByContaUsuarioId(usuarioAutenticado.getId());
        }

        @Test
        @DisplayName("Retorna lista vazia quando usuário não possui transações")
        void deveRetornarListaVaziaQuandoUsuarioNaoPossuiTransacoes() {
            when(transacaoRepository.findAllByContaUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of());

            List<TransacaoResponseDTO> response =
                    transacaoService.listarTransacoesPorUsuario(usuarioAutenticado);

            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("Mapeia entidades Transacao para TransacaoResponseDTO")
        void deveMapearEntidadesParaDTO() {
            Categoria categoria = new Categoria();
            categoria.setId(UUID.randomUUID());
            categoria.setNome("Alimentação");
            categoria.setPadrao(true);

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

            when(transacaoRepository.findAllByContaUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of(transacao));

            List<TransacaoResponseDTO> response =
                    transacaoService.listarTransacoesPorUsuario(usuarioAutenticado);

            assertThat(response).hasSize(1);

            TransacaoResponseDTO dto = response.get(0);

            assertThat(dto.getTransacaoId()).isEqualTo(transacao.getId());
            assertThat(dto.getValor()).isEqualByComparingTo(transacao.getValor());
            assertThat(dto.getData()).isEqualTo(transacao.getData());
            assertThat(dto.getDescricao()).isEqualTo(transacao.getDescricao());
            assertThat(dto.getTipoTransacao()).isEqualTo(transacao.getTipo());
            assertThat(dto.getFormaPagamento()).isEqualTo(transacao.getFormaPagamento());
            assertThat(dto.getContaId()).isEqualTo(conta.getId());
            assertThat(dto.getCategoriaId()).isEqualTo(categoria.getId());
            assertThat(dto.getImportacaoId()).isEqualTo(importacao.getId());
        }

        @Test
        @DisplayName("Mapeia categoriaId e importacaoId como nulos quando não existem")
        void deveMapearCategoriaIdEImportacaoIdNulosQuandoNaoExistem() {
            Transacao transacao = new Transacao();
            transacao.setId(UUID.randomUUID());
            transacao.setConta(conta);
            transacao.setCategoria(null);
            transacao.setImportacao(null);
            transacao.setValor(new BigDecimal("75.00"));
            transacao.setData(LocalDate.of(2026, 5, 30));
            transacao.setDescricao("Lanche");
            transacao.setTipo(TipoTransacao.DEBITO);
            transacao.setFormaPagamento(TipoPagamento.DINHEIRO);

            when(transacaoRepository.findAllByContaUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of(transacao));

            List<TransacaoResponseDTO> response =
                    transacaoService.listarTransacoesPorUsuario(usuarioAutenticado);

            assertThat(response).hasSize(1);
            assertThat(response.get(0).getCategoriaId()).isNull();
            assertThat(response.get(0).getImportacaoId()).isNull();
        }
    }
}