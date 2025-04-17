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
                    script {
                        writeFile file: 'kubeconfig.yaml', text: env.KUBECONFIG_CONTENT
                    }
                    withEnv(["KUBECONFIG=${env.WORKSPACE}/kubeconfig.yaml"]) {
                        sh '''
                            kubectl get pods -A
                        '''
                    }
                }
            }
        }
    }


    post {
        cleanup {
            sh 'rm -fr ${env.WORKSPACE}/*'
        }
        success {
            echo "✅ Образ задеплоен"
        }
        failure {
            echo "❌ При деплое возникли ошибки"
        }
    }
}
