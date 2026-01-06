pipeline {
    agent any
    environment {
        SSH_KEY64 = credentials('SSH_KEY64') // Jenkins secret text (Base64-encoded PEM)
    }
    parameters {
        string(
            name: 'SERVER_IP',
            defaultValue: '100.31.95.2',
            description: "Enter the server IP ADDRESS"
        )
    }
    stages {
        stage('Configure SSH') {
            steps {
                sh '''
                mkdir -p ~/.ssh
                chmod 700 ~/.ssh
                cat > ~/.ssh/config <<'EOF'
Host *
StrictHostKeyChecking no
EOF
                cat ~/.ssh/config   #to verify
                touch ~/.ssh/known_hosts
                chmod 600 ~/.ssh/known_hosts
                '''
            }
        }
        stage('SSH Key Access') {
            steps {
                // Use double quotes for Groovy variable interpolation
                sh """
                    mkdir -p /tmp/jenkins_keys
                    echo "$SSH_KEY64" | base64 -d > /tmp/jenkins_keys/myKey.pem
                    chmod 600 /tmp/jenkins_keys/myKey.pem
                    ssh-keygen -R ${params.SERVER_IP} || true
                """
            }
        }
        stage('Deploy Code to Server') {
            steps {
                // Use triple double quotes for Groovy interpolation
                sh """
                    ssh -i /tmp/jenkins_keys/myKey.pem ec2-user@${params.SERVER_IP} \
                        "cd /usr/share/nginx/html && git pull"
                """
            }
        }
    }
}