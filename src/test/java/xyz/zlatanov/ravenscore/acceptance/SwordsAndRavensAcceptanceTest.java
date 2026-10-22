package xyz.zlatanov.ravenscore.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SwordsAndRavensAcceptanceTest extends AcceptanceTestBase {

    @BeforeEach
    void aStageWithSixPlayers() {
        ravenscore.newTournament("name: Raven Cup");
        ravenscore.unlockAdmin();
        ravenscore.addStage("name: Round 1");
        ravenscore.addPlayer("name: Arya", "aliases: NoOne");
        ravenscore.addPlayers("Brienne", "Cersei", "Davos", "Euron", "Jaime");
    }

    @Test
    void a_game_finished_on_swords_and_ravens_updates_the_standings() {
        ravenscore.createGame("name: Online Table", "played on: Swords and Ravens");

        // players are recognised by name, regardless of case, or by one of their aliases
        swordsAndRavens.gameIsFinished("game: Online Table", "round: 9", """
                house     | player  | victory points
                Stark     | NoOne   | 7
                Lannister | cersei  | 6
                Greyjoy   | Euron   | 5
                Tyrell    | Brienne | 4
                Baratheon | Davos   | 3
                Martell   | Jaime   | 2
                """);

        ravenscore.updateFromSwordsAndRavens();

        ravenscore.verifySwordsAndRavensUpdate("game: Online Table", "round: 9", "completed: yes", """
                rank | player  | house     | points
                1    | Arya    | Stark     | 15
                2    | Cersei  | Lannister | 10
                3    | Euron   | Greyjoy   | 8
                4    | Brienne | Tyrell    | 6
                5    | Davos   | Baratheon | 4
                6    | Jaime   | Martell   | 2
                """);
        ravenscore.verifyGame("game: Online Table", "status: completed", "winner: Arya");
        ravenscore.verifyStandings("""
                rank | player  | score | wins
                1    | Arya    | 15    | 1
                2    | Cersei  | 10    | 0
                3    | Euron   | 8     | 0
                4    | Brienne | 6     | 0
                5    | Davos   | 4     | 0
                6    | Jaime   | 2     | 0
                """);
    }

    @Test
    void warns_about_swords_and_ravens_games_inactive_for_48_hours_or_more() {
        ravenscore.createGame("name: Sleepy Table", "played on: Swords and Ravens");
        ravenscore.createGame("name: Busy Table", "played on: Swords and Ravens");
        swordsAndRavens.gameIsOngoing("game: Sleepy Table", "round: 3", "last active: 50 hours ago");
        swordsAndRavens.gameIsOngoing("game: Busy Table", "round: 5", "last active: 47 hours ago");

        ravenscore.checkSwordsAndRavens();

        ravenscore.verifyInactivityWarning("game: Sleepy Table", "hours: 50");
        ravenscore.verifyNoInactivityWarning("game: Busy Table");

        ravenscore.confirmSwordsAndRavensUpdates();

        ravenscore.verifyGame("game: Sleepy Table", "round: 3", "last active: 50 hours ago");
        ravenscore.verifyGame("game: Busy Table", "round: 5", "last active: 47 hours ago");
    }

    @Test
    void games_are_left_unchanged_when_swords_and_ravens_is_unavailable() {
        ravenscore.createGame("name: Online Table", "played on: Swords and Ravens");
        swordsAndRavens.gameIsUnavailable("game: Online Table");

        ravenscore.checkSwordsAndRavens();

        ravenscore.verifySwordsAndRavensFailure("game: Online Table");
        ravenscore.verifyGame("game: Online Table", "status: not started");
    }
}
