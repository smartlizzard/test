#!/usr/bin/env groovy


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
                    def count = props.Properties.Environment.Dev.Tags.Dispature.size()
                    echo "count = $count"
                    for (int i = 1; i <= props.Properties.Environment.Dev.Tags.Dispature.size(); i++) {
                        for (key in props.Properties.Environment.Dev.Tags.Dispature.get(i)) {
                            echo "key=${key}"
                        }
                    }
                }
            }
        }
    }
}
