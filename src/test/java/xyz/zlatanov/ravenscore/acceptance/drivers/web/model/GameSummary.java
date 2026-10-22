package xyz.zlatanov.ravenscore.acceptance.drivers.web.model;

import java.util.List;

/** A game as displayed in a stage's games list; the round and last active labels are null when not shown. */
public record GameSummary(String name, boolean completed, String roundLabel, String lastActiveLabel, String link,
        String winner, List<Ranking> rankings) {

    /** One row of the game's rankings table. */
    public record Ranking(String house, String player, String points, String castles) {
    }
}
