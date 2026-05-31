package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.categoria.CategorizarTransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("PATCH /transacoes/{transacaoId}/categoria - Integração")
class CategorizarTransacaoIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransacaoService transacaoService;

    // Dependências do JwtAuthFilter
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private Usuario usuario;
    private UUID transacaoId;
    private CategorizarTransacaoRequestDTO request;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("hash");
        usuario.setCpf("12345678900");

        transacaoId = UUID.randomUUID();
        request = new CategorizarTransacaoRequestDTO();
        request.setCategoriaId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Retorna 200 com a transação categorizada")
    void deveRetornar200() throws Exception {
        TransacaoResponseDTO response = new TransacaoResponseDTO();
        response.setTransacaoId(transacaoId);
        response.setCategoriaId(request.getCategoriaId());

        when(transacaoService.categorizar(eq(transacaoId), eq(request.getCategoriaId()), any()))
                .thenReturn(response);

        mockMvc.perform(patch("/transacoes/{transacaoId}/categoria", transacaoId)
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transacaoId").value(transacaoId.toString()))
                .andExpect(jsonPath("$.categoriaId").value(request.getCategoriaId().toString()));
    }

    @Test
    @DisplayName("Retorna 404 quando a transação ou categoria não existe")
    void deveRetornar404() throws Exception {
        when(transacaoService.categorizar(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Transacao não encontrada"));

        mockMvc.perform(patch("/transacoes/{transacaoId}/categoria", transacaoId)
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Transacao não encontrada"));
    }

    @Test
    @DisplayName("Retorna 403 quando a transação pertence a outro usuário")
    void deveRetornar403() throws Exception {
        when(transacaoService.categorizar(any(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a essa transação"));

        mockMvc.perform(patch("/transacoes/{transacaoId}/categoria", transacaoId)
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
