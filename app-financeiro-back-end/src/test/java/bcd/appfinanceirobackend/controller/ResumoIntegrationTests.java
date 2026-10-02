package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("GET /resumo e /resumo/categorias - Integração com banco real")
class ResumoIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("app_financeiro_test")
            .withUsername("postgres")
            .withPassword("1234");

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EntityManager entityManager;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContaRepository contaRepository;
    @Autowired private TransacaoRepository transacaoRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    private Usuario usuarioA;
    private Usuario usuarioB;
    private Conta contaA;
    private Conta contaB;
    private Categoria alimentacao;
    private Categoria transporte;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void seed() {
        alimentacao = categoriaRepository.findByNomeAndPadraoTrue("Alimentação").orElseThrow();
        transporte = categoriaRepository.findByNomeAndPadraoTrue("Transporte").orElseThrow();

        usuarioA = salvarUsuario("usuario.a.resumo@test.com", "39053344705");
        usuarioB = salvarUsuario("usuario.b.resumo@test.com", "15350946056");
        contaA = salvarConta(usuarioA, "Conta A");
        contaB = salvarConta(usuarioB, "Conta B");

        // Usuário A — junho/2026
        salvarTransacao(contaA, "3000.00", LocalDate.of(2026, 6, 5), TipoTransacao.CREDITO, null);
        salvarTransacao(contaA, "400.00", LocalDate.of(2026, 6, 10), TipoTransacao.DEBITO, alimentacao);
        salvarTransacao(contaA, "100.00", LocalDate.of(2026, 6, 15), TipoTransacao.DEBITO, transporte);

        // Usuário B — mesmo mês, valores bem distintos (não podem vazar para A)
        salvarTransacao(contaB, "9999.00", LocalDate.of(2026, 6, 8), TipoTransacao.CREDITO, null);
        salvarTransacao(contaB, "888.00", LocalDate.of(2026, 6, 12), TipoTransacao.DEBITO, alimentacao);

        // Fora do período (não entra no filtro)
        salvarTransacao(contaA, "50.00", LocalDate.of(2026, 5, 20), TipoTransacao.DEBITO, alimentacao);

        entityManager.flush();

        tokenA = jwtUtil.gerarToken(usuarioA).getAccessToken();
        tokenB = jwtUtil.gerarToken(usuarioB).getAccessToken();
    }

    @Nested
    @DisplayName("GET /resumo")
    class GetResumo {

        @Test
        @DisplayName("retorna o resumo mensal com massa real no PostgreSQL")
        void deveRetornarResumoMensalDoUsuarioAutenticado() throws Exception {
            mockMvc.perform(get("/resumo")
                            .param("ano", "2026")
                            .param("mes", "6")
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ano").value(2026))
                    .andExpect(jsonPath("$.mes").value(6))
                    .andExpect(jsonPath("$.dataInicio").value("2026-06-01"))
                    .andExpect(jsonPath("$.dataFim").value("2026-06-30"))
                    .andExpect(jsonPath("$.totalRecebido", closeTo(3000.00, 0.001)))
                    .andExpect(jsonPath("$.totalGasto", closeTo(500.00, 0.001)))
                    .andExpect(jsonPath("$.saldo", closeTo(2500.00, 0.001)))
                    .andExpect(jsonPath("$.possuiTransacoes").value(true))
                    .andExpect(jsonPath("$.categoriaMaiorGastoNome").value("Alimentação"))
                    .andExpect(jsonPath("$.categoriaMaiorGastoTotal", closeTo(400.00, 0.001)));
        }

        @Test
        @DisplayName("não inclui valores nem categorias do outro usuário")
        void naoDeveVazarDadosDeOutroUsuario() throws Exception {
            mockMvc.perform(get("/resumo")
                            .param("ano", "2026")
                            .param("mes", "6")
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRecebido", closeTo(3000.00, 0.001)))
                    .andExpect(jsonPath("$.totalGasto", closeTo(500.00, 0.001)))
                    .andExpect(jsonPath("$.categoriaMaiorGastoTotal", closeTo(400.00, 0.001)));
        }

        @Test
        @DisplayName("bloqueia acesso sem autenticação")
        void deveBloquearSemAutenticacao() throws Exception {
            mockMvc.perform(get("/resumo").param("ano", "2026").param("mes", "6"))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("GET /resumo/categorias")
    class GetResumoCategorias {

        @Test
        @DisplayName("retorna gastos agrupados por categoria do usuário autenticado")
        void deveRetornarGastosPorCategoria() throws Exception {
            mockMvc.perform(get("/resumo/categorias")
                            .param("ano", "2026")
                            .param("mes", "6")
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nome").value("Alimentação"))
                    .andExpect(jsonPath("$[0].total", closeTo(400.00, 0.001)))
                    .andExpect(jsonPath("$[0].quantidade").value(1))
                    .andExpect(jsonPath("$[1].nome").value("Transporte"))
                    .andExpect(jsonPath("$[1].total", closeTo(100.00, 0.001)));
        }

        @Test
        @DisplayName("não retorna categorias/valores pertencentes ao outro usuário")
        void naoDeveRetornarCategoriasDeOutroUsuario() throws Exception {
            mockMvc.perform(get("/resumo/categorias")
                            .param("ano", "2026")
                            .param("mes", "6")
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].total", not(hasItem(closeTo(888.00, 0.001)))))
                    .andExpect(jsonPath("$.length()").value(2));

            mockMvc.perform(get("/resumo/categorias")
                            .param("ano", "2026")
                            .param("mes", "6")
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].nome").value("Alimentação"))
                    .andExpect(jsonPath("$[0].total", closeTo(888.00, 0.001)));
        }

        @Test
        @DisplayName("bloqueia acesso sem autenticação")
        void deveBloquearSemAutenticacao() throws Exception {
            mockMvc.perform(get("/resumo/categorias").param("ano", "2026").param("mes", "6"))
                    .andExpect(status().is4xxClientError());
        }
    }

    private Usuario salvarUsuario(String email, String cpf) {
        Usuario u = new Usuario();
        u.setNome("Usuario Teste");
        u.setEmail(email);
        u.setSenha("hash");
        u.setCpf(cpf);
        u.setCreatedAt(LocalDateTime.now());
        return usuarioRepository.save(u);
    }

    private Conta salvarConta(Usuario usuario, String nome) {
        Conta c = new Conta();
        c.setNome(nome);
        c.setUsuario(usuario);
        c.setTipoConta(TipoConta.CORRENTE);
        return contaRepository.save(c);
    }

    private void salvarTransacao(
            Conta conta,
            String valor,
            LocalDate data,
            TipoTransacao tipo,
            Categoria categoria
    ) {
        Transacao t = new Transacao();
        t.setConta(conta);
        t.setValor(new BigDecimal(valor));
        t.setData(data);
        t.setTipo(tipo);
        t.setCategoria(categoria);
        t.setFormaPagamento(TipoPagamento.PIX);
        t.setCategorizada(categoria != null);
        t.setFutura(false);
        transacaoRepository.save(t);
    }
}
