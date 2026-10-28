package xyz.zlatanov.ravenscore.acceptance.drivers.web.model;

import java.util.List;

/**
 * A game as displayed in a stage's games list; the round and last active labels are null when not shown. The
 * inactivity warning flags a player who did not act more times than their penalty points.
 */
public record GameSummary(String name, boolean completed, String roundLabel, String lastActiveLabel, String link,
        String winner, boolean inactivityWarning, List<Ranking> rankings) {

    /**
     * One row of the game's rankings table. The inactive hours are those of each time the player did not act for 48
     * hours or more, oldest first; the warning means more such times than penalty points.
     */
    public record Ranking(String house, String player, String points, String castles, List<String> inactiveHours,
            boolean inactivityWarning) {
    }
}
