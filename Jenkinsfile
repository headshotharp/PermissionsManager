pipeline {
  agent {
    docker {
      image 'registry.gmasil.de/docker/maven-build-container'
      args '-v /maven:/maven -e JAVA_TOOL_OPTIONS=\'-Duser.home=/maven\' -u root:root'
    }
  }
  stages {
    stage('compile') {
      steps {
        sh 'mvn clean package --fail-at-end'
      }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: 'target/permissions-manager.jar', fingerprint: true
      cleanWs()
      dir("${env.WORKSPACE}@tmp") {
        deleteDir()
      }
    }
  }
}
