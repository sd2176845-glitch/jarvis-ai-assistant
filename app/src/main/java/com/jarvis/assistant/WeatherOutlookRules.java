package com.jarvis.assistant;

/** Bounded formatting for an owner-invoked seven-day modelled weather outlook. */
public final class WeatherOutlookRules {
    private WeatherOutlookRules() { }

    public static String formatSevenDay(String place, String timezone, String[] dates,
                                        String[] weatherCodes, String[] maxTemperature,
                                        String[] minTemperature, String[] precipitationTotal,
                                        String[] precipitationProbability, String[] maxWind,
                                        String[] maxGust, String[] maxUv, String[] sunrise,
                                        String[] sunset, String temperatureUnit,
                                        String precipitationUnit, String windUnit) {
        if (dates == null || dates.length == 0) {
            return "No daily weather outlook is available for this location.";
        }
        int count = Math.min(7, dates.length);
        String tempUnit = safeUnit(temperatureUnit, "°C");
        String rainUnit = safeUnit(precipitationUnit, "mm");
        String windSpeedUnit = safeUnit(windUnit, "km/h");
        StringBuilder out = new StringBuilder("NEXT 7 DAYS • MODELLED WEATHER OUTLOOK\n\n");
        out.append("Place: ").append(clean(place, 80)).append("\n");
        out.append("Time zone: ").append(display(timezone, 60)).append("\n\n");
        for (int i = 0; i < count; i++) {
            String date = display(at(dates, i), 32);
            if ("Not supplied".equals(date)) date = "Day " + (i + 1);
            out.append(date).append("\n");
            out.append("Condition: ").append(condition(weatherCode(at(weatherCodes, i)))).append("\n");
            out.append("High / low: ").append(temperature(at(maxTemperature, i))).append(" / ")
                    .append(temperature(at(minTemperature, i))).append(" ").append(tempUnit).append("\n");
            out.append("Rain total: ").append(nonNegative(at(precipitationTotal, i))).append(" ")
                    .append(rainUnit).append(" • Probability: ")
                    .append(probability(at(precipitationProbability, i))).append("%\n");
            out.append("Maximum wind / gust: ").append(nonNegative(at(maxWind, i))).append(" / ")
                    .append(nonNegative(at(maxGust, i))).append(" ").append(windSpeedUnit).append("\n");
            out.append("Maximum UV index: ").append(uv(at(maxUv, i))).append("\n");
            out.append("Sunrise / sunset: ").append(display(at(sunrise, i), 32)).append(" / ")
                    .append(display(at(sunset, i), 32)).append("\n\n");
        }
        out.append("Showing ").append(count).append(" daily model estimates. Source: Open-Meteo Weather Forecast API.\n");
        out.append("This is a model-based planning outlook, not an official IMD warning or emergency alert. ");
        out.append("Forecasts and local conditions can change; check current official local advisories before travel or outdoor activity. ");
        out.append("No location is monitored and no background alerts are sent.");
        return out.toString();
    }

    private static String at(String[] values, int index) {
        return values == null || index < 0 || index >= values.length ? "" : values[index];
    }

    private static String clean(String value, int max) {
        if (value == null) return "";
        String result = value.replace((char) 10, ' ').replace((char) 13, ' ')
                .replace((char) 9, ' ').replaceAll("\\s+", " ").trim();
        return result.length() > max ? result.substring(0, max) : result;
    }

    private static String display(String value, int max) {
        String result = clean(value, max);
        return result.isEmpty() || "-".equals(result) || "null".equalsIgnoreCase(result)
                ? "Not supplied" : result;
    }

    private static String safeUnit(String value, String fallback) {
        String result = clean(value, 16);
        return result.isEmpty() ? fallback : result;
    }

    private static String temperature(String value) {
        if (value == null || !value.matches("-?[0-9]{1,4}(\\.[0-9]{1,2})?")) return "Not supplied";
        return value;
    }

    private static String nonNegative(String value) {
        if (value == null || !value.matches("[0-9]{1,6}(\\.[0-9]{1,3})?")) return "Not supplied";
        return value;
    }

    private static String probability(String value) {
        String parsed = nonNegative(value);
        if ("Not supplied".equals(parsed)) return parsed;
        try {
            double number = Double.parseDouble(parsed);
            return number <= 100 ? parsed : "Not supplied";
        } catch (NumberFormatException e) {
            return "Not supplied";
        }
    }

    private static String uv(String value) {
        String parsed = nonNegative(value);
        if ("Not supplied".equals(parsed)) return parsed;
        try {
            double number = Double.parseDouble(parsed);
            return number <= 30 ? parsed : "Not supplied";
        } catch (NumberFormatException e) {
            return "Not supplied";
        }
    }

    private static int weatherCode(String value) {
        if (value == null || !value.matches("[0-9]{1,2}")) return -1;
        try {
            int code = Integer.parseInt(value);
            return code <= 99 ? code : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String condition(int code) {
        if (code == 0) return "Clear sky";
        if (code == 1) return "Mainly clear";
        if (code == 2) return "Partly cloudy";
        if (code == 3) return "Overcast";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain showers";
        if (code == 85 || code == 86) return "Snow showers";
        if (code >= 95) return "Thunderstorm";
        return "Not supplied";
    }
}
