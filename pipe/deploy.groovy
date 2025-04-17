pipeline {
    agent any

    environment {
        KUBECONFIG = credentials('kubeconfig') // ID секрета в Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Deploy to Kubernetes') {
            
            steps {
                sh '''
                    kubectl get po
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Образ успешно опубликован"
        }
        failure {
            echo "❌ Публикация не удалась"
        }
    }
}
