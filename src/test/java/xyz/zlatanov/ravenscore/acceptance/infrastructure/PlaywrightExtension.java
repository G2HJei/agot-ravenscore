package xyz.zlatanov.ravenscore.acceptance.infrastructure;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ReducedMotion;
import lombok.val;
import org.junit.jupiter.api.extension.*;

import java.nio.file.Path;

/**
 * Gives every test a fresh browser context (no shared cookies or local storage) with a single page. When a test fails,
 * its Playwright trace and a screenshot are saved under {@code target/playwright-traces/<TestClass>/}. Open a trace
 * with {@code mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.classpathScope=test
 * "-Dexec.args=show-trace <trace.zip>"}.
 */
public class PlaywrightExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PlaywrightExtension.class);
    private static final Path                       TRACES    = Path.of("target", "playwright-traces");

    // the cookie consent banner would cover the page header
    private static final String ACCEPT_COOKIES = """
            try { localStorage.setItem('cookies-accept', 'true'); } catch (e) { }
            """;
    // hover tooltips can cover the buttons the tests click
    private static final String HIDE_TOOLTIPS  = """
            document.addEventListener('DOMContentLoaded', () => {
                const style = document.createElement('style');
                style.textContent = '.tooltip { display: none !important; }';
                document.head.appendChild(style);
            });
            """;

    @Override
    public void beforeEach(ExtensionContext context) {
        val browserContext = browser(context).newContext(new Browser.NewContextOptions()
                .setViewportSize(1600, 1000)
                .setReducedMotion(ReducedMotion.REDUCE) // disables Bootstrap's fade and collapse animations
                .setLocale("en-US")
                .setTimezoneId("UTC"));
        browserContext.setDefaultTimeout(10_000);
        browserContext.addInitScript(ACCEPT_COOKIES);
        browserContext.addInitScript(HIDE_TOOLTIPS);
        browserContext.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        val page = browserContext.newPage();
        // "Are you sure...?" confirmations (deletes, completing a stage): the test clicked the button, so it is sure
        page.onDialog(Dialog::accept);

        val store = context.getStore(NAMESPACE);
        store.put(BrowserContext.class, browserContext);
        store.put(Page.class, page);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        val store = context.getStore(NAMESPACE);
        val browserContext = store.get(BrowserContext.class, BrowserContext.class);
        try {
            if (context.getExecutionException().isPresent()) {
                val testDir = TRACES.resolve(context.getRequiredTestClass().getSimpleName());
                val testName = context.getRequiredTestMethod().getName();
                store.get(Page.class, Page.class).screenshot(new Page.ScreenshotOptions()
                        .setPath(testDir.resolve(testName + ".png"))
                        .setFullPage(true));
                browserContext.tracing().stop(new Tracing.StopOptions().setPath(testDir.resolve(testName + ".zip")));
            } else {
                browserContext.tracing().stop();
            }
        } finally {
            browserContext.close();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == Page.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE).get(Page.class, Page.class);
    }

    private static Browser browser(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent(BrowserSession.class, k -> new BrowserSession(), BrowserSession.class)
                .browser();
    }
}
