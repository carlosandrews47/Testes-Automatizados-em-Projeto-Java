package meusistema.demo.service;

import meusistema.demo.exception.AutenticacaoException;
import meusistema.demo.model.Usuario;
import meusistema.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class ValidacaoSenhaService {

    private final UsuarioRepository usuarioRepository;

    public ValidacaoSenhaService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // =====================================================
    // VALIDAR SENHA (RF02)
    // =====================================================
    public boolean validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            return false;
        }

        // Mínimo 10 e máximo 12 caracteres
        if (senha.length() < 10 || senha.length() > 12) {
            return false;
        }

        boolean temLetra = senha.matches(".*[A-Za-z].*");
        boolean temNumero = senha.matches(".*\\d.*");
        boolean temEspecial = senha.matches(".*[!@#$%&*()].*");

        return temLetra && temNumero && temEspecial;
    }

    // =====================================================
    // LOGIN (RF04, RF05, RF06)
    // =====================================================
    public Usuario realizarLogin(String email, String senha) {
        // RF03 & RF05 - Campos obrigatórios vazios
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new AutenticacaoException("Campos obrigatórios vazios.");
        }

        // RF05 - Usuário inexistente
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AutenticacaoException("Usuário inexistente."));

        // RF06 - Bloqueio de conta
        if (usuario.isContaBloqueada()) {
            throw new AutenticacaoException("Erro de autenticação: Conta bloqueada após 3 tentativas inválidas.");
        }

        // RF05 - Senha inválida
        if (!usuario.getSenha().equals(senha)) {
            usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);

            if (usuario.getTentativasLogin() >= 3) {
                usuario.setContaBloqueada(true);
            }

            usuarioRepository.save(usuario);
            throw new AutenticacaoException("Senha inválida.");
        }

        // Login realizado com sucesso -> Reseta as tentativas
        usuario.setTentativasLogin(0);
        usuarioRepository.save(usuario);

        return usuario;
    }

    // =====================================================
    // ALTERAR SENHA (RF02, RF05)
    // =====================================================
    public void alterarSenha(String email, String novaSenha) {
        if (email == null || email.isBlank() || novaSenha == null || novaSenha.isBlank()) {
            throw new AutenticacaoException("Campos obrigatórios vazios.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AutenticacaoException("Usuário inexistente."));

        if (!validarSenha(novaSenha)) {
            throw new AutenticacaoException("Senha inválida.");
        }

        usuario.setSenha(novaSenha);
        usuario.setTentativasLogin(0);
        usuario.setContaBloqueada(false);
        usuarioRepository.save(usuario);
    }
}