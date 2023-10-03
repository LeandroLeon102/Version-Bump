pipelineJob ('version_bump') {
    description('Version Bump repository')

    parameters {[
            stringParam('TARGET_BRANCH', 'main', 'Target branch Name'),
            stringParam('TARGET_REPOSITORY', '<repository>', 'Target repository name'),
            stringParam('JENKINS_BRANCH', 'main', 'Target repository name')
    ]}

    properties {
        pipelineTriggers {
            triggers {
                pollSCM {
                    scmpoll_spec('*/2 * * * *')
                }
            }
        }
    }

    definition {
        cpsScm {
            lightweight(true)
            scriptPath('jenkinsfiles/version-bump.groovy')
            scm {
                git {
                    branch('${JENKINS_BRANCH}')
                    remote {
                        credentials('<creds>')
                        url('<url>')
                    }
                }
            }
        }
    }
}
