package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.mapper.TransacaoMapper;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService - categorização")
class CategorizarTransacaoTests {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaUsuarioService contaUsuarioService;

    @Mock
    private CategoriaService categoriaService;

    private TransacaoMapper transacaoMapper;

    private TransacaoService transacaoService;

    private Usuario dono;
    private Usuario outro;
    private Conta conta;
    private Transacao transacao;
    private Categoria categoriaPadrao;

    @BeforeEach
    void setUp() {
        transacaoMapper = new TransacaoMapper();

        transacaoService = new TransacaoService(
                transacaoRepository,
                contaUsuarioService,
                transacaoMapper,
                categoriaService
        );

        dono = usuario("dono@email.com");
        outro = usuario("outro@email.com");

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setNome("Conta Corrente");
        conta.setUsuario(dono);

        transacao = new Transacao();
        transacao.setId(UUID.randomUUID());
        transacao.setConta(conta);
        transacao.setCategorizada(false);

        categoriaPadrao = new Categoria();
        categoriaPadrao.setId(UUID.randomUUID());
        categoriaPadrao.setNome("Alimentação");
        categoriaPadrao.setPadrao(true);
    }

    private Usuario usuario(String email) {
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setEmail(email);
        return u;
    }

    @Nested
    @DisplayName("categorizar()")
    class Categorizar {

        @Test
        @DisplayName("Atualiza a categoria e marca categorizada = true")
        void deveCategorizarComSucesso() {
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(categoriaService.buscarCategoriaPermitida(categoriaPadrao.getId(), dono))
                    .thenReturn(categoriaPadrao);
            when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

            TransacaoResponseDTO response =
                    transacaoService.categorizar(transacao.getId(), categoriaPadrao.getId(), dono);

            assertThat(transacao.getCategoria()).isEqualTo(categoriaPadrao);
            assertThat(transacao.getCategorizada()).isTrue();
            assertThat(response.getCategoriaId()).isEqualTo(categoriaPadrao.getId());
        }

        @Test
        @DisplayName("Persiste a transação com categorizada = true mesmo se estava false")
        void devePersistirCategorizadaTrue() {
            transacao.setCategorizada(false);
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(categoriaService.buscarCategoriaPermitida(categoriaPadrao.getId(), dono))
                    .thenReturn(categoriaPadrao);
            when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
            transacaoService.categorizar(transacao.getId(), categoriaPadrao.getId(), dono);

            verify(transacaoRepository).save(captor.capture());
            assertThat(captor.getValue().getCategorizada()).isTrue();
            assertThat(captor.getValue().getCategoria()).isEqualTo(categoriaPadrao);
        }

        @Test
        @DisplayName("Permite categorizar com categoria personalizada do próprio usuário")
        void devePermitirCategoriaDoProprioUsuario() {
            Categoria minha = new Categoria();
            minha.setId(UUID.randomUUID());
            minha.setNome("Pets");
            minha.setPadrao(false);
            minha.setUsuario(dono);

            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(categoriaService.buscarCategoriaPermitida(minha.getId(), dono))
                    .thenReturn(minha);
            when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

            transacaoService.categorizar(transacao.getId(), minha.getId(), dono);

            assertThat(transacao.getCategoria()).isEqualTo(minha);
            assertThat(transacao.getCategorizada()).isTrue();
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando a transação não existe")
        void deveLancar404QuandoTransacaoInexistente() {
            UUID id = UUID.randomUUID();
            when(transacaoRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transacaoService.categorizar(id, categoriaPadrao.getId(), dono))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transação não encontrada");

            verify(transacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança 403 quando a transação pertence a outro usuário")
        void deveLancar403QuandoTransacaoDeOutroUsuario() {
            conta.setUsuario(outro);
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));

            assertThatThrownBy(() -> transacaoService.categorizar(transacao.getId(), categoriaPadrao.getId(), dono))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Acesso negado a essa transação");

            verify(categoriaService, never()).buscarCategoriaPermitida(any(), any());
            verify(transacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando a categoria não existe")
        void deveLancar404QuandoCategoriaInexistente() {
            UUID categoriaId = UUID.randomUUID();
            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(categoriaService.buscarCategoriaPermitida(categoriaId, dono))
                    .thenThrow(new ResourceNotFoundException("Categoria não encontrada"));

            assertThatThrownBy(() -> transacaoService.categorizar(transacao.getId(), categoriaId, dono))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Categoria não encontrada");

            verify(transacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança 403 quando a categoria pertence a outro usuário e não é padrão")
        void deveLancar403QuandoCategoriaDeOutroUsuario() {
            Categoria categoriaDoOutro = new Categoria();
            categoriaDoOutro.setId(UUID.randomUUID());
            categoriaDoOutro.setNome("Secreta");
            categoriaDoOutro.setPadrao(false);
            categoriaDoOutro.setUsuario(outro);

            when(transacaoRepository.findById(transacao.getId())).thenReturn(Optional.of(transacao));
            when(categoriaService.buscarCategoriaPermitida(categoriaDoOutro.getId(), dono))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Categoria não pertence ao usuário autenticado"
                    ));

            assertThatThrownBy(() -> transacaoService.categorizar(transacao.getId(), categoriaDoOutro.getId(), dono))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Categoria não pertence ao usuário autenticado");

            verify(transacaoRepository, never()).save(any());
        }
    }
}
