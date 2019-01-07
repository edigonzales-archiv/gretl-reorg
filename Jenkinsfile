pipeline {
    agent any
    stages {
        stage('Prepare') {
            steps {
                git 'https://github.com/edigonzales/gretl-reorg.git'
            }
        }
    }
    post {
        always {
            deleteDir() 
        }
    }
}