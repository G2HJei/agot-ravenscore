package xyz.zlatanov.ravenscore.acceptance.drivers.web;

import com.microsoft.playwright.Locator;

import java.util.Optional;
import java.util.regex.Pattern;

public final class Ui {

    private static final Pattern REGEX_SPECIAL_CHARACTERS = Pattern.compile("[.*+?^${}()|\\[\\]\\\\/]");

    private Ui() {
    }

    public static boolean hasClass(Locator locator, String cssClass) {
        return (Boolean) locator.evaluate("(element, cssClass) => element.classList.contains(cssClass)", cssClass);
    }

    public static Optional<String> textIfPresent(Locator locator) {
        return locator.count() == 0 ? Optional.empty() : Optional.of(locator.first().innerText().trim());
    }

    // CSS attribute selector, e.g. [data-rs-stage-name="Group Stage"]
    public static String attribute(String name, String value) {
        return "[" + name + "=\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"]";
    }

    // Playwright hands java.util.regex.Pattern sources to the browser, so escape for JavaScript (no \Q...\E)
    public static String regexLiteral(String text) {
        return REGEX_SPECIAL_CHARACTERS.matcher(text).replaceAll("\\\\$0");
    }
}
