const http = require("node:http");

const PORT = Number(process.env.PORT || 3000);
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const GEMINI_MODEL = process.env.GEMINI_MODEL || "gemini-3.8-flash";

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
      aiConfigured: Boolean(GEMINI_API_KEY)
    }));
  }

  if (req.method !== "POST" || url.pathname !== "/api/jarvis/chat") {
    res.writeHead(404);
    return res.end(JSON.stringify({ error: "Not found" }));
  }

  if (!GEMINI_API_KEY) {
    res.writeHead(503);
    return res.end(JSON.stringify({
      error: "AI backend is deployed but not configured yet. Add GEMINI_API_KEY in the Render environment settings."
    }));
  }

  let raw = "";
  for await (const chunk of req) {
    raw += chunk;
    if (raw.length > 100_000) {
      res.writeHead(413);
      return res.end(JSON.stringify({ error: "Request too large" }));
    }
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
    // Retry transient Gemini capacity errors and fall back to a supported Flash model.
    const models = [...new Set([GEMINI_MODEL, "gemini-3.6-flash"])];
    let upstream;
    let data = {};
    let lastStatus = 0;

    for (const model of models) {
      for (let attempt = 0; attempt < 3; attempt++) {
        upstream = await fetch(
          "https://generativelanguage.googleapis.com/v1beta/interactions",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "x-goog-api-key": GEMINI_API_KEY
            },
            body: JSON.stringify({
              model,
              input: prompt,
              store: false,
              generation_config: { thinking_level: "low" }
            }),
            signal: AbortSignal.timeout(120_000)
          }
        );

        data = await upstream.json().catch(() => ({}));
        if (upstream.ok) break;
        lastStatus = upstream.status;

        const transient = upstream.status === 429 || upstream.status === 500 ||
          upstream.status === 502 || upstream.status === 503 || upstream.status === 504;
        if (!transient) break;

        console.error("Gemini transient failure:", model, upstream.status,
          String(data?.error?.message || data?.message || "").slice(0, 300));

        if (attempt < 2) {
          await new Promise(resolve => setTimeout(resolve, 1000 * (2 ** attempt)));
        }
      }
      if (upstream?.ok) break;
      if (![429, 500, 502, 503, 504].includes(lastStatus)) break;
    }

    if (!upstream?.ok) {
      const providerMessage = String(data?.error?.message || data?.message || "No provider details returned").slice(0, 500);
      const providerStatus = String(data?.error?.status || "");
      console.error("Gemini request failed:", lastStatus, providerStatus, providerMessage);
      res.writeHead(502);
      return res.end(JSON.stringify({
        error: "AI provider request failed after retries",
        status: lastStatus,
        details: providerMessage,
        providerStatus: providerStatus || undefined
      }));
    }

    let reply = String(data.output_text || "").trim();
    if (!reply && Array.isArray(data.steps)) {
      reply = data.steps
        .filter(step => step.type === "model_output")
        .flatMap(step => Array.isArray(step.content) ? step.content : [])
        .map(part => part.text || "")
        .join("")
        .trim();
    }

    if (!reply) {
      console.error("Gemini returned no output text. Response keys:", Object.keys(data || {}).join(","));
      res.writeHead(502);
      return res.end(JSON.stringify({ error: "AI provider returned an empty response" }));
    }

    res.writeHead(200);
    return res.end(JSON.stringify({ reply, text: reply }));
  } catch (error) {
    console.error("AI request error:", error?.name || "unknown", error?.message || "");
    res.writeHead(504);
    return res.end(JSON.stringify({ error: "AI provider request timed out or could not be reached" }));
  }
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`JARVIS backend listening on port ${PORT}`);
});
