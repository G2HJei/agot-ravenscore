package xyz.zlatanov.ravenscore.acceptance.drivers.web;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Most Ravenscore actions end with a full page load: a form submit followed by a redirect, or location.reload() after an
 * AJAX call. The current document is marked before the action, so the wait is over once a new document has replaced it.
 */
public final class PageReload {

    private PageReload() {
    }

    public static void expectReload(Page page, Runnable action) {
        markStale(page);
        action.run();
        awaitReload(page);
    }

    public static void markStale(Page page) {
        page.evaluate("() => document.documentElement.setAttribute('data-rs-stale', '')");
    }

    public static void awaitReload(Page page) {
        page.locator("html:not([data-rs-stale])")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
        page.waitForLoadState(LoadState.LOAD);
    }
}
