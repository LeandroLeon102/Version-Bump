pipeline {
    agent {label "ecs"}
    options {buildDiscarder(logRotator(daysToKeepStr: "7", numToKeepStr: "10"))}

    stages{
        stage('Checkout repository'){
            steps {
                checkout scm: [
                    $class: 'GitSCM',
                    branches: [[name: params.TARGET_BRANCH]],
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: params.TARGET_REPOSITORY]],
                    userRemoteConfigs: [[
                        credentialsId: '<credentials>',
                        url: "<url>/${TARGET_REPOSITORY}"
                    ]]
                ]
            }
        }

        stage('version bump'){
            steps {
                dir(params.TARGET_REPOSITORY) {
                    script {
                        def status = sh(returnStatus: true, script: "cz bump")
                        if (status == 21) {
                            sh """
                                echo 'Last Commit message has no valid action for version bump. Forcing minor version bump...'
                            """
                            sh 'git commit --allow-empty -m "fix: force patch version bump - no valid action in last commit message"'
                            sh 'cz bump'
                        }
                    }
                }
            }
        }

        stage('Push to remote'){
            steps{
                dir(params.TARGET_REPOSITORY) {
                    sshagent (credentials: ['<credentials>']) {
                        sh("git checkout ${TARGET_BRANCH}")
                        sh("git push origin")
                        sh('git push origin --tags')
                    }
                }
            }
        }
    }
}
