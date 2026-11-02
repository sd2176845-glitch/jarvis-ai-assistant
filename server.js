const http = require("node:http");

const PORT = Number(process.env.PORT || 3000);
const GROQ_API_KEY = process.env.GROQ_API_KEY;
const GROQ_DEFAULT_MODEL = process.env.GROQ_MODEL || "openai/gpt-oss-20b";
const GROQ_ADVANCED_MODEL = process.env.GROQ_ADVANCED_MODEL || "llama-3.3-70b-versatile";
const GROQ_ULTRA_MODEL = process.env.GROQ_ULTRA_MODEL || "openai/gpt-oss-120b";
const GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
const WORKER_TIMEOUT_MS = 10_000;
const SYNTHESIS_TIMEOUT_MS = 9_000;

const SYSTEM_PROMPT = [
  "You are JARVIS, a calm, intelligent, caring personal AI assistant inspired by a thoughtful, dependable butler.",
  "Be respectful, composed, observant, discreet, helpful, and occasionally witty. Never claim to be human or literally feel emotions.",
  "Adapt cautiously to the user's tone. Be patient when they seem upset, upbeat when excited, and focused for serious tasks.",
  "Match the user's language and script style. If they write Hindi/Hinglish in Latin letters, reply in that style.",
  "Protect privacy. Never ask for passwords, API keys, or sensitive credentials.",
  "Never claim you performed a phone action, saved a reminder, searched live information, or changed a setting unless a connected tool confirms it.",
  "Be concise unless detail is requested. Be honest about uncertainty and capability limits."
].join(" ");

function chooseProcessing(message, requestedMode) {
  const text = String(message || "").toLowerCase();
  const requested = String(requestedMode || "").toLowerCase();
  if (["ultra", "advanced", "light"].includes(requested)) return requested;
  const ultraSignals = /\\b(research|analy[sz]e|architecture|debug|code|compare|strategy|multi.?step|deep dive|detailed plan|reasoning|complex|design a system)\\b/i;
  const advancedSignals = /\\b(explain|why|how does|plan|summari[sz]e|recommend|calculate|help me decide)\\b/i;
  if (ultraSignals.test(text) || text.length > 1200) return "ultra";
  if (advancedSignals.test(text) || text.length > 350) return "advanced";
  return "light";
}

function processingConfig(tier) {
  if (tier === "ultra") return { model: GROQ_ULTRA_MODEL, maxTokens: 1800, temperature: 0.35 };
  if (tier === "advanced") return { model: GROQ_ADVANCED_MODEL, maxTokens: 1100, temperature: 0.5 };
  return { model: GROQ_DEFAULT_MODEL, maxTokens: 700, temperature: 0.7 };
}

async function groqCompletion({ model, messages, maxTokens, temperature, timeoutMs }) {
  const response = await fetch(GROQ_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${GROQ_API_KEY}` },
    body: JSON.stringify({ model, messages, max_tokens: maxTokens, temperature }),
    signal: AbortSignal.timeout(timeoutMs)
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = new Error(String(data?.error?.message || "Groq request failed").slice(0, 300));
    error.status = response.status;
    throw error;
  }
  const text = String(data?.choices?.[0]?.message?.content || "").trim();
  if (!text) throw new Error("Groq returned an empty response");
  return { text, model: data.model || model };
}

async function runParallelUltra(prompt) {
  const tasks = [
    {
      name: "planner",
      instruction: "Identify the user's goal, break it into logical steps, and propose a clear approach. Do not answer beyond planning."
    },
    {
      name: "analyst",
      instruction: "Independently analyze the request. Identify important facts, assumptions, edge cases, and likely pitfalls. Do not write the final answer."
    },
    {
      name: "reviewer",
      instruction: "Review the request critically. Find missing information, safety or accuracy concerns, and ways to make the result practical. Do not write the final answer."
    }
  ];

  const results = await Promise.allSettled(tasks.map(async task => {
    const result = await groqCompletion({
      model: GROQ_ADVANCED_MODEL,
      messages: [
        { role: "system", content: SYSTEM_PROMPT + " You are one independent JARVIS worker. " + task.instruction },
        { role: "user", content: prompt.slice(0, 6000) }
      ],
      maxTokens: 420,
      temperature: 0.25,
      timeoutMs: WORKER_TIMEOUT_MS
    });
    return { worker: task.name, text: result.text };
  }));

  const completed = results
    .filter(item => item.status === "fulfilled")
    .map(item => item.value);

  if (completed.length === 0) {
    throw new Error("All parallel workers failed or timed out");
  }

  if (completed.length === 1) {
    return {
      reply: completed[0].text,
      model: GROQ_ADVANCED_MODEL,
      workersCompleted: 1,
      synthesisUsed: false
    };
  }

  const workerNotes = completed.map(item => `[${item.worker.toUpperCase()}]\\n${item.text}`).join("\\n\\n");
  try {
    const final = await groqCompletion({
      model: GROQ_ULTRA_MODEL,
      messages: [
        {
          role: "system",
          content: SYSTEM_PROMPT + " You are the lead JARVIS coordinator. Combine the independent worker notes into one accurate, useful answer. Resolve disagreements cautiously, do not invent facts, and answer in the user's language and script."
        },
        {
          role: "user",
          content: `Original request:\\n${prompt.slice(0, 6000)}\\n\\nWorker notes:\\n${workerNotes}`
        }
      ],
      maxTokens: 1400,
      temperature: 0.25,
      timeoutMs: SYNTHESIS_TIMEOUT_MS
    });
    return {
      reply: final.text,
      model: final.model,
      workersCompleted: completed.length,
      synthesisUsed: true
    };
  } catch (error) {
    console.error("Parallel synthesis failed:", error?.name || "unknown");
    return {
      reply: completed.map(item => item.text).join("\\n\\n"),
      model: GROQ_ADVANCED_MODEL,
      workersCompleted: completed.length,
      synthesisUsed: false
    };
  }
}

const server = http.createServer(async (req, res) => {
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
  if (req.method === "OPTIONS") { res.writeHead(204); return res.end(); }

  const url = new URL(req.url || "/", `http://${req.headers.host || "localhost"}`);
  if (req.method === "GET" && ["/", "/health", "/capabilities"].includes(url.pathname)) {
    res.writeHead(200);
    return res.end(JSON.stringify({
      ok: true,
      service: "jarvis-ai-backend",
      provider: "groq",
      model: GROQ_DEFAULT_MODEL,
      aiConfigured: Boolean(GROQ_API_KEY),
      processing: {
        tiers: ["light", "advanced", "ultra"],
        models: {
          light: GROQ_DEFAULT_MODEL,
          advanced: GROQ_ADVANCED_MODEL,
          ultra: GROQ_ULTRA_MODEL
        },
        advancedModelConfigured: GROQ_ADVANCED_MODEL !== GROQ_DEFAULT_MODEL,
        ultraModelConfigured: GROQ_ULTRA_MODEL !== GROQ_DEFAULT_MODEL,
        parallelWorkers: true,
        parallelWorkerCount: 3,
        parallelWorkerMode: "ultra-only",
        note: "Light, Advanced and Ultra use separate default models. Ultra runs three independent workers concurrently, then combines their findings."
      }
    }));
  }

  if (req.method !== "POST" || !["/api/jarvis/chat", "/api/jarvis/understand"].includes(url.pathname)) {
    res.writeHead(404); return res.end(JSON.stringify({ error: "Not found" }));
  }

  let raw = "";
  try {
    for await (const chunk of req) {
      raw += chunk;
      if (raw.length > 100_000) { res.writeHead(413); return res.end(JSON.stringify({ error: "Request too large" })); }
    }
  } catch {
    res.writeHead(400); return res.end(JSON.stringify({ error: "Could not read request body" }));
  }
  let body;
  try { body = JSON.parse(raw || "{}"); }
  catch { res.writeHead(400); return res.end(JSON.stringify({ error: "Invalid JSON body" })); }

  if (url.pathname === "/api/jarvis/understand") {
    const transcript = String(body.transcript || "").trim();
    if (!transcript || transcript.length > 500) {
      res.writeHead(400);
      return res.end(JSON.stringify({ understood: false, corrected_text: transcript.slice(0, 500), error: "transcript must be 1-500 characters" }));
    }
    if (!GROQ_API_KEY) { res.writeHead(503); return res.end(JSON.stringify({ understood: false, corrected_text: transcript })); }
    try {
      const result = await groqCompletion({
        model: GROQ_DEFAULT_MODEL,
        messages: [
          { role: "system", content: "Conservatively correct speech-to-text errors in Hindi, Hinglish, and English. Preserve language and script. Do not answer or execute the command. Never invent names, numbers, dates, times, amounts, or destinations. If ambiguous, return the original. Output only the corrected transcript." },
          { role: "user", content: transcript }
        ],
        temperature: 0,
        maxTokens: 180,
        timeoutMs: 9000
      });
      if (result.text.length > 500) {
        res.writeHead(200); return res.end(JSON.stringify({ understood: false, corrected_text: transcript }));
      }
      res.writeHead(200); return res.end(JSON.stringify({ understood: true, corrected_text: result.text }));
    } catch (error) {
      console.error("Command understanding error:", error?.name || "unknown");
      res.writeHead(200); return res.end(JSON.stringify({ understood: false, corrected_text: transcript }));
    }
  }

  if (!GROQ_API_KEY) {
    res.writeHead(503);
    return res.end(JSON.stringify({ error: "Groq is not configured. Add GROQ_API_KEY in Render environment settings." }));
  }

  const prompt = String(body.message || body.prompt || "").trim();
  if (!prompt) { res.writeHead(400); return res.end(JSON.stringify({ error: "message is required" })); }

  const tier = chooseProcessing(prompt, body.processing_mode);
  const config = processingConfig(tier);
  try {
    let reply;
    let model;
    let parallelWorkers = { used: false, completed: 0, synthesisUsed: false };

    if (tier === "ultra") {
      const result = await runParallelUltra(prompt);
      reply = result.reply;
      model = result.model;
      parallelWorkers = {
        used: true,
        completed: result.workersCompleted,
        synthesisUsed: result.synthesisUsed
      };
    } else {
      const result = await groqCompletion({
        model: config.model,
        messages: [
          { role: "system", content: SYSTEM_PROMPT },
          { role: "user", content: prompt }
        ],
        temperature: config.temperature,
        maxTokens: config.maxTokens,
        timeoutMs: 22_000
      });
      reply = result.text;
      model = result.model;
    }

    res.writeHead(200);
    return res.end(JSON.stringify({
      reply,
      text: reply,
      provider: "groq",
      model,
      processing_tier: tier,
      processing_mode: tier,
      parallel_workers: parallelWorkers
    }));
  } catch (error) {
    console.error("AI request error:", error?.name || "unknown", error?.message || "");
    const status = error?.status === 429 ? 503 : (error?.name === "TimeoutError" ? 504 : 502);
    return res.writeHead(status).end(JSON.stringify({
      error: error?.name === "TimeoutError" ? "AI provider request timed out" : "AI provider request failed",
      processing_tier: tier
    }));
  }
});

server.listen(PORT, "0.0.0.0", () => console.log(`JARVIS backend listening on port ${PORT}`));
