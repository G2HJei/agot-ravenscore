package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import com.microsoft.playwright.Page;
import lombok.val;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.PageReload;

import java.util.Optional;

/**
 * "Tournament Admin": unlocks the tournament administration with the tournament key.
 */
public class UnlockAdminModal extends Modal {

    public UnlockAdminModal(Page page) {
        super(page, "#unlockAdminModal");
    }

    /**
     * Returns the error shown when the key is rejected.
     */
    public Optional<String> unlock(String key) {
        root.getByTestId("unlock-key").fill(key);
        val invalidFields = invalidFields();
        if (!invalidFields.isEmpty()) {
            return Optional.of(String.join("; ", invalidFields));
        }
        PageReload.markStale(page);
        val response = page.waitForResponse(r -> r.url().endsWith("/unlock-tournament"),
                () -> root.getByTestId("unlock-submit").click());
        if (response.ok()) {
            PageReload.awaitReload(page);
            return Optional.empty();
        }
        val error = root.locator("#tournamentKeyError:not(.visually-hidden)");
        error.waitFor();
        return Optional.of(error.innerText().trim());
    }
}
