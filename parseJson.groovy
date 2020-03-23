#!/usr/bin/env groovy
import groovy.json.JsonSlurper
def PUB (){
    def value = sh (script: "aws s3 ls",returnStdout: true).trim()
    return "$value"
}

pipeline {
    agent any
    parameters { 
        string(name: 'CODE_BRANCH', defaultValue: 'master', description: 'Branch Name')
        choice(name: 'ENVIRONMENT', choices: ['Prod', 'Qa', 'Stag', 'Dev'], description: 'Environment')
        choice(name: 'PUBLISHER', choices: PUB(), description: 'Environment')
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
                    def props = readJSON file: 'properties.json', returnPojo: true
                    def disp = props.Properties.get(0).Environment.get(0)."${params.ENVIRONMENT}".get(0).Tags.get(0).Dispature.get(0)
                    disp.each { key, value ->
                        echo "$key = $value"
                        disptag = "$value"
                        echo "DISPTAG = $disptag"
                    }
                }
            }
        }
    }
}
