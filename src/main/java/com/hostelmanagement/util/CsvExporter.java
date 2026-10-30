package com.hostelmanagement.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Safe CSV export utility handling commas, double-quotes, newlines, and UTF-8 encoding.
 */
public final class CsvExporter {

    private CsvExporter() {}

    public static void exportToCsv(File targetFile, List<String> headers, List<List<String>> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            
            // UTF-8 BOM so Excel opens special characters correctly
            writer.write('\ufeff');

            // Write headers
            if (headers != null && !headers.isEmpty()) {
                writer.write(formatRow(headers));
                writer.newLine();
            }

            // Write data rows
            if (rows != null) {
                for (List<String> row : rows) {
                    writer.write(formatRow(row));
                    writer.newLine();
                }
            }
        }
    }

    private static String formatRow(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(escapeValue(values.get(i)));
        }
        return sb.toString();
    }

    private static String escapeValue(String val) {
        if (val == null) return "\"\"";
        String s = val.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
