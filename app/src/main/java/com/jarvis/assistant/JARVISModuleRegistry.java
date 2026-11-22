package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Single catalogue for the 19 canonical JARVIS modules.
 *
 * This registry is intentionally descriptive: it does not claim that a listed
 * capability is fully implemented or device-verified. Status values distinguish
 * an existing app entry point from partial/shared foundations and planned work.
 * Feature screens continue to be owned by their existing studios.
 */
public final class JARVISModuleRegistry {
    public static final class Module {
        public final String id;
        public final String name;
        public final String status;
        public final String owner;
        public final String description;

        Module(String id, String name, String status, String owner, String description) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.owner = owner;
            this.description = description;
        }

        String dashboardLine() {
            return id + " • " + name + "\nStatus: " + status
                    + "\nOwner: " + owner + "\n" + description;
        }
    }

    private static final List<Module> MODULES = Collections.unmodifiableList(Arrays.asList(
            new Module("M01", "Owner Privacy", "CONNECTED • existing screen",
                    "OwnerPrivacyStudio", "Permission review and local data inventory."),
            new Module("M02", "Core Voice & Conversation", "CONNECTED • existing core",
                    "MainActivity / WakeWordService / JarvisAiClient",
                    "Voice input, speech output, wake-word service and AI handoff."),
            new Module("M03", "Language & Learning", "CONNECTED • existing screen",
                    "LanguageStudio", "Language and vocabulary workflows."),
            new Module("M04", "Universal App Intelligence", "CONNECTED • existing screen",
                    "AppIntelligenceStudio", "Installed-app information and supported app details."),
            new Module("M05", "Public Knowledge", "CONNECTED • existing screen",
                    "PublicKnowledgeStudio", "Public information lookup tools."),
            new Module("M06", "Live Information", "CONNECTED • existing screen",
                    "LiveInfoStudio", "Live information and external search handoffs."),
            new Module("M07", "Personal Secretary & Tasks", "CONNECTED • existing core",
                    "MainActivity", "Existing notes, structured tasks, reminders and briefing."),
            new Module("M08", "Workflow Orchestration", "CONNECTED • manual foundation",
                    "MainActivity", "Owner-run checklists; no unattended action execution."),
            new Module("M09", "Personal Memory & Knowledge", "CONNECTED • existing stores",
                    "MainActivity / MemoryExpansionStudio",
                    "Extends existing notes, conversation context and owner-created timeline."),
            new Module("M10", "AI Learning Academy", "CONNECTED • existing screen",
                    "LearningAcademyStudio", "Learning roadmaps and guided study workflows."),
            new Module("M11", "Software Development", "CONNECTED • existing screen",
                    "SoftwareDevelopmentStudio", "Architecture, debugging and code-review assistance."),
            new Module("M12", "Data Intelligence", "CONNECTED • existing screen",
                    "DataIntelligenceStudio", "Owner-provided data profiling and analysis."),
            new Module("M13", "Security Intelligence", "CONNECTED • existing entry",
                    "MainActivity", "Existing cybersecurity checks and threat-inspection tools."),
            new Module("M14", "Diagnostics & Controlled Repair", "CONNECTED • existing screen",
                    "DiagnosticsStudio", "Owner-started diagnostics and sanitized reports."),
            new Module("M15", "Display & Accessibility", "PARTIAL • shared Android UI",
                    "MainActivity / Android settings", "Existing display and accessibility handoffs; dedicated studio not confirmed."),
            new Module("M16", "Smart Environment & Accessories", "CONNECTED • existing studio",
                    "SmartHomeStudio / accessory handoff", "Compatible-device workflows; availability depends on hardware and permissions."),
            new Module("M17", "Connectivity & Device Intelligence", "CONNECTED • existing studio",
                    "ConnectivityIntelligenceStudio", "Network and device snapshot workflows; platform support varies."),
            new Module("M18", "Simulation & Engineering", "CONNECTED • existing screen",
                    "SimulationEngineeringStudio", "Simulation and engineering planning workflows."),
            new Module("M19", "Practical Intelligence", "CONNECTED • existing screen",
                    "PracticalIntelligenceStudio", "Practical guidance and owner-reviewed workflows.")
    ));

    private JARVISModuleRegistry() { }

    public static List<Module> getModules() {
        return MODULES;
    }

    public static void show(Activity activity) {
        StringBuilder report = new StringBuilder();
        report.append("UNIFIED JARVIS MODULE REGISTRY\n")
                .append("One canonical catalogue • no duplicate feature screens\n\n");
        for (Module module : MODULES) {
            report.append(module.dashboardLine()).append("\n\n");
        }
        report.append("IMPORTANT\n")
                .append("CONNECTED means an existing app entry point or shared foundation is present; ")
                .append("it does not mean every subfeature is complete or device-tested. ")
                .append("Build/CI and Poco device verification remain separate checks.");
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Unified Module Dashboard")
                .setMessage(report.toString())
                .setPositiveButton("Close", null)
                .show();
    }
}
