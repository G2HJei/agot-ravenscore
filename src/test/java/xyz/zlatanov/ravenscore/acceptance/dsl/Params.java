package xyz.zlatanov.ravenscore.acceptance.dsl;

import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The arguments of a DSL step: {@code "key: value"} pairs, plus an optional multi-line {@link Table}. Keys are case and
 * whitespace insensitive, and unknown keys are rejected so a typo cannot be silently ignored.
 */
final class Params {

    private final String              step;
    private final Map<String, String> values;
    private final Table               table;

    private Params(String step, Map<String, String> values, Table table) {
        this.step = step;
        this.values = values;
        this.table = table;
    }

    static Params parse(String step, String[] args, String... allowedKeys) {
        val allowed = Arrays.stream(allowedKeys).map(Params::normalize).collect(Collectors.toCollection(LinkedHashSet::new));
        val values = new LinkedHashMap<String, String>();
        Table table = null;
        for (val arg : args) {
            if (arg.contains("\n")) {
                table = Table.parse(arg);
                continue;
            }
            val separator = arg.indexOf(':');
            if (separator < 0) {
                throw new IllegalArgumentException(
                        "%s: expected \"key: value\" but got \"%s\"; keys are %s".formatted(step, arg, allowed));
            }
            val key = normalize(arg.substring(0, separator));
            if (!allowed.contains(key)) {
                throw new IllegalArgumentException("%s: unknown parameter \"%s\"; expected one of %s".formatted(step, key, allowed));
            }
            values.put(key, arg.substring(separator + 1).trim());
        }
        return new Params(step, values, table);
    }

    static String normalize(String key) {
        return key.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    Optional<String> optional(String key) {
        return Optional.ofNullable(values.get(key));
    }

    String get(String key, String defaultValue) {
        return optional(key).orElse(defaultValue);
    }

    String required(String key) {
        return optional(key).orElseThrow(() -> new IllegalArgumentException(step + ": missing parameter \"" + key + "\""));
    }

    int integer(String key, int defaultValue) {
        return optional(key).map(Integer::parseInt).orElse(defaultValue);
    }

    boolean yesNo(String key, boolean defaultValue) {
        return optional(key).map(value -> switch (normalize(value)) {
            case "yes", "true" -> true;
            case "no", "false" -> false;
            default ->
                    throw new IllegalArgumentException(step + ": \"" + key + "\" should be yes or no, not \"" + value + "\"");
        }).orElse(defaultValue);
    }

    /**
     * Comma-separated values, e.g. "players: Arya, Brienne, Cersei".
     */
    Optional<List<String>> list(String key) {
        return optional(key).map(value -> value.isBlank()
                ? List.of()
                : Arrays.stream(value.split(",")).map(String::trim).toList());
    }

    Table table() {
        if (table == null) {
            throw new IllegalArgumentException(step + ": missing table");
        }
        return table;
    }

    Optional<Table> optionalTable() {
        return Optional.ofNullable(table);
    }
}
