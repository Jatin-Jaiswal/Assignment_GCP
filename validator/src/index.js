import express from "express";
import crypto from "crypto";
import { Storage } from "@google-cloud/storage";

const app = express();
app.use(express.json({ limit: "1mb" }));

const port = process.env.PORT ? parseInt(process.env.PORT, 10) : 8080;
const bucketName = process.env.GCS_BUCKET;
const storage = new Storage();
const bucket = bucketName ? storage.bucket(bucketName) : null;

function isIsoDate(v) { const d = new Date(v); return !Number.isNaN(d.getTime()); }
function b64json(b64) { return JSON.parse(Buffer.from(b64, "base64").toString("utf8")); }
function safeName(id) {
  const safeId = String(id || "").replace(/[^a-zA-Z0-9_-]/g, "");
  const hash = crypto.createHash("sha256").update(String(id)).digest("hex").slice(0, 12);
  return `validated/${safeId || "id"}-${hash}.json`;
}

app.get("/health", (_req, res) => res.status(200).json({ status: "ok" }));

app.post("/pubsub", async (req, res) => {
  try {
    const message = req.body?.message;
    if (!message?.data) return res.status(400).json({ error: "missing message.data" });

    const decoded = b64json(message.data);
    const { id, timestamp, schemaVersion, payload } = decoded || {};
    if (!id || typeof id !== "string") return res.status(400).json({ error: "id missing" });
    if (!timestamp || typeof timestamp !== "string" || !isIsoDate(timestamp)) {
      return res.status(400).json({ error: "timestamp invalid" });
    }
    if (!schemaVersion || typeof schemaVersion !== "string") {
      return res.status(400).json({ error: "schemaVersion invalid" });
    }
    if (payload === undefined || typeof payload !== "object") {
      return res.status(400).json({ error: "payload invalid" });
    }
    if (!bucket) return res.status(500).json({ error: "GCS bucket not configured" });

    const objectName = safeName(id);
    await bucket.file(objectName).save(JSON.stringify({ id, timestamp, schemaVersion, payload }, null, 2), {
      contentType: "application/json; charset=utf-8",
      resumable: false
    });
    console.log(`saved ${objectName}`);
    return res.status(204).send();
  } catch (e) {
    console.error("processing error", e?.message || e);
    return res.status(500).json({ error: "processing error" });
  }
});

app.listen(port, () => console.log(`validator listening on :${port}`));
