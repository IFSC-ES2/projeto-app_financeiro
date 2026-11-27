package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.conta.CartaoCreditoRequestDTO;
import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.CartaoCreditoRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("Cartao, faturas e extrato futuro - Integracao com banco real")
class CartaoFaturaExtratoFuturoIntegrationTests {

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
    @Autowired private CartaoCreditoRepository cartaoCreditoRepository;
    @Autowired private FaturaRepository faturaRepository;
    @Autowired private TransacaoRepository transacaoRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private Usuario usuarioA;
    private Usuario usuarioB;
    private Conta contaCartaoA;
    private Conta contaCartaoSemCartaoA;
    private Conta contaCorrenteA;
    private Conta contaCartaoB;
    private Fatura faturaAbertaA;
    private Fatura faturaAbertaB;
    private String tokenA;
    private String tokenB;
    private YearMonth mesAtual;

    @BeforeEach
    void seed() {
        mesAtual = YearMonth.now();

        usuarioA = salvarUsuario("usuario.a.fatura@test.com", "39053344705");
        usuarioB = salvarUsuario("usuario.b.fatura@test.com", "15350946056");

        contaCartaoA = salvarConta(usuarioA, "Cartao A", TipoConta.CARTAO_CREDITO);
        contaCartaoSemCartaoA = salvarConta(usuarioA, "Cartao novo A", TipoConta.CARTAO_CREDITO);
        contaCorrenteA = salvarConta(usuarioA, "Conta Corrente A", TipoConta.CORRENTE);
        contaCartaoB = salvarConta(usuarioB, "Cartao B", TipoConta.CARTAO_CREDITO);

        salvarCartao(contaCartaoA, 10);

        YearMonth mesVencimento = mesAtual.plusMonths(1);
        faturaAbertaA = salvarFatura(contaCartaoA, mesVencimento, mesVencimento.atDay(10), StatusFatura.ABERTA);
        faturaAbertaB = salvarFatura(contaCartaoB, mesVencimento, mesVencimento.atDay(10), StatusFatura.ABERTA);

        // Compra que compoe o total da fatura do usuario A (350) e do usuario B (999)
        salvarTransacao(contaCartaoA, "350.00", mesAtual.atDay(6),
                TipoTransacao.DEBITO, TipoPagamento.CARTAO_CREDITO, false, faturaAbertaA);
        salvarTransacao(contaCartaoB, "999.00", mesAtual.atDay(6),
                TipoTransacao.DEBITO, TipoPagamento.CARTAO_CREDITO, false, faturaAbertaB);

        // Futuro do usuario A: salario neste mes e boleto no mes seguinte
        salvarTransacao(contaCorrenteA, "1000.00", mesAtual.atDay(5),
                TipoTransacao.CREDITO, TipoPagamento.PIX, true, null);
        salvarTransacao(contaCorrenteA, "200.00", mesVencimento.atDay(15),
                TipoTransacao.DEBITO, TipoPagamento.BOLETO, true, null);

        // Futuro do usuario B nao pode aparecer para o usuario A
        salvarTransacao(contaCartaoB, "888.00", mesVencimento.atDay(20),
                TipoTransacao.DEBITO, TipoPagamento.BOLETO, true, null);

        entityManager.flush();

        tokenA = jwtUtil.gerarToken(usuarioA).getAccessToken();
        tokenB = jwtUtil.gerarToken(usuarioB).getAccessToken();
    }

    @Nested
    @DisplayName("Cartao de credito")
    class Cartao {

        @Test
        @DisplayName("POST /contas/{id}/cartao associa e GET retorna o cartao criado")
        void deveAssociarEBuscarCartao() throws Exception {
            CartaoCreditoRequestDTO dto = new CartaoCreditoRequestDTO();
            dto.setLimite(new BigDecimal("5000.00"));
            dto.setDiaFechamento(3);
            dto.setDiaVencimento(10);

            mockMvc.perform(post("/contas/{contaId}/cartao", contaCartaoSemCartaoA.getId())
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.contaId").value(contaCartaoSemCartaoA.getId().toString()))
                    .andExpect(jsonPath("$.diaFechamento").value(3))
                    .andExpect(jsonPath("$.diaVencimento").value(10))
                    .andExpect(jsonPath("$.limite", closeTo(5000.00, 0.001)));

            mockMvc.perform(get("/contas/{contaId}/cartao", contaCartaoSemCartaoA.getId())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contaId").value(contaCartaoSemCartaoA.getId().toString()))
                    .andExpect(jsonPath("$.diaVencimento").value(10));
        }

        @Test
        @DisplayName("POST em conta de outro usuario retorna 403")
        void deveImpedirCartaoEmContaDeOutroUsuario() throws Exception {
            CartaoCreditoRequestDTO dto = new CartaoCreditoRequestDTO();
            dto.setLimite(new BigDecimal("1000.00"));
            dto.setDiaFechamento(3);
            dto.setDiaVencimento(10);

            mockMvc.perform(post("/contas/{contaId}/cartao", contaCartaoB.getId())
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET em conta de outro usuario retorna 403")
        void deveImpedirBuscarCartaoDeOutroUsuario() throws Exception {
            mockMvc.perform(get("/contas/{contaId}/cartao", contaCartaoB.getId())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST em conta que nao e CARTAO_CREDITO retorna 400")
        void deveImpedirCartaoEmContaCorrente() throws Exception {
            CartaoCreditoRequestDTO dto = new CartaoCreditoRequestDTO();
            dto.setLimite(new BigDecimal("1000.00"));
            dto.setDiaFechamento(3);
            dto.setDiaVencimento(10);

            mockMvc.perform(post("/contas/{contaId}/cartao", contaCorrenteA.getId())
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("bloqueia POST de cartao sem autenticacao")
        void deveBloquearCartaoSemAutenticacao() throws Exception {
            mockMvc.perform(post("/contas/{contaId}/cartao", contaCartaoSemCartaoA.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("Faturas")
    class Faturas {

        @Test
        @DisplayName("POST /contas/{id}/faturas gera fatura ABERTA para o mes informado")
        void deveGerarFatura() throws Exception {
            YearMonth mesNovo = mesAtual.plusMonths(3);

            mockMvc.perform(post("/contas/{contaId}/faturas", contaCartaoA.getId())
                            .param("mesReferencia", mesNovo.toString())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.mesReferencia").value(mesNovo.toString()))
                    .andExpect(jsonPath("$.dataVencimento").value(mesNovo.atDay(10).toString()))
                    .andExpect(jsonPath("$.status").value("ABERTA"))
                    .andExpect(jsonPath("$.nomeConta").value("Cartao A"));
        }

        @Test
        @DisplayName("POST em conta cartao de outro usuario retorna 403")
        void deveImpedirGerarFaturaEmContaDeOutroUsuario() throws Exception {
            mockMvc.perform(post("/contas/{contaId}/faturas", contaCartaoB.getId())
                            .param("mesReferencia", mesAtual.plusMonths(3).toString())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /contas/{id}/faturas lista as faturas da conta com total calculado")
        void deveListarFaturasPorConta() throws Exception {
            mockMvc.perform(get("/contas/{contaId}/faturas", contaCartaoA.getId())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].faturaId").value(faturaAbertaA.getId().toString()))
                    .andExpect(jsonPath("$[0].valorTotal", closeTo(350.00, 0.001)))
                    .andExpect(jsonPath("$[0].status").value("ABERTA"));
        }

        @Test
        @DisplayName("GET /faturas/{id} retorna a fatura do usuario")
        void deveBuscarFaturaPorId() throws Exception {
            mockMvc.perform(get("/faturas/{faturaId}", faturaAbertaA.getId())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.faturaId").value(faturaAbertaA.getId().toString()))
                    .andExpect(jsonPath("$.valorTotal", closeTo(350.00, 0.001)));
        }

        @Test
        @DisplayName("PATCH /faturas/{id}/pagar marca a fatura como PAGA")
        void deveMarcarFaturaComoPaga() throws Exception {
            mockMvc.perform(patch("/faturas/{faturaId}/pagar", faturaAbertaA.getId())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/faturas/{faturaId}", faturaAbertaA.getId())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAGA"));
        }

        @Test
        @DisplayName("impede acessar e pagar fatura de outro usuario")
        void deveImpedirFaturaDeOutroUsuario() throws Exception {
            mockMvc.perform(get("/faturas/{faturaId}", faturaAbertaB.getId())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isForbidden());

            mockMvc.perform(patch("/faturas/{faturaId}/pagar", faturaAbertaB.getId())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("bloqueia endpoints de fatura sem autenticacao")
        void deveBloquearFaturasSemAutenticacao() throws Exception {
            mockMvc.perform(get("/faturas/{faturaId}", faturaAbertaA.getId()))
                    .andExpect(status().is4xxClientError());
            mockMvc.perform(get("/contas/{contaId}/faturas", contaCartaoA.getId()))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("GET /extrato-futuro")
    class ExtratoFuturo {

        @Test
        @DisplayName("projeta boleto e fatura aberta no mes de vencimento")
        void deveProjetarFuturoDoUsuario() throws Exception {
            mockMvc.perform(get("/extrato-futuro")
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].mes").value(mesAtual.toString()))
                    .andExpect(jsonPath("$[0].totalCreditos", closeTo(1000.00, 0.001)))
                    // mes seguinte: boleto 200 + fatura 350 = 550, 2 vencimentos
                    .andExpect(jsonPath("$[1].mes").value(mesAtual.plusMonths(1).toString()))
                    .andExpect(jsonPath("$[1].totalDebitos", closeTo(550.00, 0.001)))
                    .andExpect(jsonPath("$[1].saldoPrevisto", closeTo(-550.00, 0.001)))
                    .andExpect(jsonPath("$[1].quantidadeVencimentos").value(2))
                    .andExpect(jsonPath("$[1].faturas.length()").value(1))
                    .andExpect(jsonPath("$[1].faturas[0].valorTotal", closeTo(350.00, 0.001)))
                    .andExpect(jsonPath("$[1].transacoes.length()").value(1));
        }

        @Test
        @DisplayName("retorna apenas um mes quando meses=1 e informado")
        void deveRetornarApenasUmMesQuandoMesesIgualUm() throws Exception {
            mockMvc.perform(get("/extrato-futuro")
                            .param("meses", "1")
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].mes").value(mesAtual.toString()))
                    .andExpect(jsonPath("$[0].totalCreditos", closeTo(1000.00, 0.001)));
        }

        @Test
        @DisplayName("nao mistura dados futuros de outro usuario na projecao")
        void naoDeveVazarDadosDeOutroUsuario() throws Exception {
            mockMvc.perform(get("/extrato-futuro")
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[1].totalDebitos", closeTo(999.00 + 888.00, 0.001)))
                    .andExpect(jsonPath("$[1].faturas[0].valorTotal", closeTo(999.00, 0.001)));

            mockMvc.perform(get("/extrato-futuro")
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[1].totalDebitos", closeTo(550.00, 0.001)));
        }

        @Test
        @DisplayName("bloqueia extrato futuro sem autenticacao")
        void deveBloquearSemAutenticacao() throws Exception {
            mockMvc.perform(get("/extrato-futuro"))
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

    private Conta salvarConta(Usuario usuario, String nome, TipoConta tipo) {
        Conta c = new Conta();
        c.setNome(nome);
        c.setUsuario(usuario);
        c.setTipoConta(tipo);
        return contaRepository.save(c);
    }

    private void salvarCartao(Conta contaCartao, int diaVencimento) {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setConta(contaCartao);
        cartao.setLimite(new BigDecimal("5000.00"));
        cartao.setDia_fechamento(3);
        cartao.setDia_vencimento(diaVencimento);
        cartaoCreditoRepository.save(cartao);
    }

    private Fatura salvarFatura(Conta conta, YearMonth mes, LocalDate vencimento, StatusFatura status) {
        Fatura f = new Fatura();
        f.setConta(conta);
        f.setMesReferencia(mes);
        f.setDataVencimento(vencimento);
        f.setValorTotal(BigDecimal.ZERO);
        f.setStatus(status);
        return faturaRepository.save(f);
    }

    private void salvarTransacao(
            Conta conta,
            String valor,
            LocalDate data,
            TipoTransacao tipo,
            TipoPagamento formaPagamento,
            boolean futura,
            Fatura fatura
    ) {
        Transacao t = new Transacao();
        t.setConta(conta);
        t.setValor(new BigDecimal(valor));
        t.setData(data);
        t.setTipo(tipo);
        t.setFormaPagamento(formaPagamento);
        t.setFatura(fatura);
        t.setCategorizada(false);
        t.setFutura(futura);
        transacaoRepository.save(t);
    }
}
