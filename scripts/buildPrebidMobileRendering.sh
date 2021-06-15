#!/usr/bin/env bash

if [ -d "scripts" ]; then
  cd scripts/ || true
fi

set -e

cd ..

# $1 - module name
function generateArtifacts() {
  echo "Generating artifacts and javadoc for module: ${1}"

  ./gradlew :"${1}":assemble
  ./gradlew :"${1}":javadocJar
  ./gradlew :"${1}":sourcesJar
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

MODULE_NAME_RENDERING_SDK="PrebidMobile-rendering"
MODULE_NAME_MOPUB="PrebidMobile-mopubAdapters"
MODULE_NAME_EVENT_HANDLERS="PrebidMobile-gamEventHandlers"
MODULE_NAME_OM_SDK="omsdk-android"

ARTIFACT_ID_RENDERING_SDK="prebid-mobile-sdk-rendering"
ARTIFACT_ID_MOPUB_ADAPTERS="prebid-mobile-sdk-mopubAdapters"
ARTIFACT_ID_GAM_EVENT_HANDLERS="prebid-mobile-sdk-gamEventHandlers"

PATH_BASE="$PWD"
PATH_BASE_SDK_MODULE="${PATH_BASE}/PrebidMobile"
PATH_BASE_ARTIFACT="${PATH_BASE}/build/generated-artifacts/org/prebid"
PATH_OUTPUT="${PATH_BASE}/generated/rendering"
PATH_LIBS="build/libs"

# set the default release version to what's in the project's build.gradle file
prebidVersionRegex="prebidVersionName.*=.*\"(.*)\""
omSdkVersionRegex="omSdkVersion.*=.*\"(.*)\""
VERSION_RELEASE="$(findVariableWithRegex "${prebidVersionRegex}" "${PATH_BASE}"/build.gradle)"
VERSION_OM_SDK="$(findVariableWithRegex "${omSdkVersionRegex}" "${PATH_BASE}"/build.gradle)"

echo "Building artifacts for version: [${VERSION_RELEASE}], OM SDK version: [${VERSION_OM_SDK}]"

generateArtifacts ${MODULE_NAME_RENDERING_SDK}
generateArtifacts ${MODULE_NAME_EVENT_HANDLERS}
generateArtifacts ${MODULE_NAME_MOPUB}

# Generate and publish .pom (includes previously generated .aar). This step will include OM SDK artifacts local deploy.
./gradlew publishAllPublicationsToLocalArtifactsRepository

mkdir -p "${PATH_OUTPUT}"
echo "Moving artifacts to $PATH_OUTPUT"

# Pay attention that for cycle depends on order and element count in moduleArray and artifactIdArray.
# Modify with caution.
moduleArray=("${MODULE_NAME_RENDERING_SDK}" "${MODULE_NAME_MOPUB}" "${MODULE_NAME_EVENT_HANDLERS}")
artifactIdArray=("${ARTIFACT_ID_RENDERING_SDK}" "${ARTIFACT_ID_MOPUB_ADAPTERS}" "${ARTIFACT_ID_GAM_EVENT_HANDLERS}")

for i in "${!moduleArray[@]}"; do
  moduleName="${moduleArray[i]}"
  artifactId="${artifactIdArray[i]}"

  echo "Extracting module: [${moduleName}], artifactId: [${artifactId}] for moving .aar, .pom, javadoc and sources into appropriate output folder."

  outputArtifactPath="${PATH_OUTPUT}/${artifactId}"
  mkdir -p "${outputArtifactPath}"

  # Moves and renames aar and pom files per module to output folder (with artifact id as destinationPath).
  # Renaming is performed to remove version from pom and aar file names. Example resulting path: %project%/generated/rendering/%artifactId%/%artifactId%.pom
  mv $"${PATH_BASE_ARTIFACT}/${artifactId}/${VERSION_RELEASE}/${artifactId}-${VERSION_RELEASE}.aar" "${outputArtifactPath}/${artifactId}.aar"
  mv $"${PATH_BASE_ARTIFACT}/${artifactId}/${VERSION_RELEASE}/${artifactId}-${VERSION_RELEASE}.pom" "${outputArtifactPath}/${artifactId}.pom"

  # Moves and renames generated javadoc per module.
  mv $"${PATH_BASE_SDK_MODULE}/${moduleName}/${PATH_LIBS}/${moduleName}-javadoc.jar" "${outputArtifactPath}/${artifactId}-javadoc.jar"
  mv $"${PATH_BASE_SDK_MODULE}/${moduleName}/${PATH_LIBS}/${moduleName}-sources.jar" "${outputArtifactPath}/${artifactId}-sources.jar"
done

outputArtifactPath="${PATH_OUTPUT}/${MODULE_NAME_OM_SDK}"
mkdir -p "${outputArtifactPath}"

# Moves OM SDK pom and aar to output folder.
echo "Extracting module: [OM SDK] for moving .aar and .pom into appropriate output folder."

PATH_OM_SDK_ARTIFACT="${PATH_BASE_ARTIFACT}/${MODULE_NAME_OM_SDK}/${VERSION_OM_SDK}/${MODULE_NAME_OM_SDK}-${VERSION_OM_SDK}"
mv $"${PATH_OM_SDK_ARTIFACT}.aar" "${outputArtifactPath}/${MODULE_NAME_OM_SDK}.aar"
mv $"${PATH_OM_SDK_ARTIFACT}.pom" "${outputArtifactPath}/${MODULE_NAME_OM_SDK}.pom"

echo "Done!"
echo "Files can be found in $PATH_OUTPUT"