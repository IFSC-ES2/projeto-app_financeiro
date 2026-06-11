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
import org.mockito.ArgumentCaptor;
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

    private Usuario usuario;
    private Usuario outroUsuario;
    private Conta conta;
    private CartaoCreditoRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        outroUsuario = new Usuario();
        outroUsuario.setId(UUID.randomUUID());

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setNome("Cartão Nubank");
        conta.setTipoConta(TipoConta.CARTAO_CREDITO);
        conta.setUsuario(usuario);

        dtoValido = new CartaoCreditoRequestDTO();
        dtoValido.setLimite(new BigDecimal("2000.00"));
        dtoValido.setDiaFechamento(20);
        dtoValido.setDiaVencimento(28);
    }

    @Nested
    @DisplayName("associar")
    class Associar {

        @Test
        @DisplayName("deve associar cartão de crédito à conta")
        void deveAssociarCartaoAConta() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.empty());
            when(cartaoCreditoRepository.save(any(CartaoCredito.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            CartaoCredito resultado = cartaoCreditoService.associar(conta.getId(), dtoValido, usuario);

            ArgumentCaptor<CartaoCredito> captor = ArgumentCaptor.forClass(CartaoCredito.class);
            verify(cartaoCreditoRepository).save(captor.capture());
            assertThat(captor.getValue().getConta()).isSameAs(conta);
            assertThat(resultado.getLimite()).isEqualByComparingTo("2000.00");
            assertThat(resultado.getDia_fechamento()).isEqualTo(20);
            assertThat(resultado.getDia_vencimento()).isEqualTo(28);
        }

        @Test
        @DisplayName("deve falhar quando a conta não existe")
        void deveFalharQuandoContaNaoExiste() {
            UUID contaId = UUID.randomUUID();
            when(contaRepository.findById(contaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartaoCreditoService.associar(contaId, dtoValido, usuario))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deve negar acesso a conta de outro usuário")
        void deveNegarAcessoAContaDeOutroUsuario() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

            assertThatThrownBy(() -> cartaoCreditoService.associar(conta.getId(), dtoValido, outroUsuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("deve rejeitar conta que já possui cartão")
        void deveRejeitarContaComCartaoExistente() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.of(new CartaoCredito()));

            assertThatThrownBy(() -> cartaoCreditoService.associar(conta.getId(), dtoValido, usuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);
            verify(cartaoCreditoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve rejeitar dias de fechamento ou vencimento inválidos")
        void deveRejeitarDiasInvalidos() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.empty());
            dtoValido.setDiaVencimento(32);

            assertThatThrownBy(() -> cartaoCreditoService.associar(conta.getId(), dtoValido, usuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            verify(cartaoCreditoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve rejeitar dias obrigatórios ausentes")
        void deveRejeitarDiasAusentes() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.empty());
            dtoValido.setDiaFechamento(null);

            assertThatThrownBy(() -> cartaoCreditoService.associar(conta.getId(), dtoValido, usuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("deve rejeitar limite negativo")
        void deveRejeitarLimiteNegativo() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.empty());
            dtoValido.setLimite(new BigDecimal("-1.00"));

            assertThatThrownBy(() -> cartaoCreditoService.associar(conta.getId(), dtoValido, usuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("buscarPorConta")
    class BuscarPorConta {

        @Test
        @DisplayName("deve retornar cartão da conta")
        void deveRetornarCartaoDaConta() {
            CartaoCredito cartao = new CartaoCredito();
            cartao.setId(UUID.randomUUID());
            cartao.setConta(conta);
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.of(cartao));

            CartaoCredito resultado = cartaoCreditoService.buscarPorConta(conta.getId(), usuario);

            assertThat(resultado).isSameAs(cartao);
        }

        @Test
        @DisplayName("deve falhar quando a conta não possui cartão")
        void deveFalharQuandoContaNaoPossuiCartao() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartaoCreditoService.buscarPorConta(conta.getId(), usuario))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
