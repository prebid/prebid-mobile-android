#! /bin/bash

#################################
# Update Maven Release folder
#################################

# Merge Script
if [ -d "Maven" ]; then
cd Maven/
fi

set -e

function echoX {
echo -e "PREBID DEPLOY-LOG: $@"
}

BASE_DIR="$PWD"
DEPLOY_DIR_NAME="filesToDeploy"
DEPLOY_DIR_ABSOLUTE="$BASE_DIR/$DEPLOY_DIR_NAME"

rm -r $DEPLOY_DIR_ABSOLUTE || true
mkdir $DEPLOY_DIR_ABSOLUTE

cd ..
sh ./buildPrebidMobile.sh

cp ../generated/* $DEPLOY_DIR_ABSOLUTE || true

modules=("PrebidMobile" "PrebidMobile-core")

for n in ${!modules[@]}; do

	rm -r $BASE_DIR/${modules[$n]}-pom.xml.asc || true

	#######
	# Start
	#######

	echo -e "\n"
	echoX "Deploying ${modules[$n]} on Maven..."

	#######
	# Deploy
	#######
	mvn gpg:sign-and-deploy-file "-DpomFile=$BASE_DIR/${modules[$n]}-pom.xml" "-Dfile=$DEPLOY_DIR_ABSOLUTE/${modules[$n]}.jar" "-DrepositoryId=ossrh" "-Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/" "-DstagingRepositoryId=ossrh" "-Dsources=$DEPLOY_DIR_ABSOLUTE/${modules[$n]}-sources.jar" "-Djavadoc=$DEPLOY_DIR_ABSOLUTE/${modules[$n]}-javadoc.jar" || { echoX "Deploy failed!"; echoX "End Script"; exit 1; } 

	#######
	# End
	#######
	echoX "Please complete the release process by promoting the jar at https://oss.sonatype.org/#stagingRepositories"

done

echoX "End Script"
