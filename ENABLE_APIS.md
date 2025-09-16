# Enable Required GCP APIs

The Jenkins service account doesn't have permission to enable APIs automatically. You need to enable them manually in the GCP Console.

## Required APIs to Enable

Enable these APIs in your GCP project `gcp-assignment-471905`:

### 1. Cloud Resource Manager API
- **URL**: https://console.developers.google.com/apis/api/cloudresourcemanager.googleapis.com/overview?project=gcp-assignment-471905
- **Purpose**: Required for project access and management

### 2. Kubernetes Engine API
- **URL**: https://console.developers.google.com/apis/api/container.googleapis.com/overview?project=gcp-assignment-471905
- **Purpose**: Required for GKE cluster access

### 3. Cloud Run API
- **URL**: https://console.developers.google.com/apis/api/run.googleapis.com/overview?project=gcp-assignment-471905
- **Purpose**: Required for Cloud Run deployments

### 4. Artifact Registry API
- **URL**: https://console.developers.google.com/apis/api/artifactregistry.googleapis.com/overview?project=gcp-assignment-471905
- **Purpose**: Required for Docker image registry

## How to Enable APIs

1. **Go to GCP Console**: https://console.cloud.google.com/
2. **Select your project**: `gcp-assignment-471905`
3. **Navigate to APIs & Services**: 
   - Go to "APIs & Services" → "Library"
   - Or use the direct URLs above
4. **Enable each API**:
   - Search for the API name
   - Click on it
   - Click "Enable"
5. **Wait for activation**: APIs may take a few minutes to become active

## Alternative: Enable via gcloud CLI

If you have owner/admin access, you can enable APIs via command line:

```bash
# Set your project
gcloud config set project gcp-assignment-471905

# Enable required APIs
gcloud services enable cloudresourcemanager.googleapis.com
gcloud services enable container.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable artifactregistry.googleapis.com

# Verify APIs are enabled
gcloud services list --enabled
```

## After Enabling APIs

Once all APIs are enabled:
1. Commit and push the updated Jenkinsfile
2. Re-run the Jenkins pipeline
3. The pipeline should now work without API permission errors

## Service Account Permissions

Make sure your Jenkins service account (`jenkins-service-account@gcp-assignment-471905.iam.gserviceaccount.com`) has these roles:
- **Kubernetes Engine Developer** (for GKE)
- **Cloud Run Developer** (for Cloud Run)
- **Artifact Registry Writer** (for Docker registry)
- **Storage Admin** (if using Cloud Storage)
