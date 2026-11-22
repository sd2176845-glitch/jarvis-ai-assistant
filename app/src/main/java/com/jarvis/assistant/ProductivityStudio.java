package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Extends the existing structured task list; no duplicate tasks or project database is created. */
public final class ProductivityStudio {
    private ProductivityStudio() {}

    private static final String PREFS = "jarvis_preferences";
    private static final String TASKS_KEY = "structured_tasks_v1";
    private static final String[] PRIORITIES = {
            "Urgent + Important",
            "Important, not urgent",
            "Urgent, lower impact",
            "Low priority"
    };

    private interface IndexAction { void choose(int index); }

    public static void show(Activity activity) {
        String[] options = {
                "Project Workspace • group existing tasks",
                "Priority Matrix • classify existing tasks",
                "Weekly Review • last 7 days"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Personal Productivity Studio")
                .setItems(options, (d, which) -> {
                    if (which == 0) projectWorkspace(activity);
                    else if (which == 1) priorityMatrix(activity);
                    else weeklyReview(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static JSONArray readTasks(Activity activity) {
        String raw = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
                .getString(TASKS_KEY, "[]");
        try { return new JSONArray(raw); }
        catch (Exception e) { return null; }
    }

    private static boolean saveTasks(Activity activity, JSONArray tasks) {
        if (tasks == null) return false;
        return activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
                .edit().putString(TASKS_KEY, tasks.toString()).commit();
    }

    private static String taskLabel(JSONObject item) {
        String title = item == null ? "Untitled task" : item.optString("title", "Untitled task");
        String state = item != null && item.optBoolean("done", false) ? "✓" : "○";
        String project = item == null ? "" : item.optString("project", "");
        String priority = item == null ? "" : item.optString("priority", "");
        StringBuilder label = new StringBuilder(state).append(" ").append(title);
        if (!project.trim().isEmpty()) label.append(" • ").append(project.trim());
        if (!priority.trim().isEmpty()) label.append(" • ").append(priority);
        return label.toString();
    }

    private static void chooseTask(Activity activity, String title, IndexAction action) {
        JSONArray tasks = readTasks(activity);
        if (tasks == null) {
            showMessage(activity, title, "Task data could not be read. Existing data has been preserved.");
            return;
        }
        if (tasks.length() == 0) {
            showMessage(activity, title, "There are no structured tasks yet. Add tasks in the existing Tasks & Reminders hub first.");
            return;
        }
        String[] labels = new String[tasks.length()];
        for (int i = 0; i < tasks.length(); i++) labels[i] = taskLabel(tasks.optJSONObject(i));
        new AlertDialog.Builder(activity).setTitle(title)
                .setItems(labels, (d, which) -> action.choose(which))
                .setNegativeButton("Cancel", null).show();
    }

    private static void projectWorkspace(Activity activity) {
        JSONArray tasks = readTasks(activity);
        if (tasks == null) {
            showMessage(activity, "Project Workspace", "Task data could not be read. Nothing was changed.");
            return;
        }
        if (tasks.length() == 0) {
            showMessage(activity, "Project Workspace", "No tasks yet. Add tasks in the existing Tasks & Reminders hub, then group them here.");
            return;
        }
        Map<String, int[]> groups = new LinkedHashMap<>();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject item = tasks.optJSONObject(i);
            if (item == null) continue;
            String project = item.optString("project", "").trim();
            if (project.isEmpty()) project = "Unassigned";
            int[] counts = groups.get(project);
            if (counts == null) { counts = new int[3]; groups.put(project, counts); }
            counts[0]++;
            if (item.optBoolean("done", false)) counts[2]++;
            else counts[1]++;
        }
        StringBuilder summary = new StringBuilder("PROJECT GROUPS\n");
        for (Map.Entry<String, int[]> entry : groups.entrySet()) {
            int[] c = entry.getValue();
            summary.append("\n").append(entry.getKey())
                    .append("\n   Tasks: ").append(c[0])
                    .append(" • Pending: ").append(c[1])
                    .append(" • Done: ").append(c[2]).append("\n");
        }
        new AlertDialog.Builder(activity).setTitle("Project Workspace")
                .setMessage(summary.toString())
                .setPositiveButton("Assign task", (d, w) -> assignProject(activity))
                .setNeutralButton("View task groups", (d, w) -> viewProjectGroups(activity, groups))
                .setNegativeButton("Close", null).show();
    }

    private static void assignProject(Activity activity) {
        chooseTask(activity, "Choose a task to group", index -> {
            EditText input = new EditText(activity);
            input.setHint("Project name (blank = Unassigned)");
            input.setSingleLine(true);
            input.setPadding(24, 16, 24, 16);
            LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
            new AlertDialog.Builder(activity).setTitle("Assign project")
                    .setMessage("This updates the project label on the existing task; the task itself is not duplicated.")
                    .setView(layout)
                    .setPositiveButton("Save", (d, w) -> {
                        JSONArray tasks = readTasks(activity);
                        JSONObject item = tasks == null ? null : tasks.optJSONObject(index);
                        if (item == null) {
                            showMessage(activity, "Project Workspace", "The selected task is no longer available.");
                            return;
                        }
                        try {
                            String project = input.getText().toString().trim();
                            if (project.length() > 60) {
                                showMessage(activity, "Project Workspace", "Project name must be 60 characters or fewer.");
                                return;
                            }
                            item.put("project", project);
                            if (saveTasks(activity, tasks)) {
                                Toast.makeText(activity, project.isEmpty() ? "Task moved to Unassigned" : "Task grouped under " + project, Toast.LENGTH_SHORT).show();
                                projectWorkspace(activity);
                            } else showMessage(activity, "Project Workspace", "Could not save the project label.");
                        } catch (Exception e) {
                            showMessage(activity, "Project Workspace", "Could not update this task.");
                        }
                    }).setNegativeButton("Cancel", null).show();
        });
    }

    private static void viewProjectGroups(Activity activity, Map<String, int[]> groups) {
        if (groups.isEmpty()) {
            showMessage(activity, "Project Groups", "No project groups are available.");
            return;
        }
        String[] names = groups.keySet().toArray(new String[0]);
        new AlertDialog.Builder(activity).setTitle("Choose project group")
                .setItems(names, (d, which) -> {
                    JSONArray tasks = readTasks(activity);
                    if (tasks == null) {
                        showMessage(activity, "Project Groups", "Task data could not be read.");
                        return;
                    }
                    String selected = names[which];
                    StringBuilder out = new StringBuilder(selected).append("\n\n");
                    for (int i = 0; i < tasks.length(); i++) {
                        JSONObject item = tasks.optJSONObject(i);
                        if (item == null) continue;
                        String project = item.optString("project", "").trim();
                        if (project.isEmpty()) project = "Unassigned";
                        if (selected.equals(project)) out.append("• ").append(taskLabel(item)).append("\n");
                    }
                    showMessage(activity, "Project • " + selected, out.toString());
                }).setNegativeButton("Close", null).show();
    }

    private static void priorityMatrix(Activity activity) {
        chooseTask(activity, "Choose a task to prioritize", index -> {
            new AlertDialog.Builder(activity).setTitle("Set priority")
                    .setItems(PRIORITIES, (d, which) -> {
                        JSONArray tasks = readTasks(activity);
                        JSONObject item = tasks == null ? null : tasks.optJSONObject(index);
                        if (item == null) {
                            showMessage(activity, "Priority Matrix", "The selected task is no longer available.");
                            return;
                        }
                        try {
                            item.put("priority", PRIORITIES[which]);
                            if (saveTasks(activity, tasks)) {
                                Toast.makeText(activity, "Priority updated", Toast.LENGTH_SHORT).show();
                            } else showMessage(activity, "Priority Matrix", "Could not save priority.");
                        } catch (Exception e) {
                            showMessage(activity, "Priority Matrix", "Could not update this task.");
                        }
                    }).setNegativeButton("Cancel", null).show();
        });
    }

    private static void weeklyReview(Activity activity) {
        JSONArray tasks = readTasks(activity);
        if (tasks == null) {
            showMessage(activity, "Weekly Review", "Task data could not be read. Existing data has been preserved.");
            return;
        }
        long sevenDays = 7L * 24L * 60L * 60L * 1000L;
        long cutoff = System.currentTimeMillis() - sevenDays;
        int created = 0, pending = 0, completed = 0, unassigned = 0;
        Map<String, int[]> byProject = new LinkedHashMap<>();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject item = tasks.optJSONObject(i);
            if (item == null) continue;
            if (item.optLong("created_at", 0L) < cutoff) continue;
            created++;
            boolean done = item.optBoolean("done", false);
            if (done) completed++; else pending++;
            String project = item.optString("project", "").trim();
            if (project.isEmpty()) { project = "Unassigned"; unassigned++; }
            int[] c = byProject.get(project);
            if (c == null) { c = new int[2]; byProject.put(project, c); }
            if (done) c[1]++; else c[0]++;
        }
        StringBuilder out = new StringBuilder("LAST 7 DAYS • TASKS CREATED\n")
                .append("New tasks: ").append(created)
                .append("\nCurrently pending: ").append(pending)
                .append("\nCurrently completed: ").append(completed)
                .append("\nUnassigned: ").append(unassigned)
                .append("\n\nBY PROJECT\n");
        if (byProject.isEmpty()) out.append("No tasks were created in the last 7 days.");
        else for (Map.Entry<String, int[]> entry : byProject.entrySet()) {
            out.append(entry.getKey()).append(" • Pending ").append(entry.getValue()[0])
                    .append(" • Done ").append(entry.getValue()[1]).append("\n");
        }
        out.append("\nThis is a summary of tasks created in the last 7 days and their current status; it does not infer when a task was completed.");
        showMessage(activity, "Weekly Review", out.toString());
    }

    private static void showMessage(Activity activity, String title, String message) {
        new AlertDialog.Builder(activity).setTitle(title).setMessage(message)
                .setPositiveButton("Done", null).show();
    }
}
