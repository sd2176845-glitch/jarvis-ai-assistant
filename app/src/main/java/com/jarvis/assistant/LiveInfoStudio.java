package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;

/** Owner-invoked weather, reference currency and public-holiday lookups. */
public final class LiveInfoStudio {
    private LiveInfoStudio() {}

    private interface Callback { void done(String result); }

    public static void show(Activity activity) {
        String[] options = {
                "Weather • current conditions by city",
                "Currency Converter • reference exchange rate",
                "India Public Holidays • national / listed state"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Live Information Studio")
                .setItems(options, (d, which) -> {
                    if (which == 0) weatherInput(activity);
                    else if (which == 1) currencyInput(activity);
                    else holidaysInput(activity);
                }).setNegativeButton("Close", null).show();
    }

    private interface PlaceAction {
        void selected(String placeName, String latitude, String longitude);
    }

    /** Shared city picker for weather and environmental lookups; never reads device location. */
    private static void chooseCity(Activity activity, String title, String disclosure, PlaceAction action) {
        showInput(activity, title, "Enter a city or town (no GPS required)", value -> {
            if (value.length() > 80 || value.contains("://") || value.contains("@")
                    || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                Toast.makeText(activity, "Enter a city or town name up to 80 characters, not an address or URL.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            String url = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + encode(value) + "&count=5&language=en&format=json";
            confirmAndRun(activity, "City lookup", disclosure, () -> get(url, body -> {
                try {
                    JSONObject root = new JSONObject(body);
                    JSONArray results = root.optJSONArray("results");
                    if (results == null || results.length() == 0) {
                        showResult(activity, "City lookup", "No matching city was found. Try adding a state or country.");
                        return;
                    }
                    int count = Math.min(results.length(), 5);
                    String[] labels = new String[count];
                    for (int i = 0; i < count; i++) {
                        JSONObject place = results.getJSONObject(i);
                        labels[i] = place.optString("name", "Place")
                                + (place.optString("admin1").isEmpty() ? "" : ", " + place.optString("admin1"))
                                + (place.optString("country").isEmpty() ? "" : ", " + place.optString("country"));
                    }
                    activity.runOnUiThread(() -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        new AlertDialog.Builder(activity).setTitle("Choose matching place")
                                .setItems(labels, (dialog, index) -> {
                                    try {
                                        JSONObject selected = results.getJSONObject(index);
                                        action.selected(labels[index], selected.getString("latitude"),
                                                selected.getString("longitude"));
                                    } catch (Exception e) {
                                        showResult(activity, "City lookup", "The selected location data was incomplete.");
                                    }
                                }).setNegativeButton("Cancel", null).show();
                    });
                } catch (Exception e) {
                    showResult(activity, "City lookup", "The geocoding service returned an unexpected response.");
                }
            }));
        });
    }

    private static void weatherInput(Activity activity) {
        chooseCity(activity, "Weather • City Search",
                "Only the city name you enter will be sent to Open-Meteo to find matching places. "
                        + "After you choose one, its coordinates are sent for the weather result. "
                        + "JARVIS will not read GPS or request location permission.",
                (placeName, latitude, longitude) -> {
                    String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude="
                            + encode(latitude) + "&longitude=" + encode(longitude)
                            + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m"
                            + "&timezone=auto";
                    get(weatherUrl, data -> showResult(activity, "Weather • " + placeName,
                            parseWeather(data, placeName)));
                });
    }

    /** Owner-invoked 24-hour weather watch; reuses the existing city picker and network helper. */
    public static void showWeatherHazardOutlook(Activity activity) {
        chooseCity(activity, "24-hour Weather Watch • City Search",
                "Only the city name you enter is used to find matching places. After you select one, its approximate "
                        + "coordinates are sent to Open-Meteo for hourly forecast estimates. No GPS, home address, "
                        + "background monitoring or automatic alerts are used. This is a planning aid, not an official warning.",
                (placeName, latitude, longitude) -> {
                    String forecastUrl = "https://api.open-meteo.com/v1/forecast?latitude="
                            + encode(latitude) + "&longitude=" + encode(longitude)
                            + "&hourly=weather_code,precipitation_probability,precipitation,wind_speed_10m,"
                            + "wind_gusts_10m,apparent_temperature,visibility&forecast_hours=24&timezone=auto"
                            + "&wind_speed_unit=kmh";
                    get(forecastUrl, data -> showResult(activity,
                            "24-hour Weather Watch • " + placeName,
                            parseWeatherHazardOutlook(data, placeName)));
                });
    }

    private static String parseWeatherHazardOutlook(String body, String placeName) {
        try {
            JSONObject root = new JSONObject(body);
            JSONObject hourly = root.optJSONObject("hourly");
            if (hourly == null) return "The provider did not include an hourly weather outlook.";
            JSONArray timeValues = hourly.optJSONArray("time");
            if (timeValues == null || timeValues.length() == 0) {
                return "No hourly weather estimates were returned for this location.";
            }
            int count = Math.min(24, timeValues.length());
            String[] times = new String[count];
            for (int i = 0; i < count; i++) times[i] = timeValues.optString(i, "");
            return EnvironmentIntelligenceRules.formatWeatherHazardOutlook(
                    placeName, times,
                    weatherValues(hourly, "weather_code", count),
                    weatherValues(hourly, "precipitation_probability", count),
                    weatherValues(hourly, "precipitation", count),
                    weatherValues(hourly, "wind_speed_10m", count),
                    weatherValues(hourly, "wind_gusts_10m", count),
                    weatherValues(hourly, "apparent_temperature", count),
                    weatherValues(hourly, "visibility", count),
                    root.optString("timezone", ""));
        } catch (Exception e) {
            return "The weather provider returned an unexpected response. No location or forecast history was saved.";
        }
    }

    private static String[] weatherValues(JSONObject hourly, String key, int count) {
        String[] result = new String[count];
        JSONArray values = hourly.optJSONArray(key);
        for (int i = 0; i < count; i++) {
            result[i] = values == null ? "" : values.optString(i, "");
        }
        return result;
    }

    /** Current modelled air-quality lookup; distinct from manual CPCB station observations. */
    public static void showAirQuality(Activity activity) {
        chooseCity(activity, "Live Air Quality • City Search",
                "Only the city name you enter is sent to Open-Meteo for matching. After you select a place, "
                        + "its approximate coordinates are sent to the Open-Meteo Air Quality API. "
                        + "No GPS, home address or background location is used. The result is modelled data, not a CPCB station reading.",
                (placeName, latitude, longitude) -> {
                    String airUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude="
                            + encode(latitude) + "&longitude=" + encode(longitude)
                            + "&current=us_aqi,european_aqi,pm2_5,pm10,ozone,nitrogen_dioxide,sulphur_dioxide,uv_index"
                            + "&timezone=auto";
                    get(airUrl, data -> showResult(activity, "Environmental Intelligence • " + placeName,
                            parseAirQuality(data, placeName)));
                });
    }

    /** Owner-invoked next-24-hour model forecast; no background polling or saved readings. */
    public static void showAirQualityForecast(Activity activity) {
        chooseCity(activity, "24-hour Air Quality Forecast • City Search",
                "Only the city name you enter is sent for matching. After you select a place, its approximate coordinates "
                        + "are sent to Open-Meteo for the next 24 hourly model estimates. No GPS, home address or background "
                        + "location is used. These estimates are not CPCB station readings.",
                (placeName, latitude, longitude) -> {
                    String forecastUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude="
                            + encode(latitude) + "&longitude=" + encode(longitude)
                            + "&hourly=us_aqi,european_aqi,pm2_5,pm10&forecast_hours=24&timezone=auto";
                    get(forecastUrl, data -> showResult(activity,
                            "24-hour Air Quality Forecast • " + placeName,
                            parseAirQualityForecast(data, placeName)));
                });
    }

    /** Owner-invoked UV forecast; shares the existing city picker and network helper. */
    public static void showUvForecast(Activity activity) {
        chooseCity(activity, "24-hour UV Index Forecast • City Search",
                "Only the city name you enter is sent for matching. After you select a place, its approximate coordinates "
                        + "are sent to Open-Meteo for up to 24 hourly UV-index estimates. No GPS, home address, background "
                        + "location or automatic history is used. These are model forecasts, not ground measurements.",
                (placeName, latitude, longitude) -> {
                    String uvUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude="
                            + encode(latitude) + "&longitude=" + encode(longitude)
                            + "&hourly=uv_index,uv_index_clear_sky&forecast_hours=24&timezone=auto";
                    get(uvUrl, data -> showResult(activity, "24-hour UV Forecast • " + placeName,
                            parseUvForecast(data, placeName)));
                });
    }

    private static String parseUvForecast(String body, String placeName) {
        try {
            JSONObject root = new JSONObject(body);
            JSONObject hourly = root.optJSONObject("hourly");
            if (hourly == null) return "The provider did not include an hourly UV forecast.";
            JSONArray timeValues = hourly.optJSONArray("time");
            if (timeValues == null || timeValues.length() == 0) {
                return "No hourly UV forecast values were returned for this location.";
            }
            int count = Math.min(24, timeValues.length());
            String[] times = new String[count];
            String[] uv = new String[count];
            String[] clearSky = new String[count];
            JSONArray uvValues = hourly.optJSONArray("uv_index");
            JSONArray clearSkyValues = hourly.optJSONArray("uv_index_clear_sky");
            for (int i = 0; i < count; i++) {
                times[i] = timeValues.optString(i, "");
                uv[i] = uvValues == null ? "" : uvValues.optString(i, "");
                clearSky[i] = clearSkyValues == null ? "" : clearSkyValues.optString(i, "");
            }
            return EnvironmentIntelligenceRules.formatUvForecast(
                    placeName, times, uv, clearSky, root.optString("timezone", ""));
        } catch (Exception e) {
            return "The UV forecast provider returned an unexpected response. No history was saved.";
        }
    }

    private static String parseAirQualityForecast(String body, String placeName) {
        try {
            JSONObject root = new JSONObject(body);
            JSONObject hourly = root.optJSONObject("hourly");
            if (hourly == null) return "The provider did not include an hourly air-quality forecast.";
            JSONArray timeValues = hourly.optJSONArray("time");
            if (timeValues == null || timeValues.length() == 0) {
                return "No hourly forecast values were returned for this location.";
            }
            int count = Math.min(24, timeValues.length());
            String[] times = new String[count];
            String[] usAqi = new String[count];
            String[] europeanAqi = new String[count];
            String[] pm25 = new String[count];
            String[] pm10 = new String[count];
            JSONArray usValues = hourly.optJSONArray("us_aqi");
            JSONArray euValues = hourly.optJSONArray("european_aqi");
            JSONArray pm25Values = hourly.optJSONArray("pm2_5");
            JSONArray pm10Values = hourly.optJSONArray("pm10");
            for (int i = 0; i < count; i++) {
                times[i] = timeValues.optString(i, "");
                usAqi[i] = usValues == null ? "" : usValues.optString(i, "");
                europeanAqi[i] = euValues == null ? "" : euValues.optString(i, "");
                pm25[i] = pm25Values == null ? "" : pm25Values.optString(i, "");
                pm10[i] = pm10Values == null ? "" : pm10Values.optString(i, "");
            }
            return EnvironmentIntelligenceRules.formatAirQualityForecast(
                    placeName, times, usAqi, europeanAqi, pm25, pm10,
                    root.optString("timezone", ""));
        } catch (Exception e) {
            return "The air-quality forecast provider returned an unexpected response. No observation was saved.";
        }
    }

    private static String parseAirQuality(String body, String placeName) {
        try {
            JSONObject root = new JSONObject(body);
            JSONObject current = root.optJSONObject("current");
            if (current == null) return "The provider did not include current model values for this location.";
            return EnvironmentIntelligenceRules.formatLiveAirQuality(
                    placeName,
                    current.optInt("us_aqi", -1),
                    current.optInt("european_aqi", -1),
                    current.optString("pm2_5", ""),
                    current.optString("pm10", ""),
                    current.optString("ozone", ""),
                    current.optString("nitrogen_dioxide", ""),
                    current.optString("sulphur_dioxide", ""),
                    current.optString("uv_index", ""),
                    current.optString("time", ""),
                    root.optString("timezone", ""));
        } catch (Exception e) {
            return "The air-quality provider returned an unexpected response. No observation was saved.";
        }
    }

    private static String parseWeather(String body, String place) {
        try {
            JSONObject root = new JSONObject(body);
            JSONObject current = root.optJSONObject("current");
            JSONObject units = root.optJSONObject("current_units");
            if (current == null) return "Current weather data was not included in the provider response.";
            int code = current.optInt("weather_code", -1);
            return "LOCATION\n" + place
                    + "\n\nCONDITIONS\n" + weatherDescription(code)
                    + "\nTemperature: " + current.optString("temperature_2m", "—") + " " + unit(units, "temperature_2m", "°C")
                    + "\nFeels like: " + current.optString("apparent_temperature", "—") + " " + unit(units, "apparent_temperature", "°C")
                    + "\nHumidity: " + current.optString("relative_humidity_2m", "—") + " " + unit(units, "relative_humidity_2m", "%")
                    + "\nWind: " + current.optString("wind_speed_10m", "—") + " " + unit(units, "wind_speed_10m", "km/h")
                    + "\nPrecipitation: " + current.optString("precipitation", "—") + " " + unit(units, "precipitation", "mm")
                    + "\nDaylight: " + (current.optInt("is_day", 0) == 1 ? "Day" : "Night")
                    + "\nLocal data time: " + current.optString("time", "Not supplied")
                    + "\nTime zone: " + root.optString("timezone", "Not supplied")
                    + "\n\nSource: Open-Meteo • Model-based weather data; conditions can change.";
        } catch (Exception e) {
            return "The weather service returned an unexpected response.";
        }
    }

    private static String unit(JSONObject units, String key, String fallback) {
        return units == null ? fallback : units.optString(key, fallback);
    }

    private static String weatherDescription(int code) {
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
        return "Condition code " + code;
    }

    private static void currencyInput(Activity activity) {
        showInput(activity, "Currency Converter",
                "Enter like: 100 USD INR", value -> {
                    String[] parts = value.trim().toUpperCase(Locale.ROOT).split("\\s+");
                    if (parts.length != 3) {
                        Toast.makeText(activity, "Format: amount FROM TO (example: 100 USD INR)", Toast.LENGTH_LONG).show();
                        return;
                    }
                    double amount;
                    try { amount = Double.parseDouble(parts[0]); }
                    catch (Exception e) { amount = -1; }
                    if (!(amount > 0) || amount > 1000000000000d
                            || !parts[1].matches("[A-Z]{3}") || !parts[2].matches("[A-Z]{3}")) {
                        Toast.makeText(activity, "Enter a positive amount and two 3-letter currency codes.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    final double valueAmount = amount;
                    final String from = parts[1], to = parts[2];
                    String url = "https://api.frankfurter.dev/v2/rates?base=" + from + "&quotes=" + to;
                    confirmAndRun(activity, "Currency conversion",
                            "Only the amount and currency codes you entered will be sent to Frankfurter. Rates are reference rates, not a bank quote or live trading price.",
                            () -> get(url, body -> {
                                try {
                                    JSONArray rates = new JSONArray(body);
                                    if (rates.length() == 0) {
                                        showResult(activity, "Currency Converter", "No rate was returned for " + from + " to " + to + ". Check the currency codes.");
                                        return;
                                    }
                                    JSONObject item = rates.getJSONObject(0);
                                    double rate = item.getDouble("rate");
                                    String result = String.format(Locale.US, "%.2f %s = %.2f %s",
                                            valueAmount, from, valueAmount * rate, to);
                                    showResult(activity, "Currency Converter",
                                            result + "\n\nRate: 1 " + from + " = " + rate + " " + to
                                                    + "\nRate date: " + item.optString("date", "Not supplied")
                                                    + "\nSource: Frankfurter • Central-bank reference rates. Bank spreads and fees may differ.");
                                } catch (Exception e) {
                                    showResult(activity, "Currency Converter", "The rate provider returned an unexpected response.");
                                }
                            }));
                });
    }

    private static void holidaysInput(Activity activity) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        showInput(activity, "India Public Holidays",
                "Enter year (example: " + currentYear + ")", value -> {
                    if (!value.matches("20[0-9]{2}")) {
                        Toast.makeText(activity, "Enter a four-digit year from 2000 to 2099.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String url = "https://nagerholidays.com/api/v4/Holidays/IN/" + value;
                    confirmAndRun(activity, "India public holidays",
                            "JARVIS will request the public-holiday dataset for India and the year you entered. The provider may not include every state, district or local observance.",
                            () -> get(url, body -> showResult(activity, "India Public Holidays • " + value,
                                    parseHolidays(body))));
                });
    }

    private static String parseHolidays(String body) {
        try {
            JSONArray holidays = new JSONArray(body);
            if (holidays.length() == 0) return "The provider returned no holiday entries for this year.";
            StringBuilder out = new StringBuilder();
            int limit = Math.min(holidays.length(), 40);
            for (int i = 0; i < limit; i++) {
                JSONObject h = holidays.optJSONObject(i);
                if (h == null) continue;
                out.append(h.optString("date", "Date not supplied"))
                        .append(" • ").append(h.optString("name", "Holiday"))
                        .append("\n");
                JSONArray types = h.optJSONArray("holidayTypes");
                if (types != null && types.length() > 0) out.append("Type: ").append(join(types)).append("\n");
                if (h.optBoolean("nationalHoliday", false)) out.append("Applies nationally");
                else {
                    JSONArray regions = h.optJSONArray("subdivisionCodes");
                    out.append(regions == null || regions.length() == 0
                            ? "Regional scope not specified" : "Listed regions: " + join(regions));
                }
                out.append("\n\n");
            }
            if (holidays.length() > limit) out.append("Showing first ").append(limit).append(" of ").append(holidays.length()).append(" entries.\n\n");
            out.append("Source: Nager.Date v4 • Dataset coverage varies. This is not a complete official state/district holiday calendar; check relevant government notifications.");
            return out.toString();
        } catch (Exception e) {
            return "The holiday provider returned an unexpected response.";
        }
    }

    private static String join(JSONArray values) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length(); i++) {
            if (i > 0) out.append(", ");
            out.append(values.optString(i, ""));
        }
        return out.toString();
    }

    private interface InputAction { void submit(String value); }

    private static void showInput(Activity activity, String title, String hint, InputAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(title).setView(layout)
                .setPositiveButton("Continue", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) Toast.makeText(activity, "Enter a value first.", Toast.LENGTH_SHORT).show();
                    else action.submit(value);
                }).setNegativeButton("Cancel", null).show();
    }

    private interface RunnableAction { void run(); }

    private static void confirmAndRun(Activity activity, String title, String message, RunnableAction action) {
        new AlertDialog.Builder(activity).setTitle(title).setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (d, w) -> action.run()).show();
    }

    private static void get(String address, Callback callback) {
        Thread worker = new Thread(() -> {
            HttpURLConnection connection = null;
            String result;
            try {
                connection = (HttpURLConnection) new URL(address).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Accept", "application/json");
                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder body = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null && body.length() < 500000) body.append(line);
                    }
                }
                if (status < 200 || status >= 300) {
                    result = status == 404 ? "No matching public record was found."
                            : "The provider returned HTTP " + status + ". Please try again later.";
                } else result = body.toString();
            } catch (Exception e) {
                result = "The live information provider is unavailable. Check the internet connection and try again.";
            } finally {
                if (connection != null) connection.disconnect();
            }
            callback.done(result);
        }, "jarvis-live-info");
        worker.start();
    }

    private static void showResult(Activity activity, String title, String message) {
        activity.runOnUiThread(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            new AlertDialog.Builder(activity).setTitle(title).setMessage(message)
                    .setPositiveButton("Done", null).show();
        });
    }

    private static String encode(String value) {
        try { return URLEncoder.encode(value, "UTF-8").replace("+", "%20"); }
        catch (Exception e) { return value; }
    }
}
