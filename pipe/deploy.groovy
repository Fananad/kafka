pipeline {
    agent any

    environment {
        KUBECONFIG_PATH = '~/.kube/config'
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
                        echo $KUBECONFIG | base64 -d > $KUBECONFIG_PATH
                        kubectl get configmap
                    '''

                    // Указываем переменную окружения на kubeconfig
                    // withEnv(["KUBECONFIG=${env.WORKSPACE}/${env.KUBECONFIG_PATH}"]) {
                    //     sh 'kubectl get po'
                    // }
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
