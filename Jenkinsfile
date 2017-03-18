pipeline {
  agent any
  stages {
    stage('Test') {
      steps {
        sh './gradlew clean test'
      }
    }
    stage('Build') {
      steps {
        parallel(
          "Build sources": {
            sh './gradlew sourcesJar'
          },
          "Build javadoc": {
            sh './gradlew javadocJar'
          },
          "Build lib": {
            sh './gradlew jar'
          }
        )
      }
    }
    stage('Artifacts') {
      steps {
        parallel(
          "Fingerprint": {
            fingerprint 'build/libs/*.jar'
          },
          "Publish test results": {
            junit 'build/test-results/**/*.xml'
          },
          "Archive": {
            archiveArtifacts 'build/libs/*.jar'
          }
        )
      }
    }
    stage('Publish Artifact') {
      when {
        expression { env..BRANCH_NAME == "master" }
      }
      steps {
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'bintray',
                                    usernameVariable: 'BINTRAY_USERNAME', passwordVariable: 'BINTRAY_API_KEY']]) {
          sh './gradlew bintrayUpload'
        }
      }

      when {
        expression { env..BRANCH_NAME == "master" }
      }
      steps {
        sh 'echo "Only publishing master"'
      }
    }
  }
  triggers {
    pollSCM('* * * * *')
  }
}