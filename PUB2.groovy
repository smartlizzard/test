#!/usr/bin/env groovy

def PUB2(){
    stage('HTTP_SERVICE_STOP') {
        ansiColor('xterm') {
           ansiblePlaybook(
               playbook: 'service.yaml',
               inventory: 'inventory.ini',
               limit: 'Dispatcher',
               credentialsId: 'ansibleDeploy',
               disableHostKeyChecking: true,
               extras: '-e status="stop"',
               colorized: true
            )
        }
    }
    stage('PUB2_DEPLOY') {
        echo "${PUB2}"
    }
    stage('CACHE_CLEAR') {
        ansiColor('xterm') {
           ansiblePlaybook(
               playbook: 'playbook.yaml',
               inventory: 'inventory.ini',
               limit: 'Dispatcher',
               credentialsId: 'ansibleDeploy',
               disableHostKeyChecking: true,
               colorized: true
            )
        }
    }
    stage('AKAMAI_CLEAR') {
        input {
            message "Should Akamai cache clear ?"
            ok "Yes"
        }
    }
    stage('PUBLISHER_URL_CHECK') {
        echo "${PUB2}"
    }
    stage('HTTP_SERVICE_START') {
        ansiColor('xterm') {
           ansiblePlaybook(
               playbook: 'service.yaml',
               inventory: 'inventory.ini',
               limit: 'Dispatcher',
               credentialsId: 'ansibleDeploy',
               disableHostKeyChecking: true,
               extras: '-e status="start"',
               colorized: true
            )
        }
    }
    stage('INPUT_FOR_PROCEED') {
        input {
            message "Should we continue Deployment ?"
            ok "Yes, we should."
        }
    }
    stage('REST_HTTP_SERVICE_STOP') {
        ansiColor('xterm') {
           ansiblePlaybook(
               playbook: 'service.yaml',
               inventory: 'inventory.ini',
               limit: 'RestDiapatcher',
               credentialsId: 'ansibleDeploy',
               disableHostKeyChecking: true,
               extras: '-e status="stop"',
               colorized: true
            )
        }
    }
    stage('REST_PUBLISHER_DEPLOY') {
        echo "${PUB1}"
        echo "${PUB3}"
        echo "${PUB4}"
    }
    stage('REST_CACHE_CLEAR') {
        ansiColor('xterm') {
           ansiblePlaybook(
               playbook: 'playbook.yaml',
               inventory: 'inventory.ini',
               limit: 'RestDiapatcher',
               credentialsId: 'ansibleDeploy',
               disableHostKeyChecking: true,
               colorized: true
            )
        }
    }
    stage('REST_PUBLISHER_URL_CHECK') {
        echo "${PUB1}"
        echo "${PUB3}"
        echo "${PUB4}"
    }
    stage('REST_AKAMAI_CLEAR') {
        input {
            message "Should Akamai cache clear ?"
            ok "Yes"
        }
    }
    stage('REST_HTTP_SERVICE_START') {
        ansiColor('xterm') {
           ansiblePlaybook(
               playbook: 'service.yaml',
               inventory: 'inventory.ini',
               limit: 'Dispatcher',
               credentialsId: 'ansibleDeploy',
               disableHostKeyChecking: true,
               extras: '-e status="start"',
               colorized: true
            )
        }
    }
}