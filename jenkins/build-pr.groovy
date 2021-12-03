pipeline {
    agent none

    environment {
        JOB_ID = getJobId()
    }

    stages {

        stage('Build') {

            when { not { branch 'develop' } }
            agent { label 'mobile' }

            steps {
                sh 'hostname'
                sh 'echo $ANDROID_SDK_ROOT'
                sh 'echo $ANDROID_HOME'
                setCommitShortEnv()
                sh 'bash scripts/disable_web_proxy_if_needed.sh'
                sh 'bundle install'
                sh 'bundle exec fastlane build_apps'
            }

            post {
                cleanup { deleteDir() }
            }
        }

        stage('Tests') {

            when { not { branch 'develop' } }

            parallel {

                stage('Unit Tests') {
                    agent { label 'mobile' }

                    steps {
                        sh 'hostname'
                        sh 'bash scripts/disable_web_proxy_if_needed.sh'
                        runFastlaneLane('unit_tests')
                    }
                    post {
                        cleanup {
                            deleteDir()
                        }
                    }
                }

                stage('UI Tests - PPM') {
                    agent { label 'android-tests' }

                    steps {
                        runUiTests('ui_tests_ppm')
                    }
                    post {
                        cleanup {
                            deleteDir()
                        }
                    }
                }

                stage('UI Tests - GAM') {
                    agent { label 'android-tests' }

                    steps {
                        runUiTests('ui_tests_gam')
                    }
                    post {
                        cleanup {
                            deleteDir()
                        }
                    }
                }

                stage('UI Tests - MoPub') {
                    agent { label 'android-tests' }

                    steps {
                        runUiTests('ui_tests_mopub')
                    }
                    post {
                        cleanup {
                            deleteDir()
                        }
                    }
                }
            }
        }
    }
}

static def getJobId() {
    String datePart = new Date().format('yyyyMMddHHmmss')
    int randomInt = new Random().nextInt((int) 9e7) + (int) 1e7
    return datePart + "-" + randomInt.toString()
}

def setCommitShortEnv() {
    script{
        env.COMMIT_SHORT = sh(script:'git rev-parse --short HEAD', returnStdout: true).trim()
    }
}

def runUiTests(String task){
    sh 'hostname'
    prepareEmulator()
    setupMockServer()
    runFastlaneLane(task)
}

def runFastlaneLane(String task){
    sh 'bundle install'
    sh "bundle exec fastlane ${task}"
}

def prepareEmulator() {
    sh 'caffeinate -u -t 1'
    sh 'bash scripts/disable_web_proxy_if_needed.sh'
    sh 'bash scripts/kill_emulators.sh'
}

def setupMockServer() {
    sh 'git clone git@github.com:openx/mobile-mock-server.git'
    sh 'mobile-mock-server/install.sh'
    sh 'python3 mobile-mock-server/manage.py makemigrations'
    sh 'python3 mobile-mock-server/manage.py migrate'
    sh 'python3 mobile-mock-server/manage.py runserver_plus 0.0.0.0:8000 --cert-file mobile-mock-server/emulator.crt &'
}