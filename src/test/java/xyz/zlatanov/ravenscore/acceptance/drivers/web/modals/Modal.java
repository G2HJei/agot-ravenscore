package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.val;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.PageReload;

import java.util.List;
import java.util.Optional;

/**
 * A Bootstrap modal dialog. Elements are always looked up inside the modal, as some ids repeat across modals.
 */
public abstract class Modal {

    protected final Page    page;
    protected final Locator root;

    protected Modal(Page page, String selector) {
        this.page = page;
        this.root = page.locator(selector);
        root.waitFor();
        page.locator("body.modal-open").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    }

    public void close() {
        root.locator(".btn-close").click();
        root.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    /**
     * Clicks "Done" and waits for the page to reload with the result. If the browser rejects the form (e.g. a required
     * field is empty), the modal stays open and the validation problems are returned instead.
     */
    protected Optional<String> submit() {
        val invalidFields = invalidFields();
        if (!invalidFields.isEmpty()) {
            root.getByTestId("modal-done").click();
            return Optional.of(String.join("; ", invalidFields));
        }
        PageReload.expectReload(page, () -> root.getByTestId("modal-done").click());
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    protected List<String> invalidFields() {
        return (List<String>) root.locator("form").first().evaluate("""
                form => [...form.elements]
                    .filter(field => !field.checkValidity())
                    .map(field => (field.labels && field.labels.length ? field.labels[0].innerText.trim() : field.name)
                        + ': ' + field.validationMessage)
                """);
    }
}
