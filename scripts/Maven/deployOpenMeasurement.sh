#! /bin/bash

#################################
# This script deploys Open measurement SDK. It must be called only when
# omsdk-android module have a new version.
#################################

function echoX() {
  echo -e "PREBID DEPLOY-LOG: $@"
}

# $1 - absolute pom path, $2 - absolute aar path, $3 - absolute source path, $4 - absolute javadoc path
function mavenDeploy() {
  echoX "Deploying ${2} on Maven..."

    mvn gpg:sign-and-deploy-file "-DpomFile=${1}" "-Dfile=${2}" "-DrepositoryId=ossrh" "-Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/" "-DstagingRepositoryId=ossrh" "-Dsources=${3}" "-Djavadoc=${4}" || {
      echoX "Deploy failed!"
      echoX "End Script"
      exit 1
    }

  echoX "Please complete the release process by promoting the jar at https://oss.sonatype.org/#stagingRepositories"
}

BASE_DIR="$PWD"
DEPLOY_DIR_ABSOLUTE="$BASE_DIR/filesToDeploy"

rm -r "$DEPLOY_DIR_ABSOLUTE" || true
mkdir "$DEPLOY_DIR_ABSOLUTE"

cd ..
bash ./buildPrebidMobile.sh
cp -r ../generated/* "$DEPLOY_DIR_ABSOLUTE" || true

mavenDeploy $"$BASE_DIR/PrebidMobile-open-measurement-pom.xml" $"$DEPLOY_DIR_ABSOLUTE/omsdk.jar" $"$BASE_DIR/stub.jar" $"$BASE_DIR/stub.jar"

echoX "End Script"
