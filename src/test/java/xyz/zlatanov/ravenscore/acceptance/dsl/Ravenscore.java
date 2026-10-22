package xyz.zlatanov.ravenscore.acceptance.dsl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.RavenscoreWebDriver;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.Modal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.RefreshSnrModal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.StageRankingModal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.model.GameSummary;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.model.SnrReport;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.model.StandingRow;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.pages.StagePane;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.pages.TournamentPage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ravenscore as a tournament organiser sees it: tournaments, stages, players and games.
 * <p>
 * Steps take {@code "key: value"} parameters (and sometimes a table), with sensible defaults for anything left out.
 * When Ravenscore rejects a step and shows an error, the next step fails, unless it is {@link #verifyError}.
 */
@Slf4j
public class Ravenscore {

    private static final String DEFAULT_KEY = "winteriscoming";
    private static final String SNR_GAME    = "https://swordsandravens.net/play/";

    private final RavenscoreWebDriver driver;
    private final ScenarioContext     context;
    private       String              unverifiedError;
    private       RefreshSnrModal     snrModal;
    private       SnrReport           snrReport;

    public Ravenscore(RavenscoreWebDriver driver, ScenarioContext context) {
        this.driver = driver;
        this.context = context;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Tournaments
    public void newTournament(String... args) {
        val params = step("newTournament", args, "name", "key", "rules", "private");
        val alias = params.get("name", "Tournament");
        val name = context.uniqueName(alias);
        val key = params.get("key", DEFAULT_KEY);
        val modal = driver.openHomePage().newTournament()
                .name(name)
                .rules(params.get("rules", ""))
                .privateTournament(params.yesNo("private", false))
                .key(key);
        if (submitted(modal, modal.done())) {
            context.addTournament(new ScenarioContext.Tournament(alias, name, key, driver.currentTournamentPage().url()));
        }
    }

    /**
     * Opens a tournament by its direct link. Args: name
     */
    public void openTournament(String... args) {
        val params = step("openTournament", args, "name");
        val tournament = context.tournament(params.required("name"));
        context.selectTournament(tournament);
        driver.openTournament(tournament.url());
    }

    /**
     * Opens a tournament by clicking it on the home page. Args: name
     */
    public void openTournamentFromHomePage(String... args) {
        val params = step("openTournamentFromHomePage", args, "name");
        val tournament = context.tournament(params.required("name"));
        context.selectTournament(tournament);
        driver.openHomePage().open(tournament.name());
    }

    /**
     * Unlocks the tournament administration. Args: key (default: the key the tournament was created with)
     */
    public void unlockAdmin(String... args) {
        val params = step("unlockAdmin", args, "key");
        val modal = tournamentPage().unlockAdmin();
        val error = modal.unlock(params.get("key", context.currentTournament().key()));
        if (error.isPresent()) {
            unverifiedError = error.get();
            modal.close();
        }
    }

    public void lockAdmin() {
        step("lockAdmin", new String[0]);
        tournamentPage().lockAdmin();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Stages and players

    /**
     * Creates a stage, which becomes the selected stage. Args: name (default "Group Stage"), qualifiers (default 1)
     */
    public void addStage(String... args) {
        val params = step("addStage", args, "name", "qualifiers");
        val name = params.get("name", "Group Stage");
        val modal = tournamentPage().newStage().name(name).qualifiers(params.integer("qualifiers", 1));
        if (submitted(modal, modal.done())) {
            context.selectStage(name);
        }
    }

    /**
     * Adds a player to the selected stage. Args: name, aliases (comma-separated), points modifier (default 1)
     */
    public void addPlayer(String... args) {
        val params = step("addPlayer", args, "name", "aliases", "points modifier");
        val modal = stagePane().addPlayer()
                .name(params.required("name"))
                .pointsModifier(params.get("points modifier", "1"))
                .aliases(params.list("aliases").orElse(List.of()));
        submitted(modal, modal.done());
    }

    /**
     * Adds players to the selected stage, e.g. addPlayers("Arya", "Brienne", "Cersei")
     */
    public void addPlayers(String... names) {
        for (val name : names) {
            addPlayer("name: " + name);
        }
    }

    /**
     * Changes a player of the selected stage. Args: player, name, aliases (comma-separated, replacing all), points modifier
     */
    public void editPlayer(String... args) {
        val params = step("editPlayer", args, "player", "name", "aliases", "points modifier");
        val modal = stagePane().editPlayer(params.required("player"));
        params.optional("name").ifPresent(modal::name);
        params.optional("points modifier").ifPresent(modal::pointsModifier);
        params.list("aliases").ifPresent(modal::aliases);
        submitted(modal, modal.done());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Games

    /**
     * Creates a game in the selected stage. Args: name (default "Game 1", "Game 2"...), type (default "2nd edition"), players ("all" or comma-separated, default
     * all), played on ("Swords and Ravens" to link it to a new online game), link (any other external link)
     */
    public void createGame(String... args) {
        val params = step("createGame", args, "name", "type", "players", "played on", "link");
        val name = params.optional("name").orElseGet(context::nextGameName);
        val modal = stagePane().newGame()
                .type(GameTypes.optionLabel(params.get("type", "2nd edition")))
                .name(name);
        params.optional("played on").ifPresent(site -> {
            if (!Params.normalize(site).equals("swords and ravens")) {
                throw new IllegalArgumentException("createGame: unknown \"played on\" site \"" + site + "\"");
            }
            modal.link(SNR_GAME + context.snrGameId(name));
        });
        params.optional("link").ifPresent(modal::link);
        val players = params.list("players").filter(list -> !list.equals(List.of("all")));
        if (players.isPresent()) {
            players.get().forEach(modal::selectPlayer);
        } else {
            modal.selectAllPlayers();
        }
        submitted(modal, modal.done());
    }

    /**
     * Args: game, round
     */
    public void changeRound(String... args) {
        val params = step("changeRound", args, "game", "round");
        stagePane().game(params.required("game")).selectRound(params.integer("round", 1));
    }

    /**
     * Enters a game's results: who played each house and the castles they held. Without a points column, the points
     * are calculated by Ravenscore ("Calc points").
     * <p>
     * Args: game, completed (yes/no, default yes) + table: house | player | castles [| points] [| penalty]
     */
    public void updateGameRankings(String... args) {
        val params = step("updateGameRankings", args, "game", "completed");
        val table = params.table();
        val modal = stagePane().game(params.required("game")).updateRankings();
        for (val row : table.rows()) {
            val house = GameTypes.house(row.get("house"));
            row.optional("player").ifPresent(player -> modal.player(house, player));
            row.optional("castles").ifPresent(castles -> modal.castles(house, Integer.parseInt(castles)));
            row.optional("points").ifPresent(points -> modal.points(house, Integer.parseInt(points)));
            row.optional("penalty").ifPresent(penalty -> modal.penaltyPoints(house, Integer.parseInt(penalty)));
        }
        if (!table.has("points")) {
            modal.calculatePoints();
        }
        modal.completed(params.yesNo("completed", true));
        submitted(modal, modal.done());
    }

    /**
     * Checks the selected stage's unfinished games on Swords & Ravens; nothing is saved until confirmed.
     */
    public void checkSwordsAndRavens() {
        step("checkSwordsAndRavens", new String[0]);
        snrModal = stagePane().checkSwordsAndRavensGames();
        snrReport = snrModal.report();
    }

    /**
     * Saves the updates found by {@link #checkSwordsAndRavens()}.
     */
    public void confirmSwordsAndRavensUpdates() {
        step("confirmSwordsAndRavensUpdates", new String[0]);
        if (snrModal == null) {
            throw new IllegalStateException("confirmSwordsAndRavensUpdates: call checkSwordsAndRavens() first");
        }
        val modal = snrModal;
        snrModal = null;
        modal.confirm();
        recordPageError();
    }

    /**
     * Checks the selected stage's games on Swords & Ravens and saves the updates.
     */
    public void updateFromSwordsAndRavens() {
        checkSwordsAndRavens();
        val failedGames = snrReport.games().values().stream().filter(SnrReport.Game::failed).map(SnrReport.Game::name).toList();
        assertThat(failedGames).as("games that could not be checked on Swords & Ravens").isEmpty();
        assertThat(snrReport.noUpdates()).as("Swords & Ravens reported no updates").isFalse();
        confirmSwordsAndRavensUpdates();
    }

    /**
     * Finalizes the selected stage's rankings. Args: order (optional, comma-separated: rearranges the top players)
     */
    public void finalizeStage(String... args) {
        val params = step("finalizeStage", args, "order");
        val modal = stagePane().finalizeRankings();
        params.list("order").ifPresent(order -> rearrange(modal, order));
        modal.done();
        recordPageError();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Verifications

    /**
     * Verifies that the last step was rejected. Args: message (optional: part of the error shown)
     */
    public void verifyError(String... args) {
        log.info("verifyError({})", String.join(", ", args));
        val params = Params.parse("verifyError", args, "message");
        val error = unverifiedError;
        unverifiedError = null;
        assertThat(error).as("error shown by Ravenscore after the previous step").isNotNull();
        params.optional("message").ifPresent(message -> assertThat(error).contains(message));
    }

    /**
     * Fails if Ravenscore showed an error which the test did not verify.
     */
    public void verifyNoUnexpectedErrors() {
        failOnUnverifiedError();
    }

    /**
     * Args: name (as created by the test)
     */
    public void verifyTournamentShown(String... args) {
        val params = step("verifyTournamentShown", args, "name");
        val tournament = context.tournament(params.required("name"));
        assertThat(driver.currentTournamentPage().name()).as("tournament page title").isEqualTo(tournament.name());
    }

    /**
     * Args: name, winner (optional)
     */
    public void verifyTournamentListedOnHomePage(String... args) {
        val params = step("verifyTournamentListedOnHomePage", args, "name", "winner");
        val tournament = context.tournament(params.required("name"));
        val homePage = driver.openHomePage();
        assertThat(homePage.lists(tournament.name())).as("\"%s\" listed on the home page", tournament.name()).isTrue();
        params.optional("winner").ifPresent(winner -> assertThat(homePage.winnerOf(tournament.name()))
                .as("winner of \"%s\" on the home page", tournament.name())
                .contains(winner));
    }

    /**
     * Args: name
     */
    public void verifyTournamentNotListedOnHomePage(String... args) {
        val params = step("verifyTournamentNotListedOnHomePage", args, "name");
        val tournament = context.tournament(params.required("name"));
        assertThat(driver.openHomePage().lists(tournament.name()))
                .as("\"%s\" listed on the home page", tournament.name())
                .isFalse();
    }

    public void verifyAdminUnlocked() {
        step("verifyAdminUnlocked", new String[0]);
        assertThat(tournamentPage().isAdminUnlocked()).as("tournament admin mode").isTrue();
    }

    public void verifyAdminLocked() {
        step("verifyAdminLocked", new String[0]);
        val page = tournamentPage();
        val softly = new SoftAssertions();
        softly.assertThat(page.isAdminUnlocked()).as("tournament admin mode").isFalse();
        softly.assertThat(page.offersAdminUnlock()).as("unlock administration button").isTrue();
        softly.assertThat(page.offersStageCreation()).as("create new stage button").isFalse();
        softly.assertAll();
    }

    /**
     * Verifies the top rows of the selected stage's rankings, in order.
     * <p>
     * Table with any of the columns: rank | player | score | games | wins | avg points
     */
    public void verifyStandings(String table) {
        val params = step("verifyStandings", new String[]{table});
        val expected = params.table();
        val tables = TableComparison.of(expected, stagePane().standings().stream()
                .limit(expected.rows().size())
                .map(row -> expected.columns().stream().map(column -> standingsValue(row, column)).toList())
                .toList());
        assertThat(tables.actual()).as("stage rankings").isEqualTo(tables.expected());
    }

    /**
     * Args: player, aliases (comma-separated), points modifier
     */
    public void verifyPlayer(String... args) {
        val params = step("verifyPlayer", args, "player", "aliases", "points modifier");
        val name = params.required("player");
        val player = stagePane().standings().stream()
                .filter(row -> row.player().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Player \"" + name + "\" is not in the stage rankings"));
        val softly = new SoftAssertions();
        params.list("aliases").ifPresent(aliases -> softly.assertThat(player.aliases())
                .as("aliases of %s", name)
                .containsExactlyInAnyOrderElementsOf(aliases));
        params.optional("points modifier").ifPresent(modifier -> softly.assertThat(new BigDecimal(player.pointsModifier()))
                .as("points modifier of %s", name)
                .isEqualByComparingTo(modifier));
        softly.assertAll();
    }

    /**
     * Args: game, status (completed, in progress or not started), round (unfinished games only), winner, last active (e.g.
     * "3 hours ago" or "recently")
     */
    public void verifyGame(String... args) {
        val params = step("verifyGame", args, "game", "status", "round", "winner", "last active");
        val name = params.required("game");
        val game = summary(name);
        val softly = new SoftAssertions();
        params.optional("status").ifPresent(status -> softly.assertThat(status(game)).as("status of %s", name).isEqualTo(status));
        params.optional("round").ifPresent(round -> softly.assertThat(game.roundLabel()).as("round of %s", name).isEqualTo("Round " + round));
        params.optional("winner").ifPresent(winner -> softly.assertThat(game.winner()).as("winner of %s", name).isEqualTo(winner));
        params.optional("last active").ifPresent(lastActive -> softly.assertThat(game.lastActiveLabel())
                .as("last activity of %s", name)
                .isEqualTo("Last active " + lastActive));
        softly.assertAll();
    }

    /**
     * Args: game + table with any of the columns: house | player | points | castles
     */
    public void verifyGameRankings(String... args) {
        val params = step("verifyGameRankings", args, "game");
        val expected = params.table();
        val tables = TableComparison.of(expected, summary(params.required("game")).rankings().stream()
                .map(row -> expected.columns().stream().map(column -> gameRankingValue(row, column)).toList())
                .toList());
        assertThat(tables.actual()).as("rankings of %s", params.required("game")).isEqualTo(tables.expected());
    }

    /**
     * Args: stage (default: the selected stage)
     */
    public void verifyStageCompleted(String... args) {
        val params = step("verifyStageCompleted", args, "stage");
        val page = tournamentPage();
        val stageName = params.optional("stage").orElseGet(context::currentStage);
        val stage = page.stage(stageName);
        val softly = new SoftAssertions();
        softly.assertThat(stage.isCompleted()).as("%s completed", stageName).isTrue();
        softly.assertThat(page.showsCompletedIcon(stageName)).as("completed icon on the %s tab", stageName).isTrue();
        softly.assertThat(stage.offersAddingPlayers()).as("add player button in %s", stageName).isFalse();
        softly.assertThat(stage.offersNewGames()).as("create new game button in %s", stageName).isFalse();
        softly.assertAll();
    }

    /**
     * Args: player
     */
    public void verifyTournamentWinner(String... args) {
        val params = step("verifyTournamentWinner", args, "player");
        val crowned = stagePane().standings().stream().filter(StandingRow::tournamentWinner).map(StandingRow::player).toList();
        assertThat(crowned).as("players crowned as tournament winner").containsExactly(params.required("player"));
    }

    /**
     * Args: game, round, completed (yes/no) + optional table: rank | player | house | points
     */
    public void verifySwordsAndRavensUpdate(String... args) {
        val params = step("verifySwordsAndRavensUpdate", args, "game", "round", "completed");
        val game = reportedGame(params.required("game"));
        val softly = new SoftAssertions();
        params.optional("round").ifPresent(round -> softly.assertThat(game.round()).as("round of %s", game.name())
                .isEqualTo(Integer.parseInt(round)));
        softly.assertThat(game.completed()).as("%s completed", game.name()).isEqualTo(params.yesNo("completed", game.completed()));
        params.optionalTable().ifPresent(table -> {
            val tables = TableComparison.of(table, game.rankings().stream()
                    .map(ranking -> table.columns().stream().map(column -> snrRankingValue(ranking, column)).toList())
                    .toList());
            softly.assertThat(tables.actual()).as("final rankings of %s", game.name()).isEqualTo(tables.expected());
        });
        softly.assertAll();
    }

    /**
     * Args: game, hours
     */
    public void verifyInactivityWarning(String... args) {
        val params = step("verifyInactivityWarning", args, "game", "hours");
        val game = reportedGame(params.required("game"));
        val hours = params.required("hours");
        assertThat(game.inactiveHours()).as("hours since %s was last active", game.name()).isEqualTo(Long.parseLong(hours));
        assertThat(snrReport.warnings()).as("warnings shown").contains("Last active " + hours + " hours ago!");
    }

    /**
     * Args: game
     */
    public void verifyNoInactivityWarning(String... args) {
        val params = step("verifyNoInactivityWarning", args, "game");
        val name = params.required("game");
        assertThat(checkedSnrReport().game(name).map(SnrReport.Game::inactiveHours))
                .as("inactivity warning for %s", name)
                .isEmpty();
    }

    /**
     * Args: game
     */
    public void verifySwordsAndRavensFailure(String... args) {
        val params = step("verifySwordsAndRavensFailure", args, "game");
        val game = reportedGame(params.required("game"));
        assertThat(game.failed()).as("%s could not be checked on Swords & Ravens", game.name()).isTrue();
    }

    // ---------------------------------------------------------------------------------------------------------------

    private Params step(String step, String[] args, String... keys) {
        failOnUnverifiedError();
        log.info("{}({})", step, String.join(", ", args));
        return Params.parse(step, args, keys);
    }

    private void failOnUnverifiedError() {
        if (unverifiedError != null) {
            val error = unverifiedError;
            unverifiedError = null;
            throw new AssertionError("Ravenscore showed an unexpected error after the previous step: \"" + error
                    + "\". If the step was meant to fail, verify it with ravenscore.verifyError(...)");
        }
    }

    // a rejected form leaves its modal open: remember the problem and close the modal
    private boolean submitted(Modal modal, Optional<String> formError) {
        if (formError.isPresent()) {
            unverifiedError = formError.get();
            modal.close();
            return false;
        }
        return recordPageError();
    }

    private boolean recordPageError() {
        val pageError = driver.currentTournamentPage().error();
        pageError.ifPresent(error -> unverifiedError = error);
        return pageError.isEmpty();
    }

    private TournamentPage tournamentPage() {
        if (snrModal != null) {
            snrModal.close();
            snrModal = null;
        }
        val tournament = context.currentTournament();
        return driver.isShowing(tournament.url())
                ? driver.currentTournamentPage()
                : driver.openTournament(tournament.url());
    }

    private StagePane stagePane() {
        val page = tournamentPage();
        val stage = context.currentStage();
        return stage == null ? page.selectedStage() : page.stage(stage);
    }

    private GameSummary summary(String name) {
        val game = stagePane().game(name);
        assertThat(game.exists()).as("game \"%s\" in the selected stage", name).isTrue();
        return game.summary();
    }

    private SnrReport checkedSnrReport() {
        if (snrReport == null) {
            throw new IllegalStateException("No Swords & Ravens check yet, call checkSwordsAndRavens() first");
        }
        return snrReport;
    }

    private SnrReport.Game reportedGame(String name) {
        return checkedSnrReport().game(name).orElseThrow(() -> new AssertionError(
                "Swords & Ravens check reported nothing for \"" + name + "\"; reported " + snrReport.games().keySet()));
    }

    private void rearrange(StageRankingModal modal, List<String> order) {
        for (int i = 0; i < order.size(); i++) {
            val current = modal.players();
            if (!current.get(i).equals(order.get(i))) {
                modal.movePlayerAbove(order.get(i), current.get(i));
            }
        }
        assertThat(modal.players()).as("final rankings after rearranging").startsWith(order.toArray(String[]::new));
    }

    private static String status(GameSummary game) {
        if (game.completed()) {
            return "completed";
        }
        return game.roundLabel() == null ? "not started" : "in progress";
    }

    private static String standingsValue(StandingRow row, String column) {
        return switch (column) {
            case "rank" -> row.rank().isEmpty() && row.tournamentWinner() ? "1" : row.rank();
            case "player" -> row.player();
            case "score" -> row.score();
            case "games" -> row.games();
            case "wins" -> row.wins();
            case "avg points" -> row.avgPoints();
            default -> throw new IllegalArgumentException("Unknown stage rankings column \"" + column + "\"");
        };
    }

    private static String gameRankingValue(GameSummary.Ranking row, String column) {
        return switch (column) {
            case "house" -> row.house();
            case "player" -> row.player();
            case "points" -> row.points();
            case "castles" -> row.castles();
            default -> throw new IllegalArgumentException("Unknown game rankings column \"" + column + "\"");
        };
    }

    private static String snrRankingValue(SnrReport.Ranking ranking, String column) {
        return switch (column) {
            case "rank" -> String.valueOf(ranking.rank());
            case "player" -> ranking.player();
            case "house" -> ranking.house();
            case "points" -> String.valueOf(ranking.points());
            default -> throw new IllegalArgumentException("Unknown Swords & Ravens rankings column \"" + column + "\"");
        };
    }
}
