pipeline {
    agent any

    // environment {
    //     KUBECONFIG = credentials('kubeconfig')
    // }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Подключение к кластеру') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                    withEnv(["KUBECONFIG=${KUBECONFIG_FILE}"]) {
                        sh '''
                            kubectl apply -f deploy/telegram-consumer/ingress.yaml
                        '''
                    }
                }
            }
        }
    }


    post {
        cleanup {
            cleanWs()
        }
        success {
            echo "✅ Образ задеплоен"
        }
        failure {
            echo "❌ При деплое возникли ошибки"
        }
    }
}
