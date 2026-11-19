package com.jarvis.assistant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Owner-entered, on-device finance and goal organizer. No bank connection or transactions. */
public final class JarvisPersonalOrganizer {
    private static final String PREFS = "jarvis_personal_organizer";
    private JarvisPersonalOrganizer() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void showFinance(Context c) {
        String records = prefs(c).getString("expenses", "");
        double total = totalForCurrentMonth(records);
        double budget = Double.longBitsToDouble(prefs(c).getLong("monthly_budget_bits",
                Double.doubleToLongBits(0d)));
        String budgetLine = budget > 0
                ? "\nMonthly budget: ₹" + money(budget) + "\nRemaining: ₹" + money(budget - total)
                : "\nMonthly budget: not set";
        String summary = "This month (manual entries)\nSpent: ₹" + money(total) + budgetLine
                + "\n\n" + (records.isEmpty() ? "No expenses recorded yet." : recentExpenses(records));
        new AlertDialog.Builder(c).setTitle("JARVIS • Personal Finance")
                .setMessage(summary)
                .setPositiveButton("Add expense", (d, w) -> addExpense(c))
                .setNeutralButton("Set monthly budget", (d, w) -> setBudget(c))
                .setNegativeButton("Close", null).show();
    }

    private static void addExpense(Context c) {
        LinearLayout panel = form(c);
        EditText amount = field(c, "Amount in ₹ (example: 120)");
        amount.setInputType(8194);
        EditText category = field(c, "Category (food, travel, etc.)");
        EditText note = field(c, "Short note (optional)");
        panel.addView(amount); panel.addView(category); panel.addView(note);
        new AlertDialog.Builder(c).setTitle("Add manual expense").setView(panel)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    try {
                        double value = Double.parseDouble(amount.getText().toString().trim());
                        if ((Double.isNaN(value) || Double.isInfinite(value)) || value <= 0) throw new NumberFormatException();
                        String date = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
                        String line = date + "|" + money(value) + "|" + clean(category.getText().toString())
                                + "|" + clean(note.getText().toString());
                        SharedPreferences p = prefs(c);
                        String old = p.getString("expenses", "");
                        p.edit().putString("expenses", old.isEmpty() ? line : old + "\n" + line).apply();
                        showFinance(c);
                    } catch (Exception e) {
                        new AlertDialog.Builder(c).setMessage("Enter a valid amount greater than zero.")
                                .setPositiveButton("OK", null).show();
                    }
                }).show();
    }

    private static void setBudget(Context c) {
        EditText input = field(c, "Monthly budget in ₹");
        input.setInputType(8194);
        new AlertDialog.Builder(c).setTitle("Monthly budget").setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    try {
                        double value = Double.parseDouble(input.getText().toString().trim());
                        if ((Double.isNaN(value) || Double.isInfinite(value)) || value < 0) throw new NumberFormatException();
                        prefs(c).edit().putLong("monthly_budget_bits", Double.doubleToLongBits(value)).apply();
                        showFinance(c);
                    } catch (Exception e) {
                        new AlertDialog.Builder(c).setMessage("Enter a valid non-negative budget.")
                                .setPositiveButton("OK", null).show();
                    }
                }).show();
    }

    public static void showGoals(Context c) {
        String goals = prefs(c).getString("goals", "");
        String[] rows = goals.isEmpty() ? new String[0] : goals.split("\\n");
        StringBuilder text = new StringBuilder("Your milestones (stored on this phone)\n\n");
        if (rows.length == 0) text.append("No goals added yet.");
        for (String row : rows) {
            String[] parts = row.split("\\|", 3);
            if (parts.length == 3) text.append("[").append("1".equals(parts[0]) ? "DONE" : "OPEN")
                    .append("] ").append(parts[1]).append("  •  ").append(parts[2]).append("\n");
        }
        new AlertDialog.Builder(c).setTitle("JARVIS • Goals & Milestones")
                .setMessage(text.toString())
                .setPositiveButton("Add goal", (d, w) -> addGoal(c))
                .setNeutralButton("Update status", (d, w) -> updateGoal(c))
                .setNegativeButton("Close", null).show();
    }

    private static void addGoal(Context c) {
        LinearLayout panel = form(c);
        EditText title = field(c, "Goal / milestone");
        EditText date = field(c, "Target date (optional)");
        panel.addView(title); panel.addView(date);
        new AlertDialog.Builder(c).setTitle("New milestone").setView(panel)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    String name = clean(title.getText().toString());
                    if (name.isEmpty()) return;
                    String row = "0|" + name + "|" + clean(date.getText().toString());
                    String old = prefs(c).getString("goals", "");
                    prefs(c).edit().putString("goals", old.isEmpty() ? row : old + "\n" + row).apply();
                    showGoals(c);
                }).show();
    }

    private static void updateGoal(Context c) {
        String raw = prefs(c).getString("goals", "");
        if (raw.isEmpty()) {
            new AlertDialog.Builder(c).setMessage("Add a goal first.").setPositiveButton("OK", null).show();
            return;
        }
        String[] rows = raw.split("\\n");
        String[] labels = new String[rows.length];
        for (int i = 0; i < rows.length; i++) {
            String[] p = rows[i].split("\\|", 3);
            labels[i] = (p.length > 0 && "1".equals(p[0]) ? "✓ " : "○ ")
                    + (p.length > 1 ? p[1] : "Goal");
        }
        new AlertDialog.Builder(c).setTitle("Toggle goal status")
                .setItems(labels, (d, which) -> {
                    String[] p = rows[which].split("\\|", 3);
                    if (p.length == 3) rows[which] = ("1".equals(p[0]) ? "0" : "1")
                            + "|" + p[1] + "|" + p[2];
                    StringBuilder updated = new StringBuilder();
                    for (String row : rows) {
                        if (updated.length() > 0) updated.append("\n");
                        updated.append(row);
                    }
                    prefs(c).edit().putString("goals", updated.toString()).apply();
                    showGoals(c);
                }).setNegativeButton("Close", null).show();
    }

    private static LinearLayout form(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(28, 8, 28, 0);
        return l;
    }

    private static EditText field(Context c, String hint) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(0xffaa8890);
        e.setSingleLine(true);
        return e;
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace("|", "/").replace("\\n", " ").trim();
    }

    private static double totalForCurrentMonth(String records) {
        String month = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        double total = 0;
        if (records == null || records.isEmpty()) return total;
        for (String row : records.split("\\n")) {
            String[] p = row.split("\\|", 4);
            if (p.length >= 2 && month.equals(p[0])) {
                try { total += Double.parseDouble(p[1]); } catch (Exception ignored) {}
            }
        }
        return total;
    }

    private static String recentExpenses(String records) {
        String[] rows = records.split("\\n");
        StringBuilder b = new StringBuilder("Recent entries:\n");
        int start = Math.max(0, rows.length - 5);
        for (int i = rows.length - 1; i >= start; i--) {
            String[] p = rows[i].split("\\|", 4);
            if (p.length >= 3) b.append(p[0]).append(" • ₹").append(p[1]).append(" • ")
                    .append(p[2]).append(p.length == 4 && !p[3].isEmpty() ? " — " + p[3] : "")
                    .append("\n");
        }
        return b.toString();
    }

    private static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
