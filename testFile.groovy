#!/usr/bin/env groovy

def GitBranch = "${Git_Branch}"

pipeline {
    agent any
     parameters { 
         string(name: 'Git_Branch', defaultValue: 'master', description: 'Branch Name')
     }

    stages {
        stage('CleanWorkspace') {
            steps {
                cleanWs()
            }
        }

        stage('Cloning BuildCode') {
          steps {
              script {
                echo "${GitBranch}"
                git branch: "${GitBranch}",
                credentialsId: 'GitHub',
                url: "https://github.com/smartlizzard/test.git"
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
                }
            }
        }
        stage('Test2') {
            steps {
                script {
                    load "file.propertise"
                    echo "${PUB1}"
                }
            }
        }
    }
}
