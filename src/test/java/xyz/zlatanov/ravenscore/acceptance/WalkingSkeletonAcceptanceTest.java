package xyz.zlatanov.ravenscore.acceptance;

import org.junit.jupiter.api.Test;

class WalkingSkeletonAcceptanceTest extends AcceptanceTestBase {

    @Test
    void an_organiser_hosts_a_tournament_and_unlocks_its_administration() {
        ravenscore.newTournament("name: Winter Cup", "key: winteriscoming");

        ravenscore.verifyTournamentListedOnHomePage("name: Winter Cup");
        ravenscore.openTournamentFromHomePage("name: Winter Cup");
        ravenscore.verifyAdminLocked();

        ravenscore.unlockAdmin("key: winteriscoming");
        ravenscore.verifyAdminUnlocked();
    }
}
