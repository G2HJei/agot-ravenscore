package xyz.zlatanov.ravenscore.acceptance.drivers.web.model;

import lombok.val;

import java.util.*;
import java.util.regex.Pattern;

/**
 * What the "Refresh Swords & Ravens Games Status" dialog reported, parsed from its log, e.g.
 *
 * <pre>
 * Online Table
 *     Last active 50 hours ago!
 *     Round 9 (completed)
 *     Final rankings
 *         1. Arya (Stark)                       15 pts.
 * </pre>
 */
public record SnrReport(Map<String, Game> games, boolean noUpdates, List<String> warnings) {

    private static final Pattern INACTIVE = Pattern.compile("Last active (\\d+) hours ago!");
    private static final Pattern ROUND    = Pattern.compile("Round (\\d+)( \\(completed\\))?");
    private static final Pattern RANKING  = Pattern.compile("(\\d+)\\. (.+) \\((\\w+)\\)\\s*(\\d+) pts\\..*");

    public record Game(String name, Integer round, boolean completed, Long inactiveHours, boolean failed,
                       List<Ranking> rankings) {
    }

    public record Ranking(int rank, String player, String house, int points) {
    }

    public Optional<Game> game(String name) {
        return Optional.ofNullable(games.get(name));
    }

    public static SnrReport parse(String log, List<String> warnings) {
        val games = new LinkedHashMap<String, GameBuilder>();
        var noUpdates = false;
        GameBuilder current = null;
        for (val line : log.split("\\R")) {
            val text = line.trim();
            if (text.isEmpty()) {
                current = null;
            } else if (!Character.isWhitespace(line.charAt(0))) {
                if (text.equals("No updates were found.")) {
                    noUpdates = true;
                } else {
                    current = games.computeIfAbsent(text, GameBuilder::new);
                }
            } else if (current != null) {
                current.read(text);
            }
        }
        val parsed = new LinkedHashMap<String, Game>();
        games.forEach((name, builder) -> parsed.put(name, builder.build()));
        return new SnrReport(parsed, noUpdates, warnings);
    }

    private static class GameBuilder {

        private final String        name;
        private final List<Ranking> rankings = new ArrayList<>();
        private       Integer       round;
        private       boolean       completed;
        private       Long          inactiveHours;
        private       boolean       failed;

        GameBuilder(String name) {
            this.name = name;
        }

        void read(String line) {
            val inactive = INACTIVE.matcher(line);
            val round = ROUND.matcher(line);
            val ranking = RANKING.matcher(line);
            if (inactive.matches()) {
                inactiveHours = Long.parseLong(inactive.group(1));
            } else if (round.matches()) {
                this.round = Integer.parseInt(round.group(1));
                completed |= round.group(2) != null;
            } else if (line.equals("Final rankings")) {
                completed = true;
            } else if (ranking.matches()) {
                rankings.add(new Ranking(Integer.parseInt(ranking.group(1)), ranking.group(2).trim(), ranking.group(3),
                        Integer.parseInt(ranking.group(4))));
            } else if (line.startsWith("Could not check for updates")) {
                failed = true;
            }
        }

        Game build() {
            return new Game(name, round, completed, inactiveHours, failed, List.copyOf(rankings));
        }
    }
}
