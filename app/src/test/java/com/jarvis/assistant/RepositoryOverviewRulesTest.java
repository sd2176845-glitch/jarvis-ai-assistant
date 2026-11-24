package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class RepositoryOverviewRulesTest {
    @Test public void acceptsOwnerRepoAndPublicGithubUrls() {
        assertEquals("owner/project", RepositoryOverviewRules.normalize("owner/project"));
        assertEquals("owner/project", RepositoryOverviewRules.normalize("https://github.com/owner/project.git"));
    }

    @Test public void rejectsNonRepositoryPathsAndOtherHosts() {
        assertNull(RepositoryOverviewRules.normalize("https://example.com/owner/project"));
        assertNull(RepositoryOverviewRules.normalize("owner/project/tree/main"));
        assertNull(RepositoryOverviewRules.normalize("owner"));
    }
}
