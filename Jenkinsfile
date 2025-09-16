pipeline {
    agent any
    
    environment {
        GOOGLE_CLOUD_PROJECT = 'gcp-assignment-471905'
        REGION = 'us-central1'
        ARTIFACT_REGISTRY = 'us-central1-docker.pkg.dev/gcp-assignment-471905/gcp-assignment-repo'
        GKE_CLUSTER = 'gcp-assignment-cluster'
        GKE_ZONE = 'us-central1'
        TRIVY_CACHE_DIR = '/tmp/trivy-cache'
        TRIVY_RESULTS_DIR = 'trivy-results'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }
    
    stages {
        stage('Setup GCP Authentication') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'gcp-assignment-cluster', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh """
                            # Set up GCP authentication
                            export GOOGLE_APPLICATION_CREDENTIALS=\${GOOGLE_APPLICATION_CREDENTIALS}
                            gcloud auth activate-service-account --key-file=\${GOOGLE_APPLICATION_CREDENTIALS}
                            gcloud config set project ${GOOGLE_CLOUD_PROJECT}
                            
                            # Enable required APIs
                            gcloud services enable cloudresourcemanager.googleapis.com
                            gcloud services enable container.googleapis.com
                            gcloud services enable run.googleapis.com
                            gcloud services enable artifactregistry.googleapis.com
                            
                            # Verify authentication
                            gcloud auth list
                            gcloud config get-value project
                        """
                    }
                }
            }
        }
        
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
                    
                    // Build frontend image
                    sh """
                        docker build -t ${ARTIFACT_REGISTRY}/frontend:${BUILD_NUMBER} ./app/ui
                        docker build -t ${ARTIFACT_REGISTRY}/frontend:latest ./app/ui
                    """
                }
            }
        }
        
        stage('Trivy Security Scan') {
            steps {
                script {
                    // Create results directory
                    sh "mkdir -p ${TRIVY_RESULTS_DIR}"
                    
                    // Scan app image
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \\
                            -v ${TRIVY_CACHE_DIR}:/root/.cache/trivy \\
                            -v ${WORKSPACE}/${TRIVY_RESULTS_DIR}:/results \\
                            aquasec/trivy:latest image \\
                            --format json --output /results/app-scan.json \\
                            --severity HIGH,CRITICAL \\
                            ${ARTIFACT_REGISTRY}/app:${BUILD_NUMBER}
                    """
                    
                    // Scan worker image
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \\
                            -v ${TRIVY_CACHE_DIR}:/root/.cache/trivy \\
                            -v ${WORKSPACE}/${TRIVY_RESULTS_DIR}:/results \\
                            aquasec/trivy:latest image \\
                            --format json --output /results/worker-scan.json \\
                            --severity HIGH,CRITICAL \\
                            ${ARTIFACT_REGISTRY}/worker:${BUILD_NUMBER}
                    """
                    
                    // Scan frontend image
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \\
                            -v ${TRIVY_CACHE_DIR}:/root/.cache/trivy \\
                            -v ${WORKSPACE}/${TRIVY_RESULTS_DIR}:/results \\
                            aquasec/trivy:latest image \\
                            --format json --output /results/frontend-scan.json \\
                            --severity HIGH,CRITICAL \\
                            ${ARTIFACT_REGISTRY}/frontend:${BUILD_NUMBER}
                    """
                    
                    // Generate HTML reports
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \\
                            -v ${TRIVY_CACHE_DIR}:/root/.cache/trivy \\
                            -v ${WORKSPACE}/${TRIVY_RESULTS_DIR}:/results \\
                            aquasec/trivy:latest image \\
                            --format template --template "@contrib/html.tpl" \\
                            --output /results/app-report.html \\
                            --severity HIGH,CRITICAL \\
                            ${ARTIFACT_REGISTRY}/app:${BUILD_NUMBER}
                    """
                    
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \\
                            -v ${TRIVY_CACHE_DIR}:/root/.cache/trivy \\
                            -v ${WORKSPACE}/${TRIVY_RESULTS_DIR}:/results \\
                            aquasec/trivy:latest image \\
                            --format template --template "@contrib/html.tpl" \\
                            --output /results/worker-report.html \\
                            --severity HIGH,CRITICAL \\
                            ${ARTIFACT_REGISTRY}/worker:${BUILD_NUMBER}
                    """
                    
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \\
                            -v ${TRIVY_CACHE_DIR}:/root/.cache/trivy \\
                            -v ${WORKSPACE}/${TRIVY_RESULTS_DIR}:/results \\
                            aquasec/trivy:latest image \\
                            --format template --template "@contrib/html.tpl" \\
                            --output /results/frontend-report.html \\
                            --severity HIGH,CRITICAL \\
                            ${ARTIFACT_REGISTRY}/frontend:${BUILD_NUMBER}
                    """
                }
            }
            post {
                always {
                    // Archive security scan results
                    archiveArtifacts artifacts: "${TRIVY_RESULTS_DIR}/**/*", fingerprint: true
                    
                    // Publish HTML reports
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: "${TRIVY_RESULTS_DIR}",
                        reportFiles: 'app-report.html,worker-report.html,frontend-report.html',
                        reportName: 'Trivy Security Reports'
                    ])
                }
            }
        }
        
        stage('Push to Artifact Registry') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'gcp-assignment-cluster', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh """
                            # Set up GCP authentication
                            export GOOGLE_APPLICATION_CREDENTIALS=\${GOOGLE_APPLICATION_CREDENTIALS}
                            gcloud auth activate-service-account --key-file=\${GOOGLE_APPLICATION_CREDENTIALS}
                            gcloud config set project ${GOOGLE_CLOUD_PROJECT}
                            
                            # Configure Docker for Artifact Registry
                            gcloud auth configure-docker ${REGION}-docker.pkg.dev
                            
                            # Push images
                            docker push ${ARTIFACT_REGISTRY}/app:${BUILD_NUMBER}
                            docker push ${ARTIFACT_REGISTRY}/app:latest
                            docker push ${ARTIFACT_REGISTRY}/worker:${BUILD_NUMBER}
                            docker push ${ARTIFACT_REGISTRY}/worker:latest
                            docker push ${ARTIFACT_REGISTRY}/frontend:${BUILD_NUMBER}
                            docker push ${ARTIFACT_REGISTRY}/frontend:latest
                        """
                    }
                }
            }
        }
        
        stage('Deploy to GKE') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'gcp-assignment-cluster', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh """
                            # Set up GCP authentication
                            export GOOGLE_APPLICATION_CREDENTIALS=\${GOOGLE_APPLICATION_CREDENTIALS}
                            gcloud auth activate-service-account --key-file=\${GOOGLE_APPLICATION_CREDENTIALS}
                            gcloud config set project ${GOOGLE_CLOUD_PROJECT}
                            
                            # Get GKE credentials
                            gcloud container clusters get-credentials ${GKE_CLUSTER} --zone ${GKE_ZONE}
                            
                            # Update image tags in deployment
                            sed -i 's|image: us-central1-docker.pkg.dev/gcp-assignment-471905/gcp-assignment-repo/app:latest|image: ${ARTIFACT_REGISTRY}/app:${BUILD_NUMBER}|g' k8s/app-deployment.yaml
                            sed -i 's|image: us-central1-docker.pkg.dev/gcp-assignment-471905/gcp-assignment-repo/frontend:latest|image: ${ARTIFACT_REGISTRY}/frontend:${BUILD_NUMBER}|g' k8s/frontend-deployment.yaml
                            
                            # Apply Kubernetes manifests
                            kubectl apply -f k8s/namespace.yaml
                            kubectl apply -f k8s/secrets.yaml
                            kubectl apply -f k8s/app-deployment.yaml
                            kubectl apply -f k8s/frontend-deployment.yaml
                            kubectl apply -f k8s/ingress.yaml
                            
                            # Wait for deployments to be ready
                            kubectl rollout status deployment/autovyn-app -n dev --timeout=300s
                            kubectl rollout status deployment/autovyn-frontend -n dev --timeout=300s
                        """
                    }
                }
            }
        }
        
        stage('Deploy Worker to Cloud Run') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'gcp-assignment-cluster', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh """
                            # Set up GCP authentication
                            export GOOGLE_APPLICATION_CREDENTIALS=\${GOOGLE_APPLICATION_CREDENTIALS}
                            gcloud auth activate-service-account --key-file=\${GOOGLE_APPLICATION_CREDENTIALS}
                            gcloud config set project ${GOOGLE_CLOUD_PROJECT}
                            
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
