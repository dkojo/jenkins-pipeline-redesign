// The redesigned pipeline. Every service gets the same reusable steps from the shared library
// (jenkins/shared-library/vars/svc.groovy), so adding a service is one line, not a copy-pasted job.
@Library('redesign') _

def services = [
  [label: '.NET', name: 'catalog', lang: 'dotnet', dir: 'services/catalog'],
  [label: 'Java', name: 'orders',  lang: 'java',   dir: 'services/orders'],
  [label: 'Node', name: 'cart',    lang: 'node',   dir: 'services/cart'],
]

pipeline {
  agent any
  options {
    timestamps()
    disableConcurrentBuilds()
    timeout(time: 20, unit: 'MINUTES')
  }
  stages {
    stage('Build, test, package') {
      steps {
        script {
          // All three services at once, each through build, test and package
          parallel services.collectEntries { s ->
            [(s.label): {
              svc.step("${s.label} build")   { svc.build(s) }
              svc.step("${s.label} test")    { svc.test(s) }
              svc.step("${s.label} package") { svc.pack(s) }
            }]
          }
        }
      }
    }
    stage('Deploy') {
      steps { script { svc.step('Deploy to Tomcat and NGINX') { svc.deploy() } } }
    }
    stage('Smoke') {
      steps { script { svc.step('Smoke test') { svc.smoke() } } }
    }
    stage('Release') {
      steps {
        script {
          svc.step('Release') { svc.release(services) }
        }
      }
    }
  }
}
