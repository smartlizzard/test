#!/usr/bin/env groovy

def SOURCE_CODE_BRANCH = "${CODE_BRANCH}"

def CACHE_CLEAR (){
    stage ('CACHE_CLEAR') {
        ansiColor('xterm') {
           ansiblePlaybook(
               playbook: 'playbook.yaml',
               inventory: 'inventory.ini',
               limit: "${PUBLISHER}",
               credentialsId: 'ansibleDeploy',
               disableHostKeyChecking: true,
               colorized: true
            )
        }
    }
}
def PUB1(){
    stage('PUB1_DEPLOY') {
        echo "${PUB1}"
    }
}

def PUB2(){
    stage ('STOP_HTTPD') {
        ansiColor('xterm') {
            ansiblePlaybook(
                playbook: 'service.yaml',
                inventory: 'inventory.ini',
                limit: "${PUBLISHER}",
                credentialsId: 'ansibleDeploy',
                disableHostKeyChecking: true,
                extras: '-e status="stop"',
                colorized: true
             )
        }
    }
    stage ('PUB2_DEPLOY') {
        echo "${PUB2}"
    }
    stage ('START_HTTPD') {
        ansiColor('xterm') {
            ansiblePlaybook(
                playbook: 'service.yaml',
                inventory: 'inventory.ini',
                limit: "${PUBLISHER}",
                credentialsId: 'ansibleDeploy',
                disableHostKeyChecking: true,
                extras: '-e status="start"',
                colorized: true
             )
        }
    }
}


pipeline {
    agent any
     parameters { 
         string(name: 'CODE_BRANCH', defaultValue: 'master', description: 'Branch Name')
         choice(name: 'PUBLISHER', choices: ['PUB1', 'PUB2'], description: 'Deploy On')
     }

    stages {
        stage('CleanWorkspace') {
            steps {
                cleanWs()
                sh 'printenv'
            }
        }

        stage('Checkout') {
          steps {
              script {
                  def GIT_BRANCH_LOCAL = sh (script: "echo ${GIT_BRANCH} | sed -e 's|origin/||g'",returnStdout: true).trim()
                  echo "${CODE_BRANCH}"
                  echo "${GIT_BRANCH_LOCAL}"
                  echo "${GIT_URL}"
                  git branch: "${GIT_BRANCH_LOCAL}",
                      credentialsId: 'GitHub',
                      url: "${GIT_URL}"
                  }
              }
            }
        
        stage('Test') {
            steps {
                script {
                    def files = readFile('./tags.properties').readLines()
                        for (int i = 1; i <= files.size(); i++) {
                            echo "Number of IP : $i" 
                            sh "echo \"PUB$i=\$(aws ec2 describe-instances --filter \"Name=tag:Name,Values=`head -n$i ./tags.properties | tail -1`\"  --region us-east-1  | jq .Reservations[].Instances[].NetworkInterfaces[].PrivateIpAddress)\" >> file.propertise"
                        }
                    sh "cat file.propertise"    
                }
            }
        }
        stage('Test1') {
            steps {
                script {
                    def files = readFile('./tags.properties').readLines()
                        for (int i = 1; i <= files.size(); i++) {
                            echo "Number of IP : $i"
                            sh "echo [PUB$i] >> inventory.ini"
                            sh "echo \"\$(aws ec2 describe-instances --filter \"Name=tag:Name,Values=`head -n$i ./tags.properties | tail -1`\"  --region us-east-1  | jq -r .Reservations[].Instances[].NetworkInterfaces[].PrivateIpAddress)\" >> inventory.ini"
                        }
                    sh "cat inventory.ini"
                    load "file.propertise"
                }
            }
        }
        stage('DEPLOY') {
            steps {
                script {
                    echo "${PUBLISHER}"
                    CACHE_CLEAR()
                    "${PUBLISHER}"()
                }
            }
        }
    }
}
