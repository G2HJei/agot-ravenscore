package xyz.zlatanov.ravenscore.acceptance;

import com.microsoft.playwright.Page;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import xyz.zlatanov.ravenscore.acceptance.drivers.snr.SwordsAndRavensStubDriver;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.RavenscoreWebDriver;
import xyz.zlatanov.ravenscore.acceptance.dsl.Ravenscore;
import xyz.zlatanov.ravenscore.acceptance.dsl.ScenarioContext;
import xyz.zlatanov.ravenscore.acceptance.dsl.SwordsAndRavens;
import xyz.zlatanov.ravenscore.acceptance.infrastructure.AcceptanceTestConfiguration;
import xyz.zlatanov.ravenscore.acceptance.infrastructure.PlaywrightExtension;
import xyz.zlatanov.ravenscore.acceptance.infrastructure.SnrWireMockServer;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Runs Ravenscore on a random port, backed by an in-memory H2 database and a stubbed Swords & Ravens, and gives
 * each test a fresh browser to use it with.
 * <p>
 * Every acceptance test shares this one configuration, so Spring starts Ravenscore once for the whole run. Tests do
 * not clean up after themselves: each works with its own uniquely named tournaments (see {@link ScenarioContext}).
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("acceptance")
@Import(AcceptanceTestConfiguration.class)
@ExtendWith(PlaywrightExtension.class)
public abstract class AcceptanceTestBase {

    @LocalServerPort
    private int port;

    protected Ravenscore      ravenscore;
    protected SwordsAndRavens swordsAndRavens;

    @DynamicPropertySource
    static void swordsAndRavensApi(DynamicPropertyRegistry registry) {
        registry.add("ravenscore.snr.api-url", SnrWireMockServer::baseUrl);
    }

    @BeforeEach
    void createDsl(Page page) {
        val context = new ScenarioContext();
        ravenscore = new Ravenscore(new RavenscoreWebDriver(page, "http://localhost:" + port), context);
        swordsAndRavens = new SwordsAndRavens(new SwordsAndRavensStubDriver(SnrWireMockServer.server()), context);
    }

    @AfterEach
    void failOnUnexpectedErrors() {
        ravenscore.verifyNoUnexpectedErrors();
    }
}
