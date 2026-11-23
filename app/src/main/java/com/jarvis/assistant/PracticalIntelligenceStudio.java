package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** M19 owner-invoked preparedness/legal explainers and local vehicle service log. */
public final class PracticalIntelligenceStudio {
    private PracticalIntelligenceStudio() {}

    private static final String PREFS = "jarvis_preferences";
    private static final String VEHICLE_LOG_KEY = "vehicle_service_log_v1";
    private static final String ASSET_INVENTORY_KEY = "owner_asset_inventory_v1";
    private static final int MAX_RECORDS = 50;
    private static final int MAX_ASSETS = 100;

    private interface TextAction { void submit(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] options = {
                "Emergency Readiness Planner • general checklist",
                "Legal Document Explainer • selected text only",
                "Vehicle Maintenance Log • local records",
                "Smart Asset Inventory • local records"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Practical Intelligence")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) emergencyPlanner(activity, status, transcript);
                    else if (which == 1) legalExplainer(activity, status, transcript);
                    else if (which == 2) vehicleLogMenu(activity);
                    else assetInventoryMenu(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void emergencyPlanner(Activity activity, TextView status, TextView transcript) {
        String[] hazards = {"Flood", "Cyclone / severe storm", "Heatwave", "Earthquake", "General emergency"};
        new AlertDialog.Builder(activity).setTitle("Choose preparedness topic")
                .setItems(hazards, (dialog, which) -> askText(activity, "Emergency readiness brief",
                        "Optional city/district and general household needs. Do not enter an exact address or personal details.",
                        500, brief -> confirmAi(activity, status, transcript, "EMERGENCY READINESS PLAN",
                                PracticalIntelligenceRules.emergencyPrompt(hazards[which], "Only the area you chose in your brief", brief),
                                "Only the short brief you entered will be sent. This creates general preparedness guidance, not live alerts or official evacuation instructions.")))
                .setNegativeButton("Cancel", null).show();
    }

    private static void legalExplainer(Activity activity, TextView status, TextView transcript) {
        askText(activity, "Paste a document excerpt",
                "Paste only the passage you want explained (maximum 4,000 characters). Remove names, addresses, phone numbers, ID numbers, signatures and account details first.",
                4000, excerpt -> confirmAi(activity, status, transcript, "LEGAL DOCUMENT EXPLANATION",
                        PracticalIntelligenceRules.legalPrompt(excerpt),
                        "The selected text will be sent to JARVIS AI for explanation. Remove personal identifiers first. This is informational only, not legal advice." ));
    }

    private static void askText(Activity activity, String title, String hint, int max, TextAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(3);
        input.setMaxLines(8);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(input);
        new AlertDialog.Builder(activity).setTitle(title).setView(scroll)
                .setPositiveButton("Continue", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) Toast.makeText(activity, "Enter a short brief first.", Toast.LENGTH_SHORT).show();
                    else if (value.length() > max) Toast.makeText(activity,
                            "Please keep the entry under " + max + " characters.", Toast.LENGTH_LONG).show();
                    else action.submit(value);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void confirmAi(Activity activity, TextView status, TextView transcript,
                                 String title, String prompt, String disclosure) {
        new AlertDialog.Builder(activity).setTitle("Confirm AI request")
                .setMessage(disclosure + "\n\nJARVIS will not save the submitted text in its notes or recent conversation memory.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (d, w) -> {
                    status.setText(title + " • PROCESSING");
                    transcript.setText("Preparing your result…");
                    JarvisAiClient.askStandalone(activity, prompt, reply -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        status.setText(title + " • READY");
                        transcript.setText(reply);
                        new AlertDialog.Builder(activity).setTitle("JARVIS • " + title)
                                .setMessage(reply).setPositiveButton("Done", null).show();
                    });
                }).show();
    }

    private static SharedPreferences preferences(Activity activity) {
        return activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    }

    private static JSONArray readVehicleLog(Activity activity) {
        try {
            String raw = preferences(activity).getString(VEHICLE_LOG_KEY, "[]");
            return new JSONArray(raw == null ? "[]" : raw);
        } catch (Exception e) {
            Toast.makeText(activity, "Vehicle log data is invalid; existing data was left unchanged.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static void vehicleLogMenu(Activity activity) {
        String[] actions = {"Add maintenance record", "View / remove records", "Clear maintenance log"};
        new AlertDialog.Builder(activity).setTitle("Local Vehicle Maintenance Log")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) addVehicleRecord(activity);
                    else if (which == 1) viewVehicleRecords(activity);
                    else clearVehicleLog(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void addVehicleRecord(Activity activity) {
        JSONArray current = readVehicleLog(activity);
        if (current == null) return;
        if (current.length() >= MAX_RECORDS) {
            Toast.makeText(activity, "The local log is limited to " + MAX_RECORDS + " records.", Toast.LENGTH_LONG).show();
            return;
        }
        EditText vehicle = field(activity, "Vehicle nickname (no plate/VIN)");
        EditText date = field(activity, "Service date (e.g. 2026-11-22)");
        EditText odometer = field(activity, "Odometer reading (digits only, optional)");
        odometer.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        EditText summary = field(activity, "Maintenance summary");
        summary.setMinLines(2);
        summary.setMaxLines(4);
        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 8);
        form.addView(vehicle);
        form.addView(date);
        form.addView(odometer);
        form.addView(summary);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(form);
        new AlertDialog.Builder(activity).setTitle("Add service record")
                .setMessage("Store only a nickname and maintenance summary. Do not enter registration numbers, VINs, insurance details or personal information.")
                .setView(scroll)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save locally", (dialog, which) -> {
                    String v = vehicle.getText().toString().trim();
                    String d = date.getText().toString().trim();
                    String o = odometer.getText().toString().trim();
                    String s = summary.getText().toString().trim();
                    String error = PracticalIntelligenceRules.validateVehicleEntry(v, d, o, s);
                    if (error != null) {
                        Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    JSONArray latest = readVehicleLog(activity);
                    if (latest == null || latest.length() >= MAX_RECORDS) return;
                    try {
                        JSONObject item = new JSONObject();
                        item.put("vehicle", v);
                        item.put("date", d);
                        item.put("odometer", o);
                        item.put("summary", s);
                        item.put("created_at", System.currentTimeMillis());
                        latest.put(item);
                        boolean saved = preferences(activity).edit().putString(VEHICLE_LOG_KEY, latest.toString()).commit();
                        Toast.makeText(activity, saved ? "Maintenance record saved on this phone." : "Could not save record.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(activity, "Record could not be saved.", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private static EditText field(Activity activity, String hint) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setSingleLine(true);
        return input;
    }

    private static void viewVehicleRecords(Activity activity) {
        JSONArray records = readVehicleLog(activity);
        if (records == null) return;
        if (records.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Vehicle Maintenance Log")
                    .setMessage("No service records saved yet.").setPositiveButton("OK", null).show();
            return;
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item == null) labels.add((i + 1) + ". Invalid record");
            else labels.add((i + 1) + ". " + item.optString("vehicle") + " • "
                    + item.optString("date") + " • " + item.optString("summary"));
        }
        String[] choices = labels.toArray(new String[0]);
        new AlertDialog.Builder(activity).setTitle("Saved records • tap to remove")
                .setItems(choices, (dialog, which) -> new AlertDialog.Builder(activity)
                        .setTitle("Remove this maintenance record?")
                        .setMessage(choices[which]).setNegativeButton("Cancel", null)
                        .setPositiveButton("Remove", (d, w) -> removeVehicleRecord(activity, which)).show())
                .setNegativeButton("Close", null).show();
    }

    private static void removeVehicleRecord(Activity activity, int index) {
        JSONArray current = readVehicleLog(activity);
        if (current == null || index < 0 || index >= current.length()) return;
        JSONArray updated = new JSONArray();
        for (int i = 0; i < current.length(); i++) if (i != index) updated.put(current.opt(i));
        boolean saved = preferences(activity).edit().putString(VEHICLE_LOG_KEY, updated.toString()).commit();
        Toast.makeText(activity, saved ? "Maintenance record removed." : "Could not update log.", Toast.LENGTH_SHORT).show();
    }

    private static void clearVehicleLog(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Clear vehicle maintenance log?")
                .setMessage("This deletes only the locally saved maintenance records. It does not change tasks or reminders.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    boolean saved = preferences(activity).edit().remove(VEHICLE_LOG_KEY).commit();
                    Toast.makeText(activity, saved ? "Local maintenance log cleared." : "Could not clear log.", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private static JSONArray readAssetInventory(Activity activity) {
        try {
            String raw = preferences(activity).getString(ASSET_INVENTORY_KEY, "[]");
            return new JSONArray(raw == null ? "[]" : raw);
        } catch (Exception e) {
            Toast.makeText(activity, "Asset inventory data is invalid; existing data was left unchanged.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static void assetInventoryMenu(Activity activity) {
        String[] actions = {"Add item", "View / remove items", "Search inventory", "Warranty overview", "Filter inventory", "Sort inventory", "Category summary", "Condition summary", "Warranty expiry watchlist", "Export inventory", "Clear inventory"};
        new AlertDialog.Builder(activity).setTitle("Local Smart Asset Inventory")
                .setMessage("Owner-entered catalog only. Do not enter serial numbers, passwords, account details or exact home-location information. Nothing is scanned or uploaded.")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) addAsset(activity);
                    else if (which == 1) viewAssets(activity);
                    else if (which == 2) searchAssets(activity);
                    else if (which == 3) warrantyOverview(activity);
                    else if (which == 4) filterAssets(activity);
                    else if (which == 5) sortAssets(activity);
                    else if (which == 6) assetCategorySummary(activity);
                    else if (which == 7) assetConditionSummary(activity);
                    else if (which == 8) warrantyExpiryWatchlist(activity);
                    else if (which == 9) exportAssetInventory(activity);
                    else if (which == 10) clearAssetInventory(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void addAsset(Activity activity) {
        JSONArray current = readAssetInventory(activity);
        if (current == null) return;
        if (current.length() >= MAX_ASSETS) {
            Toast.makeText(activity, "The local inventory is limited to " + MAX_ASSETS + " items.", Toast.LENGTH_LONG).show();
            return;
        }
        EditText name = field(activity, "Item name / nickname");
        EditText category = field(activity, "Category (e.g. electronics, household, tools)");
        EditText purchaseDate = field(activity, "Purchase date (optional)");
        EditText condition = field(activity, "Condition (e.g. good, needs repair)");
        EditText warrantyUntil = field(activity, "Warranty end date (optional, YYYY-MM-DD)");
        EditText notes = field(activity, "Optional notes (no sensitive details)");
        notes.setSingleLine(false);
        notes.setMinLines(2);
        notes.setMaxLines(3);
        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 8);
        form.addView(name);
        form.addView(category);
        form.addView(purchaseDate);
        form.addView(condition);
        form.addView(warrantyUntil);
        form.addView(notes);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(form);
        new AlertDialog.Builder(activity).setTitle("Add inventory item")
                .setMessage("Records stay in JARVIS app-private storage on this phone. No receipt, camera or document scanning.")
                .setView(scroll).setNegativeButton("Cancel", null)
                .setPositiveButton("Save locally", (dialog, which) -> {
                    String n = name.getText().toString().trim();
                    String cat = category.getText().toString().trim();
                    String date = purchaseDate.getText().toString().trim();
                    String cond = condition.getText().toString().trim();
                    String warranty = warrantyUntil.getText().toString().trim();
                    String note = notes.getText().toString().trim();
                    String error = PracticalIntelligenceRules.validateAssetEntry(n, cat, date, cond, note, warranty);
                    if (error != null) {
                        Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    saveNewAsset(activity, n, cat, date, cond, warranty, note, false);
                }).show();
    }

    /** Re-checks the latest local records and warns before an accidental same-name/category duplicate. */
    private static void saveNewAsset(Activity activity, String name, String category,
                                     String purchaseDate, String condition, String warranty,
                                     String notes, boolean allowDuplicate) {
        JSONArray latest = readAssetInventory(activity);
        if (latest == null) return;
        if (latest.length() >= MAX_ASSETS) {
            Toast.makeText(activity, "The local inventory is limited to " + MAX_ASSETS + " items.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        boolean duplicate = false;
        for (int i = 0; i < latest.length(); i++) {
            JSONObject existing = latest.optJSONObject(i);
            if (existing != null && AssetInventoryBatchRules.isSameAsset(
                    existing.optString("name"), existing.optString("category"), name, category)) {
                duplicate = true;
                break;
            }
        }
        if (duplicate && !allowDuplicate) {
            new AlertDialog.Builder(activity).setTitle("Similar inventory item already exists")
                    .setMessage("An item with the same name and category is already saved. The existing record will not be changed. Add another record anyway?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Add anyway", (dialog, which) ->
                            saveNewAsset(activity, name, category, purchaseDate, condition,
                                    warranty, notes, true))
                    .show();
            return;
        }
        try {
            JSONObject item = new JSONObject();
            item.put("name", name);
            item.put("category", category);
            item.put("purchase_date", purchaseDate);
            item.put("condition", condition);
            item.put("warranty_until", warranty);
            item.put("notes", notes);
            item.put("created_at", System.currentTimeMillis());
            latest.put(item);
            boolean saved = preferences(activity).edit()
                    .putString(ASSET_INVENTORY_KEY, latest.toString()).commit();
            Toast.makeText(activity, saved ? "Inventory item saved on this phone." : "Could not save item.",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Item could not be saved.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void viewAssets(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        if (records.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Smart Asset Inventory")
                    .setMessage("No items saved yet.").setPositiveButton("OK", null).show();
            return;
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item == null) labels.add((i + 1) + ". Invalid item");
            else labels.add((i + 1) + ". " + item.optString("name") + " • "
                    + item.optString("category") + " • " + item.optString("condition"));
        }
        String[] choices = labels.toArray(new String[0]);
        new AlertDialog.Builder(activity).setTitle("Saved items • tap to inspect")
                .setItems(choices, (dialog, which) -> {
                    JSONObject selected = records.optJSONObject(which);
                    String warranty = selected == null ? "" : selected.optString("warranty_until", "");
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    String warrantyState = PracticalIntelligenceRules.warrantyStatus(warranty, today);
                    long warrantyDays = PracticalIntelligenceRules.warrantyDaysRemaining(warranty, today);
                    String warrantyCountdown = warrantyDays == Long.MIN_VALUE ? "Unavailable"
                            : warrantyDays < 0L ? Math.abs(warrantyDays) + " days overdue"
                            : warrantyDays + " days remaining";
                    String details = selected == null ? choices[which]
                            : choices[which] + "\nPurchase date: " + selected.optString("purchase_date")
                            + "\nWarranty end: " + (warranty.isEmpty() ? "Not recorded" : warranty)
                            + "\nWarranty status: " + warrantyState
                            + "\nWarranty countdown: " + warrantyCountdown
                            + "\nNotes: " + selected.optString("notes");
                    new AlertDialog.Builder(activity).setTitle("Inventory item")
                            .setMessage(details).setNegativeButton("Close", null)
                            .setNeutralButton("Edit", (d, w) -> editAsset(activity, which))
                            .setPositiveButton("Remove", (d, w) -> removeAsset(activity, which)).show();
                }).setNegativeButton("Close", null).show();
    }

    private static void removeAsset(Activity activity, int index) {
        JSONArray current = readAssetInventory(activity);
        if (current == null || index < 0 || index >= current.length()) return;
        JSONArray updated = new JSONArray();
        for (int i = 0; i < current.length(); i++) if (i != index) updated.put(current.opt(i));
        boolean saved = preferences(activity).edit()
                .putString(ASSET_INVENTORY_KEY, updated.toString()).commit();
        Toast.makeText(activity, saved ? "Inventory item removed." : "Could not update inventory.",
                Toast.LENGTH_SHORT).show();
    }

    private static void clearAssetInventory(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Clear asset inventory?")
                .setMessage("This deletes only the locally saved inventory items. It does not change vehicle records, tasks or reminders.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    boolean saved = preferences(activity).edit().remove(ASSET_INVENTORY_KEY).commit();
                    Toast.makeText(activity, saved ? "Local asset inventory cleared." : "Could not clear inventory.",
                            Toast.LENGTH_SHORT).show();
                }).show();
    }


    /** Owner edits one existing record in place; original creation timestamp is preserved. */
    private static void editAsset(Activity activity, int index) {
        JSONArray records=readAssetInventory(activity);
        if(records==null||index<0||index>=records.length())return;
        JSONObject existing=records.optJSONObject(index); if(existing==null)return;
        EditText name=field(activity,"Item name"), category=field(activity,"Category");
        EditText purchase=field(activity,"Purchase date (optional)"), condition=field(activity,"Condition");
        EditText warranty=field(activity,"Warranty end YYYY-MM-DD (optional)"), notes=field(activity,"Notes (optional)");
        notes.setSingleLine(false); notes.setMinLines(2); notes.setMaxLines(3);
        name.setText(existing.optString("name")); category.setText(existing.optString("category"));
        purchase.setText(existing.optString("purchase_date")); condition.setText(existing.optString("condition"));
        warranty.setText(existing.optString("warranty_until")); notes.setText(existing.optString("notes"));
        LinearLayout form=new LinearLayout(activity); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(24,8,24,8);
        form.addView(name);form.addView(category);form.addView(purchase);form.addView(condition);form.addView(warranty);form.addView(notes);
        ScrollView scroll=new ScrollView(activity);scroll.addView(form);
        new AlertDialog.Builder(activity).setTitle("Edit inventory item").setView(scroll)
          .setNegativeButton("Cancel",null).setPositiveButton("Save changes",(d,w)->{
            String n=name.getText().toString().trim(), c=category.getText().toString().trim();
            String p=purchase.getText().toString().trim(), q=condition.getText().toString().trim();
            String wt=warranty.getText().toString().trim(), nt=notes.getText().toString().trim();
            String error=AssetInventoryBatchRules.isValidEdit(n,c,p,q,nt,wt) ? null
                    : PracticalIntelligenceRules.validateAssetEntry(n,c,p,q,nt,wt);
            if(error!=null){Toast.makeText(activity,error,Toast.LENGTH_LONG).show();return;}
            JSONArray latest=readAssetInventory(activity); if(latest==null||index>=latest.length())return;
            JSONObject current=latest.optJSONObject(index); if(current==null)return;
            try {
              // Update the existing JSON object so any fields added by newer versions remain intact.
              current.put("name", n);
              current.put("category", c);
              current.put("purchase_date", p);
              current.put("condition", q);
              current.put("warranty_until", wt);
              current.put("notes", nt);
              // created_at and any unrecognized metadata are deliberately preserved.
              latest.put(index, current);
              boolean saved=preferences(activity).edit().putString(ASSET_INVENTORY_KEY,latest.toString()).commit();
              Toast.makeText(activity,saved?"Inventory item updated.":"Could not update item.",Toast.LENGTH_SHORT).show();
            } catch(Exception e){Toast.makeText(activity,"Item could not be updated.",Toast.LENGTH_SHORT).show();}
          }).show();
    }

    /** Read-only combined category and condition filter. */
    private static void filterAssets(Activity activity) {
        EditText category = field(activity, "Category filter (blank = all)");
        EditText condition = field(activity, "Condition filter (blank = all)");
        String[] warrantyOptions = {"All", "Active", "Expired", "Not recorded", "Date unavailable"};
        Spinner warrantyStatus = new Spinner(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, warrantyOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        warrantyStatus.setAdapter(adapter);

        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.addView(category);
        form.addView(condition);
        TextView warrantyLabel = new TextView(activity);
        warrantyLabel.setText("Warranty status");
        warrantyLabel.setPadding(8, 12, 8, 4);
        form.addView(warrantyLabel);
        form.addView(warrantyStatus);

        new AlertDialog.Builder(activity).setTitle("Filter inventory").setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", (dialog, which) -> {
                    JSONArray records = readAssetInventory(activity);
                    if (records == null) return;
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    String selectedStatus = String.valueOf(warrantyStatus.getSelectedItem());
                    List<String> labels = new ArrayList<>();
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject item = records.optJSONObject(i);
                        if (item == null) continue;
                        if (AssetInventoryBatchRules.matchesFilters(
                                item.optString("category"), category.getText().toString(),
                                item.optString("condition"), condition.getText().toString())
                                && AssetInventoryBatchRules.matchesWarrantyStatus(
                                        item.optString("warranty_until", ""), today, selectedStatus)) {
                            labels.add(item.optString("name") + " • " + item.optString("category")
                                    + " • " + item.optString("condition") + " • "
                                    + PracticalIntelligenceRules.warrantyStatus(
                                            item.optString("warranty_until", ""), today));
                        }
                    }
                    String result = labels.isEmpty() ? "No matching items."
                            : "Matching items • " + labels.size() + "\n"
                                    + android.text.TextUtils.join("\n", labels);
                    new AlertDialog.Builder(activity).setTitle("Filtered inventory")
                            .setMessage(result).setPositiveButton("OK", null).show();
                }).show();
    }

    /** Sorts the displayed copy only; stored inventory order is unchanged. */
    private static void sortAssets(Activity activity) {
        String[] modes = {"Name A–Z", "Category then name",
                "Condition then name", "Warranty expiry (soonest first)"};
        new AlertDialog.Builder(activity).setTitle("Sort inventory").setItems(modes, (dialog, mode) -> {
            JSONArray records = readAssetInventory(activity);
            if (records == null) return;
            List<JSONObject> items = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                JSONObject item = records.optJSONObject(i);
                if (item != null) items.add(item);
            }
            java.util.Collections.sort(items, (left, right) -> {
                if (mode == 0) return AssetInventoryBatchRules.compareNames(
                        left.optString("name"), right.optString("name"));
                if (mode == 1) return AssetInventoryBatchRules.compareCategoriesThenNames(
                        left.optString("category"), left.optString("name"),
                        right.optString("category"), right.optString("name"));
                if (mode == 2) return AssetInventoryBatchRules.compareConditionsThenNames(
                        left.optString("condition"), left.optString("name"),
                        right.optString("condition"), right.optString("name"));
                return AssetInventoryBatchRules.compareWarrantyDates(
                        left.optString("warranty_until"), right.optString("warranty_until"));
            });
            List<String> labels = new ArrayList<>();
            for (JSONObject item : items) {
                labels.add(item.optString("name") + " • " + item.optString("category")
                        + " • " + item.optString("condition") + " • "
                        + item.optString("warranty_until", "No warranty date"));
            }
            String result = labels.isEmpty() ? "No inventory items saved."
                    : android.text.TextUtils.join("\n", labels);
            new AlertDialog.Builder(activity).setTitle(modes[mode]).setMessage(result)
                    .setPositiveButton("OK", null).show();
        }).setNegativeButton("Close", null).show();
    }

    /** Owner-invoked local search over existing inventory records; nothing is uploaded. */
    private static void searchAssets(Activity activity) {
        EditText query = field(activity, "Search name, category, condition, notes, purchase or warranty date");
        new AlertDialog.Builder(activity).setTitle("Search local inventory").setView(query)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Search", (dialog, which) -> {
                    JSONArray records = readAssetInventory(activity);
                    if (records == null) return;
                    String q = query.getText().toString();
                    List<Integer> indexes = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject item = records.optJSONObject(i);
                        if (item == null) continue;
                        if (PracticalIntelligenceRules.matchesAssetQuery(
                                item.optString("name"), item.optString("category"),
                                item.optString("condition"), item.optString("notes"),
                                item.optString("purchase_date"), item.optString("warranty_until", ""), q)) {
                            indexes.add(i);
                            labels.add(item.optString("name") + " • " + item.optString("category")
                                    + " • " + item.optString("condition"));
                        }
                    }
                    if (labels.isEmpty()) {
                        new AlertDialog.Builder(activity).setTitle("Search results")
                                .setMessage("No matching inventory items.").setPositiveButton("OK", null).show();
                        return;
                    }
                    new AlertDialog.Builder(activity).setTitle("Matching items • " + labels.size())
                            .setItems(labels.toArray(new String[0]), (d, selected) -> {
                                JSONObject item = records.optJSONObject(indexes.get(selected));
                                if (item == null) return;
                                String warranty = item.optString("warranty_until", "");
                                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                                String details = item.optString("name") + "\nCategory: " + item.optString("category")
                                        + "\nCondition: " + item.optString("condition")
                                        + "\nWarranty: " + PracticalIntelligenceRules.warrantyStatus(warranty, today);
                                new AlertDialog.Builder(activity).setTitle("Inventory search result")
                                        .setMessage(details).setPositiveButton("OK", null).show();
                            }).setNegativeButton("Close", null).show();
                }).show();
    }

    /** Read-only aggregate over the existing owner-entered warranty fields. */
    private static void warrantyOverview(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        int active = 0, expired = 0, missing = 0;
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item == null) continue;
            String state = PracticalIntelligenceRules.warrantyStatus(
                    item.optString("warranty_until", ""), today);
            if ("Active".equals(state)) active++;
            else if ("Expired".equals(state)) expired++;
            else if ("Not recorded".equals(state)) missing++;
        }
        String summary = PracticalIntelligenceRules.inventorySummary(
                records.length(), active, expired, missing);
        new AlertDialog.Builder(activity).setTitle("Warranty overview")
                .setMessage(summary + "\n\nThis is a local summary of owner-entered dates, not a manufacturer's live warranty lookup.")
                .setPositiveButton("OK", null).show();
    }


    /** Read-only category counts over the existing owner-entered inventory records. */
    private static void assetCategorySummary(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item != null) categories.add(item.optString("category"));
        }
        java.util.Map<String, Integer> counts = AssetInventoryBatchRules.countByCategory(
                categories.toArray(new String[0]));
        if (counts.isEmpty()) {
            new AlertDialog.Builder(activity).setTitle("Inventory by category")
                    .setMessage("No valid inventory items are saved yet.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        StringBuilder summary = new StringBuilder("Local item counts by category\n\n");
        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            summary.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        new AlertDialog.Builder(activity).setTitle("Inventory by category")
                .setMessage(summary.toString())
                .setPositiveButton("OK", null).show();
    }

    /** Owner-invoked read-only list using the existing warranty-window rule. */
    private static void warrantyExpiryWatchlist(Activity activity) {
        String[] windows = {"Next 7 days", "Next 30 days", "Next 90 days"};
        int[] windowDays = {7, 30, 90};
        new AlertDialog.Builder(activity).setTitle("Warranty expiry window")
                .setItems(windows, (dialog, selectedWindow) -> {
                    JSONArray records = readAssetInventory(activity);
                    if (records == null) return;
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    String[] warrantyDates = new String[records.length()];
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject item = records.optJSONObject(i);
                        warrantyDates[i] = item == null ? "" : item.optString("warranty_until", "");
                    }
                    List<Integer> indexes = AssetInventoryBatchRules.warrantyIndexesWithinWindow(
                            warrantyDates, today, windowDays[selectedWindow]);
                    if (indexes.isEmpty()) {
                        new AlertDialog.Builder(activity).setTitle("Warranty expiry watchlist")
                                .setMessage("No recorded warranties expire within the next "
                                        + windowDays[selectedWindow] + " days.")
                                .setPositiveButton("OK", null).show();
                        return;
                    }
                    List<String> labels = new ArrayList<>();
                    for (Integer index : indexes) {
                        JSONObject item = records.optJSONObject(index);
                        if (item == null) continue;
                        long remaining = PracticalIntelligenceRules.warrantyDaysRemaining(
                                item.optString("warranty_until", ""), today);
                        String timing = remaining == 0 ? "Expires today"
                                : "Expires in " + remaining + " days";
                        labels.add(item.optString("name") + " • " + item.optString("category")
                                + " • " + item.optString("warranty_until") + " • " + timing);
                    }
                    String message = labels.isEmpty() ? "No valid inventory items found."
                            : "Upcoming warranty dates • " + labels.size() + "\n\n"
                                    + android.text.TextUtils.join("\n", labels)
                                    + "\n\nDates are owner-entered; confirm details with the provider.";
                    new AlertDialog.Builder(activity).setTitle("Warranty expiry watchlist")
                            .setMessage(message).setPositiveButton("OK", null).show();
                }).setNegativeButton("Cancel", null).show();
    }

    /** Read-only condition counts over the existing owner-entered inventory records. */
    private static void assetConditionSummary(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item != null) conditions.add(item.optString("condition"));
        }
        java.util.Map<String, Integer> counts = AssetInventoryBatchRules.countByCondition(
                conditions.toArray(new String[0]));
        if (counts.isEmpty()) {
            new AlertDialog.Builder(activity).setTitle("Inventory by condition")
                    .setMessage("No valid inventory items are saved yet.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        StringBuilder summary = new StringBuilder("Local item counts by condition\n\n");
        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            summary.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        new AlertDialog.Builder(activity).setTitle("Inventory by condition")
                .setMessage(summary.toString())
                .setPositiveButton("OK", null).show();
    }

    /** Owner-reviewed export from the existing inventory; notes and purchase-date labels are excluded. */
    private static void exportAssetInventory(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item == null) continue;
            rows.add(PracticalIntelligenceRules.safeAssetExportLine(
                    item.optString("name"), item.optString("category"),
                    item.optString("condition"), item.optString("warranty_until")));
        }
        if (rows.isEmpty()) {
            new AlertDialog.Builder(activity).setTitle("Export inventory")
                    .setMessage("There are no valid inventory items to export.")
                    .setPositiveButton("OK", null).show();
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("JARVIS • LOCAL ASSET INVENTORY\n");
        report.append("Items: ").append(rows.size()).append('\n');
        report.append("Name | Category | Condition | Warranty end\n");
        for (String row : rows) report.append(row).append('\n');

        TextView preview = new TextView(activity);
        preview.setText("Review before sharing. This report includes item names, categories, conditions and warranty end dates. It excludes notes, purchase-date labels and internal metadata. Choosing a sharing app may send this information outside JARVIS.\n\n"
                + report);
        preview.setTextIsSelectable(true);
        preview.setPadding(24, 16, 24, 16);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(preview);

        new AlertDialog.Builder(activity).setTitle("Review inventory export")
                .setView(scroll)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Share…", (dialog, which) -> {
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("text/plain");
                    send.putExtra(Intent.EXTRA_SUBJECT, "JARVIS asset inventory");
                    send.putExtra(Intent.EXTRA_TEXT, report.toString());
                    try {
                        activity.startActivity(Intent.createChooser(send, "Share inventory report"));
                    } catch (Exception e) {
                        Toast.makeText(activity, "No compatible sharing app is available.",
                                Toast.LENGTH_LONG).show();
                    }
                }).show();
    }


}
