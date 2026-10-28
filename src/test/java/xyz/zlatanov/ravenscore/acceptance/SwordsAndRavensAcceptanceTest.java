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
    void a_swords_and_ravens_game_active_again_in_the_same_round_is_updated() {
        ravenscore.createGame("name: Sleepy Table", "played on: Swords and Ravens");
        swordsAndRavens.gameIsOngoing("game: Sleepy Table", "round: 3", "last active: 8 days ago");
        ravenscore.updateFromSwordsAndRavens();

        swordsAndRavens.gameIsOngoing("game: Sleepy Table", "round: 3", "last active: recently");
        ravenscore.updateFromSwordsAndRavens();

        ravenscore.verifyGame("game: Sleepy Table", "round: 3", "last active: recently");
    }

    @Test
    void a_game_that_cannot_be_updated_does_not_stop_the_updates_of_the_others() {
        ravenscore.createGame("name: Awkward Table", "played on: Swords and Ravens");
        ravenscore.createGame("name: Busy Table", "played on: Swords and Ravens");
        // Arya played two houses, once under her alias, so Ravenscore rejects the final rankings
        swordsAndRavens.gameIsFinished("game: Awkward Table", "round: 9", """
                house     | player  | victory points
                Stark     | NoOne   | 7
                Lannister | Arya    | 6
                Greyjoy   | Euron   | 5
                Tyrell    | Brienne | 4
                Baratheon | Davos   | 3
                Martell   | Jaime   | 2
                """);
        swordsAndRavens.gameIsOngoing("game: Busy Table", "round: 5");

        ravenscore.checkSwordsAndRavens();
        ravenscore.confirmSwordsAndRavensUpdates();

        ravenscore.verifyGame("game: Awkward Table", "status: in progress", "round: 9");
        ravenscore.verifyGame("game: Busy Table", "round: 5", "last active: recently");
    }

    @Test
    void flags_players_holding_up_swords_and_ravens_games_for_48_hours_or_more() {
        ravenscore.createGame("name: Sleepy Table", "played on: Swords and Ravens");
        ravenscore.createGame("name: Busy Table", "played on: Swords and Ravens");
        swordsAndRavens.gameIsOngoing("game: Sleepy Table", "round: 3", "last active: 50 hours ago",
                "waiting for: Stark (NoOne), Greyjoy (Euron)");
        swordsAndRavens.gameIsOngoing("game: Busy Table", "round: 5", "last active: 47 hours ago",
                "waiting for: Lannister (Cersei)");

        ravenscore.automaticUpdatesRun();
        // the same inactivity seen again is updated, not added twice
        ravenscore.automaticUpdatesRun();

        ravenscore.verifyGame("game: Sleepy Table", "last active: 50 hours ago", "inactivity warning: yes");
        ravenscore.verifyGame("game: Busy Table", "inactivity warning: no");
        ravenscore.verifyInactivity("game: Sleepy Table", """
                house     | inactive hours | warning
                Stark     | 50             | yes
                Greyjoy   | 50             | yes
                Lannister | none           | no
                """);
        ravenscore.verifyInactivity("game: Busy Table", """
                house     | inactive hours
                Lannister | none
                """);
    }

    @Test
    void penalty_points_cover_the_times_a_player_held_up_a_swords_and_ravens_game() {
        ravenscore.createGame("name: Sleepy Table", "played on: Swords and Ravens");
        // faceless games list only the houses
        swordsAndRavens.gameIsOngoing("game: Sleepy Table", "round: 3", "last active: 3 days ago", "waiting for: Stark");
        ravenscore.automaticUpdatesRun();

        ravenscore.updateGameRankings("game: Sleepy Table", "completed: no", """
                house | penalty | points
                Stark | 1       | 0
                """);

        ravenscore.verifyInactivity("game: Sleepy Table", """
                house | inactive hours | warning
                Stark | 72             | no
                """);
        ravenscore.verifyGame("game: Sleepy Table", "inactivity warning: no");

        // the game moved on, then stalled on the same player again
        swordsAndRavens.gameIsOngoing("game: Sleepy Table", "round: 4", "last active: 50 hours ago", "waiting for: Stark");
        ravenscore.automaticUpdatesRun();

        ravenscore.verifyInactivity("game: Sleepy Table", """
                house | inactive hours | warning
                Stark | 72, 50         | yes
                """);
        ravenscore.verifyGame("game: Sleepy Table", "inactivity warning: yes");
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
