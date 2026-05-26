package meusistema.demo;

import meusistema.demo.enums.NivelUsuario;
import meusistema.demo.exception.AutenticacaoException;
import meusistema.demo.model.Usuario;
import meusistema.demo.repository.UsuarioRepository;
import meusistema.demo.service.ValidacaoSenhaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Complemento de testes unitários — cobre RF02, RF04,
 * RF05, RF06 e RF07.
 *
 * Estratégia Caixa Cinza:
 * conhecemos parcialmente a estrutura interna do sistema,
 * mas validamos entradas, saídas e regras de negócio.
 */
class ValidacaoSenhaServiceComplementoTest {

    private UsuarioRepository usuarioRepository;
    private ValidacaoSenhaService service;

    @BeforeEach
    void setUp() {
        this.usuarioRepository = mock(UsuarioRepository.class);
        this.service = new ValidacaoSenhaService(usuarioRepository);
    }

    // =========================================================
    // RF04 — Login com credenciais válidas
    // =========================================================

    @Test
    @DisplayName("RF04 - Deve retornar o usuário ao realizar login com credenciais corretas")
    void deveRealizarLoginComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setEmail("joao.silva@empresa.com");
        usuario.setSenha("Senh@Web2026");
        usuario.setContaBloqueada(false);
        usuario.setTentativasLogin(0);
        usuario.setNivelUsuario(NivelUsuario.CLIENTE);

        when(usuarioRepository.findByEmail("joao.silva@empresa.com"))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = service.realizarLogin("joao.silva@empresa.com", "Senh@Web2026");

        assertNotNull(resultado);
        assertEquals("joao.silva@empresa.com", resultado.getEmail());
        assertEquals(0, usuario.getTentativasLogin());

        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("RF04 - Deve zerar tentativas após login bem-sucedido")
    void deveZerarTentativasAposLoginBemSucedido() {
        Usuario usuario = new Usuario();
        usuario.setEmail("maria@empresa.com");
        usuario.setSenha("Corp@2026ok");
        usuario.setContaBloqueada(false);
        usuario.setTentativasLogin(2);
        usuario.setNivelUsuario(NivelUsuario.CLIENTE);

        when(usuarioRepository.findByEmail("maria@empresa.com"))
                .thenReturn(Optional.of(usuario));

        service.realizarLogin("maria@empresa.com", "Corp@2026ok");

        assertEquals(0, usuario.getTentativasLogin());
    }

    @Test
    @DisplayName("RF04 - Deve lançar exceção quando senha estiver incorreta")
    void deveRejeitarSenhaIncorreta() {
        Usuario usuario = new Usuario();
        usuario.setEmail("carlos@empresa.com");
        usuario.setSenha("SenhaCorret@1");
        usuario.setContaBloqueada(false);
        usuario.setTentativasLogin(0);
        usuario.setNivelUsuario(NivelUsuario.CLIENTE);

        when(usuarioRepository.findByEmail("carlos@empresa.com"))
                .thenReturn(Optional.of(usuario));

        AutenticacaoException ex = assertThrows(
                AutenticacaoException.class,
                () -> service.realizarLogin("carlos@empresa.com", "SenhaErrada#9")
        );

        assertEquals("Senha inválida.", ex.getMessage());
        assertEquals(1, usuario.getTentativasLogin());
    }

    @Test
    @DisplayName("RF04 - Deve bloquear login quando conta estiver bloqueada")
    void deveRejeitarLoginComContaBloqueada() {
        Usuario usuario = new Usuario();
        usuario.setEmail("bloqueado@empresa.com");
        usuario.setSenha("Senh@Web2026");
        usuario.setContaBloqueada(true);
        usuario.setTentativasLogin(3);
        usuario.setNivelUsuario(NivelUsuario.CLIENTE);

        when(usuarioRepository.findByEmail("bloqueado@empresa.com"))
                .thenReturn(Optional.of(usuario));

        AutenticacaoException ex = assertThrows(
                AutenticacaoException.class,
                () -> service.realizarLogin("bloqueado@empresa.com", "Senh@Web2026")
        );

        assertEquals("Erro de autenticação: Conta bloqueada após 3 tentativas inválidas.", ex.getMessage());
    }

    @Test
    @DisplayName("RF04 - Deve lançar exceção quando campos forem vazios")
    void deveRejeitarCamposVaziosNoLogin() {
        assertThrows(AutenticacaoException.class, () -> service.realizarLogin("", "Senha@1234"));
        assertThrows(AutenticacaoException.class, () -> service.realizarLogin("user@empresa.com", ""));
        assertThrows(AutenticacaoException.class, () -> service.realizarLogin(null, null));
    }

    // =========================================================
    // RF07 — Níveis de usuário
    // =========================================================

    @Test
    @DisplayName("RF07 - Deve retornar nível CLIENTE")
    void deveRetornarNivelCliente() {
        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@empresa.com");
        usuario.setSenha("Senh@Web2026");
        usuario.setContaBloqueada(false);
        usuario.setTentativasLogin(0);
        usuario.setNivelUsuario(NivelUsuario.CLIENTE);

        when(usuarioRepository.findByEmail("cliente@empresa.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = service.realizarLogin("cliente@empresa.com", "Senh@Web2026");
        assertEquals(NivelUsuario.CLIENTE, resultado.getNivelUsuario());
    }

    @Test
    @DisplayName("RF07 - Deve retornar nível GERENTE")
    void deveRetornarNivelGerente() {
        Usuario usuario = new Usuario();
        usuario.setEmail("gerente@empresa.com");
        usuario.setSenha("Senh@Web2026");
        usuario.setContaBloqueada(false);
        usuario.setTentativasLogin(0);
        usuario.setNivelUsuario(NivelUsuario.GERENTE);

        when(usuarioRepository.findByEmail("gerente@empresa.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = service.realizarLogin("gerente@empresa.com", "Senh@Web2026");
        assertEquals(NivelUsuario.GERENTE, resultado.getNivelUsuario());
    }

    @Test
    @DisplayName("RF07 - Deve retornar nível ADMIN")
    void deveRetornarNivelAdmin() {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@empresa.com");
        usuario.setSenha("Senh@Web2026");
        usuario.setContaBloqueada(false);
        usuario.setTentativasLogin(0);
        usuario.setNivelUsuario(NivelUsuario.ADMIN);

        when(usuarioRepository.findByEmail("admin@empresa.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = service.realizarLogin("admin@empresa.com", "Senh@Web2026");
        assertEquals(NivelUsuario.ADMIN, resultado.getNivelUsuario());
    }

    @Test
    @DisplayName("RF07 - Deve preservar nível após login")
    void devePreservarNivelAposLogin() {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin2@empresa.com");
        usuario.setSenha("Senh@Web2026");
        usuario.setContaBloqueada(false);
        usuario.setTentativasLogin(0);
        usuario.setNivelUsuario(NivelUsuario.ADMIN);

        when(usuarioRepository.findByEmail("admin2@empresa.com")).thenReturn(Optional.of(usuario));

        service.realizarLogin("admin2@empresa.com", "Senh@Web2026");
        assertEquals(NivelUsuario.ADMIN, usuario.getNivelUsuario());
    }

    // =========================================================
    // RF02 — Validação de senha
    // =========================================================

    @Test
    @DisplayName("RF02 - Deve rejeitar senha curta")
    void deveRejeitarSenhaCurta() {
        assertFalse(service.validarSenha("Sen@1"));
    }

    @Test
    @DisplayName("RF02 - Deve rejeitar senha longa")
    void deveRejeitarSenhaLonga() {
        assertFalse(service.validarSenha("Senh@Web20261"));
    }

    @Test
    @DisplayName("RF02 - Deve rejeitar senha sem números")
    void deveRejeitarSenhaSemNumero() {
        assertFalse(service.validarSenha("Senha@Corp!"));
    }

    @Test
    @DisplayName("RF02 - Deve rejeitar senha sem letras")
    void deveRejeitarSenhaSemLetras() {
        assertFalse(service.validarSenha("1234567@89"));
    }

    @Test
    @DisplayName("RF02 - Deve aceitar senha válida no limite máximo")
    void deveAceitarSenhaNoLimiteMaximo() {
        assertTrue(service.validarSenha("Senh@Web2026"));
    }

    @Test
    @DisplayName("RF02 - Deve aceitar senha válida no limite mínimo")
    void deveAceitarSenhaNoLimiteMinimo() {
        assertTrue(service.validarSenha("Corp@12345"));
    }

    // =========================================================
    // RF06 — Bloqueio de conta
    // =========================================================

    @Test
    @DisplayName("RF06 - Deve bloquear conta após 3 tentativas inválidas")
    void deveBloquearContaAposTresTentativas() {
        Usuario usuario = new Usuario();
        usuario.setEmail("teste@empresa.com");
        usuario.setSenha("Senha@1234");
        usuario.setContaBloqueada(false);
        usuario.setTentativasLogin(2);
        usuario.setNivelUsuario(NivelUsuario.CLIENTE);

        when(usuarioRepository.findByEmail("teste@empresa.com")).thenReturn(Optional.of(usuario));

        assertThrows(
                AutenticacaoException.class,
                () -> service.realizarLogin("teste@empresa.com", "senhaErrada")
        );

        assertTrue(usuario.isContaBloqueada());
        assertEquals(3, usuario.getTentativasLogin());
    }

    // =========================================================
    // RF05 — Usuário inexistente
    // =========================================================

    @Test
    @DisplayName("RF05 - Deve lançar exceção para usuário inexistente")
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(usuarioRepository.findByEmail("naoexiste@empresa.com")).thenReturn(Optional.empty());

        AutenticacaoException ex = assertThrows(
                AutenticacaoException.class,
                () -> service.realizarLogin("naoexiste@empresa.com", "123")
        );

        assertEquals("Usuário inexistente.", ex.getMessage());
    }
}