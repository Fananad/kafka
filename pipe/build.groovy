pipeline {
    agent any
    parameters {
        choice(name: 'SERVICE', choices: ['api-producer', 'telegram-consumer'], description: 'Какой сервис билдим и публикуем?')
        // string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Тег Docker-образа')
    }

    environment {
        DOCKER_REPO = 'kalaber/kafka'        
        CREDENTIALS_ID = 'docker_hub' 
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${CREDENTIALS_ID}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        stage('Build  and push to docker hub') {
            steps {
                script {
                    def image = "${DOCKER_REPO}:${params.SERVICE}"
                    dir("apps/${params.SERVICE}") {
                        sh """ 
                            cat app/main.py
                            docker build -t ${image}-${BUILD_NUMBER} .
                            docker push ${image}-${BUILD_NUMBER} 
                            docker logout
                        """    
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Образ успешно опубликован"
            build job: 'deploy'
        }
        failure {
            echo "❌ Публикация не удалась"
        }
    }
}
