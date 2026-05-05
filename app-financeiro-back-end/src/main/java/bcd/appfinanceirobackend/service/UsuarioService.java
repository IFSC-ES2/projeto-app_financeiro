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
}
