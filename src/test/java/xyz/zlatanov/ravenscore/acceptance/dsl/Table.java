package xyz.zlatanov.ravenscore.acceptance.dsl;

import lombok.val;

import java.util.*;

/**
 * A pipe-delimited table written in a test, first line being the column names:
 *
 * <pre>
 * house     | player  | castles
 * Stark     | Arya    | 7
 * Lannister | Cersei  | 6
 * </pre>
 */
public record Table(List<String> columns, List<Row> rows) {

    public record Row(Map<String, String> cells) {

        public String get(String column) {
            return optional(column).orElseThrow(() -> new IllegalArgumentException(
                    "Missing column \"" + column + "\" in table with columns " + cells.keySet()));
        }

        public Optional<String> optional(String column) {
            return Optional.ofNullable(cells.get(Params.normalize(column))).filter(value -> !value.isEmpty());
        }
    }

    public static Table parse(String text) {
        val lines = text.lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
        val columns = split(lines.getFirst()).stream().map(Params::normalize).toList();
        val rows = new ArrayList<Row>();
        for (val line : lines.subList(1, lines.size())) {
            val values = split(line);
            if (values.size() != columns.size()) {
                throw new IllegalArgumentException("Row \"" + line + "\" does not match the columns " + columns);
            }
            val cells = new LinkedHashMap<String, String>();
            for (int i = 0; i < columns.size(); i++) {
                cells.put(columns.get(i), values.get(i));
            }
            rows.add(new Row(cells));
        }
        return new Table(columns, List.copyOf(rows));
    }

    public boolean has(String column) {
        return columns.contains(Params.normalize(column));
    }

    private static List<String> split(String line) {
        return Arrays.stream(line.split("\\|", -1)).map(String::trim).toList();
    }
}
