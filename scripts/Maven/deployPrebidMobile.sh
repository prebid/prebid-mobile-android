#! /bin/bash

#################################
# Update Maven Release folder
#################################

function echoX {
echo -e "PREBID DEPLOY-LOG: $@"
}

DEPLOY_DIR="filesToDeploy"

rm -r $DEPLOY_DIR
mkdir $DEPLOY_DIR

sh ../buildPrebidMobile.sh

cp ../../generated/* $DEPLOY_DIR

# modules=("PrebidMobile" "PrebidMobile-core")
modules=("PrebidMobile-core")

for n in ${!modules[@]}; do
	#######
	# Start
	#######
	echoX "Deploying Prebid Mobile SDK on Maven..."

	#######
	# Deploy
	#######
	mvn gpg:sign-and-deploy-file "-DpomFile=pom-${modules[$n]}.xml" "-Dfile=$DEPLOY_DIR/${modules[$n]}.jar" "-DrepositoryId=ossrh" "-Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/" "-DstagingRepositoryId=ossrh" "-Dsources=$DEPLOY_DIR/${modules[$n]}-sources.jar" "-Djavadoc=$DEPLOY_DIR/${modules[$n]}-javadoc.jar" || { echoX "Deploy failed!"; echoX "End Script"; exit 1; } 

	#######
	# End
	#######
	echoX "Please complete the release process by promoting the jar at https://oss.sonatype.org/#stagingRepositories"

done

echoX "End Script"
