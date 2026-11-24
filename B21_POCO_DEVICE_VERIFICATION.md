# JARVIS B21 — Poco M4 Pro 4G Device Verification

**Project:** JARVIS only. Do not install or modify Even as part of this checklist.  
**Build:** Use the APK artifact attached to successful Actions run [45058814748](https://github.com/sd2176845-glitch/jarvis-ai-assistant/actions/runs/45058814748).  
**Current status:** Automated CI passed. Every item below remains **not device-verified** until physically tested and recorded.

## Before installing
- [ ] Confirm the APK came from the successful B21 workflow artifact, not an old local build.
- [ ] Confirm Android version and available storage on the Poco M4 Pro 4G.
- [ ] Back up any important JARVIS local notes/tasks before replacing an existing install.
- [ ] Review requested Android permissions; deny anything not needed for the test being performed.

## Core app and voice
- [ ] Install and launch JARVIS; confirm dashboard renders without crash.
- [ ] Grant microphone permission only when testing speech features.
- [ ] Test speech input in English (India), Hindi (India), and Bengali (India) if the installed recognition service offers each locale.
- [ ] Confirm listening/thinking/speaking/standby status is visible and stop speech works.
- [ ] Confirm repeat-last-response works and does not replay stale/private content after app restart.
- [ ] Test wake phrase with screen on, then separately with screen off.
- [ ] Test MIUI battery restrictions and record the exact settings needed; do not assume background listening is reliable until repeated tests pass.

## Task and workflow features
- [ ] Create a task with category, priority and due date.
- [ ] Edit the task and confirm existing completion state/history is preserved.
- [ ] Complete and reopen a task; inspect completion history.
- [ ] Confirm overdue, today's agenda, category filter, priority filter, search and overview counts.
- [ ] Preview a manual workflow; confirm dry-run does not execute any step.
- [ ] Duplicate a workflow; confirm the copy starts with unchecked steps.
- [ ] Confirm per-step history and last-updated information are accurate.

## Communication and contacts
- [ ] Test contact lookup only after granting Contacts permission.
- [ ] If multiple contacts match, confirm JARVIS asks the owner to select one.
- [ ] Confirm a call still requires the existing explicit confirmation.
- [ ] Draft an SMS, edit it, and verify JARVIS opens the messaging app without sending it.
- [ ] Cancel the draft and confirm no message is sent.

## OCR, privacy and other integrations
- [ ] Select an image manually and confirm OCR preview/cancel behavior.
- [ ] Select a PDF manually and test first-page OCR, Latin and Devanagari selection where supported.
- [ ] Confirm AI explanation requires a second explicit consent and only selected extracted text is sent.
- [ ] Confirm private studios require Android device-credential authentication where configured.
- [ ] Enter Guest Mode and confirm private notes/tasks/memory are not exposed; exit only through device authentication.
- [ ] Test media-session controls with a supported media player; record unsupported-player behavior.
- [ ] Test app catalogue launch and public Play Store handoff.
- [ ] Test backend health check and distinguish endpoint reachability from a successful AI response.

## Record results
For every failed or unavailable item, record the exact screen, Android version, permission state, steps, and non-sensitive error text. Do not include OTPs, API keys, passwords, contact lists, message contents, or private notes in shared reports.

| Area | Result | Notes / build details |
|---|---|---|
| Install and dashboard | Pending | |
| Voice and wake word | Pending | |
| Tasks and workflows | Pending | |
| Contacts and message draft | Pending | |
| OCR and privacy | Pending | |
| Media and app handoff | Pending | |
| Backend health | Pending | |

**Release gate:** Keep PR #47 draft and unmerged until the owner has reviewed the device results. CI success alone does not establish Poco device compatibility.
