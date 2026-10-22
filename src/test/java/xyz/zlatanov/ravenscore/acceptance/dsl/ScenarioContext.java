package xyz.zlatanov.ravenscore.acceptance.dsl;

import lombok.val;

import java.security.SecureRandom;
import java.util.*;

/**
 * What the DSL remembers during a single test, so the test can refer to things by the names it gave them.
 * <p>
 * All tests share one running Ravenscore and one database, without any cleanup in between. Tournament names are
 * visible to every test (on the home page), so each test's tournament names get a unique suffix, e.g. "Winter Cup"
 * becomes "Winter Cup ~k3f9a1". Stages, players, and games belong to a tournament, so their names are used as written.
 * Swords & Ravens games get a fresh random id per test.
 */
public class ScenarioContext {

    private static final String       ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM   = new SecureRandom();

    private final String                  suffix       = " ~" + randomId();
    private final Map<String, Tournament> tournaments  = new LinkedHashMap<>();
    private final Map<String, UUID>       snrGameIds   = new HashMap<>();
    private       Tournament              tournament;
    private       String                  stage;
    private       int                     gamesCreated = 0;

    /**
     * A tournament created by the test: the name the test uses, the actual name in Ravenscore, its key, and page.
     */
    public record Tournament(String alias, String name, String key, String url) {
    }

    String uniqueName(String alias) {
        val name = alias + suffix;
        return name.length() <= 100 ? name : name.substring(0, 100);
    }

    void addTournament(Tournament created) {
        tournaments.put(created.alias(), created);
        tournament = created;
        stage = null;
    }

    Tournament tournament(String alias) {
        return Optional.ofNullable(tournaments.get(alias))
                .orElseThrow(() -> new IllegalArgumentException("No tournament \"" + alias + "\" was created in this test; created: " + tournaments.keySet()));
    }

    Tournament currentTournament() {
        return Optional.ofNullable(tournament)
                .orElseThrow(() -> new IllegalStateException("No tournament yet, start with ravenscore.newTournament(...)"));
    }

    void selectTournament(Tournament selected) {
        if (tournament != selected) {
            stage = null;
        }
        tournament = selected;
    }

    String currentStage() {
        return stage;
    }

    void selectStage(String name) {
        stage = name;
    }

    UUID snrGameId(String gameName) {
        return snrGameIds.computeIfAbsent(gameName, k -> UUID.randomUUID());
    }

    String nextGameName() {
        return "Game " + (++gamesCreated);
    }

    private static String randomId() {
        val id = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            id.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return id.toString();
    }
}
