const http = require("node:http");

const PORT = Number(process.env.PORT || 3000);
const GROQ_API_KEY = process.env.GROQ_API_KEY;
const GROQ_DEFAULT_MODEL = process.env.GROQ_MODEL || "openai/gpt-oss-120b";
const GROQ_ADVANCED_MODEL = process.env.GROQ_ADVANCED_MODEL || GROQ_DEFAULT_MODEL;
const GROQ_ULTRA_MODEL = process.env.GROQ_ULTRA_MODEL || GROQ_ADVANCED_MODEL;
const GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

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
  if (["ultra", "advanced", "light"].includes(requested)) {
    return requested === "light" ? "light" : requested;
  }
  const ultraSignals = /\b(research|analy[sz]e|architecture|debug|code|compare|strategy|multi.?step|deep dive|detailed plan|reasoning|complex|design a system)\b/i;
  const advancedSignals = /\b(explain|why|how does|plan|summari[sz]e|recommend|calculate|help me decide)\b/i;
  if (ultraSignals.test(text) || text.length > 1200) return "ultra";
  if (advancedSignals.test(text) || text.length > 350) return "advanced";
  return "light";
}

function processingConfig(tier) {
  if (tier === "ultra") return { model: GROQ_ULTRA_MODEL, maxTokens: 1800, temperature: 0.35 };
  if (tier === "advanced") return { model: GROQ_ADVANCED_MODEL, maxTokens: 1100, temperature: 0.5 };
  return { model: GROQ_DEFAULT_MODEL, maxTokens: 700, temperature: 0.7 };
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
      ok: true, service: "jarvis-ai-backend", provider: "groq",
      model: GROQ_DEFAULT_MODEL, aiConfigured: Boolean(GROQ_API_KEY),
      processing: {
        tiers: ["light", "advanced", "ultra"],
        advancedModelConfigured: Boolean(process.env.GROQ_ADVANCED_MODEL),
        ultraModelConfigured: Boolean(process.env.GROQ_ULTRA_MODEL),
        parallelWorkers: false,
        note: "Tier routing is active. Unconfigured tiers safely fall back to the configured default model."
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
      const upstream = await fetch(GROQ_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${GROQ_API_KEY}` },
        body: JSON.stringify({
          model: GROQ_DEFAULT_MODEL,
          messages: [
            { role: "system", content: "Conservatively correct speech-to-text errors in Hindi, Hinglish, and English. Preserve language and script. Do not answer or execute the command. Never invent names, numbers, dates, times, amounts, or destinations. If ambiguous, return the original. Output only the corrected transcript." },
            { role: "user", content: transcript }
          ],
          temperature: 0, max_tokens: 180
        }),
        signal: AbortSignal.timeout(9000)
      });
      const data = await upstream.json().catch(() => ({}));
      const candidate = String(data?.choices?.[0]?.message?.content || "").trim();
      if (!upstream.ok || !candidate || candidate.length > 500) {
        res.writeHead(200); return res.end(JSON.stringify({ understood: false, corrected_text: transcript }));
      }
      res.writeHead(200); return res.end(JSON.stringify({ understood: true, corrected_text: candidate }));
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
    let upstream;
    let data = {};
    const retryable = new Set([408, 429, 500, 502, 503, 504]);
    for (let attempt = 0; attempt < 2; attempt++) {
      upstream = await fetch(GROQ_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${GROQ_API_KEY}` },
        body: JSON.stringify({
          model: config.model,
          messages: [
            { role: "system", content: SYSTEM_PROMPT + (tier === "ultra" ? " For this complex task, reason carefully, break the work into clear steps, check assumptions, and state limitations. Do not pretend to run tools you do not have." : "") },
            { role: "user", content: prompt }
          ],
          temperature: config.temperature,
          max_tokens: config.maxTokens
        }),
        signal: AbortSignal.timeout(25_000)
      });
      data = await upstream.json().catch(() => ({}));
      if (upstream.ok) break;
      const transient = retryable.has(upstream.status);
      console.error("Groq request failed:", upstream.status, String(data?.error?.message || "").slice(0, 250));
      if (!transient || attempt === 1) break;
      await new Promise(resolve => setTimeout(resolve, 800 * (2 ** attempt)));
    }

    if (!upstream?.ok) {
      const status = upstream?.status || 502;
      const providerMessage = String(data?.error?.message || "AI provider request failed").slice(0, 400);
      res.writeHead(status === 429 ? 503 : 502);
      return res.end(JSON.stringify({ error: "AI provider request failed", status, details: providerMessage, processing_tier: tier }));
    }
    const reply = String(data?.choices?.[0]?.message?.content || "").trim();
    if (!reply) { res.writeHead(502); return res.end(JSON.stringify({ error: "AI provider returned an empty response" })); }
    res.writeHead(200);
    return res.end(JSON.stringify({
      reply, text: reply, provider: "groq", model: data.model || config.model,
      processing_tier: tier, processing_mode: tier
    }));
  } catch (error) {
    console.error("AI request error:", error?.name || "unknown", error?.message || "");
    res.writeHead(error?.name === "TimeoutError" ? 504 : 502);
    return res.end(JSON.stringify({ error: error?.name === "TimeoutError" ? "AI provider request timed out" : "AI provider could not be reached" }));
  }
});

server.listen(PORT, "0.0.0.0", () => console.log(`JARVIS backend listening on port ${PORT}`));
