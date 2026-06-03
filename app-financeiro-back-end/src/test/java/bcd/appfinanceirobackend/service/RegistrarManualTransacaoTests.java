package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService - registrarManual")
class RegistrarManualTransacaoTests {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    private Usuario usuarioDono;
    private Usuario outroUsuario;
    private Conta conta;
    private TransacaoRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
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

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setNome("Conta Corrente");
        conta.setUsuario(usuarioDono);

        dtoValido = new TransacaoRequestDTO();
        dtoValido.setValor(new BigDecimal("150.00"));
        dtoValido.setData(LocalDate.now());
        dtoValido.setDescricao("Mercado");
        dtoValido.setTipoTransacao(TipoTransacao.DEBITO);
        dtoValido.setFormaPagamento(TipoPagamento.PIX);
        dtoValido.setContaId(conta.getId());
    }

    // ─────────────────────────── CAMPOS OBRIGATÓRIOS ────────────────────────────

    @Nested
    @DisplayName("Validação de campos obrigatórios")
    class ValidacaoCamposObrigatorios {

        @Test
        @DisplayName("Lança exceção quando valor é nulo")
        void deveLancarExcecaoQuandoValorNulo() {
            dtoValido.setValor(null);

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Campos obrigatórios não informados");
        }

        @Test
        @DisplayName("Lança exceção quando valor é zero")
        void deveLancarExcecaoQuandoValorZero() {
            dtoValido.setValor(BigDecimal.ZERO);

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("O valor informado deve ser maior que zero");
        }

        @Test
        @DisplayName("Lança exceção quando valor é negativo")
        void deveLancarExcecaoQuandoValorNegativo() {
            dtoValido.setValor(new BigDecimal("-50.00"));

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("O valor informado deve ser maior que zero");
        }

        @Test
        @DisplayName("Lança exceção quando contaId é nulo")
        void deveLancarExcecaoQuandoContaIdNulo() {
            dtoValido.setContaId(null);

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Campos obrigatórios não informados");
        }

        @Test
        @DisplayName("Lança exceção quando data é nula")
        void deveLancarExcecaoQuandoDataNula() {
            dtoValido.setData(null);

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Campos obrigatórios não informados");
        }

        @Test
        @DisplayName("Lança exceção quando tipoTransacao é nulo")
        void deveLancarExcecaoQuandoTipoTransacaoNulo() {
            dtoValido.setTipoTransacao(null);

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Campos obrigatórios não informados");
        }

        @Test
        @DisplayName("Não consulta o repositório quando campos obrigatórios estão ausentes")
        void naoDeveConsultarRepositorioComCamposFaltando() {
            dtoValido.setValor(null);

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(contaRepository, transacaoRepository, categoriaRepository);
        }
    }

    // ──────────────────────────── VALIDAÇÃO DE CONTA ─────────────────────────────

    @Nested
    @DisplayName("Validação de conta")
    class ValidacaoConta {

        @Test
        @DisplayName("Lança ResourceNotFoundException quando conta não existe")
        void deveLancarExcecaoQuandoContaNaoEncontrada() {
            when(contaRepository.findById(dtoValido.getContaId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Conta não encontrada");
        }

        @Test
        @DisplayName("Lança ResponseStatusException 403 quando a conta pertence a outro usuário")
        void deveLancarForbiddenQuandoContaNaoPertenceAoUsuario() {
            conta.setUsuario(outroUsuario);
            when(contaRepository.findById(dtoValido.getContaId())).thenReturn(Optional.of(conta));

            assertThatThrownBy(() -> transacaoService.registrarManual(dtoValido, usuarioDono))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Acesso negado a esta conta");

            verifyNoInteractions(transacaoRepository);
        }
    }

    // ──────────────────────────── REGISTRO COM SUCESSO ───────────────────────────

    @Nested
    @DisplayName("Registro bem-sucedido")
    class RegistroBemSucedido {

        @BeforeEach
        void mockRepositorios() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> {
                Transacao t = inv.getArgument(0);
                t.setId(UUID.randomUUID());
                return t;
            });
        }

        @Test
        @DisplayName("Retorna DTO com os dados da transação registrada")
        void deveRetornarResponseDTOComDadosCorretos() {
            TransacaoResponseDTO response = transacaoService.registrarManual(dtoValido, usuarioDono);

            assertThat(response).isNotNull();
            assertThat(response.getValor()).isEqualByComparingTo(dtoValido.getValor());
            assertThat(response.getData()).isEqualTo(dtoValido.getData());
            assertThat(response.getDescricao()).isEqualTo(dtoValido.getDescricao());
            assertThat(response.getTipoTransacao()).isEqualTo(dtoValido.getTipoTransacao());
            assertThat(response.getFormaPagamento()).isEqualTo(dtoValido.getFormaPagamento());
            assertThat(response.getContaId()).isEqualTo(conta.getId());
        }


        @Test
        @DisplayName("Persiste a transação com futura = false para data atual")
        void deveSalvarTransacaoComFuturaFalseParaDataAtual() {
            dtoValido.setData(LocalDate.now());
            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

            transacaoService.registrarManual(dtoValido, usuarioDono);

            verify(transacaoRepository).save(captor.capture());
            assertThat(captor.getValue().getFutura()).isFalse();
        }

        @Test
        @DisplayName("Persiste a transação com categoria quando categoriaId é informado")
        void deveSalvarTransacaoComCategoriaQuandoCategoriaIdInformado() {
            Categoria categoria = new Categoria();
            categoria.setId(UUID.randomUUID());
            categoria.setNome("Alimentação");
            categoria.setPadrao(true);

            dtoValido.setCategoriaId(categoria.getId());

            when(categoriaRepository.findById(categoria.getId())).thenReturn(Optional.of(categoria));

            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

            TransacaoResponseDTO response = transacaoService.registrarManual(dtoValido, usuarioDono);

            verify(transacaoRepository).save(captor.capture());

            assertThat(captor.getValue().getCategoria()).isEqualTo(categoria);
            assertThat(captor.getValue().getCategorizada()).isTrue();
            assertThat(response.getCategoriaId()).isEqualTo(categoria.getId());
            assertThat(response.isCategorizada()).isTrue();
        }

        @Test
        @DisplayName("Persiste a transação com futura = true para data futura")
        void deveSalvarTransacaoComFuturaTrueParaDataFutura() {
            dtoValido.setData(LocalDate.now().plusDays(5));
            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

            transacaoService.registrarManual(dtoValido, usuarioDono);

            verify(transacaoRepository).save(captor.capture());
            assertThat(captor.getValue().getFutura()).isTrue();
        }

        @Test
        @DisplayName("Persiste a transação com importacaoId nulo")
        void deveSalvarTransacaoComImportacaoIdNulo() {
            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

            transacaoService.registrarManual(dtoValido, usuarioDono);

            verify(transacaoRepository).save(captor.capture());
            assertThat(captor.getValue().getImportacao()).isNull();
        }

        @Test
        @DisplayName("Persiste a transação com faturaId nulo")
        void deveSalvarTransacaoComFaturaIdNulo() {
            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

            transacaoService.registrarManual(dtoValido, usuarioDono);

            verify(transacaoRepository).save(captor.capture());
            assertThat(captor.getValue().getFatura()).isNull();
        }

        @Test
        @DisplayName("Persiste a transação com a conta correta")
        void deveSalvarTransacaoComContaCorreta() {
            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

            transacaoService.registrarManual(dtoValido, usuarioDono);

            verify(transacaoRepository).save(captor.capture());
            assertThat(captor.getValue().getConta()).isEqualTo(conta);
        }

        @Test
        @DisplayName("Registra transação sem descrição (campo opcional)")
        void deveRegistrarTransacaoSemDescricao() {
            dtoValido.setDescricao(null);

            TransacaoResponseDTO response = transacaoService.registrarManual(dtoValido, usuarioDono);

            assertThat(response.getDescricao()).isNull();
        }

        @Test
        @DisplayName("Registra transação sem forma de pagamento (campo opcional)")
        void deveRegistrarTransacaoSemFormaPagamento() {
            dtoValido.setFormaPagamento(null);

            TransacaoResponseDTO response = transacaoService.registrarManual(dtoValido, usuarioDono);

            assertThat(response.getFormaPagamento()).isNull();
        }

        @Test
        @DisplayName("Persiste e retorna transação não categorizada quando categoriaId não é informado")
        void deveSalvarERetornarTransacaoNaoCategorizadaQuandoCategoriaIdNaoInformado() {
            dtoValido.setCategoriaId(null);

            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

            TransacaoResponseDTO response = transacaoService.registrarManual(dtoValido, usuarioDono);

            verify(transacaoRepository).save(captor.capture());

            assertThat(captor.getValue().getCategoria()).isNull();
            assertThat(captor.getValue().getCategorizada()).isFalse();
            assertThat(response.getCategoriaId()).isNull();
            assertThat(response.isCategorizada()).isFalse();
        }

        @Test
        @DisplayName("Chama o repositório de transação exatamente uma vez")
        void deveChamarRepositorioDeTransacaoUmaVez() {
            transacaoService.registrarManual(dtoValido, usuarioDono);

            verify(transacaoRepository, times(1)).save(any(Transacao.class));
        }

        @ParameterizedTest(name = "TipoTransacao = {0}")
        @EnumSource(TipoTransacao.class)
        @DisplayName("Registra corretamente para cada TipoTransacao")
        void deveRegistrarParaCadaTipoTransacao(TipoTransacao tipo) {
            dtoValido.setTipoTransacao(tipo);

            TransacaoResponseDTO response = transacaoService.registrarManual(dtoValido, usuarioDono);

            assertThat(response.getTipoTransacao()).isEqualTo(tipo);
        }
    }
}