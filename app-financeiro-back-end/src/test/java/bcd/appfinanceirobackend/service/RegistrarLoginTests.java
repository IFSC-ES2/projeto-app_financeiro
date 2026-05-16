package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.auth.LoginRequestDTO;
import bcd.appfinanceirobackend.dto.auth.RegisterRequestDTO;
import bcd.appfinanceirobackend.dto.auth.TokenDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class   RegistrarLoginTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    private RegisterRequestDTO dtoCadastro;
    private LoginRequestDTO    dtoLogin;
    private Usuario            usuarioNoBanco;

    @BeforeEach
    void setUp() {
        dtoCadastro = new RegisterRequestDTO();
        dtoCadastro.setNome("Maria Silva");
        dtoCadastro.setEmail("maria@email.com");
        dtoCadastro.setSenha("Senha@123");
        dtoCadastro.setCpf("11144477735");

        dtoLogin = new LoginRequestDTO();
        dtoLogin.setEmail("maria@email.com");
        dtoLogin.setSenha("Senha@123");

        usuarioNoBanco = new Usuario();
        usuarioNoBanco.setId(UUID.randomUUID());
        usuarioNoBanco.setNome("Maria Silva");
        usuarioNoBanco.setEmail("maria@email.com");
        usuarioNoBanco.setSenha("$2a$10$hashBcryptSimulado"); 
        usuarioNoBanco.setCpf("11144477735");
        usuarioNoBanco.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("registrar()")
    class Registrar {

        /**
         * R1 — Dados válidos: email e CPF livres, todos os campos preenchidos.
         * O service deve salvar, retornar o usuário persistido e nunca
         * armazenar a senha em texto puro.
         */
        @Test
        @DisplayName("R1 - dados válidos → retorna Usuario salvo com senha em hash")
        void registrar_dadosValidos_retornaUsuario() {
            when(usuarioRepository.existsByEmail(dtoCadastro.getEmail()))
                    .thenReturn(false);
            when(usuarioRepository.existsByCpf(dtoCadastro.getCpf()))
                    .thenReturn(false);
            when(passwordEncoder.encode(dtoCadastro.getSenha()))
                    .thenReturn("$2a$10$hashBcryptSimulado");
            when(usuarioRepository.save(any(Usuario.class)))
                    .thenReturn(usuarioNoBanco);

            Usuario resultado = usuarioService.registrar(dtoCadastro);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isNotNull();
            assertThat(resultado.getCreatedAt()).isNotNull();
            assertThat(resultado.getSenha()).isNotEqualTo(dtoCadastro.getSenha());
            verify(usuarioRepository).save(any(Usuario.class));
        }

        /**
         * R2 — Email já cadastrado: banco já possui esse email (unique).
         * Esperamos DataIntegrityViolationException e nenhum save adicional.
         */
        @Test
        @DisplayName("R2 - email já cadastrado → lança DataIntegrityViolationException")
        void registrar_emailJaCadastrado_lancaExcecao() {
            when(usuarioRepository.existsByEmail(dtoCadastro.getEmail()))
                    .thenReturn(true);

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(usuarioRepository, never()).save(any());
        }

        /**
         * R3 — CPF já cadastrado: banco já possui esse CPF (unique).
         */
        @Test
        @DisplayName("R3 - CPF já cadastrado → lança DataIntegrityViolationException")
        void registrar_cpfJaCadastrado_lancaExcecao() {
            when(usuarioRepository.existsByEmail(dtoCadastro.getEmail()))
                    .thenReturn(false);
            when(usuarioRepository.existsByCpf(dtoCadastro.getCpf()))
                    .thenReturn(true);

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(usuarioRepository, never()).save(any());
        }

        /**
         * R4 — Email nulo: service deve rejeitar antes de consultar o banco.
         */
        @Test
        @DisplayName("R4 - email nulo → lança IllegalArgumentException")
        void registrar_emailNulo_lancaExcecao() {
            dtoCadastro.setEmail(null);

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        /**
         * R4b — Email em branco.
         */
        @Test
        @DisplayName("R4b - email em branco → lança IllegalArgumentException")
        void registrar_emailVazio_lancaExcecao() {
            dtoCadastro.setEmail("   ");

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        /**
         * R5 — Senha nula.
         */
        @Test
        @DisplayName("R5 - senha nula → lança IllegalArgumentException")
        void registrar_senhaNula_lancaExcecao() {
            dtoCadastro.setSenha(null);

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        /**
         * R5b — Senha em branco.
         */
        @Test
        @DisplayName("R5b - senha em branco → lança IllegalArgumentException")
        void registrar_senhaVazia_lancaExcecao() {
            dtoCadastro.setSenha("   ");

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        /**
         * R6 — Nome nulo.
         */
        @Test
        @DisplayName("R6 - nome nulo → lança IllegalArgumentException")
        void registrar_nomeNulo_lancaExcecao() {
            dtoCadastro.setNome(null);

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        /**
         * R6b — Nome em branco.
         */
        @Test
        @DisplayName("R6b - nome em branco → lança IllegalArgumentException")
        void registrar_nomeVazio_lancaExcecao() {
            dtoCadastro.setNome("   ");

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        // R7 - CPF nulo → lança IllegalArgumentException
        @Test
        @DisplayName("R7 - CPF nulo → lança IllegalArgumentException")
        void registrar_cpfNulo_lancaExcecao() {
            dtoCadastro.setCpf(null);

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        // R7b - CPF em branco → lança IllegalArgumentException
        @Test
        @DisplayName("R7b - CPF em branco → lança IllegalArgumentException")
        void registrar_cpfVazio_lancaExcecao() {
            dtoCadastro.setCpf(""); // String vazia

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        // R7c - CPF com formato inválido (menos de 11 dígitos) → lança IllegalArgumentException
        @Test
        @DisplayName("R7c - CPF com formato inválido → lança IllegalArgumentException")
        void registrar_cpfFormatoInvalido_lancaExcecao() {
            dtoCadastro.setCpf("1234"); // String com menos de 11 caracteres

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        // R8 - email com formato inválido (sem @) → lança IllegalArgumentException
        @Test
        @DisplayName("R8 - email com formato inválido → lança IllegalArgumentException")
        void registrar_emailFormatoInvalido_lancaExcecao() {
            dtoCadastro.setEmail("emailsemarroba.com");

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        // R9 - CPF com 11 dígitos mas dígitos verificadores inválidos → lança IllegalArgumentException
        @Test
        @DisplayName("R9 - CPF com dígitos verificadores inválidos → lança IllegalArgumentException")
        void registrar_cpfDigitosVerificadoresInvalidos_lancaExcecao() {
            dtoCadastro.setCpf("12345678901");

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }

        // R9b - CPF com todos os dígitos iguais → lança IllegalArgumentException
        @Test
        @DisplayName("R9b - CPF com dígitos repetidos → lança IllegalArgumentException")
        void registrar_cpfDigitosRepetidos_lancaExcecao() {
            dtoCadastro.setCpf("11111111111");

            assertThatThrownBy(() -> usuarioService.registrar(dtoCadastro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("autenticar()")
    class Autenticar {

        /**
         * A1 — Credenciais corretas: usuário existe e senha bate.
         * O service deve retornar um TokenDTO com token e tipo preenchidos.
         */
        @Test
        @DisplayName("A1 - credenciais corretas → retorna TokenDTO")
        void autenticar_credenciaisCorretas_retornaToken() {
            when(usuarioRepository.findByEmail(dtoLogin.getEmail()))
                    .thenReturn(Optional.of(usuarioNoBanco));
            when(passwordEncoder.matches(dtoLogin.getSenha(), usuarioNoBanco.getSenha()))
                    .thenReturn(true);

            TokenDTO tokenEsperado = new TokenDTO();
            tokenEsperado.setAccessToken("jwt.token.simulado");
            tokenEsperado.setTipo("Bearer");
            tokenEsperado.setExpiracao(LocalDateTime.now().plusHours(1));
            when(jwtUtil.gerarToken(usuarioNoBanco)).thenReturn(tokenEsperado);

            TokenDTO resultado = usuarioService.autenticar(
                    dtoLogin.getEmail(),
                    dtoLogin.getSenha()
            );

            assertThat(resultado).isNotNull();
            assertThat(resultado.getAccessToken()).isNotBlank();
            assertThat(resultado.getTipo()).isNotBlank();
        }

        /**
         * A2 — Email não encontrado: repositório retorna Optional vazio.
         */
        @Test
        @DisplayName("A2 - email inexistente → lança UsernameNotFoundException")
        void autenticar_emailInexistente_lancaExcecao() {
            when(usuarioRepository.findByEmail(dtoLogin.getEmail()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.autenticar(
                    dtoLogin.getEmail(),
                    dtoLogin.getSenha()
            )).isInstanceOf(UsernameNotFoundException.class);

            verify(jwtUtil, never()).gerarToken(any());
        }

        /**
         * A3 — Senha incorreta: usuário existe mas o hash não bate.
         */
        @Test
        @DisplayName("A3 - senha incorreta → lança BadCredentialsException")
        void autenticar_senhaIncorreta_lancaExcecao() {
            when(usuarioRepository.findByEmail(dtoLogin.getEmail()))
                    .thenReturn(Optional.of(usuarioNoBanco));
            when(passwordEncoder.matches(dtoLogin.getSenha(), usuarioNoBanco.getSenha()))
                    .thenReturn(false);

            assertThatThrownBy(() -> usuarioService.autenticar(
                    dtoLogin.getEmail(),
                    dtoLogin.getSenha()
            )).isInstanceOf(BadCredentialsException.class);

            verify(jwtUtil, never()).gerarToken(any());
        }

        /**
         * A4 — Email nulo: validação deve ocorrer antes de qualquer consulta.
         */
        @Test
        @DisplayName("A4 - email nulo → lança IllegalArgumentException")
        void autenticar_emailNulo_lancaExcecao() {
            assertThatThrownBy(() -> usuarioService.autenticar(
                    null,
                    dtoLogin.getSenha()
            )).isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).findByEmail(any());
            verify(jwtUtil, never()).gerarToken(any());
        }

        /**
         * A4b — Email em branco.
         */
        @Test
        @DisplayName("A4b - email em branco → lança IllegalArgumentException")
        void autenticar_emailVazio_lancaExcecao() {
            assertThatThrownBy(() -> usuarioService.autenticar(
                    "   ",
                    dtoLogin.getSenha()
            )).isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).findByEmail(any());
        }

        /**
         * A5 — Senha nula.
         */
        @Test
        @DisplayName("A5 - senha nula → lança IllegalArgumentException")
        void autenticar_senhaNula_lancaExcecao() {
            assertThatThrownBy(() -> usuarioService.autenticar(
                    dtoLogin.getEmail(),
                    null
            )).isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).findByEmail(any());
            verify(jwtUtil, never()).gerarToken(any());
        }

        /**
         * A5b — Senha em branco.
         */
        @Test
        @DisplayName("A5b - senha em branco → lança IllegalArgumentException")
        void autenticar_senhaVazia_lancaExcecao() {
            assertThatThrownBy(() -> usuarioService.autenticar(
                    dtoLogin.getEmail(),
                    "   "
            )).isInstanceOf(IllegalArgumentException.class);

            verify(usuarioRepository, never()).findByEmail(any());
        }
    }
}