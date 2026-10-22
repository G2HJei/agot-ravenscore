package xyz.zlatanov.ravenscore.acceptance.drivers.web.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.val;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.PageReload;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.Ui;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.TournamentModal;

import java.util.Optional;

/**
 * The home page, listing the public tournaments.
 */
public class HomePage {

    private final Page page;

    public HomePage(Page page) {
        this.page = page;
    }

    public TournamentModal newTournament() {
        page.getByTestId("home-new-tournament").click();
        return new TournamentModal(page);
    }

    public boolean lists(String tournamentName) {
        return card(tournamentName).count() > 0;
    }

    public Optional<String> winnerOf(String tournamentName) {
        return Ui.textIfPresent(card(tournamentName).getByTestId("home-tournament-winner"));
    }

    public TournamentPage open(String tournamentName) {
        PageReload.expectReload(page, () -> card(tournamentName).click());
        return new TournamentPage(page);
    }

    private Locator card(String tournamentName) {
        val name = page.getByTestId("home-tournament-name")
                .and(page.getByText(tournamentName, new Page.GetByTextOptions().setExact(true)));
        return page.getByTestId("home-tournament-card").filter(new Locator.FilterOptions().setHas(name));
    }
}
