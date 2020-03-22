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
                    def jsonSlurper = new JsonSlurper()
                    def input = new JsonSluper().parse(new File ("${env.WORKSPACE}/properties.json")
                    def count = input.get("Properties").get(0).get("Environment").get(0).get("Prod").get(0).get("Tags").get(0).get("Dispature")
                    echo "Count = ${count}"
                    /*
                    for (int i = 1; i <= count.lenth; i++) {
                        for (key in props.Properties.Environment.Prod.Tags.Dispature.get(i)) {
                            echo "key=${key}"
                        }   
                    }
                    */
                    /*
                    props.Properties.Environment.Prod.Tags.Dispature.each { key, value ->
                        echo "Walked through key $key and value $value"
                    }*/
                }
            }
        }
    }
}
