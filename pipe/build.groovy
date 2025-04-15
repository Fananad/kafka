pipeline {
    agent any
    parameters {
        choice(name: 'SERVICE', choices: ['api-producer', 'telegram-consumer'], description: 'Какой сервис билдим и публикуем?')
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Тег Docker-образа')
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

        stage('Build Image') {
            steps {
                script {
                    def image = "${DOCKER_REPO}/${params.SERVICE}:${params.IMAGE_TAG}"
                    dir("apps/${params.SERVICE}") {
                        sh "docker build -t ${image} ."
                    }
                }
            }
        }

        stage('Push to DockerHub') {
            steps {
                script {
                    def image = "${DOCKER_REPO}/${params.SERVICE}:${params.IMAGE_TAG}"
                    withCredentials([
                        usernamePassword(
                            credentialsId: "${CREDENTIALS_ID}",
                            usernameVariable: 'DOCKER_USER',
                            passwordVariable: 'DOCKER_PASS'
                        )
                    ]) {
                        sh """
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker push ${image}
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
        }
        failure {
            echo "❌ Публикация не удалась"
        }
    }
}
