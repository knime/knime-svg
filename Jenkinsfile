#!groovy
library "knime-pipeline@$DEFAULT_LIBRARY_VERSION"

properties([
    pipelineTriggers([
        upstream('knime-base/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
    ]),
    parameters(workflowTests.getConfigurationsAsParameters()),
    buildDiscarder(logRotator(numToKeepStr: '5')),
    disableConcurrentBuilds()
])

try {
    knimetools.defaultTychoBuild('org.knime.update.ext.svg')

    workflowTests.runTests(
        dependencies: [ repositories: ['knime-core', 'knime-svg', 'knime-json', 'knime-xml', 'knime-productivity-oss', 'knime-filehandling', 'knime-reporting'], ius: ['org.knime.json.tests'] ]
    )

    stage('Sonarqube analysis') {
        env.lastStage = env.STAGE_NAME
        workflowTests.runSonar()
    }
} catch (ex) {
    currentBuild.result = 'FAILURE'
    throw ex
} finally {
    notifications.notifyBuild(currentBuild.result);
}
/* vim: set shiftwidth=4 expandtab smarttab: */
