package xyz.zlatanov.ravenscore.acceptance.drivers.web.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.val;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.Ui;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.GameRankingModal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.model.GameSummary;

import java.util.List;
import java.util.Map;

/**
 * A game in a stage's games list, which expands to show its details and admin actions.
 */
public class GameItem {

    private static final String READ_SUMMARY = """
            item => {
                const find = testId => item.querySelector(`[data-testid=${testId}]`);
                const round = find('game-round-badge');
                const lastActive = find('game-last-active-badge');
                const crown = find('game-winner-crown');
                return {
                    name: find('game-name').innerText.trim(),
                    completed: find('game-completed-badge') !== null,
                    round: round && !round.classList.contains('visually-hidden') ? round.innerText.trim() : null,
                    lastActive: lastActive ? lastActive.innerText.trim() : null,
                    link: item.querySelector('.game-data').dataset.rsGameLink || null,
                    winner: crown ? crown.closest('tr').dataset.rsPlayerName : null,
                    inactivityWarning: find('game-inactivity-warning') !== null,
                    rankings: [...item.querySelectorAll('[data-testid=game-ranking-row]')].map(row => {
                        // tooltip: a heading, then one "dd/MM/yyyy HH:mm UTC: N hours" line per inactivity
                        const inactivity = row.querySelector('[data-testid=player-inactivity]');
                        return {
                            house: row.dataset.rsPlayerHouse,
                            player: row.dataset.rsPlayerName,
                            points: row.dataset.rsPlayerPoints || '',
                            castles: row.dataset.rsPlayerCastles || '',
                            inactiveHours: inactivity
                                ? inactivity.dataset.bsTitle.split('<br>').slice(1).map(line => line.match(/(\\d+) hours?$/)[1])
                                : [],
                            inactivityWarning: inactivity !== null && inactivity.dataset.rsInactivityWarning === 'true'
                        };
                    })
                };
            }
            """;

    private final Page    page;
    private final Locator root;

    GameItem(Page page, Locator root) {
        this.page = page;
        this.root = root;
    }

    public boolean exists() {
        return root.count() > 0;
    }

    /**
     * Expands the game, unless already expanded (Ravenscore remembers the last expanded game).
     */
    public GameItem expand() {
        if (!Ui.hasClass(root.locator(".game-data"), "show")) {
            root.getByTestId("game-toggle").click();
        }
        root.locator(".game-data.show").waitFor();
        return this;
    }

    public GameRankingModal updateRankings() {
        expand();
        root.getByTestId("game-update-rankings").click();
        return new GameRankingModal(page);
    }

    public void selectRound(int round) {
        expand();
        page.waitForResponse(r -> r.url().endsWith("/round/" + round),
                () -> root.getByTestId("game-round-select").selectOption(String.valueOf(round)));
    }

    @SuppressWarnings("unchecked")
    public GameSummary summary() {
        val summary = (Map<String, Object>) root.evaluate(READ_SUMMARY);
        val rankings = ((List<Map<String, Object>>) summary.get("rankings")).stream()
                .map(r -> new GameSummary.Ranking((String) r.get("house"), (String) r.get("player"),
                        (String) r.get("points"), (String) r.get("castles"), (List<String>) r.get("inactiveHours"),
                        (Boolean) r.get("inactivityWarning")))
                .toList();
        return new GameSummary(
                (String) summary.get("name"),
                (Boolean) summary.get("completed"),
                (String) summary.get("round"),
                (String) summary.get("lastActive"),
                (String) summary.get("link"),
                (String) summary.get("winner"),
                (Boolean) summary.get("inactivityWarning"),
                rankings);
    }
}
