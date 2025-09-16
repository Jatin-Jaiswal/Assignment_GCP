# Cloud Run Validator Service

Receives Pub/Sub push messages, validates, and writes valid records to GCS.

## Env
- GCS_BUCKET: e.g., gcp-assignment-uploads-471905

## Local test
```bash
cd validator
npm ci
npm run dev
curl -s -X POST http://localhost:8080/pubsub -H 'Content-Type: application/json' \
  -d '{"message":{"data":"eyJpZCI6InQxIiwidGltZXN0YW1wIjoiMjAyNS0wOS0xNVQxMDowMDowMFoiLCJzY2hlbWFWZXJzaW9uIjoiMSIsInBheWxvYWQiOnsiaGVsbG8iOiJ3b3JsZCJ9fQ=="}}'
```

## Build & push
```bash
PROJECT_ID=gcp-assignment-471905
REPO=us-central1-docker.pkg.dev/$PROJECT_ID/gcp-assignment-repo
IMAGE=$REPO/validator:latest

docker build -t $IMAGE ./validator
docker push $IMAGE
```

## Deploy + Pub/Sub wiring
```bash
PROJECT_ID=gcp-assignment-471905
REGION=us-central1
SERVICE=validator-svc
REPO=us-central1-docker.pkg.dev/$PROJECT_ID/gcp-assignment-repo
IMAGE=$REPO/validator:latest
BUCKET=gcp-assignment-uploads-471905
SA=$SERVICE-sa
TOPIC=gcp-assignment-topic
SUB=validator-sub
DLQ=gcp-assignment-dlq

# SA
gcloud iam service-accounts create $SA --project=$PROJECT_ID || true

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$SA@$PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/pubsub.subscriber

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$SA@$PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/storage.objectCreator

# Deploy
gcloud run deploy $SERVICE \
  --image=$IMAGE \
  --region=$REGION \
  --project=$PROJECT_ID \
  --service-account=$SA@$PROJECT_ID.iam.gserviceaccount.com \
  --set-env-vars=GCS_BUCKET=$BUCKET \
  --allow-unauthenticated=false

# Push subscription + DLQ
URL=$(gcloud run services describe $SERVICE --region=$REGION --format='value(status.url)')

gcloud pubsub topics create $DLQ --project=$PROJECT_ID || true

gcloud pubsub subscriptions create $SUB \
  --topic=$TOPIC \
  --push-endpoint="$URL/pubsub" \
  --push-auth-service-account="$SA@$PROJECT_ID.iam.gserviceaccount.com" \
  --dead-letter-topic=$DLQ \
  --max-delivery-attempts=8 \
  --project=$PROJECT_ID || true

# Test publish
gcloud pubsub topics publish $TOPIC \
  --message='{"id":"t1","timestamp":"2025-09-15T10:00:00Z","schemaVersion":"1","payload":{"hello":"world"}}' \
  --project=$PROJECT_ID
```
