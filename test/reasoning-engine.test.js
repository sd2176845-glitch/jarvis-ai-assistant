"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const {
  chooseProcessing,
  normalizeWorkerResults,
  buildWorkerNotes,
  buildFallbackPrompt,
  MAX_WORKER_CHARS
} = require("../ReasoningEngineRules");

test("simple requests use the light tier", () => {
  assert.deepEqual(chooseProcessing("Hello JARVIS"), {
    tier: "light", reason: "short_or_simple_request", explicit: false
  });
});

test("analysis requests use the advanced tier", () => {
  const result = chooseProcessing("Explain how a solar panel works");
  assert.equal(result.tier, "advanced");
  assert.equal(result.reason, "analysis_signal");
  assert.equal(result.explicit, false);
});

test("high-complexity requests use the ultra tier", () => {
  const result = chooseProcessing("Compare options and build a detailed system design");
  assert.equal(result.tier, "ultra");
  assert.equal(result.reason, "high_complexity_signal");
});

test("Hindi and Hinglish reasoning cues are recognized", () => {
  assert.equal(chooseProcessing("Iska vistaar se vishleshan karo").tier, "ultra");
  assert.equal(chooseProcessing("Mujhe simple yojana samjhao").tier, "advanced");
});

test("long requests are promoted to the appropriate existing tier", () => {
  assert.equal(chooseProcessing("x".repeat(351)).tier, "advanced");
  const long = chooseProcessing("x".repeat(1201));
  assert.equal(long.tier, "ultra");
  assert.equal(long.reason, "long_request");
});

test("valid explicit modes override automatic routing", () => {
  assert.deepEqual(chooseProcessing("design a complex system", "light"), {
    tier: "light", reason: "explicit_mode", explicit: true
  });
  assert.equal(chooseProcessing("hello", "ULTRA").tier, "ultra");
});

test("invalid explicit modes are ignored", () => {
  assert.equal(chooseProcessing("hello", "turbo").tier, "light");
  assert.equal(chooseProcessing("explain this", "unknown").tier, "advanced");
});

test("worker normalization rejects failures, unknown workers, duplicates and empty text", () => {
  const workers = normalizeWorkerResults([
    { status: "fulfilled", value: { worker: "planner", text: "Plan" } },
    { status: "rejected", reason: new Error("timeout") },
    { status: "fulfilled", value: { worker: "unknown", text: "Ignore" } },
    { status: "fulfilled", value: { worker: "analyst", text: "   " } },
    { status: "fulfilled", value: { worker: "planner", text: "Duplicate" } },
    { status: "fulfilled", value: { worker: "reviewer", text: "Check" } }
  ]);
  assert.deepEqual(workers, [
    { worker: "planner", text: "Plan" },
    { worker: "reviewer", text: "Check" }
  ]);
});

test("worker text is capped before it enters synthesis context", () => {
  const workers = normalizeWorkerResults([
    { status: "fulfilled", value: { worker: "analyst", text: "x".repeat(MAX_WORKER_CHARS + 500) } }
  ]);
  assert.equal(workers[0].text.length, MAX_WORKER_CHARS);
});

test("worker notes are structured and fallback prompt never asks to reveal reasoning", () => {
  const notes = buildWorkerNotes([
    { worker: "planner", text: "Three safe steps." },
    { worker: "analyst", text: "One assumption needs checking." }
  ]);
  assert.match(notes, /\[PLANNER\]/);
  assert.match(notes, /\[ANALYST\]/);
  const fallback = buildFallbackPrompt("What should I do?", notes);
  assert.match(fallback, /Original user request/);
  assert.match(fallback, /fallible private analysis/);
  assert.match(fallback, /Do not reveal internal worker notes/);
  assert.doesNotMatch(fallback, /\[PLANNER\]/);
});

test("malformed worker collections fail closed", () => {
  assert.deepEqual(normalizeWorkerResults(null), []);
  assert.deepEqual(normalizeWorkerResults("not-an-array"), []);
  assert.equal(buildWorkerNotes(null), "");
});
