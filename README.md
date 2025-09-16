## Autovyn Sample App with Jenkins CI/CD

This repo contains a Java full-stack application with integrated Jenkins CI/CD pipeline and Trivy security scanning:

- `app` — Spring Boot REST API with CRUD, health, simple file upload, local signed links, and a basic event publisher (HTTP webhook).
- `app/ui` — React (Vite + TypeScript) single-page UI for CRUD, uploads, and health.
- `worker` — Spring Boot worker that accepts events via HTTP and logs/derives records locally.
- `validator` — Node.js validation service for file processing.

### Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 18+
- Docker & Docker Compose
- Google Cloud SDK (gcloud)
- Jenkins (local setup provided)

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

## Jenkins CI/CD Pipeline

This project includes a comprehensive Jenkins CI/CD pipeline with Trivy security scanning.

### Pipeline Features

- **Automated Build:** Maven builds for Java services, npm build for React UI
- **Docker Image Creation:** Builds images for app, worker, and frontend
- **Security Scanning:** Trivy scans all Docker images for vulnerabilities
- **Artifact Registry:** Pushes images to Google Cloud Artifact Registry
- **GKE Deployment:** Deploys to Google Kubernetes Engine
- **Cloud Run:** Deploys worker service to Cloud Run
- **Health Checks:** Automated testing of deployed services

### Trivy Security Scanning

The pipeline includes comprehensive security scanning:
- Scans all Docker images for vulnerabilities
- Generates JSON and HTML reports
- Focuses on HIGH and CRITICAL severity issues
- Reports are archived and available in Jenkins UI

### Setting up Jenkins Pipeline

1. **Create Jenkins Job:**
   - Go to Jenkins Dashboard
   - Click "New Item" → Enter "autovyn-pipeline" → Select "Pipeline"
   - Configure Pipeline script from SCM → Git → Point to this repository
   - Set Script Path to `Jenkinsfile`

2. **Add GCP Credentials:**
   - Go to Manage Jenkins → Manage Credentials
   - Add new credential: Secret file with ID `gcp-service-account-key`
   - Upload your GCP service account key JSON file

3. **Run Pipeline:**
   - Click "Build Now" to execute the full CI/CD pipeline

### Notes
- Storage is in-memory for now, with simple local file storage under `app/uploads/`.
- Event publishing posts to the worker at `http://localhost:8081/events`.
- Trivy security scanning is integrated into the CI/CD pipeline.
- All services are deployed to GCP (GKE + Cloud Run).


