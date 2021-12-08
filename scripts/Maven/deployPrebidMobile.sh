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

BASE_DIR="$PWD"
DEPLOY_DIR_NAME="filesToDeploy"
DEPLOY_DIR_ABSOLUTE="$BASE_DIR/$DEPLOY_DIR_NAME"

rm -r $DEPLOY_DIR_ABSOLUTE || true
mkdir $DEPLOY_DIR_ABSOLUTE

cd ..
sh ./buildPrebidMobile.sh

cp -r ../generated/* $DEPLOY_DIR_ABSOLUTE || true

modules=("PrebidMobile" "PrebidMobile-core" "PrebidMobile-rendering" "PrebidMobile-gamEventHandlers" "PrebidMobile-mopubAdapters")
extensions=("jar" "jar" "aar" "jar" "jar")
for n in ${!modules[@]}; do
  #######
  # Start
  #######

  echo -e "\n"
  echoX "Deploying ${modules[$n]} on Maven..."

  #######
  # Deploy
  #######
  extension="${extensions[$n]}"
  module="${modules[$n]}"
  if [ $extension == "aar" ]; then
    compiledPath=$"$DEPLOY_DIR_ABSOLUTE/aar/${module}-release.aar"
  else
    compiledPath=$"$DEPLOY_DIR_ABSOLUTE/${module}.jar"
  fi
  mavenDeploy $"$BASE_DIR/${module}-pom.xml" "$compiledPath" $"$DEPLOY_DIR_ABSOLUTE/${module}-sources.jar" $"$DEPLOY_DIR_ABSOLUTE/${module}-javadoc.jar"

  #######
  # End
  #######
  echoX "Please complete the release process by promoting the jar at https://oss.sonatype.org/#stagingRepositories"

done

#######
# Open measurement SDK
#######
mavenDeploy $"$BASE_DIR/PrebidMobile-open-measurement-pom.xml" $"$DEPLOY_DIR_ABSOLUTE/omsdk.jar" $"$BASE_DIR/stub.jar" $"$BASE_DIR/stub.jar"

echoX "End Script"
