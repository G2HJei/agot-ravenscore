package xyz.zlatanov.ravenscore.acceptance.dsl;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zlatanov.ravenscore.acceptance.drivers.snr.SnrGameJson;
import xyz.zlatanov.ravenscore.acceptance.drivers.snr.SwordsAndRavensStubDriver;

import java.time.Instant;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.*;

/**
 * Swords & Ravens, the site where online games are played. A test sets up what is happening in an online game,
 * referring to it by its name in Ravenscore (see {@link Ravenscore#createGame} with "played on: Swords and Ravens").
 */
public class SwordsAndRavens {

    private static final Logger  log      = LoggerFactory.getLogger(SwordsAndRavens.class);
    private static final Pattern TIME_AGO = Pattern.compile("(\\d+) (minute|hour|day)s? ago");

    private final SwordsAndRavensStubDriver driver;
    private final ScenarioContext           context;

    public SwordsAndRavens(SwordsAndRavensStubDriver driver, ScenarioContext context) {
        this.driver = driver;
        this.context = context;
    }

    /**
     * Args: game, round (default 1), last active (e.g. "50 hours ago", default "recently")
     */
    public void gameIsOngoing(String... args) {
        val params = step("gameIsOngoing", args, "game", "round", "last active");
        val game = params.required("game");
        driver.stubGame(context.snrGameId(game), new SnrGameJson(context.snrGameId(game), game)
                .state("ONGOING")
                .round(params.integer("round", 1))
                .lastActiveAt(timeAgo(params.get("last active", "recently"))));
    }

    /**
     * Args: game, round (default 10), last active (default "recently") + table, in finishing order:
     * house | player | victory points [| land areas] [| supply] [| throne]
     */
    public void gameIsFinished(String... args) {
        val params = step("gameIsFinished", args, "game", "round", "last active");
        val game = params.required("game");
        val json = new SnrGameJson(context.snrGameId(game), game)
                .state("FINISHED")
                .round(params.integer("round", 10))
                .lastActiveAt(timeAgo(params.get("last active", "recently")));
        for (val row : params.table().rows()) {
            json.player(row.get("house"), row.get("player"),
                    Integer.parseInt(row.get("victory points")),
                    Integer.parseInt(row.optional("land areas").orElse("0")),
                    Integer.parseInt(row.optional("supply").orElse("0")),
                    Integer.parseInt(row.optional("throne").orElse("0")));
        }
        driver.stubGame(context.snrGameId(game), json);
    }

    /**
     * The site fails to answer for this game. Args: game
     */
    public void gameIsUnavailable(String... args) {
        val params = step("gameIsUnavailable", args, "game");
        driver.stubUnavailable(context.snrGameId(params.required("game")));
    }

    private static Params step(String step, String[] args, String... keys) {
        log.info("swordsAndRavens.{}({})", step, String.join(", ", args));
        return Params.parse(step, args, keys);
    }

    private static Instant timeAgo(String timeAgo) {
        val now = Instant.now();
        val normalized = Params.normalize(timeAgo);
        if (normalized.equals("recently") || normalized.equals("just now")) {
            return now.minus(5, MINUTES);
        }
        val matcher = TIME_AGO.matcher(normalized);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Expected \"recently\" or e.g. \"5 minutes ago\", \"50 hours ago\", \"3 days ago\" but got \"" + timeAgo + "\"");
        }
        val amount = Long.parseLong(matcher.group(1));
        // an extra half hour, so the whole hours shown by Ravenscore do not change while the test runs
        return switch (matcher.group(2)) {
            case "minute" -> now.minus(amount, MINUTES);
            case "hour" -> now.minus(amount, HOURS).minus(30, MINUTES);
            default -> now.minus(amount, DAYS).minus(30, MINUTES);
        };
    }
}
