pipeline {
    agent {
        docker {
            image 'yamanshakya/ssh-client'
            args '-u 0:0'  
        }
    }
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


