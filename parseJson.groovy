#!/usr/bin/env groovy
import groovy.json.JsonSlurper

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
                    def inputFile = new File('properties.json')
                    def inputJSON = new JsonSlurper().parse(inputFile)
                    def keys = inputJSON.keySet() as List
                    echo "keys = $keys"
                    /*
                    props.Properties.Environment.Prod.Tags.Dispature.each { key, value ->
                        echo "Walked through key $key and value $value"
                    }*/
                }
            }
        }
    }
}
