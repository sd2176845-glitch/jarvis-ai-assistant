package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Local-first CSV profile and quality checks; only aggregate metrics may be sent for AI narration. */
public final class DataIntelligenceStudio {
    private DataIntelligenceStudio() {}

    private static final int MAX_CHARS = 50000;
    private static final int MAX_ROWS = 1000;
    private static final int MAX_COLUMNS = 50;

    private static final class Dataset {
        final List<List<String>> rows;
        final List<String> headers;
        Dataset(List<List<String>> rows) {
            this.rows = rows;
            this.headers = rows.isEmpty() ? new ArrayList<>() : rows.get(0);
        }
        int dataRows() { return Math.max(0, rows.size() - 1); }
        int columns() { return headers.size(); }
    }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] options = {
                "Dataset Profiler • rows, columns and numeric summary",
                "Data Quality Inspector • missing / duplicate / inconsistent",
                "AI Insight Report • aggregate statistics only"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Data Intelligence Center")
                .setItems(options, (d, which) -> {
                    if (which == 0) inputDataset(activity, status, transcript, 0);
                    else if (which == 1) inputDataset(activity, status, transcript, 1);
                    else inputDataset(activity, status, transcript, 2);
                }).setNegativeButton("Close", null).show();
    }

    private static void inputDataset(Activity activity, TextView status, TextView transcript, int mode) {
        String title = mode == 0 ? "Dataset Profiler" : mode == 1 ? "Data Quality Inspector" : "AI Insight Report";
        EditText input = new EditText(activity);
        input.setHint("Paste CSV text here. First row must contain column headers.");
        input.setMinLines(5);
        input.setMaxLines(12);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(title).setMessage(
                "Local analysis only. Do not paste passwords, contact details, health records, financial identifiers or confidential data. "
                        + "Limits: 50,000 characters, 1,000 rows and 50 columns. The original CSV is not saved.")
                .setView(layout)
                .setPositiveButton("Analyze", (d, w) -> {
                    String csv = input.getText().toString();
                    if (csv.trim().isEmpty()) {
                        Toast.makeText(activity, "Paste a CSV dataset first.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (csv.length() > MAX_CHARS) {
                        Toast.makeText(activity, "Dataset is larger than 50,000 characters.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    try {
                        Dataset data = parse(csv);
                        if (mode == 0) showResult(activity, title, profile(data, true));
                        else if (mode == 1) showResult(activity, title, quality(data));
                        else createAiReport(activity, status, transcript, data);
                    } catch (IllegalArgumentException e) {
                        showResult(activity, "Dataset could not be analyzed", e.getMessage());
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private static Dataset parse(String csv) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < csv.length(); i++) {
            char c = csv.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else quoted = false;
                } else cell.append(c);
            } else if (c == '"' && cell.length() == 0) {
                quoted = true;
            } else if (c == ',') {
                row.add(cell.toString());
                cell.setLength(0);
            } else if (c == '\n') {
                row.add(cell.toString());
                cell.setLength(0);
                if (!isBlankRow(row)) {
                    if (row.size() > MAX_COLUMNS) throw new IllegalArgumentException("A CSV row exceeds the 50-column limit.");
                    rows.add(new ArrayList<>(row));
                }
                row.clear();
                if (rows.size() > MAX_ROWS) throw new IllegalArgumentException("Dataset exceeds the 1,000-row limit.");
            } else if (c != '\r') {
                cell.append(c);
            }
        }
        if (quoted) throw new IllegalArgumentException("A quoted CSV field was not closed. Check quotation marks.");
        if (cell.length() > 0 || !row.isEmpty()) {
            row.add(cell.toString());
            if (!isBlankRow(row)) {
                if (row.size() > MAX_COLUMNS) throw new IllegalArgumentException("A CSV row exceeds the 50-column limit.");
                rows.add(new ArrayList<>(row));
            }
        }
        if (rows.isEmpty()) throw new IllegalArgumentException("No CSV rows were found.");
        if (rows.get(0).size() > MAX_COLUMNS) throw new IllegalArgumentException("Dataset exceeds the 50-column limit.");
        if (rows.size() > MAX_ROWS) throw new IllegalArgumentException("Dataset exceeds the 1,000-row limit.");
        if (rows.get(0).size() == 0) throw new IllegalArgumentException("The first row must contain column headers.");
        return new Dataset(rows);
    }

    private static boolean isBlankRow(List<String> row) {
        for (String value : row) if (value != null && !value.trim().isEmpty()) return false;
        return true;
    }

    private static String profile(Dataset data, boolean includeHeaderNames) {
        StringBuilder out = new StringBuilder("DATASET PROFILE\nRows: ")
                .append(data.dataRows()).append("\nColumns: ").append(data.columns()).append("\n\n");
        for (int col = 0; col < data.columns(); col++) {
            String name = includeHeaderNames ? data.headers.get(col).trim() : "Column " + (col + 1);
            if (includeHeaderNames && name.isEmpty()) name = "(unnamed column " + (col + 1) + ")";
            int present = 0, numeric = 0;
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY, sum = 0;
            for (int r = 1; r < data.rows.size(); r++) {
                List<String> cells = data.rows.get(r);
                String value = col < cells.size() ? cells.get(col).trim() : "";
                if (value.isEmpty()) continue;
                present++;
                try {
                    double number = Double.parseDouble(value);
                    if (Double.isNaN(number) || Double.isInfinite(number)) continue;
                    numeric++;
                    min = Math.min(min, number);
                    max = Math.max(max, number);
                    sum += number;
                } catch (NumberFormatException ignored) {}
            }
            out.append(col + 1).append(". ").append(name)
                    .append("\n   Non-empty: ").append(present).append(" / ").append(data.dataRows());
            if (present > 0 && numeric == present) {
                out.append("\n   Numeric • min ").append(fmt(min))
                        .append(" • max ").append(fmt(max))
                        .append(" • average ").append(fmt(sum / numeric));
            } else if (numeric > 0) {
                out.append("\n   Mixed values • numeric cells ").append(numeric);
            } else out.append("\n   Text / mixed values");
            out.append("\n\n");
        }
        out.append("Calculated locally on this device. Source CSV is not saved or uploaded.");
        return out.toString();
    }

    private static String quality(Dataset data) {
        int expected = data.columns();
        int missing = 0, inconsistent = 0, duplicateRows = 0;
        Set<String> seenRows = new HashSet<>();
        Set<String> seenHeaders = new HashSet<>();
        List<String> warnings = new ArrayList<>();
        for (int i = 0; i < data.headers.size(); i++) {
            String h = data.headers.get(i).trim().toLowerCase(Locale.ROOT);
            if (h.isEmpty()) warnings.add("Column " + (i + 1) + " has an empty header.");
            else if (!seenHeaders.add(h)) warnings.add("Duplicate header at column " + (i + 1) + ".");
        }
        for (int r = 1; r < data.rows.size(); r++) {
            List<String> cells = data.rows.get(r);
            if (cells.size() != expected) inconsistent++;
            for (int c = 0; c < expected; c++) {
                String value = c < cells.size() ? cells.get(c).trim() : "";
                if (value.isEmpty()) missing++;
            }
            String key = rowKey(cells);
            if (!seenRows.add(key)) duplicateRows++;
        }
        StringBuilder out = new StringBuilder("DATA QUALITY REPORT\n")
                .append("Data rows: ").append(data.dataRows())
                .append("\nColumns: ").append(expected)
                .append("\nMissing cells: ").append(missing)
                .append("\nRows with inconsistent column count: ").append(inconsistent)
                .append("\nDuplicate data rows: ").append(duplicateRows)
                .append("\n\nHEADER CHECKS\n");
        if (warnings.isEmpty()) out.append("No empty or duplicate headers detected.\n");
        else for (String warning : warnings) out.append("• ").append(warning).append("\n");
        out.append("\nChecks are structural only; they do not prove that values are factually correct.");
        return out.toString();
    }

    private static String rowKey(List<String> row) {
        StringBuilder key = new StringBuilder();
        for (String value : row) {
            String cell = value == null ? "" : value.trim();
            key.append(cell.length()).append(':').append(cell).append('|');
        }
        return key.toString();
    }

    private static void createAiReport(Activity activity, TextView status, TextView transcript, Dataset data) {
        String summary = profile(data, false);
        String qualitySummary = quality(data);
        String aggregate = summary + "\n\n" + qualitySummary;
        new AlertDialog.Builder(activity).setTitle("Review AI report input")
                .setMessage("The original CSV will NOT be sent. JARVIS will send only these locally calculated aggregate statistics and column labels to its AI service:\n\n"
                        + trimForPreview(aggregate, 2500))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate report", (d, w) -> {
                    String prompt = "Write a cautious, readable data-insight report using only the following locally calculated aggregate summary. "
                            + "Do not infer causation, identity or facts not supported by the summary. State limitations and suggest safe follow-up checks. "
                            + "The raw CSV rows were not provided.\n\n" + aggregate;
                    status.setText("DATA INTELLIGENCE • GENERATING REPORT");
                    transcript.setText("Preparing an aggregate-only insight report…");
                    JarvisAiClient.askStandalone(activity, prompt, reply -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        status.setText("DATA INSIGHT REPORT READY");
                        transcript.setText(reply);
                        new AlertDialog.Builder(activity).setTitle("JARVIS • Data Insight Report")
                                .setMessage(reply).setPositiveButton("Done", null).show();
                    });
                }).show();
    }

    private static String trimForPreview(String value, int limit) {
        return value.length() <= limit ? value : value.substring(0, limit) + "\n… preview shortened";
    }

    private static String fmt(double value) {
        return String.format(Locale.US, "%.3f", value);
    }

    private static void showResult(Activity activity, String title, String message) {
        new AlertDialog.Builder(activity).setTitle(title).setMessage(message)
                .setPositiveButton("Done", null).show();
    }
}
