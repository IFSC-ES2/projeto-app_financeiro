package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.conta.ContaRequestDTO;
import bcd.appfinanceirobackend.dto.conta.ContaResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
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

import java.util.List;
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
@DisplayName("ContaService")
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private ContaService contaService;

    private Usuario usuarioAutenticado;
    private ContaRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");

        dtoValido = new ContaRequestDTO();
        dtoValido.setNome("Conta Nubank");
        dtoValido.setTipoConta(TipoConta.CORRENTE);
        dtoValido.setBanco("Nubank");
        dtoValido.setDescricao("Conta principal");
    }

    @Nested
    @DisplayName("Registro de conta")
    class RegistroConta {

        @Test
        @DisplayName("Cria conta para o usuário autenticado")
        void deveCriarContaParaUsuarioAutenticado() {
            when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
                Conta conta = invocation.getArgument(0);
                conta.setId(UUID.randomUUID());
                return conta;
            });
            ArgumentCaptor<Conta> captor = ArgumentCaptor.forClass(Conta.class);

            contaService.registrar(dtoValido, usuarioAutenticado);

            verify(contaRepository).save(captor.capture());
            Conta contaSalva = captor.getValue();

            assertThat(contaSalva.getNome()).isEqualTo(dtoValido.getNome());
            assertThat(contaSalva.getTipoConta()).isEqualTo(dtoValido.getTipoConta());
            assertThat(contaSalva.getBanco()).isEqualTo(dtoValido.getBanco());
            assertThat(contaSalva.getDescricao()).isEqualTo(dtoValido.getDescricao());
            assertThat(contaSalva.getUsuario()).isEqualTo(usuarioAutenticado);
        }

        @Test
        @DisplayName("Retorna DTO com os dados da conta registrada")
        void deveRetornarResponseDTOComDadosCorretos() {
            when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
                Conta conta = invocation.getArgument(0);
                conta.setId(UUID.randomUUID());
                return conta;
            });

            ContaResponseDTO response = contaService.registrar(dtoValido, usuarioAutenticado);

            assertThat(response).isNotNull();
            assertThat(response.getContaId()).isNotNull();
            assertThat(response.getNome()).isEqualTo(dtoValido.getNome());
            assertThat(response.getTipoConta()).isEqualTo(dtoValido.getTipoConta());
            assertThat(response.getBanco()).isEqualTo(dtoValido.getBanco());
            assertThat(response.getDescricao()).isEqualTo(dtoValido.getDescricao());
        }

        @Test
        @DisplayName("Permite banco nulo porque o campo é opcional")
        void deveRegistrarContaComBancoNulo() {
            dtoValido.setBanco(null);
            when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
                Conta conta = invocation.getArgument(0);
                conta.setId(UUID.randomUUID());
                return conta;
            });

            ContaResponseDTO response = contaService.registrar(dtoValido, usuarioAutenticado);

            assertThat(response.getBanco()).isNull();
        }

        @Test
        @DisplayName("Permite descrição nula porque o campo é opcional")
        void deveRegistrarContaComDescricaoNula() {
            dtoValido.setDescricao(null);
            when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
                Conta conta = invocation.getArgument(0);
                conta.setId(UUID.randomUUID());
                return conta;
            });

            ContaResponseDTO response = contaService.registrar(dtoValido, usuarioAutenticado);

            assertThat(response.getDescricao()).isNull();
        }
    }

    @Nested
    @DisplayName("Validação de campos obrigatórios")
    class ValidacaoCamposObrigatorios {

        @Test
        @DisplayName("Lança exceção quando nome é nulo")
        void deveLancarExcecaoQuandoNomeNulo() {
            dtoValido.setNome(null);

            assertThatThrownBy(() -> contaService.registrar(dtoValido, usuarioAutenticado))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Campos obrigatórios de uma conta não informados");

            verify(contaRepository, never()).save(any(Conta.class));
        }

        @Test
        @DisplayName("Lança exceção quando tipoConta é nulo")
        void deveLancarExcecaoQuandoTipoContaNulo() {
            dtoValido.setTipoConta(null);

            assertThatThrownBy(() -> contaService.registrar(dtoValido, usuarioAutenticado))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Campos obrigatórios de uma conta não informados");

            verify(contaRepository, never()).save(any(Conta.class));
        }

        @Test
        @DisplayName("Não salva conta quando request está inválido")
        void naoDeveSalvarQuandoRequestInvalido() {
            dtoValido.setNome(null);

            assertThatThrownBy(() -> contaService.registrar(dtoValido, usuarioAutenticado))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(contaRepository, never()).save(any(Conta.class));
        }
    }

    @Nested
    @DisplayName("Listagem de contas")
    class ListagemContas {

        @Test
        @DisplayName("Busca apenas contas do usuário autenticado")
        void deveBuscarApenasContasDoUsuarioAutenticado() {
            when(contaRepository.findByUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of());

            contaService.listarPorUsuario(usuarioAutenticado);

            verify(contaRepository).findByUsuarioId(usuarioAutenticado.getId());
        }

        @Test
        @DisplayName("Retorna lista vazia quando usuário não possui contas")
        void deveRetornarListaVaziaQuandoUsuarioNaoPossuiContas() {
            when(contaRepository.findByUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of());

            List<ContaResponseDTO> response = contaService.listarPorUsuario(usuarioAutenticado);

            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("Mapeia contas para DTO")
        void deveMapearContasParaDTO() {
            Conta contaCorrente = criarConta(
                    "Conta Corrente",
                    TipoConta.CORRENTE,
                    "Banco A",
                    "Principal"
            );
            Conta carteira = criarConta(
                    "Carteira",
                    TipoConta.CARTEIRA,
                    null,
                    "Dinheiro/manual"
            );

            when(contaRepository.findByUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of(contaCorrente, carteira));

            List<ContaResponseDTO> response = contaService.listarPorUsuario(usuarioAutenticado);

            assertThat(response).hasSize(2);

            assertThat(response.get(0).getContaId()).isEqualTo(contaCorrente.getId());
            assertThat(response.get(0).getNome()).isEqualTo(contaCorrente.getNome());
            assertThat(response.get(0).getTipoConta()).isEqualTo(contaCorrente.getTipoConta());
            assertThat(response.get(0).getBanco()).isEqualTo(contaCorrente.getBanco());
            assertThat(response.get(0).getDescricao()).isEqualTo(contaCorrente.getDescricao());

            assertThat(response.get(1).getContaId()).isEqualTo(carteira.getId());
            assertThat(response.get(1).getNome()).isEqualTo(carteira.getNome());
            assertThat(response.get(1).getTipoConta()).isEqualTo(carteira.getTipoConta());
            assertThat(response.get(1).getBanco()).isNull();
            assertThat(response.get(1).getDescricao()).isEqualTo(carteira.getDescricao());
        }
    }

    @Nested
    @DisplayName("Mapeamento para response")
    class MapeamentoResponse {

        @Test
        @DisplayName("Converte entidade Conta para ContaResponseDTO")
        void deveConverterContaParaResponseDTO() {
            Conta conta = criarConta(
                    "Poupança",
                    TipoConta.POUPANCA,
                    "Banco B",
                    "Reserva"
            );

            ContaResponseDTO response = contaService.toResponse(conta);

            assertThat(response.getContaId()).isEqualTo(conta.getId());
            assertThat(response.getNome()).isEqualTo(conta.getNome());
            assertThat(response.getTipoConta()).isEqualTo(conta.getTipoConta());
            assertThat(response.getBanco()).isEqualTo(conta.getBanco());
            assertThat(response.getDescricao()).isEqualTo(conta.getDescricao());
        }
    }

    @Nested
    @DisplayName("Remoção de conta")
    class RemocaoConta {

        @Test
        @DisplayName("Remove conta própria sem transações vinculadas")
        void deveRemoverContaPropriaSemTransacoes() {
            Conta conta = criarConta("Conta Corrente", TipoConta.CORRENTE, "Banco A", "Principal");
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(transacaoRepository.existsByContaId(conta.getId())).thenReturn(false);

            contaService.removerConta(usuarioAutenticado, conta.getId());

            verify(contaRepository).delete(conta);
        }

        @Test
        @DisplayName("Lança 404 quando a conta não existe")
        void deveLancarNotFoundQuandoContaNaoExiste() {
            UUID id = UUID.randomUUID();
            when(contaRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contaService.removerConta(usuarioAutenticado, id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Conta não encontrada");

            verify(contaRepository, never()).delete(any(Conta.class));
            verifyNoInteractions(transacaoRepository);
        }

        @Test
        @DisplayName("Lança 403 ao tentar remover conta de outro usuário")
        void deveLancarForbiddenQuandoContaDeOutroUsuario() {
            Usuario outroUsuario = new Usuario();
            outroUsuario.setId(UUID.randomUUID());

            Conta conta = criarConta("Conta Alheia", TipoConta.CORRENTE, "Banco B", "De outro");
            conta.setUsuario(outroUsuario);
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

            assertThatThrownBy(() -> contaService.removerConta(usuarioAutenticado, conta.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Acesso negado a esta conta")
                    .extracting(excecao -> ((ResponseStatusException) excecao).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            verify(contaRepository, never()).delete(any(Conta.class));
            verify(transacaoRepository, never()).existsByContaId(any());
        }

        @Test
        @DisplayName("Lança 409 ao tentar remover conta com transações vinculadas")
        void deveLancarConflictQuandoContaPossuiTransacoes() {
            Conta conta = criarConta("Conta Movimentada", TipoConta.CORRENTE, "Banco C", "Com histórico");
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(transacaoRepository.existsByContaId(conta.getId())).thenReturn(true);

            assertThatThrownBy(() -> contaService.removerConta(usuarioAutenticado, conta.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Não é possível remover uma conta com transações vinculadas")
                    .extracting(excecao -> ((ResponseStatusException) excecao).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);

            verify(contaRepository, never()).delete(any(Conta.class));
        }
    }

    private Conta criarConta(String nome, TipoConta tipoConta, String banco, String descricao) {
        Conta conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setNome(nome);
        conta.setTipoConta(tipoConta);
        conta.setBanco(banco);
        conta.setDescricao(descricao);
        conta.setUsuario(usuarioAutenticado);
        return conta;
    }
}