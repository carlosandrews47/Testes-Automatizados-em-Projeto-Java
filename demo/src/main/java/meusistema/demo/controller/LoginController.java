package meusistema.demo.controller;

import meusistema.demo.enums.NivelUsuario;
import meusistema.demo.exception.AutenticacaoException;
import meusistema.demo.model.Usuario;
import meusistema.demo.repository.UsuarioRepository;
import meusistema.demo.service.ValidacaoSenhaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final ValidacaoSenhaService validacaoSenhaService;

    private final UsuarioRepository usuarioRepository;

    public LoginController(
            ValidacaoSenhaService validacaoSenhaService,
            UsuarioRepository usuarioRepository) {

        this.validacaoSenhaService =
                validacaoSenhaService;

        this.usuarioRepository =
                usuarioRepository;
    }

    // =====================================================
    // HOME
    // =====================================================

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("modo", "login");

        return "login";
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @GetMapping("/login")
    public String mostrarLogin(Model model) {

        model.addAttribute("modo", "login");

        return "login";
    }

    @PostMapping("/login")
    public String realizarLogin(
            @RequestParam String email,
            @RequestParam String senha,
            Model model) {

        try {

            Usuario usuario =
                    validacaoSenhaService
                            .realizarLogin(email, senha);

            model.addAttribute(
                    "sucesso",
                    "Login realizado com sucesso!");

            model.addAttribute(
                    "nivel",
                    usuario.getNivelUsuario());

        } catch (AutenticacaoException ex) {

            model.addAttribute(
                    "erro",
                    ex.getMessage());
        }

        model.addAttribute("modo", "login");

        return "login";
    }

    // =====================================================
    // CADASTRO
    // =====================================================

    @GetMapping("/cadastro")
    public String mostrarCadastro(Model model) {

        model.addAttribute("modo", "cadastro");

        return "login";
    }

    @PostMapping("/cadastro")
    public String cadastrarUsuario(
            @RequestParam String nome,
            @RequestParam String login,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam NivelUsuario nivelUsuario,
            Model model) {

        model.addAttribute("modo", "cadastro");

        try {

            if (nome.isBlank()
                    || login.isBlank()
                    || email.isBlank()
                    || senha.isBlank()) {

                throw new AutenticacaoException(
                        "Campos obrigatórios vazios.");
            }

            // VERIFICA SE EMAIL JÁ EXISTE
            if (usuarioRepository
                    .findByEmail(email)
                    .isPresent()) {

                throw new AutenticacaoException(
                        "E-mail já cadastrado.");
            }

            // VALIDA SENHA
            if (!validacaoSenhaService
                    .validarSenha(senha)) {

                throw new AutenticacaoException(
                        "Senha fora do padrão.");
            }

            Usuario usuario = new Usuario();

            usuario.setNome(nome);

            usuario.setLogin(login);

            usuario.setEmail(email);

            usuario.setSenha(senha);

            usuario.setNivelUsuario(nivelUsuario);

            usuario.setContaBloqueada(false);

            usuario.setTentativasLogin(0);

            usuarioRepository.save(usuario);

            model.addAttribute(
                    "sucesso",
                    "Usuário cadastrado com sucesso!");

        } catch (AutenticacaoException ex) {

            model.addAttribute(
                    "erro",
                    ex.getMessage());
        }

        return "login";
    }

    // =====================================================
    // ALTERAR SENHA
    // =====================================================

    @GetMapping("/alterar-senha")
    public String mostrarAlterarSenha(Model model) {

        model.addAttribute("modo", "senha");

        return "login";
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(
            @RequestParam String email,
            @RequestParam String novaSenha,
            Model model) {

        model.addAttribute("modo", "senha");

        try {

            validacaoSenhaService
                    .alterarSenha(email, novaSenha);

            model.addAttribute(
                    "sucesso",
                    "Senha alterada com sucesso!");

        } catch (Exception ex) {

            model.addAttribute(
                    "erro",
                    ex.getMessage());
        }

        return "login";
    }
}