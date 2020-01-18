#!/usr/bin/env groovy

def SOURCE_CODE_BRANCH = "${CODE_BRANCH}"
def PUB1(){
    stage('PUB1_DEPLOY') {
        steps {
            script {
                load "file.propertise"
                echo "${PUB1}"
            }
        }
    } 
}

def PUB2(){
    stage('PUB2_DEPLOY') {
        steps {
            script {
                load "file.propertise"
                echo "${PUB2}"
            }
        }
    }
}


pipeline {
    agent any
     parameters { 
         string(name: 'CODE_BRANCH', defaultValue: 'master', description: 'Branch Name')
     }

    stages {
        stage('CleanWorkspace') {
            steps {
                cleanWs()
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
                            sh "echo [PUB$i] >> file2.propertise"
                            sh "echo \"\$(aws ec2 describe-instances --filter \"Name=tag:Name,Values=`head -n$i ./tags.properties | tail -1`\"  --region us-east-1  | jq -r .Reservations[].Instances[].NetworkInterfaces[].PrivateIpAddress)\" >> file2.propertise"
                        }
                    sh "cat file2.propertise"
                    load "file.propertise"
                }
            }
        }
        stage('DEPLOY') {
            steps {
               PUB1()
            }
        }
    }
}
