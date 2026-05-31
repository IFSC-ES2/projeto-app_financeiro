package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.comum.PaginaDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.TransacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("GET /transacoes (paginado/filtrado) - Integração")
class ListarTransacoesIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransacaoService transacaoService;

    // Dependências do JwtAuthFilter
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("hash");
        usuario.setCpf("12345678900");
    }

    @Test
    @DisplayName("Retorna 200 com o envelope paginado")
    void deveRetornar200ComEnvelopePaginado() throws Exception {
        TransacaoResponseDTO dto = new TransacaoResponseDTO();
        dto.setTransacaoId(UUID.randomUUID());
        dto.setContaId(UUID.randomUUID());
        when(transacaoService.listarTransacoesPorUsuario(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PaginaDTO<>(List.of(dto), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/transacoes").with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.ultima").value(true));
    }

    @Test
    @DisplayName("Repassa filtros (data, categoria, tipo) e paginação ao service")
    void deveRepassarFiltrosEPaginacaoAoService() throws Exception {
        when(transacaoService.listarTransacoesPorUsuario(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PaginaDTO<>(List.of(), 2, 5, 0, 0, true, true));

        UUID categoriaId = UUID.randomUUID();

        mockMvc.perform(get("/transacoes")
                        .param("dataInicio", "2024-01-01")
                        .param("dataFim", "2024-01-31")
                        .param("categoriaId", categoriaId.toString())
                        .param("tipo", "CREDITO")
                        .param("page", "2")
                        .param("size", "5")
                        .with(user(usuario)))
                .andExpect(status().isOk());

        var dataInicio = forClass(LocalDate.class);
        var dataFim = forClass(LocalDate.class);
        var categoria = forClass(UUID.class);
        var tipo = forClass(TipoTransacao.class);
        var pageable = forClass(Pageable.class);
        verify(transacaoService).listarTransacoesPorUsuario(
                any(), dataInicio.capture(), dataFim.capture(), categoria.capture(), tipo.capture(), pageable.capture());

        assertThat(dataInicio.getValue()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(dataFim.getValue()).isEqualTo(LocalDate.of(2024, 1, 31));
        assertThat(categoria.getValue()).isEqualTo(categoriaId);
        assertThat(tipo.getValue()).isEqualTo(TipoTransacao.CREDITO);
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("Bloqueia o acesso sem autenticação")
    void deveBloquearSemAutenticacao() throws Exception {
        mockMvc.perform(get("/transacoes"))
                .andExpect(status().is4xxClientError());
    }
}
