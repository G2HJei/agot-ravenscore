package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import java.util.Optional;

import com.microsoft.playwright.Page;

/** "Host New Tournament" / "Modify Tournament" */
public class TournamentModal extends Modal {

    public TournamentModal(Page page) {
        super(page, "#tournamentModal");
    }

    public TournamentModal name(String name) {
        root.locator("#tournamentName").fill(name);
        return this;
    }

    public TournamentModal rules(String rules) {
        root.locator("#tournamentDescription").fill(rules);
        return this;
    }

    public TournamentModal privateTournament(boolean privateTournament) {
        root.locator("#tournamentHidden").setChecked(privateTournament);
        return this;
    }

    public TournamentModal key(String key) {
        root.locator("#tournamentKey").fill(key);
        return this;
    }

    public Optional<String> done() {
        return submit();
    }
}
