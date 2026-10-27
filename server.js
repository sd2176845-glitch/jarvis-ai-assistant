const http = require("node:http");

const PORT = Number(process.env.PORT || 3000);
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const GEMINI_MODEL = process.env.GEMINI_MODEL || "gemini-2.5-flash";

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
    const upstream = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(GEMINI_MODEL)}:generateContent?key=${encodeURIComponent(GEMINI_API_KEY)}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{ role: "user", parts: [{ text: prompt }] }],
          generationConfig: { temperature: 0.7, maxOutputTokens: 700 }
        }),
        signal: AbortSignal.timeout(25_000)
      }
    );

    const data = await upstream.json().catch(() => ({}));
    if (!upstream.ok) {
      console.error("Gemini request failed with status", upstream.status);
      res.writeHead(502);
      return res.end(JSON.stringify({ error: "AI provider request failed", status: upstream.status }));
    }

    const reply = (data.candidates?.[0]?.content?.parts || [])
      .map(part => part.text || "")
      .join("")
      .trim();

    if (!reply) {
      res.writeHead(502);
      return res.end(JSON.stringify({ error: "AI provider returned an empty response" }));
    }

    res.writeHead(200);
    return res.end(JSON.stringify({ reply, text: reply }));
  } catch (error) {
    console.error("AI request error:", error?.name || "unknown");
    res.writeHead(504);
    return res.end(JSON.stringify({ error: "AI provider request timed out or could not be reached" }));
  }
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`JARVIS backend listening on port ${PORT}`);
});