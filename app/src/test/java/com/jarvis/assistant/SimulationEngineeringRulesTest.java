package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimulationEngineeringRulesTest {
    @Test public void digitalWorldPromptStaysConceptualAndNonViolent() {
        String prompt = SimulationEngineeringRules.digitalWorldPrompt("Ecosystem balance", "Study food webs");
        assertTrue(prompt.contains("text-only conceptual simulation"));
        assertTrue(prompt.contains("Do not model attacks, weapons"));
        assertTrue(prompt.contains("assumptions and limitations"));
    }

    @Test public void experimentPromptExcludesRealHazardousProcedures() {
        String prompt = SimulationEngineeringRules.experimentPrompt("Chemistry concepts", "Understand acids and bases");
        assertTrue(prompt.contains("conceptual virtual science exploration"));
        assertTrue(prompt.contains("Do not provide real-world experiment procedures"));
        assertTrue(prompt.contains("harmless thought experiment"));
    }

    @Test public void inventionPromptIsBenignAndNotManufacturingReady() {
        String prompt = SimulationEngineeringRules.inventionPrompt("School / desk organizer", "Organize stationery");
        assertTrue(prompt.contains("original, text-only conceptual 3D product design brief"));
        assertTrue(prompt.contains("Do not design weapons"));
        assertTrue(prompt.contains("not a CAD file or manufacturing-ready design"));
    }

    @Test public void virtualExperimentRequiresAssumptionsUnitsAndSourceNotes() {
        String prompt = SimulationEngineeringRules.experimentPrompt("Physics concepts", "motion");
        assertTrue(prompt.contains("assumptions"));
        assertTrue(prompt.contains("units"));
        assertTrue(prompt.contains("source notes"));
        assertTrue(prompt.contains("SIMULATION ONLY"));
        assertTrue(prompt.contains("Do not provide real-world experiment procedures"));
    }

    @Test public void comparisonPromptComparesConceptualCasesWithoutRealWorldProcedures() {
        String prompt = SimulationEngineeringRules.comparePrompt("baseline ecosystem", "increased rainfall");
        assertTrue(prompt.contains("text-only educational comparison"));
        assertTrue(prompt.contains("side-by-side table"));
        assertTrue(prompt.contains("SIMULATION ONLY"));
        assertTrue(prompt.contains("Do not provide real-world experiment procedures"));
        assertTrue(prompt.contains("untrusted descriptive input"));
    }
}
