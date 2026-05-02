pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "omartapia/user-api-omar-tapia:${env.BRANCH_NAME}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Login Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'docker login -u $DOCKER_USER -p $DOCKER_PASS'
                }
            }
        }

        stage('Pull Docker Image') {
            steps {
                sh "docker pull $DOCKER_IMAGE"
            }
        }

        stage('Stop Old Container') {
            steps {
                sh '''
                    docker stop user-api || true
                    docker rm user-api || true
                '''
            }
        }

        stage('Run New Container') {
            steps {
                sh "docker run -d --name user-api -p 8080:8080 $DOCKER_IMAGE"
            }
        }
    }
}