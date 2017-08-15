#! /bin/bash

#################################
# Update Maven Release folder
#################################

rm PrebidMobile.jar >/dev/null 2>/dev/null
rm PrebidMobile-sources.jar >/dev/null 2>/dev/null
rm PrebidMobile-javadoc.jar >/dev/null 2>/dev/null
cp ../out/PrebidMobile.jar .
cp ../out/PrebidMobile.jar PrebidMobile-sources.jar
cp -r ../out/Javadoc Javadoc
jar cf PrebidMobile-javadoc.jar Javadoc

function echoX {
echo -e "PREBID DEPLOY-LOG: $@"
}

#######
# Start
#######
echoX "Deploying Prebid Mobile SDK on Maven..."


#######
# Deploy
#######
mvn gpg:sign-and-deploy-file "-DpomFile=pom.xml" "-Dfile=PrebidMobile.jar" "-DrepositoryId=ossrh" "-Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/" "-DstagingRepositoryId=ossrh" "-Dsources=PrebidMobile-sources.jar" "-Djavadoc=PrebidMobile-javadoc.jar" || { echoX "Deploy failed!"; echoX "End Script"; exit 1; } 



#######
# End
#######
echoX "Please complete the release process by promoting the jar at https://oss.sonatype.org/#stagingRepositories"
echoX "End Script"
