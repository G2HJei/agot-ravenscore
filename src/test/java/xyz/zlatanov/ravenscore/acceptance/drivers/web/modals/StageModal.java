package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import java.util.Optional;

import com.microsoft.playwright.Page;

/** "New Tournament Stage" / "Modify Tournament Stage" */
public class StageModal extends Modal {

    public StageModal(Page page) {
        super(page, "#stageModal");
    }

    public StageModal name(String name) {
        root.locator("#stageName").fill(name);
        return this;
    }

    public StageModal qualifiers(int qualifiers) {
        root.locator("#qualifyCount").fill(String.valueOf(qualifiers));
        return this;
    }

    public Optional<String> done() {
        return submit();
    }
}
