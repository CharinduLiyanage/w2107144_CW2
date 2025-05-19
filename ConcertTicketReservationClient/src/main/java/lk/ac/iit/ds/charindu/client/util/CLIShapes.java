package lk.ac.iit.ds.charindu.client.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CLIShapes {

    /**
     * Prints a list of objects as a CLI table.
     *
     * @param <T>     the object type
     * @param items   the list of items to print
     * @param columns a LinkedHashMap whose keys are column headers (in order),
     *                and whose values are Functions extracting that column’s value from T
     */
    public static <T> void printTable(List<T> items,
                                      LinkedHashMap<String, Function<T, ?>> columns) {
        if (items == null || items.isEmpty()) {
            System.out.println("No data to display.");
            return;
        }

        // 1) Compute column widths (max of header vs. data)
        Map<String, Integer> widths = new LinkedHashMap<>();
        for (String header : columns.keySet()) {
            widths.put(header, header.length());
        }
        for (T item : items) {
            for (Map.Entry<String, Function<T, ?>> col : columns.entrySet()) {
                String header = col.getKey();
                Object value = col.getValue().apply(item);
                String cell = (value == null ? "" : value.toString());
                widths.put(header, Math.max(widths.get(header), cell.length()));
            }
        }

        // 2) Build the horizontal separator, e.g. +------+-------+
        StringBuilder sep = new StringBuilder("+");
        for (int w : widths.values()) {
            sep.append(repeat('-', w + 2)).append("+");
        }
        String separator = sep.toString();

        // 3) Print header row
        System.out.println(separator);
        StringBuilder headerRow = new StringBuilder("|");
        for (Map.Entry<String, Integer> col : widths.entrySet()) {
            headerRow
                    .append(" ")
                    .append(String.format("%-" + col.getValue() + "s", col.getKey()))
                    .append(" |");
        }
        System.out.println(headerRow.toString());
        System.out.println(separator);

        // 4) Print each data row
        for (T item : items) {
            StringBuilder row = new StringBuilder("|");
            for (Map.Entry<String, Function<T, ?>> col : columns.entrySet()) {
                String header = col.getKey();
                Object value = col.getValue().apply(item);
                String cell = (value == null ? "" : value.toString());
                row
                        .append(" ")
                        .append(String.format("%-" + widths.get(header) + "s", cell))
                        .append(" |");
            }
            System.out.println(row.toString());
        }

        // 5) Bottom separator
        System.out.println(separator);
    }

    /**
     * Utility to repeat a character n times (Java 8 compatible).
     */
    private static String repeat(char ch, int count) {
        char[] buf = new char[count];
        Arrays.fill(buf, ch);
        return new String(buf);
    }

    public static void alertBox(String message, char borderChar) {
        // use the Java 8–compatible repeat method
        String edge = repeat(borderChar, message.length() + 4);
        String out = String.format(
                "%s%n" +            // top edge
                        "%c %s %c%n" +      // borderChar, message, borderChar
                        "%s",               // bottom edge
                edge, borderChar, message, borderChar, edge
        );
        System.out.println(out);
    }

    public static void promptBox(String message) {
        // build the horizontal line with the repeat utility
        String line = repeat('─', message.length() + 2);
        String out = String.format(
                "┌%s┐%n" +      // top border
                        "│ %s │%n" +    // message line
                        "└%s┘",         // bottom border
                line, message, line
        );
        System.out.println(out);
    }

    public static void numberMenu(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf(" %d) ▶ %s%n", i + 1, options[i]);
        }
    }
}
