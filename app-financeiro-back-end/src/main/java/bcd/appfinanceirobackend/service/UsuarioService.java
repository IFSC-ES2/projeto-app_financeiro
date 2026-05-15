package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.auth.RegisterRequestDTO;
import bcd.appfinanceirobackend.dto.auth.TokenDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern CPF_REGEX = Pattern.compile("^\\d{11}$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Usuario registrar(RegisterRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados de cadastro obrigatórios.");
        }
        validarObrigatorio(dto.getNome(), "nome");
        validarObrigatorio(dto.getEmail(), "email");
        if (!EMAIL_REGEX.matcher(dto.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("E-mail em formato inválido.");
        }
        validarObrigatorio(dto.getSenha(), "senha");
        validarObrigatorio(dto.getCpf(), "cpf");
        if (!CPF_REGEX.matcher(dto.getCpf().trim()).matches()) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos numéricos.");
        }
        if (!isCpfValido(dto.getCpf().trim())) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new DataIntegrityViolationException("E-mail já cadastrado.");
        }
        if (usuarioRepository.existsByCpf(dto.getCpf())) {
            throw new DataIntegrityViolationException("CPF já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(dto.getEmail().trim());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCpf(dto.getCpf().trim());
        usuario.setCreatedAt(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    public TokenDTO autenticar(String email, String senha) {
        validarObrigatorio(email, "email");
        validarObrigatorio(senha, "senha");

        Optional<Usuario> existente = usuarioRepository.findByEmail(email);
        Usuario usuario = existente.orElseThrow(
                () -> new UsernameNotFoundException("Usuário não encontrado.")
        );

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }

        return jwtUtil.gerarToken(usuario);
    }

    private static void validarObrigatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo " + campo + " é obrigatório.");
        }
    }

    // Ref: https://www.dio.me/articles/formatacao-de-cpf-em-java
    private static boolean isCpfValido(String cpf) {
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        int[] digitos = new int[11];
        for (int i = 0; i < 11; i++) {
            digitos[i] = cpf.charAt(i) - '0';
        }
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += digitos[i] * (10 - i);
        }
        int resto = soma % 11;
        int dv1 = (resto < 2) ? 0 : (11 - resto);

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += digitos[i] * (11 - i);
        }
        resto = soma % 11;
        int dv2 = (resto < 2) ? 0 : (11 - resto);

        return dv1 == digitos[9] && dv2 == digitos[10];
    }
}
