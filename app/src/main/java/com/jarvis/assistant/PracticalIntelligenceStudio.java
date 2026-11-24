package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
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
    private static final int ASSET_DOCUMENT_PICK_REQUEST = 7317;
    private static int pendingDocumentIndex = -1;
    private static long pendingDocumentCreatedAt = -1L;
    private static String pendingDocumentName = "";
    private static String pendingDocumentCategory = "";

    private interface TextAction { void submit(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] options = {
                "Emergency Readiness Planner • general checklist",
                "Legal Document Explainer • selected text only",
                "Vehicle Maintenance Log • local records",
                "Smart Asset Inventory • local records",
                "Offline Emergency Preparedness Pack • works without internet"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Practical Intelligence")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) emergencyPlanner(activity, status, transcript);
                    else if (which == 1) legalExplainer(activity, status, transcript);
                    else if (which == 2) vehicleLogMenu(activity);
                    else if (which == 3) assetInventoryMenu(activity);
                    else EmergencyPreparednessPackStudio.show(activity);
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
        String[] actions = {"Add maintenance record", "View / remove records", "Clear maintenance log", "Odometer consistency check", "Service due overview"};
        new AlertDialog.Builder(activity).setTitle("Local Vehicle Maintenance Log")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) addVehicleRecord(activity);
                    else if (which == 1) viewVehicleRecords(activity);
                    else if (which == 2) clearVehicleLog(activity);
                    else if (which == 3) vehicleOdometerAudit(activity);
                    else vehicleServiceDueOverview(activity);
                }).setNegativeButton("Close", null).show();
    }

    /** Owner-invoked, read-only check of chronological odometer consistency. */
    private static void vehicleOdometerAudit(Activity activity) {
        JSONArray records = readVehicleLog(activity);
        if (records == null) return;
        String[] vehicles = new String[records.length()];
        String[] dates = new String[records.length()];
        String[] readings = new String[records.length()];
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            vehicles[i] = item == null ? "" : item.optString("vehicle", "");
            dates[i] = item == null ? "" : item.optString("date", "");
            readings[i] = item == null ? "" : item.optString("odometer", "");
        }
        int[] result = PracticalIntelligenceRules.vehicleOdometerAudit(vehicles, dates, readings);
        String message;
        if (records.length() == 0) {
            message = "No maintenance records are saved yet.";
        } else if (result[1] == 0) {
            message = "No comparable chronological odometer pairs were found. "
                    + "Add numeric readings on valid YYYY-MM-DD service dates to enable this check.";
        } else if (result[2] == 0) {
            message = "Valid readings checked: " + result[0] + "\n"
                    + "Chronological comparisons: " + result[1] + "\n"
                    + "Decreases detected: 0\n\n"
                    + "No decrease was found in the comparable records. This is not a mechanical inspection.";
        } else {
            message = "Valid readings checked: " + result[0] + "\n"
                    + "Chronological comparisons: " + result[1] + "\n"
                    + "Potential decreases: " + result[2] + "\n\n"
                    + "Review the original entries for data-entry errors. This check does not diagnose vehicle condition.";
        }
        new AlertDialog.Builder(activity).setTitle("Odometer consistency check")
                .setMessage(message).setPositiveButton("Close", null).show();
    }

    /** Read-only summary of optional owner/guardian-entered service review dates. */
    private static void vehicleServiceDueOverview(Activity activity) {
        JSONArray records = readVehicleLog(activity);
        if (records == null) return;
        String[] vehicles = new String[records.length()];
        String[] dueDates = new String[records.length()];
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            vehicles[i] = item == null ? "" : item.optString("vehicle", "");
            dueDates[i] = item == null ? "" : item.optString("next_service_due", "");
        }
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String report = PracticalIntelligenceRules.vehicleServiceDueSummary(vehicles, dueDates, today);
        new AlertDialog.Builder(activity).setTitle("Vehicle service due overview")
                .setMessage(report).setPositiveButton("Close", null).show();
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
        EditText nextServiceDue = field(activity, "Next service / review date (optional YYYY-MM-DD)");
        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 8);
        form.addView(vehicle);
        form.addView(date);
        form.addView(odometer);
        form.addView(summary);
        form.addView(nextServiceDue);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(form);
        new AlertDialog.Builder(activity).setTitle("Add service record")
                .setMessage("Store only a nickname and maintenance summary. Do not enter registration numbers, VINs, policy numbers or personal information. The optional next date is an administrative review date, not a mechanical diagnosis.")
                .setView(scroll)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save locally", (dialog, which) -> {
                    String v = vehicle.getText().toString().trim();
                    String d = date.getText().toString().trim();
                    String o = odometer.getText().toString().trim();
                    String s = summary.getText().toString().trim();
                    String due = nextServiceDue.getText().toString().trim();
                    String error = PracticalIntelligenceRules.validateVehicleEntry(v, d, o, s, due);
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
                        item.put("next_service_due", due);
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
                    + item.optString("date") + " • " + item.optString("summary")
                    + (item.optString("next_service_due", "").isEmpty() ? ""
                    : " • Next review " + item.optString("next_service_due")));
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
        String[] actions = {"Add item", "View / remove items", "Search inventory", "Warranty overview", "Filter inventory", "Sort inventory", "Category summary", "Condition summary", "Warranty expiry watchlist", "Export inventory", "Clear inventory", "Data quality check", "Purchase-age summary", "Manage receipt / manual", "Set warranty reminder", "Encrypted backup / restore"};
        new AlertDialog.Builder(activity).setTitle("Local Smart Asset Inventory")
                .setMessage("Owner-entered catalog only. Do not enter serial numbers, passwords, account details or exact home-location information. Nothing is scanned or uploaded. Receipt/manual files stay in their provider; JARVIS saves only a local reference and never uploads them.")
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
                    else if (which == 11) auditAssetInventory(activity);
                    else if (which == 12) assetPurchaseAgeSummary(activity);
                    else if (which == 13) manageAssetDocuments(activity);
                    else if (which == 14) scheduleWarrantyReminder(activity);
                    else if (which == 15) AssetInventoryArchiveStudio.show(activity);
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
                            + "\nReceipt / manual: " + (selected == null
                                    ? "Not attached" : selected.optString("document_name", "Not attached"))
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
        JSONObject removed = current.optJSONObject(index);
        JSONArray updated = new JSONArray();
        for (int i = 0; i < current.length(); i++) if (i != index) updated.put(current.opt(i));
        boolean saved = preferences(activity).edit()
                .putString(ASSET_INVENTORY_KEY, updated.toString()).commit();
        if (saved && removed != null) releaseAssetDocumentPermission(activity,
                removed.optString("document_uri", ""));
        Toast.makeText(activity, saved ? "Inventory item removed." : "Could not update inventory.",
                Toast.LENGTH_SHORT).show();
    }

    private static void clearAssetInventory(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Clear asset inventory?")
                .setMessage("This deletes only the locally saved inventory items. It does not change vehicle records, tasks or reminders.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    JSONArray oldRecords = readAssetInventory(activity);
                    boolean saved = preferences(activity).edit().remove(ASSET_INVENTORY_KEY).commit();
                    if (saved && oldRecords != null) {
                        for (int i = 0; i < oldRecords.length(); i++) {
                            JSONObject item = oldRecords.optJSONObject(i);
                            if (item != null) releaseAssetDocumentPermission(activity,
                                    item.optString("document_uri", ""));
                        }
                    }
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
        EditText purchaseFrom = field(activity, "Purchased on/after (YYYY-MM-DD, optional)");
        EditText purchaseTo = field(activity, "Purchased on/before (YYYY-MM-DD, optional)");
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
        form.addView(purchaseFrom);
        form.addView(purchaseTo);
        TextView warrantyLabel = new TextView(activity);
        warrantyLabel.setText("Warranty status");
        warrantyLabel.setPadding(8, 12, 8, 4);
        form.addView(warrantyLabel);
        form.addView(warrantyStatus);

        new AlertDialog.Builder(activity).setTitle("Filter inventory").setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", (dialog, which) -> {
                    String fromDate = purchaseFrom.getText().toString().trim();
                    String toDate = purchaseTo.getText().toString().trim();
                    if (!AssetInventoryBatchRules.isValidPurchaseDateRange(fromDate, toDate)) {
                        Toast.makeText(activity,
                                "Enter valid YYYY-MM-DD dates and make sure the start is not after the end.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
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
                                        item.optString("warranty_until", ""), today, selectedStatus)
                                && AssetInventoryBatchRules.matchesPurchaseDateRange(
                                        item.optString("purchase_date", ""), fromDate, toDate)) {
                            labels.add(item.optString("name") + " • " + item.optString("category")
                                    + " • " + item.optString("condition") + " • Purchase: "
                                    + (item.optString("purchase_date", "").isEmpty()
                                            ? "Not recorded" : item.optString("purchase_date"))
                                    + " • Warranty: "
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
                "Condition then name", "Warranty expiry (soonest first)",
                "Purchase date (newest first)"};
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
                if (mode == 3) return AssetInventoryBatchRules.compareWarrantyDates(
                        left.optString("warranty_until"), right.optString("warranty_until"));
                return AssetInventoryBatchRules.comparePurchaseDatesNewestFirst(
                        left.optString("purchase_date"), right.optString("purchase_date"));
            });
            List<String> labels = new ArrayList<>();
            for (JSONObject item : items) {
                String purchaseDate = item.optString("purchase_date", "");
                labels.add(item.optString("name") + " • " + item.optString("category")
                        + " • " + item.optString("condition")
                        + " • Purchase: " + (purchaseDate.isEmpty() ? "Not recorded" : purchaseDate)
                        + " • Warranty: " + (item.optString("warranty_until", "").isEmpty()
                                ? "Not recorded" : item.optString("warranty_until")));
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


    /** Owner-controlled receipt/manual references stored inside the selected existing inventory record. */
    private static void manageAssetDocuments(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        if (records.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Receipt / manual")
                    .setMessage("Add an inventory item first, then attach its receipt or manual.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            labels.add(item == null ? (i + 1) + ". Invalid item"
                    : (i + 1) + ". " + item.optString("name") + " • "
                            + item.optString("category") + (item.optString("document_uri").isEmpty()
                                    ? " • No document" : " • Document attached"));
        }
        new AlertDialog.Builder(activity).setTitle("Choose inventory item")
                .setMessage("Select a receipt or manual for one item. Supported files: PDF, JPEG, PNG and WebP. The original file remains in its current provider; JARVIS stores only a local content reference. Do not attach documents you are not allowed to access.")
                .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                    JSONObject item = records.optJSONObject(which);
                    if (item == null) return;
                    showAssetDocumentActions(activity, which, item);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void showAssetDocumentActions(Activity activity, int index, JSONObject item) {
        String savedName = item.optString("document_name", "");
        String savedUri = item.optString("document_uri", "");
        if (savedUri.isEmpty()) {
            new AlertDialog.Builder(activity).setTitle(item.optString("name"))
                    .setMessage("No receipt or manual is attached. Choose a PDF or image you own or are authorized to use.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Choose document", (d, w) -> chooseAssetDocument(activity, index))
                    .show();
            return;
        }
        String[] options = {"Open " + savedName, "Replace document", "Remove attachment"};
        new AlertDialog.Builder(activity).setTitle("Saved document")
                .setMessage(savedName + "\nStored as a local reference only. Opening hands this selected file to an installed viewer.")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openAssetDocument(activity, index);
                    else if (which == 1) chooseAssetDocument(activity, index);
                    else confirmRemoveAssetDocument(activity, index);
                }).setNegativeButton("Close", null).show();
    }

    private static void chooseAssetDocument(Activity activity, int index) {
        JSONArray records = readAssetInventory(activity);
        if (records == null || index < 0 || index >= records.length()) return;
        JSONObject item = records.optJSONObject(index);
        if (item == null) return;
        pendingDocumentIndex = index;
        pendingDocumentCreatedAt = item.optLong("created_at", -1L);
        pendingDocumentName = item.optString("name", "");
        pendingDocumentCategory = item.optString("category", "");

        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("*/*");
        pick.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf", "image/jpeg", "image/png", "image/webp"
        });
        pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            activity.startActivityForResult(pick, ASSET_DOCUMENT_PICK_REQUEST);
        } catch (Exception e) {
            clearPendingDocumentSelection();
            Toast.makeText(activity, "No compatible document picker is available.", Toast.LENGTH_LONG).show();
        }
    }

    /** MainActivity delegates this request code here; returns true only for our picker result. */
    public static boolean handleAssetDocumentPickerResult(Activity activity, int requestCode,
                                                           int resultCode, Intent data) {
        if (AssetInventoryArchiveStudio.handlePickerResult(activity, requestCode, resultCode, data)) return true;
        if (requestCode != ASSET_DOCUMENT_PICK_REQUEST) return false;
        int requestedIndex = pendingDocumentIndex;
        long expectedCreatedAt = pendingDocumentCreatedAt;
        String expectedName = pendingDocumentName;
        String expectedCategory = pendingDocumentCategory;
        clearPendingDocumentSelection();
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            Toast.makeText(activity, "Document selection cancelled; inventory unchanged.", Toast.LENGTH_SHORT).show();
            return true;
        }
        Uri uri = data.getData();
        String uriText = uri.toString();
        String mime;
        String displayName;
        try {
            mime = activity.getContentResolver().getType(uri);
            displayName = queryAssetDocumentName(activity, uri);
        } catch (Exception e) {
            Toast.makeText(activity, "The selected provider could not be read; nothing was attached.", Toast.LENGTH_LONG).show();
            return true;
        }
        if (mime == null || mime.trim().isEmpty()
                || "application/octet-stream".equalsIgnoreCase(mime.trim())) {
            mime = mimeFromAssetDocumentName(displayName);
        }
        displayName = AssetInventoryBatchRules.sanitizeAssetDocumentName(displayName);
        if (!AssetInventoryBatchRules.isValidAssetDocumentUri(uriText)
                || !AssetInventoryBatchRules.isSupportedAssetDocumentMimeType(mime)) {
            Toast.makeText(activity, "Unsupported document. Choose PDF, JPEG, PNG or WebP.", Toast.LENGTH_LONG).show();
            return true;
        }
        if ((data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            Toast.makeText(activity, "The provider did not grant read access; nothing was attached.", Toast.LENGTH_LONG).show();
            return true;
        }
        JSONArray beforeSave = readAssetInventory(activity);
        if (beforeSave == null) return true;
        int target = findPendingDocumentItem(beforeSave, requestedIndex, expectedCreatedAt,
                expectedName, expectedCategory);
        if (target < 0) {
            Toast.makeText(activity, "The selected inventory item changed; document was not attached.", Toast.LENGTH_LONG).show();
            return true;
        }
        boolean referenceAlreadyStored = false;
        for (int i = 0; i < beforeSave.length(); i++) {
            JSONObject other = beforeSave.optJSONObject(i);
            if (other == null) continue;
            if (uriText.equals(other.optString("document_uri", ""))) {
                if (i != target) {
                    Toast.makeText(activity, "That document is already linked to another inventory item.", Toast.LENGTH_LONG).show();
                    return true;
                }
                referenceAlreadyStored = true;
            }
        }
        try {
            // Persist read access only; JARVIS never requests write access to the selected file.
            activity.getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            Toast.makeText(activity, "This provider cannot keep a saved document reference. Choose another provider.", Toast.LENGTH_LONG).show();
            return true;
        }
        boolean saved = saveAssetDocumentReference(activity, requestedIndex, expectedCreatedAt,
                expectedName, expectedCategory, uriText, mime, displayName);
        if (!saved && !referenceAlreadyStored) releaseAssetDocumentPermission(activity, uriText);
        return true;
    }

    private static boolean saveAssetDocumentReference(Activity activity, int requestedIndex,
                                                       long expectedCreatedAt, String expectedName,
                                                       String expectedCategory, String uriText,
                                                       String mime, String displayName) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return false;
        int target = findPendingDocumentItem(records, requestedIndex, expectedCreatedAt,
                expectedName, expectedCategory);
        if (target < 0) {
            Toast.makeText(activity, "The selected inventory item changed; document was not attached.", Toast.LENGTH_LONG).show();
            return false;
        }
        for (int i = 0; i < records.length(); i++) {
            JSONObject other = records.optJSONObject(i);
            if (i != target && other != null
                    && uriText.equals(other.optString("document_uri", ""))) {
                Toast.makeText(activity, "That document is already linked to another inventory item.", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        JSONObject item = records.optJSONObject(target);
        String oldUri = item.optString("document_uri", "");
        try {
            item.put("document_uri", uriText);
            item.put("document_name", displayName);
            item.put("document_mime", mime);
            records.put(target, item);
            boolean saved = preferences(activity).edit()
                    .putString(ASSET_INVENTORY_KEY, records.toString()).commit();
            if (!saved) {
                Toast.makeText(activity, "Document reference could not be saved.", Toast.LENGTH_LONG).show();
                return false;
            }
            if (!oldUri.isEmpty() && !oldUri.equals(uriText)) {
                releaseAssetDocumentPermission(activity, oldUri);
            }
            Toast.makeText(activity, "Receipt / manual linked locally to " + expectedName + ".", Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(activity, "Document reference could not be saved.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private static int findPendingDocumentItem(JSONArray records, int requestedIndex,
                                               long expectedCreatedAt, String expectedName,
                                               String expectedCategory) {
        if (expectedCreatedAt >= 0L) {
            for (int i = 0; i < records.length(); i++) {
                JSONObject item = records.optJSONObject(i);
                if (item != null && item.optLong("created_at", -2L) == expectedCreatedAt
                        && expectedName.equals(item.optString("name", ""))
                        && expectedCategory.equals(item.optString("category", ""))) return i;
            }
            return -1;
        }
        if (requestedIndex >= 0 && requestedIndex < records.length()) {
            JSONObject item = records.optJSONObject(requestedIndex);
            if (item != null && expectedName.equals(item.optString("name", ""))
                    && expectedCategory.equals(item.optString("category", ""))) return requestedIndex;
        }
        return -1;
    }

    private static void openAssetDocument(Activity activity, int index) {
        JSONArray records = readAssetInventory(activity);
        if (records == null || index < 0 || index >= records.length()) return;
        JSONObject item = records.optJSONObject(index);
        if (item == null) return;
        String uriText = item.optString("document_uri", "");
        String mime = item.optString("document_mime", "");
        if (!AssetInventoryBatchRules.isValidAssetDocumentUri(uriText)
                || !AssetInventoryBatchRules.isSupportedAssetDocumentMimeType(mime)) {
            Toast.makeText(activity, "Saved document reference is invalid. Remove and attach it again.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent view = new Intent(Intent.ACTION_VIEW);
            view.setDataAndType(Uri.parse(uriText), mime);
            view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(Intent.createChooser(view, "Open receipt / manual"));
        } catch (Exception e) {
            Toast.makeText(activity, "No compatible viewer found, or the original document is unavailable.", Toast.LENGTH_LONG).show();
        }
    }

    private static void confirmRemoveAssetDocument(Activity activity, int index) {
        new AlertDialog.Builder(activity).setTitle("Remove receipt / manual reference?")
                .setMessage("This removes only the JARVIS link and releases its saved read access. The original file in its provider is not deleted.")
                .setNegativeButton("Keep", null)
                .setPositiveButton("Remove", (d, w) -> removeAssetDocumentReference(activity, index))
                .show();
    }

    private static void removeAssetDocumentReference(Activity activity, int index) {
        JSONArray records = readAssetInventory(activity);
        if (records == null || index < 0 || index >= records.length()) return;
        JSONObject item = records.optJSONObject(index);
        if (item == null) return;
        String uri = item.optString("document_uri", "");
        item.remove("document_uri");
        item.remove("document_name");
        item.remove("document_mime");
        try {
            records.put(index, item);
        } catch (Exception e) {
            Toast.makeText(activity, "Could not remove document reference.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean saved = preferences(activity).edit()
                .putString(ASSET_INVENTORY_KEY, records.toString()).commit();
        if (saved) releaseAssetDocumentPermission(activity, uri);
        Toast.makeText(activity, saved ? "Document reference removed; original file kept."
                : "Could not remove document reference.", Toast.LENGTH_SHORT).show();
    }

    private static String queryAssetDocumentName(Activity activity, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = activity.getContentResolver().query(uri,
                    new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (column >= 0) return cursor.getString(column);
            }
        } catch (Exception ignored) {
            // A provider may not expose a display name; fall back to the URI segment.
        } finally {
            if (cursor != null) { try { cursor.close(); } catch (Exception ignored) { } }
        }
        String fallback = uri.getLastPathSegment();
        return fallback == null ? "Selected document" : fallback;
    }

    private static String mimeFromAssetDocumentName(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "";
    }

    private static void releaseAssetDocumentPermission(Activity activity, String rawUri) {
        if (!AssetInventoryBatchRules.isValidAssetDocumentUri(rawUri)) return;
        try {
            activity.getContentResolver().releasePersistableUriPermission(
                    Uri.parse(rawUri), Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
            // Permission may already have been revoked by its provider or Android.
        }
    }

    private static void clearPendingDocumentSelection() {
        pendingDocumentIndex = -1;
        pendingDocumentCreatedAt = -1L;
        pendingDocumentName = "";
        pendingDocumentCategory = "";
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
    /** Owner-selected warranty reminder; reuses the existing ReminderStore/receiver. */
    private static void scheduleWarrantyReminder(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        List<Integer> indexes = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item == null) continue;
            String warranty = item.optString("warranty_until", "").trim();
            if (!PracticalIntelligenceRules.isValidIsoDate(warranty) || warranty.compareTo(today) < 0) continue;
            indexes.add(i);
            labels.add(item.optString("name", "Inventory item") + " • " + warranty);
        }
        if (indexes.isEmpty()) {
            new AlertDialog.Builder(activity).setTitle("Set warranty reminder")
                    .setMessage("No current or future valid warranty dates are recorded. Add a valid warranty end date first.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        new AlertDialog.Builder(activity).setTitle("Choose an item")
                .setItems(labels.toArray(new String[0]), (dialog, selected) -> {
                    int index = indexes.get(selected);
                    JSONObject item = records.optJSONObject(index);
                    if (item == null) return;
                    showWarrantyReminderDateTime(activity, item.optString("name", "Inventory item"), item.optString("warranty_until", ""));
                }).setNegativeButton("Cancel", null).show();
    }

    private static void showWarrantyReminderDateTime(Activity activity, String assetName, String warrantyUntil) {
        java.util.Calendar initial = java.util.Calendar.getInstance();
        try {
            int year = Integer.parseInt(warrantyUntil.substring(0, 4));
            int month = Integer.parseInt(warrantyUntil.substring(5, 7)) - 1;
            int day = Integer.parseInt(warrantyUntil.substring(8, 10));
            initial.set(year, month, day, 9, 0, 0);
            initial.set(java.util.Calendar.MILLISECOND, 0);
            if (initial.getTimeInMillis() - System.currentTimeMillis() > 7L * 24L * 60L * 60L * 1000L) {
                initial.add(java.util.Calendar.DAY_OF_MONTH, -7);
            }
            if (initial.get(java.util.Calendar.YEAR) < java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    || initial.getTimeInMillis() < System.currentTimeMillis() - 24L * 60L * 60L * 1000L) {
                initial = java.util.Calendar.getInstance();
            }
        } catch (Exception invalidDate) {
            initial.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        new android.app.DatePickerDialog(activity, (dateView, year, month, day) -> {
            new android.app.TimePickerDialog(activity, (timeView, hour, minute) -> {
                java.util.Calendar trigger = java.util.Calendar.getInstance();
                trigger.set(year, month, day, hour, minute, 0);
                trigger.set(java.util.Calendar.MILLISECOND, 0);
                long triggerAt = trigger.getTimeInMillis();
                String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                if (selectedDate.compareTo(warrantyUntil) > 0) {
                    Toast.makeText(activity, "Choose a reminder date on or before the warranty end date.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!ReminderRules.isFuture(triggerAt, System.currentTimeMillis())) {
                    Toast.makeText(activity, "Choose a future date and time.", Toast.LENGTH_LONG).show();
                    return;
                }
                android.app.NotificationManager notifications =
                        (android.app.NotificationManager) activity.getSystemService(Activity.NOTIFICATION_SERVICE);
                if (notifications == null || (android.os.Build.VERSION.SDK_INT >= 24 && !notifications.areNotificationsEnabled())) {
                    new AlertDialog.Builder(activity).setTitle("Enable JARVIS notifications")
                            .setMessage("Android notifications are currently disabled for JARVIS. Enable them in system settings, then create the reminder again.")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Open settings", (d, w) -> {
                                try {
                                    Intent settings;
                                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                                        settings = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                        settings.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
                                    } else {
                                        settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.parse("package:" + activity.getPackageName()));
                                    }
                                    activity.startActivity(settings);
                                } catch (Exception ignored) {
                                    Toast.makeText(activity, "Could not open notification settings.", Toast.LENGTH_LONG).show();
                                }
                            }).show();
                    return;
                }
                String title = AssetInventoryBatchRules.warrantyReminderTitle(assetName);
                int reminderId = (int) (System.currentTimeMillis() % 1900000000L) + 100000000;
                if (title.isEmpty() || !ReminderStore.save(activity, reminderId, title, triggerAt)) {
                    Toast.makeText(activity, "Could not save the warranty reminder.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!ReminderStore.schedule(activity, reminderId, title, triggerAt)) {
                    ReminderStore.remove(activity, reminderId);
                    Toast.makeText(activity, "Could not schedule the warranty reminder; saved reminder was rolled back.", Toast.LENGTH_LONG).show();
                    return;
                }
                new AlertDialog.Builder(activity).setTitle("Warranty reminder scheduled")
                        .setMessage(title + "\\n" + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                .format(new Date(triggerAt))
                                + "\\n\nUses the existing JARVIS reminder notification and snooze controls.")
                        .setPositiveButton("Done", null).show();
            }, 9, 0, false).show();
        }, initial.get(java.util.Calendar.YEAR), initial.get(java.util.Calendar.MONTH),
                initial.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

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

    /** Read-only age distribution over optional owner-entered purchase dates. */
    private static void assetPurchaseAgeSummary(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        String[] dates = new String[records.length()];
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            dates[i] = item == null ? "" : item.optString("purchase_date", "");
        }
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        java.util.Map<String, Integer> counts =
                AssetInventoryBatchRules.purchaseAgeSummary(dates, today);
        StringBuilder report = new StringBuilder("Records checked: ").append(records.length());
        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            report.append("\\n").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        report.append("\\n\\nBased only on purchase dates entered by you. "
                + "Missing, malformed and future dates are shown separately. "
                + "This summary does not estimate resale value or remaining useful life.");
        new AlertDialog.Builder(activity).setTitle("Purchase-age summary")
                .setMessage(report.toString()).setPositiveButton("OK", null).show();
    }

    /** Owner-invoked, read-only audit; never changes the stored inventory. */
    private static void auditAssetInventory(Activity activity) {
        JSONArray records = readAssetInventory(activity);
        if (records == null) return;
        String[] names = new String[records.length()];
        String[] categories = new String[records.length()];
        String[] conditions = new String[records.length()];
        String[] warranties = new String[records.length()];
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            names[i] = item == null ? "" : item.optString("name", "");
            categories[i] = item == null ? "" : item.optString("category", "");
            conditions[i] = item == null ? "" : item.optString("condition", "");
            warranties[i] = item == null ? "" : item.optString("warranty_until", "");
        }
        List<String> issues = AssetInventoryBatchRules.inventoryDataQualityIssues(
                names, categories, conditions, warranties);
        String message;
        if (records.length() == 0) {
            message = "No inventory records are saved yet.";
        } else if (issues.isEmpty()) {
            message = "No missing required fields or malformed warranty dates were found. "
                    + "Optional purchase dates and notes are not evaluated.";
        } else if (issues.size() == 1 && issues.get(0).startsWith("Audit unavailable:")) {
            message = issues.get(0);
        } else {
            message = "Records checked: " + records.length() + "\n"
                    + "Records needing review: " + issues.size() + "\n\n"
                    + android.text.TextUtils.join("\n", issues)
                    + "\n\nThis report is read-only. Review and edit records individually if needed.";
        }
        TextView report = new TextView(activity);
        report.setText(message);
        report.setTextIsSelectable(true);
        report.setPadding(24, 16, 24, 16);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(report);
        new AlertDialog.Builder(activity).setTitle("Inventory data quality check")
                .setView(scroll).setPositiveButton("Close", null).show();
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
