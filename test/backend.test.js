const { test, before, after } = require("node:test");
const assert = require("node:assert/strict");
const { spawn } = require("node:child_process");
const { once } = require("node:events");
const net = require("node:net");

let child;
let baseUrl;

async function unusedPort() {
  const server = net.createServer();
  server.listen(0, "127.0.0.1");
  await once(server, "listening");
  const port = server.address().port;
  await new Promise(resolve => server.close(resolve));
  return port;
}

before(async () => {
  const port = await unusedPort();
  baseUrl = `http://127.0.0.1:${port}`;
  child = spawn(process.execPath, ["server.js"], {
    env: {
      ...process.env,
      PORT: String(port),
      GROQ_API_KEY: "",
      CORS_ALLOWED_ORIGINS: "https://allowed.example",
      RATE_LIMIT_MAX: "20"
    },
    stdio: "ignore"
  });

  const deadline = Date.now() + 8000;
  while (Date.now() < deadline) {
    try {
      const response = await fetch(`${baseUrl}/health`);
      if (response.ok) return;
    } catch {}
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error("Backend did not start in time");
});

after(() => {
  if (child && !child.killed) child.kill("SIGTERM");
});

test("health endpoint is available and responses are not cached", async () => {
  const response = await fetch(`${baseUrl}/health`);
  assert.equal(response.status, 200);
  assert.equal(response.headers.get("cache-control"), "no-store");
  assert.equal(response.headers.get("x-content-type-options"), "nosniff");
});

test("unlisted browser origins are rejected", async () => {
  const response = await fetch(`${baseUrl}/api/jarvis/chat`, {
    method: "POST",
    headers: {
      Origin: "https://untrusted.example",
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ message: "hello" })
  });
  assert.equal(response.status, 403);
});

test("POST requests must declare JSON content type", async () => {
  const response = await fetch(`${baseUrl}/api/jarvis/chat`, {
    method: "POST",
    body: "message=hello"
  });
  assert.equal(response.status, 415);
});

test("malformed JSON is rejected", async () => {
  const response = await fetch(`${baseUrl}/api/jarvis/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: "{invalid"
  });
  assert.equal(response.status, 400);
});
