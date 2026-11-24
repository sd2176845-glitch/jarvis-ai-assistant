package com.jarvis.assistant;

public final class EmergencyPreparednessPackRules {
    private EmergencyPreparednessPackRules() { }
    public static final String SACHET_URL = "https://sachet.ndma.gov.in/";
    public static final String NDMA_DOS_DONTS_URL = "https://sachet.ndma.gov.in/DosDont";
    public static final String IMD_URL = "https://mausam.imd.gov.in/";
    public static final String MHA_112_URL = "https://www.mha.gov.in/emergency-response-support-system-erss";
    public static final String SOURCE_REVIEW_DATE = "24 November 2026";

    public static String[] topics() {
        return new String[]{"General emergency", "Flood", "Cyclone / severe storm", "Earthquake", "Heatwave"};
    }

    public static String buildPack(String topic) {
        if (topic == null) return unsupported();
        String key = topic.trim().toLowerCase(java.util.Locale.ROOT);
        StringBuilder out = new StringBuilder();
        out.append("OFFLINE EMERGENCY PREPAREDNESS GUIDE\n");
        out.append("TOPIC: ").append(topic.trim()).append("\n\n");
        out.append("IMPORTANT\nThis is a saved, general preparedness reference, not an official warning, not a live forecast, evacuation order or substitute for local emergency instructions. Stay with a parent, guardian or trusted adult and follow local authorities.\n\n");
        out.append("EMERGENCY HELP IN INDIA\nFor an immediate emergency, call 112 — the nationwide Emergency Response Support System number.\n\n");

        if (key.equals("general emergency") || key.equals("general")) {
            out.append("PREPARE WITH YOUR HOUSEHOLD\n")
               .append("• With a parent/guardian, agree on a family meeting place and a trusted contact outside the immediate area.\n")
               .append("• Keep a basic kit together: drinking water, ready-to-eat food, torch and spare batteries, battery radio or charged power bank, basic first-aid supplies, routine medicines managed by an adult, hygiene items and copies of important documents in a waterproof pouch.\n")
               .append("• Keep essential phone numbers written on paper as well as saved in a phone.\n")
               .append("• Know the official shelter or safe location identified by local authorities.\n\n")
               .append("DURING AN EMERGENCY\n")
               .append("• Stay with your household or trusted adult, listen to official announcements and use the safest route advised by local authorities.\n")
               .append("• Do not spread or rely on unverified messages. Do not approach damaged buildings, floodwater or fallen electrical wires.\n\n")
               .append("AFTER\n")
               .append("• Return only when authorities say the area is safe. Use drinking water and food confirmed safe by local health authorities.\n");
        } else if (key.equals("flood") || key.equals("floods")) {
            out.append("BEFORE / WHEN A WARNING IS ISSUED\n")
               .append("• Follow official weather and disaster alerts. With a trusted adult, know the nearest designated safe shelter, higher ground and safe evacuation route.\n")
               .append("• Keep the emergency kit and important documents in waterproof protection; keep phones charged.\n")
               .append("• If flash flooding is possible or authorities advise evacuation, move to higher ground promptly with your household. Do not wait near rising water.\n\n")
               .append("DURING\n")
               .append("• Stay away from rivers, drains, underpasses, submerged roads and fast-moving water. Never walk, swim or travel through moving or unknown floodwater.\n")
               .append("• Keep children away from floodwater. Never touch electrical equipment or wires while wet or standing in water; stay clear of fallen power lines.\n")
               .append("• Follow evacuation and shelter instructions from local authorities.\n\n")
               .append("AFTER\n")
               .append("• Return only after authorities declare the area safe. Do not use electrical equipment or enter damaged buildings until checked by qualified people.\n")
               .append("• Do not drink floodwater or eat food that contacted floodwater; follow local drinking-water advice.\n");
        } else if (key.equals("cyclone") || key.equals("cyclone / severe storm")
                || key.equals("severe storm") || key.equals("cyclone/severe storm")) {
            out.append("BEFORE / WHEN A WARNING IS ISSUED\n")
               .append("• Follow official NDMA/SACHET and IMD bulletins; keep phones charged and a battery radio available.\n")
               .append("• With a trusted adult, identify the designated cyclone shelter and safe route. Evacuate when officials direct, preferably before conditions worsen.\n")
               .append("• Protect important documents in a waterproof pouch and keep the household emergency kit ready.\n\n")
               .append("DURING\n")
               .append("• If indoors, stay inside and away from windows. Keep listening to official updates.\n")
               .append("• A temporary lull can occur during a cyclone. Do not go outside or assume danger has passed until the official all-clear.\n")
               .append("• If officials direct evacuation, go to the designated safe shelter with your household. Stay away from the coast, trees, poles and fallen wires.\n\n")
               .append("AFTER\n")
               .append("• Do not return until authorities advise it is safe. Avoid damaged buildings, loose electrical wires and unsafe water.\n");
        } else if (key.equals("earthquake") || key.equals("earthquakes")) {
            out.append("BEFORE\n")
               .append("• With your household, agree on meeting places and a communication plan. Keep the basic emergency kit ready.\n")
               .append("• Ask an adult to secure heavy shelves and objects that could fall.\n\n")
               .append("DURING\n")
               .append("• Indoors: DROP, COVER and HOLD under sturdy furniture if available. Protect your head and neck; stay away from windows and mirrors. Do not use lifts or rush outside while shaking continues.\n")
               .append("• Outdoors: move to an open area away from buildings, bridges, trees and overhead power lines.\n")
               .append("• Stay calm and follow local emergency instructions.\n\n")
               .append("AFTER\n")
               .append("• Expect possible aftershocks. Stay away from damaged buildings and fallen wires; do not re-enter a damaged structure.\n")
               .append("• Follow official announcements and have a qualified adult check for damage before utilities or equipment are used.\n");
        } else if (key.equals("heatwave") || key.equals("heat wave")) {
            out.append("BEFORE / DURING A HEAT ALERT\n")
               .append("• Check current IMD district warnings and local health advisories; this offline guide cannot show current temperatures or alert levels.\n")
               .append("• Stay in a cool or shaded place where possible, reduce outdoor activity during the hottest part of the day and keep drinking water available.\n")
               .append("• Wear light, loose clothing and use a hat, cloth or umbrella when outside. Drink water regularly, even if you do not feel thirsty.\n")
               .append("• Tell a parent/guardian or another trusted adult promptly if anyone feels unwell; seek appropriate medical help.\n\n")
               .append("HOUSEHOLD CHECK\n")
               .append("• Check that children, older adults and anyone needing extra assistance have a cool place, drinking water and a way to contact a trusted adult.\n");
        } else {
            return unsupported();
        }

        out.append("\nOFFICIAL UPDATES (INTERNET REQUIRED)\n")
           .append("NDMA SACHET — nationwide disaster alerts and safety guidance: ").append(SACHET_URL).append("\n")
           .append("India Meteorological Department — weather bulletins and heat warnings: ").append(IMD_URL).append("\n")
           .append("NDMA Do's & Don'ts — hazard-specific guidance: ").append(NDMA_DOS_DONTS_URL).append("\n")
           .append("MHA information on emergency number 112: ").append(MHA_112_URL).append("\n")
           .append("Source pages checked: ").append(SOURCE_REVIEW_DATE).append(". The guide is available offline; live warnings are not.\n");
        return out.toString();
    }

    private static String unsupported() {
        return "This preparedness topic is not available in the offline pack. Choose one of the listed topics.";
    }
}
