#!/usr/bin/env groovy

pipeline {
    agent any
    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }
    parameters {
        choice choices: ['PUB1', 'PUB2', 'PUB3', 'PUB4'], description: 'Select Publisher', name: 'PUBLISHER'
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
                    echo "${GIT_BRANCH_LOCAL}"
                    echo "${GIT_URL}"
                    git branch: "${GIT_BRANCH_LOCAL}",
                    credentialsId: 'GitHub',
                    url: "${GIT_URL}"
                }
            }
        }
        /*
        stage('CREATE_INVENTORY') {
            steps {
                script {
                    sh "ls -lrth"
                    sh "sh script.sh ${params.PUBLISHER}"
                    sh 'cat inventory.ini'
                }
            }
        }
        */
        stage('CREATE_INVENTORY') {
            steps {
                script {
                    def lines = readFile('./tags.properties').readLines()
                    def count = '1';
                    while("$count" <= lines.size()) {
                        def DISPIP = sh (script: "grep DISP$count= disp.properties | sed -e 's|DISP$line=/||g' | sed -e 's|\"/||g'",returnStdout: true).trim()
                        echo "DISP IP IS $DISPIP"
                        echo "Count is $count"
                    count++;
                    }
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
        /*
        stage('DEPLOY') {
            steps {
                script {
                    load "${PUBLISHER}.groovy"
                    "${PUBLISHER}"()
                }
            }
        }
        */
    }
}
