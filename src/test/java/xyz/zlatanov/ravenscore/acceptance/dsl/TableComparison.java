package xyz.zlatanov.ravenscore.acceptance.dsl;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

/**
 * Expected and actual rows rendered as aligned text tables, so a failed comparison shows a readable diff.
 */
record TableComparison(String actual, String expected) {

    static TableComparison of(Table expected, List<List<String>> actualRows) {
        val expectedRows = expected.rows().stream()
                .map(row -> expected.columns().stream().map(row::get).toList())
                .toList();
        val widths = new int[expected.columns().size()];
        val allRows = new ArrayList<List<String>>();
        allRows.add(expected.columns());
        allRows.addAll(expectedRows);
        allRows.addAll(actualRows);
        for (val row : allRows) {
            for (int i = 0; i < row.size(); i++) {
                widths[i] = Math.max(widths[i], String.valueOf(row.get(i)).length());
            }
        }
        return new TableComparison(render(expected.columns(), actualRows, widths),
                render(expected.columns(), expectedRows, widths));
    }

    private static String render(List<String> columns, List<List<String>> rows, int[] widths) {
        val text = new StringBuilder("\n").append(line(columns, widths));
        rows.forEach(row -> text.append(line(row, widths)));
        return text.toString();
    }

    private static String line(List<String> cells, int[] widths) {
        val padded = new ArrayList<String>();
        for (int i = 0; i < cells.size(); i++) {
            padded.add(String.format("%-" + widths[i] + "s", cells.get(i)));
        }
        return String.join(" | ", padded).stripTrailing() + "\n";
    }
}
