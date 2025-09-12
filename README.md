## Autovyn Sample App (Local Only)

This repo contains a Java full-stack sample for later GCP deployment. For now it runs locally:

- `app` — Spring Boot REST API with CRUD, health, simple file upload, local signed links, and a basic event publisher (HTTP webhook).
- `app/ui` — React (Vite + TypeScript) single-page UI for CRUD, uploads, and health.
- `worker` — Spring Boot worker that accepts events via HTTP and logs/derives records locally.

### Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 18+

### Run Backend
```bash
cd app
mvn spring-boot:run
```
Backend defaults to `http://localhost:8080`.

### Run Worker
```bash
cd worker
mvn spring-boot:run
```
Worker defaults to `http://localhost:8081`.

### Run UI
```bash
cd app/ui
npm install
npm run dev
```
UI at `http://localhost:5173`.

### API Highlights
- `GET /health/ready` → 200 when app is ready
- `GET /health/live` → 200 when app is alive
- `GET /info` → build/version info
- CRUD at `/v1/items`
- `POST /v1/files` — upload file (multipart); returns a time-limited signed-like URL for local download

### Notes
- Storage is in-memory for now, with simple local file storage under `app/uploads/`.
- Event publishing posts to the worker at `http://localhost:8081/events`.
- No GCP resources yet. Terraform, CI/CD, GKE, etc. will be added later.


