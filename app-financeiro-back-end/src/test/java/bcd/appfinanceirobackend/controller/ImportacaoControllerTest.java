package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.importacao.ImportacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusImportacao;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.ImportacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportacaoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("ImportacaoController")
class ImportacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImportacaoService importacaoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado;
    private UUID contaId;
    private UUID importacaoId;
    private ImportacaoResponseDTO responseConcluida;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("Lucas");
        usuarioAutenticado.setEmail("lucas@email.com");
        usuarioAutenticado.setCpf("12345678900");
        usuarioAutenticado.setSenha("hash");

        contaId = UUID.randomUUID();
        importacaoId = UUID.randomUUID();

        responseConcluida = new ImportacaoResponseDTO();
        responseConcluida.setId(importacaoId);
        responseConcluida.setStatus(StatusImportacao.CONCLUIDO);
        responseConcluida.setSucessos(2);
        responseConcluida.setFalhas(1);
        responseConcluida.setImportadoEm(LocalDateTime.of(2026, 5, 23, 10, 30));
        responseConcluida.setMensagemErro(null);
    }

    @Nested
    @DisplayName("POST /importacoes")
    class PostImportacoes {

        @Test
        @DisplayName("retorna 201 Created com o resultado da importação")
        void importar_quandoRequisicaoValida_retorna201ComBody() throws Exception {
            when(importacaoService.processar(any(MultipartFile.class), eq(contaId), eq(usuarioAutenticado)))
                    .thenReturn(responseConcluida);

            mockMvc.perform(multipart("/importacoes")
                            .file(csvValido())
                            .param("contaId", contaId.toString())
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(importacaoId.toString()))
                    .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                    .andExpect(jsonPath("$.sucessos").value(2))
                    .andExpect(jsonPath("$.falhas").value(1))
                    .andExpect(jsonPath("$.importadoEm").exists())
                    .andExpect(jsonPath("$.mensagemErro").doesNotExist());
        }

        @Test
        @DisplayName("delega para o service com arquivo, contaId e usuário autenticado")
        void importar_quandoRequisicaoValida_delegaComArgumentosCorretos() throws Exception {
            when(importacaoService.processar(any(MultipartFile.class), eq(contaId), eq(usuarioAutenticado)))
                    .thenReturn(responseConcluida);

            mockMvc.perform(multipart("/importacoes")
                            .file(csvValido())
                            .param("contaId", contaId.toString())
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isCreated());

            ArgumentCaptor<MultipartFile> arquivoCaptor = ArgumentCaptor.forClass(MultipartFile.class);
            ArgumentCaptor<UUID> contaIdCaptor = ArgumentCaptor.forClass(UUID.class);
            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

            verify(importacaoService).processar(
                    arquivoCaptor.capture(),
                    contaIdCaptor.capture(),
                    usuarioCaptor.capture()
            );

            MultipartFile arquivoRecebido = arquivoCaptor.getValue();

            assertAll(
                    () -> assertEquals("extrato.csv", arquivoRecebido.getOriginalFilename()),
                    () -> assertEquals(contaId, contaIdCaptor.getValue()),
                    () -> assertSame(usuarioAutenticado, usuarioCaptor.getValue())
            );
        }

        @Test
        @DisplayName("retorna 403 quando não há autenticação")
        void importar_semAutenticacao_retorna403() throws Exception {
            mockMvc.perform(multipart("/importacoes")
                            .file(csvValido())
                            .param("contaId", contaId.toString()))
                    .andExpect(status().isForbidden());

            verify(importacaoService, never()).processar(any(), any(), any());
        }

        @Test
        @DisplayName("retorna 400 quando arquivo não é enviado")
        void importar_semArquivo_retorna400() throws Exception {
            mockMvc.perform(multipart("/importacoes")
                            .param("contaId", contaId.toString())
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isBadRequest());

            verify(importacaoService, never()).processar(any(), any(), any());
        }

        @Test
        @DisplayName("retorna 400 quando contaId não é enviado")
        void importar_semContaId_retorna400() throws Exception {
            mockMvc.perform(multipart("/importacoes")
                            .file(csvValido())
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isBadRequest());

            verify(importacaoService, never()).processar(any(), any(), any());
        }

        @Test
        @DisplayName("retorna 400 quando contaId não é UUID válido")
        void importar_contaIdInvalido_retorna400() throws Exception {
            mockMvc.perform(multipart("/importacoes")
                            .file(csvValido())
                            .param("contaId", "uuid-invalido")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isBadRequest());

            verify(importacaoService, never()).processar(any(), any(), any());
        }

        @Test
        @DisplayName("retorna 404 quando o service informa que a conta não existe")
        void importar_contaInexistente_retorna404() throws Exception {
            when(importacaoService.processar(any(MultipartFile.class), eq(contaId), eq(usuarioAutenticado)))
                    .thenThrow(new ResourceNotFoundException("Conta não encontrada"));

            mockMvc.perform(multipart("/importacoes")
                            .file(csvValido())
                            .param("contaId", contaId.toString())
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Conta não encontrada"));
        }

        @Test
        @DisplayName("retorna 403 quando o service informa que a conta não pertence ao usuário")
        void importar_contaDeOutroUsuario_retorna403() throws Exception {
            when(importacaoService.processar(any(MultipartFile.class), eq(contaId), eq(usuarioAutenticado)))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Conta não pertence ao usuário autenticado"
                    ));

            mockMvc.perform(multipart("/importacoes")
                            .file(csvValido())
                            .param("contaId", contaId.toString())
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("retorna 400 quando o service rejeita arquivo inválido")
        void importar_arquivoInvalido_retorna400() throws Exception {
            when(importacaoService.processar(any(MultipartFile.class), eq(contaId), eq(usuarioAutenticado)))
                    .thenThrow(new IllegalArgumentException("Arquivo vazio"));

            mockMvc.perform(multipart("/importacoes")
                            .file(csvValido())
                            .param("contaId", contaId.toString())
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Arquivo vazio"));
        }
    }

    @Nested
    @DisplayName("GET /importacoes/{id}/status")
    class GetStatusImportacao {

        @Test
        @DisplayName("retorna 200 com status da importação")
        void status_quandoImportacaoExiste_retorna200ComStatus() throws Exception {
            when(importacaoService.buscarStatus(importacaoId, usuarioAutenticado))
                    .thenReturn(StatusImportacao.CONCLUIDO);

            mockMvc.perform(get("/importacoes/{id}/status", importacaoId)
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("\"CONCLUIDO\""));
        }

        @Test
        @DisplayName("delega para o service com id e usuário autenticado")
        void status_quandoImportacaoExiste_delegaComArgumentosCorretos() throws Exception {
            when(importacaoService.buscarStatus(importacaoId, usuarioAutenticado))
                    .thenReturn(StatusImportacao.PROCESSANDO);

            mockMvc.perform(get("/importacoes/{id}/status", importacaoId)
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk());

            verify(importacaoService).buscarStatus(importacaoId, usuarioAutenticado);
        }

        @Test
        @DisplayName("retorna 403 quando não há autenticação")
        void status_semAutenticacao_retorna403() throws Exception {
            mockMvc.perform(get("/importacoes/{id}/status", importacaoId))
                    .andExpect(status().isForbidden());

            verify(importacaoService, never()).buscarStatus(any(), any());
        }

        @Test
        @DisplayName("retorna 400 quando id não é UUID válido")
        void status_idInvalido_retorna400() throws Exception {
            mockMvc.perform(get("/importacoes/{id}/status", "uuid-invalido")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isBadRequest());

            verify(importacaoService, never()).buscarStatus(any(), any());
        }

        @Test
        @DisplayName("retorna 404 quando a importação não existe")
        void status_importacaoInexistente_retorna404() throws Exception {
            when(importacaoService.buscarStatus(importacaoId, usuarioAutenticado))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Importação não encontrada"));

            mockMvc.perform(get("/importacoes/{id}/status", importacaoId)
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna 403 quando a importação pertence a outro usuário")
        void status_importacaoDeOutroUsuario_retorna403() throws Exception {
            when(importacaoService.buscarStatus(importacaoId, usuarioAutenticado))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Importação não pertence ao usuário autenticado"
                    ));

            mockMvc.perform(get("/importacoes/{id}/status", importacaoId)
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isForbidden());
        }
    }

    private MockMultipartFile csvValido() {
        String conteudo = "data,descricao,valor,tipo\n" +
                "2024-01-10,Mercado,-50.00,DEBITO\n";

        return new MockMultipartFile(
                "arquivo",
                "extrato.csv",
                "text/csv",
                conteudo.getBytes(StandardCharsets.UTF_8)
        );
    }
}