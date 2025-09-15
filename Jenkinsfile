pipeline {
    agent any
    
    environment {
        GOOGLE_CLOUD_PROJECT = 'gcp-assignment-471905'
        REGION = 'us-central1'
        ARTIFACT_REGISTRY = 'us-central1-docker.pkg.dev/gcp-assignment-471905/gcp-assignment-repo'
        GKE_CLUSTER = 'gcp-assignment-cluster'
        GKE_ZONE = 'us-central1-a'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build App') {
            steps {
                dir('app') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Build Worker') {
            steps {
                dir('worker') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    // Build app image
                    sh """
                        docker build -t ${ARTIFACT_REGISTRY}/app:${BUILD_NUMBER} ./app
                        docker build -t ${ARTIFACT_REGISTRY}/app:latest ./app
                    """
                    
                    // Build worker image
                    sh """
                        docker build -t ${ARTIFACT_REGISTRY}/worker:${BUILD_NUMBER} ./worker
                        docker build -t ${ARTIFACT_REGISTRY}/worker:latest ./worker
                    """
                }
            }
        }
        
        stage('Push to Artifact Registry') {
            steps {
                script {
                    sh """
                        gcloud auth configure-docker ${REGION}-docker.pkg.dev
                        docker push ${ARTIFACT_REGISTRY}/app:${BUILD_NUMBER}
                        docker push ${ARTIFACT_REGISTRY}/app:latest
                        docker push ${ARTIFACT_REGISTRY}/worker:${BUILD_NUMBER}
                        docker push ${ARTIFACT_REGISTRY}/worker:latest
                    """
                }
            }
        }
        
        stage('Deploy to GKE') {
            steps {
                script {
                    sh """
                        gcloud container clusters get-credentials ${GKE_CLUSTER} --zone ${GKE_ZONE}
                        
                        # Update image tags in deployment
                        sed -i 's|image: us-central1-docker.pkg.dev/gcp-assignment-471905/gcp-assignment-repo/app:latest|image: ${ARTIFACT_REGISTRY}/app:${BUILD_NUMBER}|g' k8s/app-deployment.yaml
                        
                        # Apply Kubernetes manifests
                        kubectl apply -f k8s/namespace.yaml
                        kubectl apply -f k8s/secrets.yaml
                        kubectl apply -f k8s/app-deployment.yaml
                        kubectl apply -f k8s/ingress.yaml
                        
                        # Wait for deployment to be ready
                        kubectl rollout status deployment/autovyn-app -n dev --timeout=300s
                    """
                }
            }
        }
        
        stage('Deploy Worker to Cloud Run') {
            steps {
                script {
                    sh """
                        # Deploy worker to Cloud Run
                        gcloud run deploy autovyn-worker \\
                            --image ${ARTIFACT_REGISTRY}/worker:${BUILD_NUMBER} \\
                            --region ${REGION} \\
                            --platform managed \\
                            --allow-unauthenticated \\
                            --port 9091 \\
                            --memory 512Mi \\
                            --cpu 1 \\
                            --max-instances 10 \\
                            --set-env-vars GOOGLE_CLOUD_PROJECT=${GOOGLE_CLOUD_PROJECT}
                    """
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    // Wait for app to be ready
                    sh 'sleep 30'
                    
                    // Get the external IP
                    sh """
                        EXTERNAL_IP=\$(kubectl get ingress autovyn-ingress -n dev -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
                        echo "External IP: \$EXTERNAL_IP"
                        
                        # Test health endpoints
                        curl -f http://\$EXTERNAL_IP/health/ready || exit 1
                        curl -f http://\$EXTERNAL_IP/health/live || exit 1
                    """
                }
            }
        }
    }
    
    post {
        always {
            // Clean up workspace
            cleanWs()
        }
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
