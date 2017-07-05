#! /bin/bash

#################################
# Update Maven Release folder
#################################
rm PrebidMobile.jar >/dev/null 2>/dev/null
rm PrebidMobile-sources.jar >/dev/null 2>/dev/null
rm PrebidMobile-sources.jar >/dev/null 2>/dev/null
cp ../out/PrebidMobile.jar .
cp ../out/PrebidMobile.jar PrebidMobile-sources.jar

function echoX {
echo -e "APPNEXUS DEPLOY-LOG: $@"
}

spinner()
{
local pid=$1
local delay=0.75
local spinstr='|/-\'
while [ "$(ps a | awk '{print $1}' | grep $pid)" ]; do
local temp=${spinstr#?}
printf " [%c]  " "$spinstr"
local spinstr=$temp${spinstr%"$temp"}
sleep $delay
printf "\b\b\b\b\b\b"
done
printf "    \b\b\b\b"
}

#######
# Start
#######
echoX "Deploying Prebid Mobile SDK on Maven..."


#######
# Deploy
#######
(mvn gpg:sign-and-deploy-file "-DpomFile=pom.xml" "-Dfile=PrebidMobile.jar" "-DrepositoryId=ossrh" "-Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/" "-DstagingRepositoryId=ossrh" "-Dsources=PrebidMobile-sources.jar" "-Djavadoc=PrebidMobile-javadoc.jar" || { echoX "Deploy failed!"; echoX "End Script"; exit 1; } ) & spinner $!



#######
# End
#######
echoX "Please complete the release process by promoting the jar at https://oss.sonatype.org/#stagingRepositories"
echoX "End Script"
