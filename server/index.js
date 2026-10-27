import express from "express";

const app = express();
const port = Number(process.env.PORT || 8080);
const apiKey = process.env.GEMINI_API_KEY;
const model = process.env.GEMINI_MODEL || "gemini-2.5-flash";
app.disable("x-powered-by");
app.use(express.json({ limit: "16kb" }));

// Small per-process guardrail. Use a gateway/WAF for production deployments.
const requests = new Map();
app.use("/api/", (req, res, next) => {
  const now = Date.now();
  const key = req.ip || "unknown";
  const recent = (requests.get(key) || []).filter((time) => now - time < 60_000);
  if (recent.length >= 20) return res.status(429).json({ error: "Too many requests. Try again shortly." });
  recent.push(now); requests.set(key, recent); next();
});

app.get("/api/jarvis/status", (_req, res) => {
  res.json({ ok: true, configured: Boolean(apiKey) });
});

app.post("/api/jarvis/chat", async (req, res) => {
  if (!apiKey) return res.status(503).json({ error: "Server AI key is not configured." });
  const message = typeof req.body?.message === "string" ? req.body.message.trim() : "";
  if (!message) return res.status(400).json({ error: "message is required." });
  if (message.length > 4000) return res.status(413).json({ error: "message is too long." });
  try {
    const upstream = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:generateContent?key=${encodeURIComponent(apiKey)}`, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        system_instruction: { parts: [{ text: "You are JARVIS, a concise and helpful Android assistant. Reply naturally in the language the user uses, including Hindi or Hinglish. For a clear explicit request to call someone, output [ACTION:CALL | contact or number]. For a clear explicit request to prepare a WhatsApp message, output [ACTION:WHATSAPP | contact or number | message]. For opening an app, output [ACTION:OPEN_APP | app name]. For a web search, output [ACTION:SEARCH | search terms]. For flashlight requests, output [ACTION:TORCH | ON] or [ACTION:TORCH | OFF]. For battery status, output [ACTION:BATTERY]. Put a short natural-language explanation before the tag. Never output action tags for hypothetical questions or ordinary conversation." }] },
        contents: [{ role: "user", parts: [{ text: message }] }],
        generationConfig: { temperature: 0.6, maxOutputTokens: 512 }
      }),
      signal: AbortSignal.timeout(35_000)
    });
    if (!upstream.ok) {
      // Do not expose upstream details or credentials to the mobile client.
      return res.status(502).json({ error: "AI provider request failed." });
    }
    const data = await upstream.json();
    const text = data?.candidates?.[0]?.content?.parts?.map((part) => part.text || "").join("").trim();
    if (!text) return res.status(502).json({ error: "AI provider returned no text." });
    return res.json({ text });
  } catch {
    return res.status(504).json({ error: "AI request timed out or failed." });
  }
});

app.listen(port, "0.0.0.0", () => {
  console.log(`JARVIS Gemini proxy listening on port ${port}`);
});
