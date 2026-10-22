package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import lombok.val;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.PageReload;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.Ui;

import java.util.Optional;

/**
 * "Rankings" of a single game: one row per house.
 */
public class GameRankingModal extends Modal {

    public GameRankingModal(Page page) {
        super(page, "#rankingModal");
    }

    /**
     * @param house as displayed by Ravenscore, e.g. "Stark"
     */
    public GameRankingModal player(String house, String player) {
        row(house).locator("select[name=participantId]").selectOption(new SelectOption().setLabel(player));
        return this;
    }

    public GameRankingModal castles(String house, int castles) {
        row(house).locator("input[name=castles]").fill(String.valueOf(castles));
        return this;
    }

    public GameRankingModal points(String house, int points) {
        row(house).locator("input[name=points]").fill(String.valueOf(points));
        return this;
    }

    public GameRankingModal penaltyPoints(String house, int penaltyPoints) {
        row(house).locator("input[name=penaltyPoints]").fill(String.valueOf(penaltyPoints));
        return this;
    }

    public GameRankingModal completed(boolean completed) {
        root.locator("#gameCompleted").setChecked(completed);
        return this;
    }

    /**
     * "Calc points": points from the order of castles count, plus castles, plus the winner's bonus.
     */
    public GameRankingModal calculatePoints() {
        root.getByTestId("ranking-calc-points").click();
        return this;
    }

    /**
     * Returns the error shown when Ravenscore rejects the rankings.
     */
    public Optional<String> done() {
        val invalidFields = invalidFields();
        if (!invalidFields.isEmpty()) {
            return Optional.of(String.join("; ", invalidFields));
        }
        PageReload.markStale(page);
        val response = page.waitForResponse(
                r -> r.url().endsWith("/rankings") && "POST".equals(r.request().method()),
                () -> root.getByTestId("modal-done").click());
        if (response.ok()) {
            PageReload.awaitReload(page);
            return Optional.empty();
        }
        val error = root.locator("#rankingsError:not(.visually-hidden)");
        error.waitFor();
        return Optional.of(error.innerText().trim());
    }

    private Locator row(String house) {
        return root.locator("[data-testid=ranking-row]" + Ui.attribute("data-rs-house", house));
    }
}
