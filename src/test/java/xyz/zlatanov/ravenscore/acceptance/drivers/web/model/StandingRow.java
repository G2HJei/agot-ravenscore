package xyz.zlatanov.ravenscore.acceptance.drivers.web.model;

import java.util.List;

/** One row of a stage's rankings table, as displayed. */
public record StandingRow(String rank, String player, String score, String games, String wins, String avgPoints,
        boolean tournamentWinner, List<String> aliases, String pointsModifier) {
}
