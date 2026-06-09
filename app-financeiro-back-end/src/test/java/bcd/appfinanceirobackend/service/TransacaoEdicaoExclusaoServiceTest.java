package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.mapper.TransacaoMapper;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService - editar e excluir")
class TransacaoEdicaoExclusaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaUsuarioService contaUsuarioService;

    @Mock
    private CategoriaService categoriaService;

    private TransacaoMapper transacaoMapper;

    private TransacaoService transacaoService;

    private Usuario usuarioDono;
    private Usuario outroUsuario;
    private Conta contaAtual;
    private Conta novaConta;
    private Transacao transacao;
    private TransacaoRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
        transacaoMapper = new TransacaoMapper();

        transacaoService = new TransacaoService(
                transacaoRepository,
                contaUsuarioService,
                transacaoMapper,
                categoriaService
        );
        usuarioDono = new Usuario();
        usuarioDono.setId(UUID.randomUUID());
        usuarioDono.setNome("João Silva");
        usuarioDono.setEmail("joao@email.com");
        usuarioDono.setSenha("hash");
        usuarioDono.setCpf("12345678900");

        outroUsuario = new Usuario();
        outroUsuario.setId(UUID.randomUUID());
        outroUsuario.setNome("Outro Usuário");
        outroUsuario.setEmail("outro@email.com");
        outroUsuario.setSenha("hash");
        outroUsuario.setCpf("99988877766");

        contaAtual = new Conta();
        contaAtual.setId(UUID.randomUUID());
        contaAtual.setNome("Conta atual");
        contaAtual.setUsuario(usuarioDono);

        novaConta = new Conta();
        novaConta.setId(UUID.randomUUID());
        novaConta.setNome("Nova conta");
        novaConta.setUsuario(usuarioDono);

        transacao = new Transacao();
        transacao.setId(UUID.randomUUID());
        transacao.setConta(contaAtual);
        transacao.setValor(new BigDecimal("100.00"));
        transacao.setData(LocalDate.of(2026, 5, 30));
        transacao.setDescricao("Descrição antiga");
        transacao.setTipo(TipoTransacao.DEBITO);
        transacao.setFormaPagamento(TipoPagamento.PIX);
        transacao.setCategorizada(false);
        transacao.setFutura(false);

        dtoValido = new TransacaoRequestDTO();
        dtoValido.setValor(new BigDecimal("250.00"));
        dtoValido.setData(LocalDate.now());
        dtoValido.setDescricao("Descrição atualizada");
        dtoValido.setTipoTransacao(TipoTransacao.CREDITO);
        dtoValido.setFormaPagamento(TipoPagamento.PIX);
        dtoValido.setContaId(novaConta.getId());
    }

    @Nested
    @DisplayName("Edição")
    class Edicao {

        @Test
        @DisplayName("Edita transação própria com dados válidos")
        void deveEditarTransacaoPropriaComDadosValidos() {
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(contaUsuarioService.resolverConta(dtoValido, usuarioDono)).thenReturn(novaConta);
            when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

            TransacaoResponseDTO response = transacaoService.editar(transacao.getId(), dtoValido, usuarioDono);

            assertThat(response.getTransacaoId()).isEqualTo(transacao.getId());
            assertThat(response.getValor()).isEqualByComparingTo(dtoValido.getValor());
            assertThat(response.getData()).isEqualTo(dtoValido.getData());
            assertThat(response.getDescricao()).isEqualTo(dtoValido.getDescricao());
            assertThat(response.getTipoTransacao()).isEqualTo(dtoValido.getTipoTransacao());
            assertThat(response.getFormaPagamento()).isEqualTo(dtoValido.getFormaPagamento());
            assertThat(response.getContaId()).isEqualTo(novaConta.getId());
        }

        @Test
        @DisplayName("Persiste a transação editada")
        void devePersistirTransacaoEditada() {
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(contaUsuarioService.resolverConta(dtoValido, usuarioDono)).thenReturn(novaConta);
            when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

            transacaoService.editar(transacao.getId(), dtoValido, usuarioDono);

            verify(transacaoRepository).save(captor.capture());
            assertThat(captor.getValue().getConta()).isEqualTo(novaConta);
            assertThat(captor.getValue().getValor()).isEqualByComparingTo("250.00");
            assertThat(captor.getValue().getDescricao()).isEqualTo("Descrição atualizada");
        }

        @Test
        @DisplayName("Remove categoria quando categoriaId é nulo")
        void deveRemoverCategoriaQuandoCategoriaIdNulo() {
            Categoria categoriaAntiga = new Categoria();
            categoriaAntiga.setId(UUID.randomUUID());
            categoriaAntiga.setPadrao(true);

            transacao.setCategoria(categoriaAntiga);
            transacao.setCategorizada(true);
            dtoValido.setCategoriaId(null);

            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(contaUsuarioService.resolverConta(dtoValido, usuarioDono)).thenReturn(novaConta);
            when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

            TransacaoResponseDTO response = transacaoService.editar(transacao.getId(), dtoValido, usuarioDono);

            assertThat(response.getCategoriaId()).isNull();
            assertThat(response.isCategorizada()).isFalse();
            assertThat(transacao.getCategoria()).isNull();
            assertThat(transacao.getCategorizada()).isFalse();
        }

        @Test
        @DisplayName("Edita categoria quando categoriaId é informado")
        void deveEditarCategoriaQuandoCategoriaIdInformado() {
            Categoria categoria = new Categoria();
            categoria.setId(UUID.randomUUID());
            categoria.setNome("Alimentação");
            categoria.setPadrao(true);

            dtoValido.setCategoriaId(categoria.getId());

            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(contaUsuarioService.resolverConta(dtoValido, usuarioDono)).thenReturn(novaConta);
            when(categoriaService.buscarCategoriaPermitida(categoria.getId(), usuarioDono))
                    .thenReturn(categoria);
            when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

            TransacaoResponseDTO response = transacaoService.editar(transacao.getId(), dtoValido, usuarioDono);

            assertThat(response.getCategoriaId()).isEqualTo(categoria.getId());
            assertThat(response.isCategorizada()).isTrue();
            assertThat(transacao.getCategoria()).isEqualTo(categoria);
        }

        @Test
        @DisplayName("Lança 404 quando transação não existe")
        void deveLancarNotFoundQuandoTransacaoNaoExiste() {
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transacaoService.editar(transacao.getId(), dtoValido, usuarioDono))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transação não encontrada");

            verifyNoInteractions(contaUsuarioService, categoriaService);
            verify(transacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança 403 quando transação pertence a outro usuário")
        void deveLancarForbiddenQuandoTransacaoPertenceAOutroUsuario() {
            contaAtual.setUsuario(outroUsuario);

            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));

            assertThatThrownBy(() -> transacaoService.editar(transacao.getId(), dtoValido, usuarioDono))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Acesso negado a essa transação");

            verifyNoInteractions(contaUsuarioService, categoriaService);
            verify(transacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança 403 quando nova conta pertence a outro usuário")
        void deveLancarForbiddenQuandoNovaContaPertenceAOutroUsuario() {
            when(transacaoRepository.findById(transacao.getId()))
                    .thenReturn(Optional.of(transacao));

            when(contaUsuarioService.resolverConta(dtoValido, usuarioDono))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Acesso negado a esta conta"
                    ));

            assertThatThrownBy(() -> transacaoService.editar(transacao.getId(), dtoValido, usuarioDono))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Acesso negado a esta conta");

            verify(transacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança exceção quando valor é inválido")
        void deveLancarExcecaoQuandoValorInvalido() {
            dtoValido.setValor(BigDecimal.ZERO);

            assertThatThrownBy(() -> transacaoService.editar(transacao.getId(), dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("O valor informado deve ser maior que zero");

            verify(transacaoRepository, never()).findById(any());
            verify(transacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança exceção quando formaPagamento é nula")
        void deveLancarExcecaoQuandoFormaPagamentoNula() {
            dtoValido.setFormaPagamento(null);

            assertThatThrownBy(() -> transacaoService.editar(transacao.getId(), dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Campos obrigatórios não informados");

            verify(transacaoRepository, never()).findById(any());
            verify(transacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança 403 quando categoria personalizada pertence a outro usuário")
        void deveLancarForbiddenQuandoCategoriaPersonalizadaPertenceAOutroUsuario() {
            Categoria categoriaDeOutroUsuario = new Categoria();
            categoriaDeOutroUsuario.setId(UUID.randomUUID());
            categoriaDeOutroUsuario.setNome("Categoria privada");
            categoriaDeOutroUsuario.setPadrao(false);
            categoriaDeOutroUsuario.setUsuario(outroUsuario);

            dtoValido.setCategoriaId(categoriaDeOutroUsuario.getId());

            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(contaUsuarioService.resolverConta(dtoValido, usuarioDono)).thenReturn(novaConta);
            when(categoriaService.buscarCategoriaPermitida(categoriaDeOutroUsuario.getId(), usuarioDono))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Categoria não pertence ao usuário autenticado"
                    ));

            assertThatThrownBy(() -> transacaoService.editar(transacao.getId(), dtoValido, usuarioDono))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);

            verify(transacaoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Exclusão")
    class Exclusao {

        @Test
        @DisplayName("Exclui transação própria")
        void deveExcluirTransacaoPropria() {
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));

            transacaoService.excluir(transacao.getId(), usuarioDono);

            verify(transacaoRepository).delete(transacao);
        }

        @Test
        @DisplayName("Lança 404 quando transação não existe")
        void deveLancarNotFoundQuandoTransacaoNaoExiste() {
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transacaoService.excluir(transacao.getId(), usuarioDono))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transação não encontrada");

            verify(transacaoRepository, never()).delete(any(Transacao.class));
        }

        @Test
        @DisplayName("Lança 403 quando transação pertence a outro usuário")
        void deveLancarForbiddenQuandoTransacaoPertenceAOutroUsuario() {
            contaAtual.setUsuario(outroUsuario);

            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));

            assertThatThrownBy(() -> transacaoService.excluir(transacao.getId(), usuarioDono))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Acesso negado a essa transação");

            verify(transacaoRepository, never()).delete(any(Transacao.class));
        }
    }
}