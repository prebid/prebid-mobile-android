#! /bin/bash
### Constants
# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
NO_COLOR='\033[0m'

# Paths
if [ -d "scripts" ]; then
  cd scripts/
fi

set -e
cd ..
BASEDIR="$PWD"
PATH_ARTIFACTS=$BASEDIR/build/generated-artifacts/org/prebid
PATH_GENERATED=$BASEDIR/generated
PATH_LOGS=$PATH_GENERATED/logs
PATH_FAT=$PATH_GENERATED/fat
PATH_TEMP=$PATH_GENERATED/temp
PATH_AAR=build/outputs/aar
PATH_LIBS=build/libs

echo "$BASEDIR"

### Methods
function echoX {
  echo -e "PREBID: $@"
}

# $1 - gradle command
function runGradle() {
  echo
  echoX "Running gradle command: ${1}"
  "$BASEDIR"/gradlew "${1}"
  # > "$PATH_LOGS"/"${1}".log 2>&1
  # || die "Build failed, check log in $PATH_LOGS/build.log"
  echo
}

# $1 - module name
function generateArtifacts() {
  echoX "Generating artifacts and javadoc for module: ${1}"

  runGradle :"${1}":assemble
  runGradle :"${1}":javadocJar
  runGradle :"${1}":sourcesJar
  runGradle :"${1}":publishAllPublicationsToLocalArtifactsRepository
#  runGradle :"${1}":generateMetadataFileForLibraryPublication
}

# $1 - regex, $2 - file to search, $3 createDir
function findVariableWithRegex() {
  local foundMatch=""

  while read -r line; do
    if [[ $line =~ ${1} ]]; then
      foundMatch=${BASH_REMATCH[1]}
    fi
  done <"${2}"

  echo "$foundMatch"
}

die() { runGradle "--stop"; echoX "$@" 1>&2; echoX "End Script"; exit 1;  }


### Code
# Set Prebid and OM SDK version from root build.gradle file
VERSION_PREBID="$(findVariableWithRegex "prebidVersionName.*=.*\"(.*)\"" "$BASEDIR"/build.gradle)"
VERSION_OM_SDK="$(findVariableWithRegex "omSdkVersion.*=.*\"(.*)\"" "$BASEDIR"/build.gradle)"
echoX "Building Prebid: [${VERSION_PREBID}], OM SDK version: [${VERSION_OM_SDK}]"


echoX "Cleaning old builds"
rm -rf "$PATH_GENERATED"
rm -rf "$BASEDIR/build"
mkdir "$PATH_GENERATED"
cd "$BASEDIR"
runGradle "clean"


echoX "Generating artifacts"
modules=(
  "PrebidMobile"
  "PrebidMobile-core"
  "PrebidMobile-rendering"
)
artifactIds=(
  "prebid-mobile-sdk"
  "prebid-mobile-sdk-core"
  "prebid-mobile-sdk-rendering"
)
projectPaths=(
  "$BASEDIR/PrebidMobile"
  "$BASEDIR/PrebidMobile/PrebidMobile-core"
  "$BASEDIR/PrebidMobile/PrebidMobile-rendering"
)
for n in "${!modules[@]}"; do
	echoX "Work with module: ${modules[$n]}, artifact id: ${artifactIds[$n]}"
	generateArtifacts "${modules[$n]}"

  # Moving aar
	mv "${PATH_ARTIFACTS}/${artifactIds[$n]}/${VERSION_PREBID}/${artifactIds[$n]}-${VERSION_PREBID}.aar" "${PATH_GENERATED}/${artifactIds[$n]}.aar"
	# Moving pom
	mv "${PATH_ARTIFACTS}/${artifactIds[$n]}/${VERSION_PREBID}/${artifactIds[$n]}-${VERSION_PREBID}.pom" "${PATH_GENERATED}/${artifactIds[$n]}.pom"
	# Moving javadoc
	mv "${projectPaths[$n]}/build/libs/${modules[$n]}-javadoc.jar" "${PATH_GENERATED}/${artifactIds[$n]}-javadoc.jar"
	# Moving sources$
	mv "${projectPaths[$n]}/build/libs/${modules[$n]}-sources.jar" "${PATH_GENERATED}/${artifactIds[$n]}-sources.jar"
done


echoX "Work with Open measurement SDK"
mv "${PATH_ARTIFACTS}/prebid-mobile-sdk-open-measurement/${VERSION_OM_SDK}/prebid-mobile-sdk-open-measurement-${VERSION_OM_SDK}.aar" "${PATH_GENERATED}/prebid-mobile-sdk-open-measurement.aar"
mv "${PATH_ARTIFACTS}/prebid-mobile-sdk-open-measurement/${VERSION_OM_SDK}/prebid-mobile-sdk-open-measurement-${VERSION_OM_SDK}.pom" "${PATH_GENERATED}/prebid-mobile-sdk-open-measurement.pom"


echoX "Completed!"
runGradle clean
runGradle --stop

#  # Make generated/temp/output folder
#	mkdir "$PATH_TEMP"
#	cd "$PATH_TEMP"
#	mkdir output
#
#	MODULE_AAR="${projectPaths[$n]}/$PATH_AAR"
#	cd "$MODULE_AAR"
#	unzip -q -o "${modules[$n]}"-release.aar
#	cd "$PATH_TEMP"/output
#
#	# Extracting the Contents of a JAR File
#	jar xf $MODULE_AAR/classes.jar
#	rm $MODULE_AAR/classes.jar
#
#	# Handle ProGuard rules from .aar into .jar
#	# rename proguard.txt to proguard.pro
#	mv $MODULE_AAR/proguard.{txt,pro}
#	mkdir -p $MODULE_AAR/META-INF
#	mkdir $MODULE_AAR/META-INF/proguard
#	mv $MODULE_AAR/proguard.pro $MODULE_AAR/META-INF/proguard
#	# move META-INF into a result direcotory
#	mv $MODULE_AAR/META-INF $PATH_TEMP/output
#
#	# Creating a JAR File
#	jar cf ${modules[$n]}.jar org* META-INF*
#
#	# move jar into a result direcotory
#	mv ${modules[$n]}.jar $PATH_GENERATED
#
#	cd $BASEDIR
#
#	# Javadoc
#	echoX "Preparing ${modules[$n]} Javadoc"
#	./gradlew -i --no-daemon ${modules[$n]}:javadocJar>$PATH_LOGS/javadoc.log 2>&1 || die "Build Javadoc failed, check log in $PATH_LOGS/javadoc.log"
#
#	# Sources
#	echoX "Preparing ${modules[$n]} Sources"
#	./gradlew -i --no-daemon ${modules[$n]}:sourcesJar>$PATH_LOGS/sources.log 2>&1 || die "Build Sources failed, check log in $PATH_LOGS/sources.log"
#
#	# copy sources and javadoc into a result direcotory
#	PATH_LIBS_ABSOLUTE="${projectPaths[$n]}/$PATH_LIBS"
#	cp -a $PATH_LIBS_ABSOLUTE/. $PATH_GENERATED/
#
#	# clean tmp dir
#	rm -r $PATH_TEMP
#done

#### omsdk
#echo -e "\n"
#echoX "Assembling omsdk"
#
#mkdir $PATH_TEMP
#cd $PATH_TEMP
#mkdir output
#cd output
#cp -a "$BASEDIR/PrebidMobile/omsdk-android/omsdk-android-1.3.17.aar" "$PATH_TEMP/output"
#unzip -q -o omsdk-android-1.3.17.aar
## Delete all files instead classes.jar
#find . ! -name 'classes.jar' -type f -exec rm -f {} +
#unzip -q -o classes.jar
#rm classes.jar
#
#jar cf omsdk.jar com*
#mv omsdk.jar $PATH_GENERATED
#cd $BASEDIR
#rm -r $PATH_TEMP
#
## Prepare fat PrebidDemo library which can be used for LocalJar
#echo -e "\n"
#echoX "Preparing fat PrebidDemo library"
#cd $PATH_GENERATED
#mkdir $PATH_TEMP
#
#cd $PATH_TEMP;
#
#unzip -uo $PATH_GENERATED/omsdk.jar
#unzip -uo $PATH_GENERATED/PrebidMobile.jar
#unzip -uo $PATH_GENERATED/PrebidMobile-core.jar
#unzip -uo $PATH_GENERATED/PrebidMobile-rendering.jar
#
## unzip second proguard
#unzip -B $PATH_GENERATED/PrebidMobile.jar "META-INF/proguard/proguard.pro"
## append text from second proguard
#cat "$PATH_TEMP/META-INF/proguard/proguard.pro~" >> "$PATH_TEMP/META-INF/proguard/proguard.pro"
#rm "$PATH_TEMP/META-INF/proguard/proguard.pro~"
#
#rm $PATH_TEMP/org/prebid/mobile/core/BuildConfig.class
#jar -cvf PrebidMobile.jar -C $PATH_TEMP .
#
#mkdir $PATH_FAT
#mv PrebidMobile.jar $PATH_FAT
#
#rm -r $PATH_TEMP
#
########
## End
########
#echoX "Please find Prebid Mobile product in $PATH_GENERATED"
#echo -e "\n${GREEN}Done!${NO_COLOR} \n"
