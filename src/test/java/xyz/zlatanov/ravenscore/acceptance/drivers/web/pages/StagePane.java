package xyz.zlatanov.ravenscore.acceptance.drivers.web.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.val;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.Ui;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.GameModal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.PlayerModal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.RefreshSnrModal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.StageRankingModal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.model.StandingRow;

import java.util.List;
import java.util.Map;

/**
 * A stage's tab content: the stage rankings on one side, its games on the other.
 */
public class StagePane {

    private static final String READ_STANDINGS = """
            rows => rows.map(row => {
                const cell = testId => row.querySelector(`[data-testid=${testId}]`);
                const modifier = row.querySelector('.participant-data').dataset.rsParticipantPointsModifier;
                return {
                    rank: cell('standings-rank').innerText.trim(),
                    player: row.querySelector('.participant-data').dataset.rsParticipantName,
                    score: cell('standings-score').querySelector('span').innerText.trim(),
                    games: cell('standings-games').innerText.trim() || '0',
                    wins: String(cell('standings-wins').querySelectorAll('img').length),
                    avgPoints: cell('standings-avg-points').innerText.trim(),
                    tournamentWinner: cell('tournament-winner-crown') !== null,
                    aliases: [...row.querySelectorAll('.player-alias')].map(alias => alias.innerText.trim()),
                    pointsModifier: modifier
                };
            })
            """;

    private final Page    page;
    private final Locator root;

    StagePane(Page page, Locator root) {
        this.page = page;
        this.root = root;
    }

    public boolean isCompleted() {
        return "true".equals(root.getAttribute("data-rs-stage-completed"));
    }

    public boolean offersAddingPlayers() {
        return root.getByTestId("stage-add-player").isVisible();
    }

    public boolean offersNewGames() {
        return root.getByTestId("game-new").isVisible();
    }

    public PlayerModal addPlayer() {
        root.getByTestId("stage-add-player").click();
        return new PlayerModal(page);
    }

    public PlayerModal editPlayer(String player) {
        val row = standingsRow(player);
        row.locator(".action-container").hover(); // the edit icon only shows on hover
        row.getByTestId("player-edit").click();
        return new PlayerModal(page);
    }

    public GameModal newGame() {
        root.getByTestId("game-new").click();
        return new GameModal(page);
    }

    public GameItem game(String name) {
        val gameName = page.getByTestId("game-name").and(page.getByText(name, new Page.GetByTextOptions().setExact(true)));
        return new GameItem(page, root.getByTestId("game-item").filter(new Locator.FilterOptions().setHas(gameName)));
    }

    public RefreshSnrModal checkSwordsAndRavensGames() {
        root.getByTestId("games-check-snr").click();
        return new RefreshSnrModal(page);
    }

    public StageRankingModal finalizeRankings() {
        root.getByTestId("stage-finalize").click();
        return new StageRankingModal(page);
    }

    @SuppressWarnings("unchecked")
    public List<StandingRow> standings() {
        val rows = (List<Map<String, Object>>) root.getByTestId("standings-row").evaluateAll(READ_STANDINGS);
        return rows.stream()
                .map(row -> new StandingRow(
                        (String) row.get("rank"),
                        (String) row.get("player"),
                        (String) row.get("score"),
                        (String) row.get("games"),
                        (String) row.get("wins"),
                        (String) row.get("avgPoints"),
                        (Boolean) row.get("tournamentWinner"),
                        (List<String>) row.get("aliases"),
                        (String) row.get("pointsModifier")))
                .toList();
    }

    private Locator standingsRow(String player) {
        return root.getByTestId("standings-row")
                .filter(new Locator.FilterOptions().setHas(
                        page.locator(".participant-data" + Ui.attribute("data-rs-participant-name", player))));
    }
}
