pipeline {
    agent any

    environment {
        KUBECONFIG = credentials('kubeconfig')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Подключение к кластеру') {
            steps {
                withCredentials([string(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_CONTENT')]) {
                    sh '''
                        mkdir -p ~/.kube
                        echo "$KUBECONFIG_CONTENT" > ~/.kube/config
                        chmod 600 ~/.kube/config

                        kubectl config get-contexts
                        kubectl get pods -A
                    '''
                }
            }
        }
    }


    post {
        success {
            echo "✅ Образ задеплоен"
        }
        failure {
            echo "❌ При деплое возникли ошибки"
        }
    }
}
