#! /bin/bash

#################################
# Update Maven Release folder
#################################

# Merge Script
if [ -d "Maven" ]; then
  cd Maven/
fi

set -e

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

# $1 - absolute pom path, $2 - absolute aar path
function mavenDeployWithoutSources() {
  echoX "Deploying ${2} on Maven..."

    mvn gpg:sign-and-deploy-file "-DpomFile=${1}" "-Dfile=${2}" "-DrepositoryId=ossrh" "-Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/" "-DstagingRepositoryId=ossrh" || {
      echoX "Deploy failed!"
      echoX "End Script"
      exit 1
    }

  echoX "Please complete the release process by promoting the jar at https://oss.sonatype.org/#stagingRepositories"
}

BASE_DIR="$PWD"
DEPLOY_DIR_NAME="filesToDeploy"
DEPLOY_DIR_ABSOLUTE="$BASE_DIR/$DEPLOY_DIR_NAME"

rm -r $DEPLOY_DIR_ABSOLUTE || true
mkdir $DEPLOY_DIR_ABSOLUTE

cd ..
sh ./buildPrebidMobile.sh

cp -r ../generated/* $DEPLOY_DIR_ABSOLUTE || true

modules=("PrebidMobile" "PrebidMobile-core" "PrebidMobile-rendering")
for n in ${!modules[@]}; do
  #######
  # Start
  #######

  echo -e "\n"
  echoX "Deploying ${modules[$n]} on Maven..."

  #######
  # Deploy
  #######
  module="${modules[$n]}"
  mavenDeploy $"$BASE_DIR/${module}-pom.xml" $"$DEPLOY_DIR_ABSOLUTE/${module}.jar" $"$DEPLOY_DIR_ABSOLUTE/${module}-sources.jar" $"$DEPLOY_DIR_ABSOLUTE/${module}-javadoc.jar"

  #######
  # End
  #######
  echoX "Please complete the release process by promoting the jar at https://oss.sonatype.org/#stagingRepositories"

done

#######
# Open measurement SDK
#######
mavenDeployWithoutSources $"PrebidMobile-open-measurement-pom.xml" $"$DEPLOY_DIR_ABSOLUTE/omsdk.jar"

echoX "End Script"
