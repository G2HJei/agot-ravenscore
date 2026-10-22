package xyz.zlatanov.ravenscore.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ManualGameRankingAcceptanceTest extends AcceptanceTestBase {

    @BeforeEach
    void aStageWithSixPlayers() {
        ravenscore.newTournament("name: Winter Cup");
        ravenscore.unlockAdmin();
        ravenscore.addStage("name: Group Stage", "qualifiers: 2");
        ravenscore.addPlayers("Arya", "Brienne", "Cersei", "Davos", "Euron", "Jaime");
    }

    @Test
    void a_finished_game_awards_points_for_position_and_castles_plus_a_bonus_for_the_winner() {
        ravenscore.createGame("name: Table 1", "type: 2nd edition", "players: Arya, Brienne, Cersei, Davos, Euron, Jaime");

        ravenscore.updateGameRankings("game: Table 1", "completed: yes", """
                house     | player  | castles
                Stark     | Arya    | 7
                Lannister | Cersei  | 6
                Greyjoy   | Euron   | 5
                Tyrell    | Brienne | 4
                Baratheon | Davos   | 3
                Martell   | Jaime   | 2
                """);

        ravenscore.verifyGame("game: Table 1", "status: completed", "winner: Arya");
        ravenscore.verifyGameRankings("game: Table 1", """
                house     | player  | points | castles
                Stark     | Arya    | 15     | 7
                Lannister | Cersei  | 10     | 6
                Greyjoy   | Euron   | 8      | 5
                Tyrell    | Brienne | 6      | 4
                Baratheon | Davos   | 4      | 3
                Martell   | Jaime   | 2      | 2
                """);
        ravenscore.verifyStandings("""
                rank | player  | score | games | wins | avg points
                1    | Arya    | 15    | 1     | 1    | 15.00
                2    | Cersei  | 10    | 1     | 0    | 10.00
                3    | Euron   | 8     | 1     | 0    | 8.00
                4    | Brienne | 6     | 1     | 0    | 6.00
                5    | Davos   | 4     | 1     | 0    | 4.00
                6    | Jaime   | 2     | 1     | 0    | 2.00
                """);
    }

    @Test
    void a_game_cannot_be_completed_until_every_house_has_a_player() {
        ravenscore.createGame("name: Table 1");

        ravenscore.updateGameRankings("game: Table 1", "completed: yes", """
                house | player | castles
                Stark | Arya   | 7
                """);

        ravenscore.verifyError("message: Cannot complete game without revealing all players!");
        ravenscore.verifyGame("game: Table 1", "status: not started");
    }

    @Test
    void the_current_round_shows_while_a_game_is_in_progress() {
        ravenscore.createGame("name: Table 1");

        ravenscore.changeRound("game: Table 1", "round: 4");

        ravenscore.verifyGame("game: Table 1", "status: in progress", "round: 4");
    }
}
