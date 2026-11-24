package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class WeatherOutlookRulesTest {
    @Test
    public void sevenDayOutlookFormatsDailyWeatherAndUnits() {
        String report = WeatherOutlookRules.formatSevenDay(
                "Kolkata", "Asia/Kolkata",
                new String[]{"2026-11-24", "2026-11-25"},
                new String[]{"2", "95"},
                new String[]{"29", "28"}, new String[]{"21", "20"},
                new String[]{"1.5", "12.0"}, new String[]{"40", "85"},
                new String[]{"18", "25"}, new String[]{"30", "45"},
                new String[]{"7.2", "8.5"},
                new String[]{"2026-11-24T05:45", "2026-11-25T05:46"},
                new String[]{"2026-11-24T17:00", "2026-11-25T17:01"},
                "°C", "mm", "km/h");
        assertTrue(report.contains("NEXT 7 DAYS"));
        assertTrue(report.contains("Partly cloudy"));
        assertTrue(report.contains("Thunderstorm"));
        assertTrue(report.contains("High / low: 29 / 21 °C"));
        assertTrue(report.contains("Rain total: 12.0 mm • Probability: 85%"));
        assertTrue(report.contains("Maximum wind / gust: 25 / 45 km/h"));
        assertTrue(report.contains("Maximum UV index: 8.5"));
        assertTrue(report.contains("not an official IMD warning"));
    }

    @Test
    public void sevenDayOutlookHandlesMissingMalformedAndOutOfRangeValues() {
        String report = WeatherOutlookRules.formatSevenDay(
                "Kolkata\nInjected", "Asia/Kolkata",
                new String[]{"2026-11-24"},
                new String[]{"bad"}, new String[]{"-5000"}, new String[]{"-2.5"},
                new String[]{"NaN"}, new String[]{"120"}, new String[]{"Infinity"},
                null, new String[]{"31"}, null, null, "°C", "mm", "km/h");
        assertTrue(report.contains("Kolkata Injected"));
        assertTrue(report.contains("Condition: Not supplied"));
        assertTrue(report.contains("High / low: Not supplied / -2.5 °C"));
        assertTrue(report.contains("Probability: Not supplied"));
        assertTrue(report.contains("Maximum UV index: Not supplied"));
    }

    @Test
    public void sevenDayOutlookIsCappedAtSevenDays() {
        String[] dates = new String[9];
        String[] codes = new String[9];
        for (int i = 0; i < 9; i++) {
            dates[i] = "Day " + i;
            codes[i] = "0";
        }
        dates[8] = "EXTRA_NINTH_DAY";
        String report = WeatherOutlookRules.formatSevenDay(
                "Kolkata", "Asia/Kolkata", dates, codes,
                null, null, null, null, null, null, null, null, null,
                "°C", "mm", "km/h");
        assertTrue(report.contains("Showing 7 daily model estimates"));
        assertFalse(report.contains("EXTRA_NINTH_DAY"));
    }
}
