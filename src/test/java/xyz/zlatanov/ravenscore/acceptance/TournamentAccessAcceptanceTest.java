package xyz.zlatanov.ravenscore.acceptance;

import org.junit.jupiter.api.Test;

class TournamentAccessAcceptanceTest extends AcceptanceTestBase {

    @Test
    void the_wrong_key_does_not_unlock_the_administration() {
        ravenscore.newTournament("name: Spring Cup", "key: winteriscoming");

        ravenscore.unlockAdmin("key: summeriscoming");

        ravenscore.verifyError("message: Incorrect key!");
        ravenscore.verifyAdminLocked();
    }

    @Test
    void a_private_tournament_is_only_reachable_by_its_link() {
        ravenscore.newTournament("name: Secret Cup", "private: yes");

        ravenscore.verifyTournamentNotListedOnHomePage("name: Secret Cup");
        ravenscore.openTournament("name: Secret Cup");
        ravenscore.verifyTournamentShown("name: Secret Cup");
    }

    @Test
    void locking_the_administration_hides_the_admin_controls() {
        ravenscore.newTournament("name: Summer Cup");
        ravenscore.unlockAdmin();

        ravenscore.lockAdmin();

        ravenscore.verifyAdminLocked();
    }
}
