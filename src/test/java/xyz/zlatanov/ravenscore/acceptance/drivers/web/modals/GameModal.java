package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import java.util.Optional;
import java.util.regex.Pattern;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import xyz.zlatanov.ravenscore.acceptance.drivers.web.Ui;

/** "New Game" / "Modify Game Details" */
public class GameModal extends Modal {

    public GameModal(Page page) {
        super(page, "#gameModal");
    }

    /** @param type the option as shown, e.g. "Base Game 2nd Edition" */
    public GameModal type(String type) {
        root.locator("#gameType").selectOption(new SelectOption().setLabel(type));
        return this;
    }

    public GameModal name(String name) {
        root.locator("#gameName").fill(name);
        return this;
    }

    public GameModal link(String link) {
        root.locator("#gameLink").fill(link);
        return this;
    }

    public GameModal selectAllPlayers() {
        root.locator(".participant-check").all().forEach(check -> check.setChecked(true));
        return this;
    }

    public GameModal selectPlayer(String name) {
        // labelled with the player's stage rank and points modifier, e.g. "3. Arya" or "3. Arya (x0.5)"
        root.getByLabel(Pattern.compile("^\\d+\\. " + Ui.regexLiteral(name) + "( \\(x[\\d.]+\\))?$")).setChecked(true);
        return this;
    }

    public Optional<String> done() {
        return submit();
    }
}
