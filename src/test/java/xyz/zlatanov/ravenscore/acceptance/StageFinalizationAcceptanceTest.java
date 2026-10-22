package xyz.zlatanov.ravenscore.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StageFinalizationAcceptanceTest extends AcceptanceTestBase {

    @BeforeEach
    void aFinalWhoseOnlyGameIsOver() {
        ravenscore.newTournament("name: Kings Cup");
        ravenscore.unlockAdmin();
        ravenscore.addStage("name: Final", "qualifiers: 1");
        ravenscore.addPlayers("Arya", "Brienne", "Cersei", "Davos", "Euron", "Jaime");
        ravenscore.createGame("name: Final Table");
        ravenscore.updateGameRankings("game: Final Table", """
                house     | player  | castles
                Stark     | Arya    | 7
                Lannister | Cersei  | 6
                Greyjoy   | Euron   | 5
                Tyrell    | Brienne | 4
                Baratheon | Davos   | 3
                Martell   | Jaime   | 2
                """);
    }

    @Test
    void finalizing_the_last_stage_crowns_the_tournament_winner() {
        ravenscore.finalizeStage();

        ravenscore.verifyStageCompleted();
        ravenscore.verifyTournamentWinner("player: Arya");
        ravenscore.verifyTournamentListedOnHomePage("name: Kings Cup", "winner: Arya");
    }

    @Test
    void the_organiser_can_rearrange_the_final_rankings() {
        ravenscore.finalizeStage("order: Cersei, Arya");

        ravenscore.verifyStandings("""
                rank | player
                1    | Cersei
                2    | Arya
                3    | Euron
                """);
        ravenscore.verifyTournamentWinner("player: Cersei");
    }
}
