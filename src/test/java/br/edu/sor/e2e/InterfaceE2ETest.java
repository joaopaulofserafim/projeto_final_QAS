package br.edu.sor.e2e;

import static br.edu.sor.suporte.Api.pedidoReserva;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import br.edu.sor.suporte.IntegracaoBase;

/**
 * Story 1.9 — testes de ponta a ponta no navegador (Chromium headless) contra a aplicação real e o PostgreSQL
 * do Testcontainers. No CI (E2E_OBRIGATORIO=true) a ausência do navegador falha o build; localmente, pula.
 */
@Tag("e2e")
class InterfaceE2ETest extends IntegracaoBase {

    private static Playwright playwright;
    private static Browser navegador;

    private BrowserContext contexto;
    private Page pagina;
    private final List<String> errosNoConsole = new ArrayList<>();

    @BeforeAll
    static void abrirNavegador() {
        try {
            playwright = Playwright.create();
            navegador = playwright.chromium().launch();
        } catch (RuntimeException e) {
            if ("true".equalsIgnoreCase(System.getenv("E2E_OBRIGATORIO"))) {
                throw e;
            }
            Assumptions.abort("Navegador do Playwright indisponível neste ambiente: " + e.getMessage());
        }
    }

    @AfterAll
    static void fecharNavegador() {
        if (navegador != null) {
            navegador.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void novaPagina() {
        abrir(new Browser.NewContextOptions());
    }

    @AfterEach
    void fecharPagina() {
        contexto.close();
    }

    private void abrir(Browser.NewContextOptions opcoes) {
        if (contexto != null) {
            contexto.close();
        }
        contexto = navegador.newContext(opcoes.setLocale("pt-BR").setTimezoneId("America/Sao_Paulo"));
        pagina = contexto.newPage();
        // O navegador usa o mesmo "agora" do servidor de teste: a tela recusa datas passadas pelo relógio local.
        pagina.clock().setFixedTime("2026-11-09T08:00:00-03:00");
        // Exceções de JavaScript e violações da CSP; respostas HTTP 4xx esperadas (ex.: 409) não contam.
        pagina.onPageError(errosNoConsole::add);
        pagina.onConsoleMessage(m -> {
            if ("error".equals(m.type()) && m.text().contains("Content Security Policy")) {
                errosNoConsole.add(m.text());
            }
        });
    }

    private String url(String caminho) {
        return "http://localhost:" + porta + caminho;
    }

    private void entrar(String usuario, String nomeEsperado) {
        pagina.navigate(url("/"));
        pagina.fill("#login-usuario", usuario);
        pagina.fill("#login-senha", "Senha@123");
        pagina.click("[data-testid=entrar]");
        assertThat(pagina.locator("[data-testid=usuario-atual]")).containsText(nomeEsperado);
    }

    @Test
    void solicitanteReservaVeResultadoEmJanelaECartaoOcupadoFicaBloqueado() {
        long sala = api.criarRecurso("SALA-E2E", "SALA", false);
        entrar("solicitante", "Marina Costa");

        pagina.selectOption("#busca-tipo", "SALA");
        pagina.fill("#busca-inicio", "2026-11-20T10:00");
        pagina.fill("#busca-fim", "2026-11-20T12:00");
        pagina.click("[data-testid=pesquisar]");
        String cartao = "#grade-recursos .recurso-card[data-recurso='" + sala + "']";
        assertThat(pagina.locator(cartao)).containsText("Livre neste período");
        assertThat(pagina.locator("#grade-recursos .secao-recursos-titulo").first()).containsText("Salas");
        pagina.check(cartao + " input.escolher");

        pagina.fill("#reserva-finalidade", "Aula de Engenharia de Software");
        pagina.click("[data-testid=solicitar-reserva]");
        assertThat(pagina.locator("[data-testid=modal]")).containsText("Reserva confirmada");
        assertThat(pagina.locator("[data-testid=modal]")).containsText("SALA-E2E");
        pagina.click("[data-testid=modal-botao-0]");
        assertThat(pagina.locator("[data-testid=modal]")).isHidden();

        // Depois de reservar: campos limpos, nada selecionado e o cartão fica cinza, sem clique
        assertThat(pagina.locator("#reserva-finalidade")).hasValue("");
        assertThat(pagina.locator("#escolhidos")).containsText("Nenhum recurso escolhido");
        assertThat(pagina.locator(cartao)).hasClass(java.util.regex.Pattern.compile("indisponivel"));
        assertThat(pagina.locator(cartao)).containsText("Reservado neste horário");
        assertThat(pagina.locator(cartao + " input.escolher")).isDisabled();

        pagina.click("button[data-aba=minhas]");
        assertThat(pagina.locator("#lista-minhas .reserva-card")).hasCount(1);
        assertThat(pagina.locator("#lista-minhas")).containsText("Aprovada");
        pagina.click("#lista-minhas button[data-acao=historico]");
        assertThat(pagina.locator("#lista-minhas .reserva-card .rv-historico")).containsText("Confirmada na hora");

        pagina.click("#lista-minhas button[data-acao=cancelar]");
        assertThat(pagina.locator("[data-testid=modal]")).containsText("Cancelar a reserva");
        pagina.click("[data-testid=modal-botao-1]");
        assertThat(pagina.locator("[data-testid=modal]")).containsText("Reserva cancelada");
        pagina.click("[data-testid=modal-botao-0]");
        assertThat(pagina.locator("#lista-minhas")).containsText("Cancelada");
        assertThat(errosNoConsole).as("erros de JavaScript/CSP no console").isEmpty();
    }

    @Test
    void conflitoNoMomentoDoEnvioApareceEmJanelaCentral() {
        long sala = api.criarRecurso("SALA-DISPUTADA", "SALA", false);
        entrar("solicitante", "Marina Costa");
        pagina.fill("#busca-inicio", "2026-11-20T10:00");
        pagina.fill("#busca-fim", "2026-11-20T12:00");
        pagina.click("[data-testid=pesquisar]");
        String cartao = "#grade-recursos .recurso-card[data-recurso='" + sala + "']";
        assertThat(pagina.locator(cartao)).containsText("Livre neste período");
        pagina.check(cartao + " input.escolher");

        // Outra pessoa reserva o mesmo horário enquanto a tela ainda mostra "livre"
        api.como("solicitante2").post("/api/v1/reservas", pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", sala));
        pagina.fill("#reserva-finalidade", "Monitoria");
        pagina.click("[data-testid=solicitar-reserva]");

        assertThat(pagina.locator("[data-testid=modal]")).containsText("Conflito de horário");
        assertThat(pagina.locator("[data-testid=modal]")).containsText("já está reservado");
        pagina.click("[data-testid=modal-botao-0]");
        assertThat(pagina.locator(cartao)).hasClass(java.util.regex.Pattern.compile("indisponivel"));
        assertThat(errosNoConsole).as("erros de JavaScript/CSP no console").isEmpty();
    }

    @Test
    void responsavelAprovaSolicitacaoRestritaPelaInterface() {
        long laboratorio = api.criarRecurso("LAB-E2E", "SALA", true);
        long id = api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", laboratorio)).getBody().get("id").asLong();
        entrar("responsavel", "Rita Responsável");

        pagina.click("button[data-aba=aprovacoes]");
        String linha = "#lista-fila .reserva-card[data-reserva='" + id + "']";
        assertThat(pagina.locator(linha)).containsText("LAB-E2E");
        pagina.fill(linha + " .motivo", "Liberado para a disciplina");
        pagina.click(linha + " button[data-acao=aprovar]");

        assertThat(pagina.locator("[data-testid=mensagem]")).containsText("Aprovada");
        assertThat(pagina.locator("#lista-uso .reserva-card[data-reserva='" + id + "']")).containsText("Iniciar uso");
        assertThat(pagina.locator("button[data-aba=admin]")).isHidden();
    }

    @Test
    void loginInvalidoMostraMensagemCompreensivel() {
        pagina.navigate(url("/"));
        pagina.fill("#login-usuario", "solicitante");
        pagina.fill("#login-senha", "errada");
        pagina.click("[data-testid=entrar]");

        assertThat(pagina.locator("[data-testid=login-erro]")).containsText("Usuário ou senha inválidos");
        assertThat(pagina.locator("[data-testid=mensagem]")).isHidden();
        assertThat(pagina.locator("#tela-app")).isHidden();

        pagina.fill("#login-senha", "visivel");
        pagina.click("#mostrar-senha");
        assertThat(pagina.locator("#login-senha")).hasAttribute("type", "text");
    }

    @Test
    void interfaceFuncionaEmTelaDeCelularSemRolagemHorizontal() {
        abrir(new Browser.NewContextOptions().setViewportSize(390, 844).setIsMobile(true).setHasTouch(true));
        entrar("admin", "Administrador do Sistema");

        pagina.click("button[data-aba=admin]");
        assertThat(pagina.locator("[data-testid=painel-resumo]")).containsText("Conflitos evitados");
        Object larguraExcedente = pagina.evaluate("document.documentElement.scrollWidth - window.innerWidth");
        assertThat(((Number) larguraExcedente).intValue()).as("pixels de rolagem horizontal").isLessThanOrEqualTo(0);
        assertThat(pagina.locator("button[data-aba=minhas]")).isHidden();
    }

    @Test
    void sairEncerraASessao() {
        entrar("solicitante", "Marina Costa");

        pagina.click("#botao-sair");

        assertThat(pagina.locator("#tela-login")).isVisible();
        pagina.reload();
        assertThat(pagina.locator("#tela-login")).isVisible();
    }

    @Test
    void documentacaoDaApiCarregaNoNavegador() {
        pagina.navigate(url("/swagger-ui.html"));

        assertThat(pagina.locator(".swagger-ui .info .title")).containsText("Organização de Recursos");
    }
}
