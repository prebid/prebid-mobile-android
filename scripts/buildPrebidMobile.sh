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
PATH_TEMP=$PATH_GENERATED/temp

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
}

# $1 - aar name (without extension)
function aarToJar() {
  echoX "Transforming aar to jar"

  cd "$PATH_GENERATED"
  mkdir "$PATH_TEMP"
  cp "${1}.aar" "$PATH_TEMP/${1}.aar"
  cd "$PATH_TEMP"

  # Extracting aar and classes.jar
  unzip -q -o "${1}.aar"
  mkdir output
  mv classes.jar output/
  cd output
  jar xf classes.jar
  rm classes.jar

  if [ -f "../proguard.txt" ]; then
    # Copying proguard rules
    mv ../proguard.txt proguard.pro
    mkdir -p META-INF/proguard
    mv ./proguard.pro META-INF/proguard/

    # Creating a jar file
    jar cf "${1}".jar org* META-INF*
    mv "${1}.jar" "$PATH_GENERATED"
  else
    # Creating a jar file for Open Measurement SDK
    jar cf "${1}".jar com*
    mv "${1}.jar" "$PATH_GENERATED"
  fi

  cd "$BASEDIR"
  rm -r "$PATH_TEMP"
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

	savePath="${PATH_GENERATED}/${artifactIds[$n]}"
  # Moving aar
	mv "${PATH_ARTIFACTS}/${artifactIds[$n]}/${VERSION_PREBID}/${artifactIds[$n]}-${VERSION_PREBID}.aar" "${savePath}.aar"
	# Moving pom
	mv "${PATH_ARTIFACTS}/${artifactIds[$n]}/${VERSION_PREBID}/${artifactIds[$n]}-${VERSION_PREBID}.pom" "${savePath}.pom"
	# Moving javadoc
	mv "${projectPaths[$n]}/build/libs/${modules[$n]}-javadoc.jar" "${savePath}-javadoc.jar"
	# Moving sources$
	mv "${projectPaths[$n]}/build/libs/${modules[$n]}-sources.jar" "${savePath}-sources.jar"

	aarToJar "${artifactIds[$n]}"
done


echoX "Work with Open measurement SDK"
openMeasurementPath="${PATH_GENERATED}/prebid-mobile-sdk-open-measurement"
mv "${PATH_ARTIFACTS}/prebid-mobile-sdk-open-measurement/${VERSION_OM_SDK}/prebid-mobile-sdk-open-measurement-${VERSION_OM_SDK}.aar" "${openMeasurementPath}.aar"
mv "${PATH_ARTIFACTS}/prebid-mobile-sdk-open-measurement/${VERSION_OM_SDK}/prebid-mobile-sdk-open-measurement-${VERSION_OM_SDK}.pom" "${openMeasurementPath}.pom"
aarToJar "prebid-mobile-sdk-open-measurement"


echoX "Preparing fat PrebidDemo library (jar for local usages)"
cd "$PATH_GENERATED"
mkdir "$PATH_TEMP"
cp *.jar temp
cd "$PATH_TEMP"
unzip -uo "${PATH_GENERATED}/prebid-mobile-sdk.jar"
unzip -uo "${PATH_GENERATED}/prebid-mobile-sdk-core.jar"
unzip -uo "${PATH_GENERATED}/prebid-mobile-sdk-rendering.jar"
unzip -uo "${PATH_GENERATED}/prebid-mobile-sdk-open-measurement.jar"
rm *.jar

# Append proguard rules
#unzip -B "$PATH_GENERATED/PrebidMobile.jar" "META-INF/proguard/proguard.pro"
#cat "$PATH_TEMP/META-INF/proguard/proguard.pro~" >> "$PATH_TEMP/META-INF/proguard/proguard.pro"
#rm "$PATH_TEMP/META-INF/proguard/proguard.pro~"

rm "${PATH_TEMP}/org/prebid/mobile/core/BuildConfig.class"
jar -cvf PrebidMobile.jar -C "$PATH_TEMP" .
mv PrebidMobile.jar "$PATH_GENERATED/fat.jar"
rm -r "$PATH_TEMP"

echoX "Please find Prebid Mobile product in $PATH_GENERATED"
echoX "Completed!"
runGradle clean
runGradle --stop