package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import com.microsoft.playwright.Page;
import lombok.val;

import java.util.List;
import java.util.Optional;

/**
 * "New Player" / "Modify Player"
 */
public class PlayerModal extends Modal {

    public PlayerModal(Page page) {
        super(page, "#playerModal");
    }

    public PlayerModal name(String name) {
        root.locator("#playerName").fill(name);
        return this;
    }

    public PlayerModal pointsModifier(String pointsModifier) {
        root.locator("#playerPointsModifier").fill(pointsModifier);
        return this;
    }

    /**
     * Replaces the player's profile links and aliases.
     */
    public PlayerModal aliases(List<String> aliases) {
        val inputs = root.locator("input[name=profileLinks]");
        while (inputs.count() < aliases.size()) {
            root.locator("#addAlias").click();
        }
        val fields = inputs.all();
        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).fill(i < aliases.size() ? aliases.get(i) : ""); // empty aliases are ignored by Ravenscore
        }
        return this;
    }

    public Optional<String> done() {
        return submit();
    }
}
