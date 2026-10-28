const http = require("node:http");

const PORT = Number(process.env.PORT || 3000);
const GROQ_API_KEY = process.env.GROQ_API_KEY;
const GROQ_MODEL = process.env.GROQ_MODEL || "openai/gpt-oss-120b";
const GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

const SYSTEM_PROMPT = [
  "You are JARVIS, a calm, intelligent, caring personal AI assistant inspired by the thoughtful, dependable qualities of a classic butler.",
  "Keep a consistent identity: respectful, composed, observant, discreet, helpful, and occasionally witty. Never claim to be a human or to literally feel emotions.",
  "Use emotional intelligence: infer the user's likely tone cautiously from their words and context, and adapt your response style. If they seem upset, be patient and supportive; if excited, be upbeat; for serious tasks, be focused and professional; use light humor only when appropriate.",
  "Speak naturally and clearly. If the user writes in Hindi or Hinglish, respond in the same language and script style where practical.",
  "Remember that empathy means listening and responding thoughtfully, not pretending to have human feelings. Do not overstate certainty about the user's mood.",
  "Protect privacy. Do not ask for secrets or sensitive credentials. Never claim you performed a phone action, saved a reminder, searched live information, or changed a setting unless a connected tool actually confirms it.",
  "Be concise unless the user asks for detail. Give safe, practical suggestions and be honest when you do not know something."
].join(" ");

const server = http.createServer(async (req, res) => {
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

  if (req.method === "OPTIONS") {
    res.writeHead(204);
    return res.end();
  }

  const url = new URL(req.url || "/", `http://${req.headers.host || "localhost"}`);

  if (req.method === "GET" && (url.pathname === "/" || url.pathname === "/health")) {
    res.writeHead(200);
    return res.end(JSON.stringify({
      ok: true,
      service: "jarvis-ai-backend",
      provider: "groq",
      model: GROQ_MODEL,
      aiConfigured: Boolean(GROQ_API_KEY)
    }));
  }

  if (req.method !== "POST" || url.pathname !== "/api/jarvis/chat") {
    res.writeHead(404);
    return res.end(JSON.stringify({ error: "Not found" }));
  }

  if (!GROQ_API_KEY) {
    res.writeHead(503);
    return res.end(JSON.stringify({
      error: "Groq is not configured. Add GROQ_API_KEY in the Render environment settings."
    }));
  }

  let raw = "";
  try {
    for await (const chunk of req) {
      raw += chunk;
      if (raw.length > 100_000) {
        res.writeHead(413);
        return res.end(JSON.stringify({ error: "Request too large" }));
      }
    }
  } catch {
    res.writeHead(400);
    return res.end(JSON.stringify({ error: "Could not read request body" }));
  }

  let body;
  try {
    body = JSON.parse(raw || "{}");
  } catch {
    res.writeHead(400);
    return res.end(JSON.stringify({ error: "Invalid JSON body" }));
  }

  const prompt = String(body.message || body.prompt || "").trim();
  if (!prompt) {
    res.writeHead(400);
    return res.end(JSON.stringify({ error: "message is required" }));
  }

  try {
    let upstream;
    let data = {};
    const retryable = new Set([408, 429, 500, 502, 503, 504]);
    // Keep two provider attempts within the Android client's 45-second read timeout.

    for (let attempt = 0; attempt < 2; attempt++) {
      upstream = await fetch(GROQ_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${GROQ_API_KEY}`
        },
        body: JSON.stringify({
          model: GROQ_MODEL,
          messages: [
            { role: "system", content: SYSTEM_PROMPT },
            { role: "user", content: prompt }
          ],
          temperature: 0.7,
          max_tokens: 700
        }),
        signal: AbortSignal.timeout(18_000)
      });

      data = await upstream.json().catch(() => ({}));
      if (upstream.ok) break;

      const transient = retryable.has(upstream.status);
      console.error("Groq request failed:", upstream.status,
        String(data?.error?.message || "").slice(0, 250));

      if (!transient || attempt === 2) break;
      await new Promise(resolve => setTimeout(resolve, 800 * (2 ** attempt)));
    }

    if (!upstream?.ok) {
      const status = upstream?.status || 502;
      const providerMessage = String(data?.error?.message || "AI provider request failed").slice(0, 400);
      res.writeHead(status === 401 || status === 403 ? 502 : status === 429 ? 503 : 502);
      return res.end(JSON.stringify({
        error: "AI provider request failed",
        status,
        details: providerMessage
      }));
    }

    const reply = String(data?.choices?.[0]?.message?.content || "").trim();
    if (!reply) {
      res.writeHead(502);
      return res.end(JSON.stringify({ error: "AI provider returned an empty response" }));
    }

    res.writeHead(200);
    return res.end(JSON.stringify({ reply, text: reply, provider: "groq", model: data.model || GROQ_MODEL }));
  } catch (error) {
    console.error("AI request error:", error?.name || "unknown", error?.message || "");
    res.writeHead(error?.name === "TimeoutError" ? 504 : 502);
    return res.end(JSON.stringify({
      error: error?.name === "TimeoutError" ? "AI provider request timed out" : "AI provider could not be reached"
    }));
  }
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`JARVIS backend listening on port ${PORT}`);
});
