package xyz.zlatanov.ravenscore.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StageAndPlayersAcceptanceTest extends AcceptanceTestBase {

    @BeforeEach
    void anUnlockedTournament() {
        ravenscore.newTournament("name: Autumn Cup");
        ravenscore.unlockAdmin();
    }

    @Test
    void players_join_a_stage_without_any_points() {
        ravenscore.addStage("name: Group Stage", "qualifiers: 2");

        ravenscore.addPlayer("name: Arya", "aliases: NoOne, FacelessGirl");
        ravenscore.addPlayer("name: Brienne", "points modifier: 0.5");
        ravenscore.addPlayers("Cersei", "Davos", "Euron", "Jaime");

        ravenscore.verifyStandings("""
                rank | player  | score | games | wins
                1    | Arya    | 0     | 0     | 0
                2    | Brienne | 0     | 0     | 0
                3    | Cersei  | 0     | 0     | 0
                4    | Davos   | 0     | 0     | 0
                5    | Euron   | 0     | 0     | 0
                6    | Jaime   | 0     | 0     | 0
                """);
        ravenscore.verifyPlayer("player: Arya", "aliases: NoOne, FacelessGirl");
        ravenscore.verifyPlayer("player: Brienne", "points modifier: 0.5");
    }

    @Test
    void an_organiser_changes_a_players_aliases() {
        ravenscore.addStage();
        ravenscore.addPlayer("name: Arya", "aliases: NoOne");

        ravenscore.editPlayer("player: Arya", "aliases: NoOne, Mercy");

        ravenscore.verifyPlayer("player: Arya", "aliases: NoOne, Mercy");
    }

    @Test
    void two_players_of_a_tournament_cannot_have_the_same_name() {
        ravenscore.addStage();
        ravenscore.addPlayer("name: Arya");

        ravenscore.addPlayer("name: Arya");

        ravenscore.verifyError("message: Player with the name \"Arya\" is already in this tournament");
    }
}
