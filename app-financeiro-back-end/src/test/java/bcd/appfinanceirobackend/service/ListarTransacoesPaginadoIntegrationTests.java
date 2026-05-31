package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.comum.PaginaDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("Listagem paginada/filtrada de transações - Integração com banco real")
class ListarTransacoesPaginadoIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("app_financeiro_test")
            .withUsername("postgres")
            .withPassword("1234");

    @Autowired private TransacaoService transacaoService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContaRepository contaRepository;
    @Autowired private TransacaoRepository transacaoRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    private static final Pageable PRIMEIRA_PAGINA =
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "data"));

    private Usuario dono;
    private Conta contaDono;
    private Categoria alimentacao;
    private Categoria transporte;

    @BeforeEach
    void seed() {
        dono = salvarUsuario("dono106@test.com", "98765432100");
        contaDono = salvarConta(dono);
        alimentacao = categoriaRepository.findByNomeAndPadraoTrue("Alimentação").orElseThrow();
        transporte = categoriaRepository.findByNomeAndPadraoTrue("Transporte").orElseThrow();

        salvarTransacao(contaDono, LocalDate.of(2024, 1, 10), TipoTransacao.DEBITO, alimentacao);
        salvarTransacao(contaDono, LocalDate.of(2024, 2, 15), TipoTransacao.CREDITO, null);
        salvarTransacao(contaDono, LocalDate.of(2024, 3, 20), TipoTransacao.DEBITO, transporte);
        salvarTransacao(contaDono, LocalDate.of(2024, 3, 25), TipoTransacao.BOLETO, null);

        // Transação de outro usuário — não deve aparecer para o dono
        Usuario outro = salvarUsuario("outro106@test.com", "39053344705");
        salvarTransacao(salvarConta(outro), LocalDate.of(2024, 2, 1), TipoTransacao.DEBITO, null);
    }

    @Test
    @DisplayName("Sem filtros, retorna apenas as transações do próprio usuário, ordenadas por data desc")
    void semFiltros_retornaSomenteDoUsuario() {
        PaginaDTO<TransacaoResponseDTO> pagina =
                transacaoService.listarTransacoesPorUsuario(dono, null, null, null, null, PRIMEIRA_PAGINA);

        assertThat(pagina.totalElementos()).isEqualTo(4);
        assertThat(pagina.conteudo()).hasSize(4);
        // ordenado por data desc → primeiro é 2024-03-25
        assertThat(pagina.conteudo().getFirst().getData()).isEqualTo(LocalDate.of(2024, 3, 25));
    }

    @Test
    @DisplayName("Filtra por intervalo de datas")
    void filtraPorData() {
        PaginaDTO<TransacaoResponseDTO> pagina = transacaoService.listarTransacoesPorUsuario(
                dono, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 21), null, null, PRIMEIRA_PAGINA);

        assertThat(pagina.totalElementos()).isEqualTo(2);
        assertThat(pagina.conteudo()).extracting(TransacaoResponseDTO::getData)
                .containsExactlyInAnyOrder(LocalDate.of(2024, 2, 15), LocalDate.of(2024, 3, 20));
    }

    @Test
    @DisplayName("Filtra por categoria")
    void filtraPorCategoria() {
        PaginaDTO<TransacaoResponseDTO> pagina = transacaoService.listarTransacoesPorUsuario(
                dono, null, null, alimentacao.getId(), null, PRIMEIRA_PAGINA);

        assertThat(pagina.totalElementos()).isEqualTo(1);
        assertThat(pagina.conteudo().getFirst().getCategoriaId()).isEqualTo(alimentacao.getId());
    }

    @Test
    @DisplayName("Filtra por tipo de transação")
    void filtraPorTipo() {
        PaginaDTO<TransacaoResponseDTO> pagina = transacaoService.listarTransacoesPorUsuario(
                dono, null, null, null, TipoTransacao.DEBITO, PRIMEIRA_PAGINA);

        assertThat(pagina.totalElementos()).isEqualTo(2);
        assertThat(pagina.conteudo()).extracting(TransacaoResponseDTO::getTipoTransacao)
                .containsOnly(TipoTransacao.DEBITO);
    }

    @Test
    @DisplayName("Pagina o resultado conforme tamanho solicitado")
    void paginaResultado() {
        PaginaDTO<TransacaoResponseDTO> primeira = transacaoService.listarTransacoesPorUsuario(
                dono, null, null, null, null, PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "data")));

        assertThat(primeira.conteudo()).hasSize(2);
        assertThat(primeira.totalElementos()).isEqualTo(4);
        assertThat(primeira.totalPaginas()).isEqualTo(2);
        assertThat(primeira.primeira()).isTrue();
        assertThat(primeira.ultima()).isFalse();

        PaginaDTO<TransacaoResponseDTO> segunda = transacaoService.listarTransacoesPorUsuario(
                dono, null, null, null, null, PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "data")));

        assertThat(segunda.conteudo()).hasSize(2);
        assertThat(segunda.primeira()).isFalse();
        assertThat(segunda.ultima()).isTrue();
    }

    private Usuario salvarUsuario(String email, String cpf) {
        Usuario u = new Usuario();
        u.setNome("Teste");
        u.setEmail(email);
        u.setSenha("hash");
        u.setCpf(cpf);
        u.setCreatedAt(LocalDateTime.now());
        return usuarioRepository.save(u);
    }

    private Conta salvarConta(Usuario usuario) {
        Conta c = new Conta();
        c.setNome("Conta Corrente");
        c.setUsuario(usuario);
        c.setTipoConta(TipoConta.CORRENTE);
        return contaRepository.save(c);
    }

    private void salvarTransacao(Conta conta, LocalDate data, TipoTransacao tipo, Categoria categoria) {
        Transacao t = new Transacao();
        t.setConta(conta);
        t.setValor(new BigDecimal("10.00"));
        t.setData(data);
        t.setTipo(tipo);
        t.setCategoria(categoria);
        transacaoRepository.save(t);
    }
}
