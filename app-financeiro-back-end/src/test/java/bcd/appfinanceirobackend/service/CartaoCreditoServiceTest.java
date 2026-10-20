package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.conta.CartaoCreditoRequestDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.repository.CartaoCreditoRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartaoCreditoService")
class CartaoCreditoServiceTest {

    @Mock
    private CartaoCreditoRepository cartaoCreditoRepository;

    @Mock
    private ContaRepository contaRepository;

    @InjectMocks
    private CartaoCreditoService cartaoCreditoService;

    private Usuario dono;
    private Usuario outroUsuario;
    private Conta contaCartao;
    private Conta contaCorrente;

    @BeforeEach
    void setUp() {
        dono = usuario("dono@test.com");
        outroUsuario = usuario("outro@test.com");
        contaCartao = conta(dono, TipoConta.CARTAO_CREDITO, "Cartao Nubank");
        contaCorrente = conta(dono, TipoConta.CORRENTE, "Conta Corrente");
    }

    private Usuario usuario(String email) {
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setNome("Usuario Teste");
        u.setEmail(email);
        return u;
    }

    private Conta conta(Usuario usuario, TipoConta tipo, String nome) {
        Conta c = new Conta();
        c.setId(UUID.randomUUID());
        c.setNome(nome);
        c.setTipoConta(tipo);
        c.setUsuario(usuario);
        return c;
    }

    private CartaoCreditoRequestDTO dtoValido() {
        CartaoCreditoRequestDTO dto = new CartaoCreditoRequestDTO();
        dto.setLimite(new BigDecimal("5000.00"));
        dto.setDiaFechamento(3);
        dto.setDiaVencimento(10);
        return dto;
    }

    @Nested
    @DisplayName("associar()")
    class Associar {

        @Test
        @DisplayName("associa cartao a uma conta CARTAO_CREDITO valida do usuario")
        void deveAssociarCartaoAContaValida() {
            when(contaRepository.findById(contaCartao.getId())).thenReturn(Optional.of(contaCartao));
            when(cartaoCreditoRepository.findByContaId(contaCartao.getId())).thenReturn(Optional.empty());
            when(cartaoCreditoRepository.save(any(CartaoCredito.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            CartaoCredito salvo = cartaoCreditoService.associar(contaCartao.getId(), dtoValido(), dono);

            assertThat(salvo.getConta()).isEqualTo(contaCartao);
            assertThat(salvo.getLimite()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(salvo.getDia_fechamento()).isEqualTo(3);
            assertThat(salvo.getDia_vencimento()).isEqualTo(10);
            verify(cartaoCreditoRepository).save(any(CartaoCredito.class));
        }

        @Test
        @DisplayName("impede associar cartao em conta de outro usuario")
        void deveImpedirContaDeOutroUsuario() {
            Conta contaDeOutro = conta(outroUsuario, TipoConta.CARTAO_CREDITO, "Cartao de outro");
            when(contaRepository.findById(contaDeOutro.getId())).thenReturn(Optional.of(contaDeOutro));

            assertThatThrownBy(() -> cartaoCreditoService.associar(contaDeOutro.getId(), dtoValido(), dono))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

            verify(cartaoCreditoRepository, never()).save(any());
        }

        @Test
        @DisplayName("impede associar cartao em conta que nao seja CARTAO_CREDITO")
        void deveImpedirContaQueNaoSejaCartaoCredito() {
            when(contaRepository.findById(contaCorrente.getId())).thenReturn(Optional.of(contaCorrente));

            assertThatThrownBy(() -> cartaoCreditoService.associar(contaCorrente.getId(), dtoValido(), dono))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("CARTAO_CREDITO");
                    });

            verify(cartaoCreditoRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanca ResourceNotFound quando a conta nao existe")
        void deveLancarQuandoContaNaoExiste() {
            UUID inexistente = UUID.randomUUID();
            when(contaRepository.findById(inexistente)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartaoCreditoService.associar(inexistente, dtoValido(), dono))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Conta não encontrada");
        }

        @Test
        @DisplayName("impede associar quando a conta ja possui cartao")
        void deveImpedirQuandoContaJaPossuiCartao() {
            CartaoCredito existente = new CartaoCredito();
            existente.setConta(contaCartao);
            when(contaRepository.findById(contaCartao.getId())).thenReturn(Optional.of(contaCartao));
            when(cartaoCreditoRepository.findByContaId(contaCartao.getId())).thenReturn(Optional.of(existente));

            assertThatThrownBy(() -> cartaoCreditoService.associar(contaCartao.getId(), dtoValido(), dono))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

            verify(cartaoCreditoRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejeita dias de fechamento/vencimento ausentes ou fora de 1 a 31")
        void deveRejeitarDiasInvalidos() {
            when(contaRepository.findById(contaCartao.getId())).thenReturn(Optional.of(contaCartao));
            when(cartaoCreditoRepository.findByContaId(contaCartao.getId())).thenReturn(Optional.empty());

            CartaoCreditoRequestDTO semDia = dtoValido();
            semDia.setDiaVencimento(null);
            assertThatThrownBy(() -> cartaoCreditoService.associar(contaCartao.getId(), semDia, dono))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

            CartaoCreditoRequestDTO diaFora = dtoValido();
            diaFora.setDiaFechamento(32);
            assertThatThrownBy(() -> cartaoCreditoService.associar(contaCartao.getId(), diaFora, dono))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

            verify(cartaoCreditoRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejeita limite negativo")
        void deveRejeitarLimiteNegativo() {
            when(contaRepository.findById(contaCartao.getId())).thenReturn(Optional.of(contaCartao));
            when(cartaoCreditoRepository.findByContaId(contaCartao.getId())).thenReturn(Optional.empty());

            CartaoCreditoRequestDTO dto = dtoValido();
            dto.setLimite(new BigDecimal("-1.00"));

            assertThatThrownBy(() -> cartaoCreditoService.associar(contaCartao.getId(), dto, dono))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

            verify(cartaoCreditoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("buscarPorConta()")
    class BuscarPorConta {

        @Test
        @DisplayName("retorna o cartao associado a conta do usuario")
        void deveRetornarCartaoDaConta() {
            CartaoCredito cartao = new CartaoCredito();
            cartao.setConta(contaCartao);
            cartao.setDia_fechamento(3);
            cartao.setDia_vencimento(10);
            when(contaRepository.findById(contaCartao.getId())).thenReturn(Optional.of(contaCartao));
            when(cartaoCreditoRepository.findByContaId(contaCartao.getId())).thenReturn(Optional.of(cartao));

            CartaoCredito resultado = cartaoCreditoService.buscarPorConta(contaCartao.getId(), dono);

            assertThat(resultado).isEqualTo(cartao);
        }

        @Test
        @DisplayName("lanca ResourceNotFound quando a conta nao possui cartao associado")
        void deveLancarQuandoContaSemCartao() {
            when(contaRepository.findById(contaCartao.getId())).thenReturn(Optional.of(contaCartao));
            when(cartaoCreditoRepository.findByContaId(contaCartao.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartaoCreditoService.buscarPorConta(contaCartao.getId(), dono))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("cartão de crédito");
        }
    }
}
