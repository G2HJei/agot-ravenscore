package xyz.zlatanov.ravenscore.acceptance.infrastructure;

import org.junit.jupiter.api.extension.ExtensionContext;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/**
 * One browser per test run, closed by JUnit at the end. Configurable with system properties:
 * <ul>
 * <li>{@code playwright.browser}: chromium (default), firefox or webkit</li>
 * <li>{@code playwright.headless}: true (default) or false to watch the tests run</li>
 * <li>{@code playwright.slowMo}: milliseconds to pause between browser actions, e.g. 200</li>
 * </ul>
 */
class BrowserSession implements ExtensionContext.Store.CloseableResource {

    private final Playwright playwright;
    private final Browser    browser;

    BrowserSession() {
        playwright = Playwright.create();
        browser = browserType().launch(new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(System.getProperty("playwright.headless", "true")))
                .setSlowMo(Double.parseDouble(System.getProperty("playwright.slowMo", "0"))));
    }

    Browser browser() {
        return browser;
    }

    @Override
    public void close() {
        browser.close();
        playwright.close();
    }

    private BrowserType browserType() {
        return switch (System.getProperty("playwright.browser", "chromium")) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };
    }
}
