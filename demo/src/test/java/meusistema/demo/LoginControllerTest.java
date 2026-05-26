package meusistema.demo;

import meusistema.demo.controller.LoginController;
import meusistema.demo.exception.AutenticacaoException;
import meusistema.demo.repository.UsuarioRepository;
import meusistema.demo.service.ValidacaoSenhaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ValidacaoSenhaService validacaoSenhaService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("HTML Único - Deve carregar a página de login com o modo correto")
    void deveExibirFormularioLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("modo", "login"));
    }

    @Test
    @DisplayName("HTML Único - Deve exibir o formulário de cadastro na mesma página")
    void deveExibirFormularioCadastro() throws Exception {
        mockMvc.perform(get("/cadastro"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("modo", "cadastro"));
    }

    @Test
    @DisplayName("HTML Único - Deve exibir o formulário de alteração de senha na mesma página")
    void deveExibirFormularioAlterarSenha() throws Exception {
        mockMvc.perform(get("/alterar-senha"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("modo", "senha"));
    }

    @Test
    @DisplayName("RF05 - Deve injetar mensagem de erro no modelo caso ocorra falha de autenticação")
    void deveExibirErroQuandoLoginFalhar() throws Exception {
        doThrow(new AutenticacaoException("Senha inválida."))
                .when(validacaoSenhaService).realizarLogin(anyString(), anyString());

        mockMvc.perform(post("/login")
                        .param("email", "erro@empresa.com")
                        .param("senha", "errada123"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attribute("erro", "Senha inválida."))
                .andExpect(model().attribute("modo", "login"));
    }
}