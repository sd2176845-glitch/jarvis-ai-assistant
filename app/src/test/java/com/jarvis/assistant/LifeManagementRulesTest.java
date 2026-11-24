package com.jarvis.assistant;

import org.junit.Test;
import java.util.Calendar;
import static org.junit.Assert.*;

public class LifeManagementRulesTest {
    private long localDate(int year, int month, int day, int hour, int minute) {
        Calendar value = Calendar.getInstance();
        value.set(year, month, day, hour, minute, 0);
        value.set(Calendar.MILLISECOND, 0);
        return value.getTimeInMillis();
    }

    private String isoDate(long millis) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date(millis));
    }

    @Test
    public void overviewAggregatesExistingTasksRemindersWorkflowsAndTimeline() {
        long now = localDate(2026, Calendar.NOVEMBER, 24, 10, 0);
        String today = isoDate(now);
        String yesterday = isoDate(localDate(2026, Calendar.NOVEMBER, 23, 12, 0));
        String inThreeDays = isoDate(localDate(2026, Calendar.NOVEMBER, 27, 12, 0));
        long[] reminderTimes = {
                now + 60 * 60 * 1000L,
                now + 2 * 24 * 60 * 60 * 1000L,
                now + 8 * 24 * 60 * 60 * 1000L
        };

        String report = LifeManagementRules.buildOverview(
                new String[]{"Overdue assignment", "Today's task", "Upcoming task", "No due date", "Finished task", "Bad date task"},
                new String[]{yesterday, today, inThreeDays, "", today, "2026-02-30"},
                new boolean[]{false, false, false, false, true, false},
                new String[]{"Today reminder", "Later reminder", "Far reminder"},
                reminderTimes, 2, 4, now);

        assertTrue(report.contains("Open: 5 • Completed: 1"));
        assertTrue(report.contains("Overdue: 1 • Due today: 1 • Due in next 7 days: 1"));
        assertTrue(report.contains("Dates needing review: 1"));
        assertTrue(report.contains("Upcoming scheduled: 3 • Today: 1 • Next 7 days: 1"));
        assertTrue(report.contains("Next reminder: Today reminder"));
        assertTrue(report.contains("Saved manual checklists: 2"));
        assertTrue(report.contains("Personal timeline entries: 4"));
        assertTrue(report.contains("Calendar events are not imported"));
    }

    @Test
    public void overviewHandlesEmptyDataAndUnavailableOptionalStores() {
        long now = localDate(2026, Calendar.NOVEMBER, 24, 10, 0);
        String report = LifeManagementRules.buildOverview(
                new String[0], new String[0], new boolean[0],
                new String[0], new long[0], -1, -1, now);
        assertTrue(report.contains("Open: 0 • Completed: 0"));
        assertTrue(report.contains("Next reminder: none scheduled"));
        assertTrue(report.contains("Manual checklists: unavailable"));
        assertTrue(report.contains("Personal timeline entries: unavailable"));
    }

    @Test
    public void overviewRejectsMismatchedOrMissingInputs() {
        String report = LifeManagementRules.buildOverview(
                new String[]{"Task"}, new String[0], new boolean[]{false},
                new String[0], new long[0], 0, 0, 100L);
        assertTrue(report.startsWith("Life overview unavailable"));
        assertTrue(LifeManagementRules.buildOverview(null, new String[0], new boolean[0],
                new String[0], new long[0], 0, 0, 100L).startsWith("Life overview unavailable"));
    }

    @Test
    public void reminderTimesInPastAreNotCounted() {
        long now = localDate(2026, Calendar.NOVEMBER, 24, 10, 0);
        String report = LifeManagementRules.buildOverview(
                new String[0], new String[0], new boolean[0],
                new String[]{"Old reminder"}, new long[]{now - 1000L}, 0, 0, now);
        assertTrue(report.contains("Upcoming scheduled: 0 • Today: 0 • Next 7 days: 0"));
        assertTrue(report.contains("Next reminder: none scheduled"));
    }
}
