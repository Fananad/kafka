pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Deploy telegram-consumer') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                    withEnv(["KUBECONFIG=${KUBECONFIG_FILE}"]) {
                        sh '''
                            kubectl apply -f deploy/telegram-consumer/ingress.yaml
                            kubectl apply -f deploy/telegram-consumer/service.yaml
                            kubectl apply -f deploy/telegram-consumer/deplyment.yaml
                        '''
                    }
                }
            }
        }
        stage('Deploy api-producer') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                    withEnv(["KUBECONFIG=${KUBECONFIG_FILE}"]) {
                        sh '''
                            kubectl apply -f deploy/api-producer/deployment.yaml
                            kubectl apply -f deploy/api-producer/ingress.yaml
                            kubectl apply -f deploy/api-producer/service.yaml
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
