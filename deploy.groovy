pipeline {
    agent any

    environment {
        SSH_KEY64 = credentials('SSH_KEY64')  // Jenkins secret text (Base64-encoded PEM)
    }

    parameters {
        string(
            name: 'SERVER_IP',
            defaultValue: '54.82.30.170',
            description: 'Enter the Server IP Address'
        )
    }

    stages {
        stage('Configure SSH') {
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
            steps {
                sh '''#!/bin/bash
                    set -e
                    mkdir -p /tmp/jenkins_keys
                    echo "$SSH_KEY64" | base64 -d > /tmp/jenkins_keys/mykey.pem
                    chmod 400 /tmp/jenkins_keys/mykey.pem
                    ssh-keygen -R "$SERVER_IP" || true
                '''
            }
        }

        stage('Deploy Code to Server') {
            steps {
                sh '''#!/bin/bash
                    ssh -i /tmp/jenkins_keys/mykey.pem ec2-user@$SERVER_IP -T \
                    "cd /usr/share/nginx/html && git pull origin main"
                '''
            }
        }

        
    }

}

// pipeline {   //dpsh
//     agent any

//     environment {
//         SSH_KEY64 = credentials('SSH_KEY64') // Jenkins Secret text (BASE64 encoded PEM file)
//     }

//     parameters {
//         string(
//             name: 'SERVER_IP',
//             defaultValue:'54.82.30.170'   // <-- EC2 Server Public IP 
//             description: "Enter the Server IP Address"
//         )
//     }

//     stages {
//         stage('Configure SSH') {
//             steps{
//                 sh '''
//                     mkdir -p ~/.ssh
//                     chmod 400 ~/.ssh
//                     echo -e "Host *\\n\\tStrictHostKeyChecking no\\n\\n" > ~/.ssh/config
//                     cat ~/.ssh/config
//                     touch ~/.ssh/known_hosts
//                     chmod 600 ~/.ssh/known_hosts
//                 '''
//             }
//         }

//         stage('SSH Key Access') {
//             steps {
//                 // Use double quotes for Groovy variable interpolation
//                 sh '''
//                     mkdir -p /tmp/jenkins_keys
//                     echo "$SSH_KEY64" | base64 -d > /tmp/jenkins_keys/mykey.pem
//                     chmod 600 /tmp/jenkins_keys/mykey.pem
//                     ssh-keygen -R ${params.SERVER_IP} || true
//                 '''
//             }
//         }

//         stage('Deploy Code to Server') {
//             steps {
//                 //Use triple double quotes for Groovy Interpolation
//                 ssh '''
//                     ssh -i /tmp/jenkins_keys/myKey.pem ec2-user@{params.SERVER_IP} \
//                     "cd /usr/share/nginx/html && git pull"
//                 '''     
//             }
//         }
//     }
// }



// pipeline {
//     agent any
//     environment {
//         APP_DIR  = 'jenkins-website'
//         REPO_URL = 'https://github.com/Pujanstha1/Jenkins.git'
//         SSH_USER = 'ubuntu'
//     }
//     stages {
//         stage('Checkout') {
//             steps {
//                 checkout scm
//             }
//         }
//         stage('Configure SSH & Deploy') {
//             steps {
//                 withCredentials([
//                     string(credentialsId: 'SERVER_IP', variable: 'SERVER_IP'),
//                     file(credentialsId: 'SSH_KEY64', variable: 'SSH_KEY_FILE')
//                 ]) {
//                     sh '''
//                     set -e
//                     chmod 400 "$SSH_KEY_FILE"
//                     mkdir -p ~/.ssh
//                     chmod 700 ~/.ssh
//                     echo -e "Host *\\n\\tStrictHostKeyChecking no\\n" > ~/.ssh/config
//                     chmod 600 ~/.ssh/config
//                     ssh-keygen -R "$SERVER_IP" || true
//                     ssh -i "$SSH_KEY_FILE" ${SSH_USER}@${SERVER_IP} << 'EOF'
//                       set -e
//                       APP_DIR="jenkins-website"
//                       REPO_URL="https://github.com/Pujanstha1/Jenkins.git"
//                       if [ -d ~/$APP_DIR/.git ]; then
//                         cd ~/$APP_DIR
//                         git reset --hard
//                         git pull origin main
//                       else
//                         git clone $REPO_URL $APP_DIR
//                         cd ~/$APP_DIR
//                       fi
//                       docker compose pull
//                       docker compose up -d --build
//                     '''
//                 }
//             }
//         }
//     }
//     post {
//         success {
//             echo "Deployment successful"
//         }
//         failure {
//             echo "Deployment failed"
//         }
//     }
// }