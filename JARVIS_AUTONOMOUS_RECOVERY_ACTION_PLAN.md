# JARVIS Autonomous Survival Intelligence — Action Workflow

Status: **Approved for planning; implementation not started.**  
Repository: `sd2176845-glitch/jarvis-ai-assistant`  
Scope: JARVIS only. Do not modify the separate Even project.

## Execution order

### Phase 0 — Baseline and safeguards
- [ ] Inspect current Android app, backend, CI workflow and existing diagnostics before changing code.
- [ ] Record current stable app/backend versions and establish a recoverable baseline.
- [ ] Define a versioned repair allowlist with risk, reversibility, data impact and required permissions.
- [ ] Separate runtime self-recovery from source-code modification and deployment.
- [ ] Add feature flags and a global autonomous-maintenance pause control.

### Phase 1 — Health monitor
- [ ] Add a structured health model for app services, storage, database, network and backend.
- [ ] Collect only JARVIS-owned, non-sensitive signals; timestamp observations.
- [ ] Add bounded monitoring intervals, battery-aware scheduling and backoff.
- [ ] Surface status as healthy, degraded, recovering or needs-maintainer-review.

### Phase 2 — Safe autonomous repair
- [ ] Implement allowlisted service restart with bounded retries.
- [ ] Implement cleanup of designated disposable cache only.
- [ ] Add bounded transient network retry and verified fallback-provider selection.
- [ ] Add crash-loop detection and safe degraded mode.
- [ ] Log every repair decision and outcome.

### Phase 3 — Backup, integrity and rollback
- [ ] Identify JARVIS data/configuration that can safely be backed up.
- [ ] Add encrypted, owner-approved backup destinations and versioned snapshots.
- [ ] Verify backup checksums and perform restore drills.
- [ ] Store known-good configuration/release references.
- [ ] Roll back failed eligible updates/configuration changes after health-check failure.

### Phase 4 — Trusted update and security response
- [ ] Verify trusted release source, signature/integrity and device compatibility.
- [ ] Stage updates and run health checks before declaring success.
- [ ] Reject unknown code/plugins and invalid signatures.
- [ ] Detect suspicious JARVIS-owned configuration changes and isolate only affected JARVIS components.
- [ ] Never disable Android security controls or inspect unrelated apps.

### Phase 5 — Recovery journal and resource management
- [ ] Add privacy-minimized, tamper-evident repair history.
- [ ] Redact credentials, tokens, contacts, message contents and personal notes.
- [ ] Monitor JARVIS battery, memory, storage and network consumption.
- [ ] Pause nonessential jobs under resource pressure.
- [ ] Add offline/degraded mode and expire stale queued work; never replay sensitive external actions without fresh authorization.

### Phase 6 — Legacy Continuity Protocol
- [ ] Write owner-facing continuity and recovery documentation.
- [ ] Allow owner to explicitly designate a trusted successor in advance.
- [ ] Define limited, authenticated, revocable and auditable successor permissions.
- [ ] Test owner-unavailable behavior without exposing private data or expanding authority.
- [ ] Do not infer a successor or grant access automatically.

### Phase 7 — Verification and release
- [ ] Add automated tests for each recovery policy and blocked high-risk action.
- [ ] Test outage, crash-loop, low-storage, failed-update, invalid-signature, backup/restore and rollback scenarios.
- [ ] Test battery impact, offline behavior, permission denial and privacy redaction.
- [ ] Build APK and backend checks; inspect CI results.
- [ ] Device-test on Poco M4 Pro 4G before marking any item verified.
- [ ] Update the Master Blueprint status only when evidence supports it.

## Non-negotiable autonomy boundary
JARVIS may automatically perform only pre-approved, reversible, low-risk maintenance. Source edits, APK replacement, backend deployment, credential rotation, permission/security changes, external communication, purchases, data sharing and irreversible actions remain blocked pending explicit authorization by an authorized maintainer/owner. When uncertain, stop safely and record the issue.

## Current implementation status
No code implementation is implied by this plan. This file is an ordered work queue; checkboxes are completed only after code, tests and required device verification.
