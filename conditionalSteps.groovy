#!/usr/bin/env groovy

def SOURCE_CODE_BRANCH = "${CODE_BRANCH}"

pipeline {
    agent any
    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }
    parameters { 
        string(name: 'CODE_BRANCH', defaultValue: 'Development', description: 'Branch Name').trim()
        choice(name: 'PUBLISHER', choices: ['PUB1', 'PUB2', 'PUB3', 'PUB4'], description: 'Deploy On').trim()
    }

    stages {
        stage('CLEAN_WORKSPACE') {
            steps {
                cleanWs()
                sh 'printenv'
            }
        }

        stage('SCRIPT_CHECKOUT') {
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

        stage('CREATE_INVENTORY') {
            steps {
                script {
                    load "tags.properties"
                    if ( params.PUBLISHER == 'PUB1' ) {
                        writeFile file: 'inventory.ini', text: '[Diapatcher]\n'
                        sh "echo ${DISP1} >> inventory.ini"
                        sh "echo [RestDiapatcher] >> inventory.ini"
                        sh "echo ${DISP2} >> inventory.ini"
                        sh "echo ${DISP3} >> inventory.ini"
                        sh "echo ${DISP4} >> inventory.ini"
                    }
                    if ( params.PUBLISHER == 'PUB2' ) {
                        writeFile file: 'inventory.ini', text: '[Diapatcher]\n'
                        sh "echo ${DISP2} >> inventory.ini"
                        sh "echo [RestDiapatcher] >> inventory.ini"
                        sh "echo ${DISP1} >> inventory.ini"
                        sh "echo ${DISP3} >> inventory.ini"
                        sh "echo ${DISP4} >> inventory.ini"
                    }
                    if ( params.PUBLISHER == 'PUB3' ) {
                        writeFile file: 'inventory.ini', text: '[Diapatcher]\n'
                        sh "echo ${DISP3} >> inventory.ini"
                        sh "echo [RestDiapatcher] >> inventory.ini"
                        sh "echo ${DISP1} >> inventory.ini"
                        sh "echo ${DISP2} >> inventory.ini"
                        sh "echo ${DISP4} >> inventory.ini"
                    }
                    if ( params.PUBLISHER == 'PUB4' ) {
                        writeFile file: 'inventory.ini', text: '[Diapatcher]\n'
                        sh "echo ${DISP4} >> inventory.ini"
                        sh "echo [RestDiapatcher] >> inventory.ini"
                        sh "echo ${DISP1} >> inventory.ini"
                        sh "echo ${DISP2} >> inventory.ini"
                        sh "echo ${DISP3} >> inventory.ini"
                    }
                    sh 'cat inventory.ini'
                }
            }
        }
        /*
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
        */
        stage('DEPLOY') {
            steps {
                script {
                    load "${PUBLISHER}.groovy"
                    "${PUBLISHER}"()
                }
            }
        }
    }
}
