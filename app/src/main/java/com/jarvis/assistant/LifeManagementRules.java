package com.jarvis.assistant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Read-only aggregation for Life Management using existing local JARVIS stores. */
public final class LifeManagementRules {
    private LifeManagementRules() { }

    public static String buildOverview(String[] taskTitles, String[] taskDueDates,
            boolean[] taskCompleted, String[] reminderTitles, long[] reminderTimes,
            int workflowCount, int timelineCount, long nowMillis) {
        if (taskTitles == null || taskDueDates == null || taskCompleted == null
                || taskTitles.length != taskDueDates.length
                || taskTitles.length != taskCompleted.length
                || reminderTitles == null || reminderTimes == null
                || reminderTitles.length != reminderTimes.length
                || nowMillis <= 0L) {
            return "Life overview unavailable: saved task or reminder data could not be read consistently.";
        }

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        iso.setLenient(false);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nowMillis);
        String today = iso.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        String sevenDays = iso.format(calendar.getTime());

        int open = 0, completed = 0, overdue = 0, dueToday = 0;
        int dueNextSevenDays = 0, invalidDates = 0;
        List<String> datedTasks = new ArrayList<>();
        for (int i = 0; i < taskTitles.length; i++) {
            String title = clean(taskTitles[i], "Untitled task");
            String due = taskDueDates[i] == null ? "" : taskDueDates[i].trim();
            if (taskCompleted[i]) {
                completed++;
                continue;
            }
            open++;
            if (due.isEmpty()) continue;
            if (!B21TaskRules.isValidDueDate(due)) {
                invalidDates++;
                continue;
            }
            if (due.compareTo(today) < 0) {
                overdue++;
                datedTasks.add("OVERDUE • " + title + " — " + due);
            } else if (due.equals(today)) {
                dueToday++;
                datedTasks.add("DUE TODAY • " + title);
            } else if (due.compareTo(sevenDays) <= 0) {
                dueNextSevenDays++;
                datedTasks.add("UPCOMING • " + title + " — " + due);
            }
        }

        int scheduledReminders = 0, reminderToday = 0, reminderNextSevenDays = 0;
        long nextReminderAt = Long.MAX_VALUE;
        String nextReminderTitle = "";
        for (int i = 0; i < reminderTimes.length; i++) {
            long trigger = reminderTimes[i];
            if (trigger < nowMillis) continue;
            scheduledReminders++;
            String reminderDate = iso.format(new Date(trigger));
            if (reminderDate.equals(today)) reminderToday++;
            else if (reminderDate.compareTo(today) > 0
                    && reminderDate.compareTo(sevenDays) <= 0) reminderNextSevenDays++;
            if (trigger < nextReminderAt) {
                nextReminderAt = trigger;
                nextReminderTitle = clean(reminderTitles[i], "Untitled reminder");
            }
        }

        SimpleDateFormat reminderFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy • hh:mm a", Locale.getDefault());
        StringBuilder report = new StringBuilder("LIFE MANAGEMENT OVERVIEW\n\n");
        report.append("TASKS\nOpen: ").append(open)
                .append(" • Completed: ").append(completed)
                .append("\nOverdue: ").append(overdue)
                .append(" • Due today: ").append(dueToday)
                .append(" • Due in next 7 days: ").append(dueNextSevenDays)
                .append("\nDates needing review: ").append(invalidDates).append("\n");

        if (datedTasks.isEmpty()) {
            report.append("\nNo open tasks are overdue or due in the next 7 days.\n");
        } else {
            report.append("\nDUE TASKS\n");
            int limit = Math.min(5, datedTasks.size());
            for (int i = 0; i < limit; i++) report.append("• ").append(datedTasks.get(i)).append("\n");
            if (datedTasks.size() > limit) {
                report.append("…and ").append(datedTasks.size() - limit).append(" more\n");
            }
        }

        report.append("\nREMINDERS\nUpcoming scheduled: ").append(scheduledReminders)
                .append(" • Today: ").append(reminderToday)
                .append(" • Next 7 days: ").append(reminderNextSevenDays).append("\n");
        if (nextReminderAt == Long.MAX_VALUE) {
            report.append("Next reminder: none scheduled\n");
        } else {
            report.append("Next reminder: ").append(nextReminderTitle).append("\n")
                    .append(reminderFormat.format(new Date(nextReminderAt))).append("\n");
        }

        report.append("\nEXISTING PERSONAL ORGANIZATION\n");
        report.append(workflowCount < 0 ? "Manual checklists: unavailable\n"
                : "Saved manual checklists: " + workflowCount + "\n");
        report.append(timelineCount < 0 ? "Personal timeline entries: unavailable\n"
                : "Personal timeline entries: " + timelineCount + "\n");
        report.append("\nBased only on information already saved locally in JARVIS. "
                + "Calendar events are not imported or monitored by this overview. "
                + "No records were changed and no network request was made.");
        return report.toString();
    }

    private static String clean(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
