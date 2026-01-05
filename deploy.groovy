pipeline {
    agent any
    environment {
        SSH_KEY64 = credentials('SSH_KEY64')
    }

    parameters {
        string(
            name: 'SERVER_IP',
            defaultValue: '44.192.96.212',
            description: 'Target Server'
        )
    }

    stages {
        stage('configure SSH') {
            steps {
                sh '''
                mkdir -p ~/.ssh
                chmod 700 ~/.ssh
                echo -e "Host *\n\tStrictHostKeyChecking no\n" > ~/.ssh/config
                chmod 600 ~/.ssh/config
                touch ~/.ssh/known_hosts
                chmod 600 ~/.ssh/known_hosts
                '''
    
            }
        }
        stage('SSH KEY ACCESS') {
            steps{
                sh '''
                    echo "$SSH_KEY64" | base64 -d > mykey.pem
                    chmod 400 mykey.pem
                    ssh-keygen -R ${params.SERVER_IP}
                '''
            }
        }
        stage('Deploy Code to Server') {
            steps {
                sh '''
                ssh ec2-user@${params.SERVER_IP} -i mykey.pem -T \
                    'cd /usr/share/nginx/html && git pull origin main'
                '''
            }
        }
        
    }

}