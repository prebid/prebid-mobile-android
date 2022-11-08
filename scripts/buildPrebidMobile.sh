#! /bin/bash
# This script helps to build the Prebid products in the following steps:
# It will ask you the version you're releasing
# Check if it's the same as the one in the project's build.gradle
# Package releases
# End

######################
# Helper Methods
######################

# Merge Script
if [ -d "scripts" ]; then
  cd scripts/
fi

set -e

cd ..
echo -e "$PWD"

# Setup some constants for use later on.
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

function echoX() {
  echo -e "PREBID BUILDLOG: $@"
}

die() {
  echoX "$@" 1>&2
  echoX "End Script"
  exit 1
}

######################
# Build Settings
######################

# file paths
BASEDIR="$PWD"
OUTDIR=$BASEDIR/generated
LOGPATH=$OUTDIR/logs
FAT_PATH=$OUTDIR/fat
AARPATH=build/outputs/aar
BUILD_LIBS_PATH=build/libs
TEMPDIR=$OUTDIR/temp
LIBDIR=$BASEDIR
PREBIDCORE=PrebidMobile

echoX "$BASEDIR"

# set the default release version to what's in the project's build.gradle file
RELEASE_VERSION=""
regex="prebidVersionName.*=.*\"(.*)\""
while read -r line; do
  if [[ $line =~ $regex ]]; then
    RELEASE_VERSION=${BASH_REMATCH[1]}
  fi
done <$LIBDIR/build.gradle

echoX "Start building Prebid Mobile version $RELEASE_VERSION"

###########################
# Prepare
###########################
echoX "Clean directories"
rm -rf $OUTDIR
mkdir $OUTDIR
mkdir $LOGPATH
rm -rf $TEMPDIR
cd $LIBDIR
./gradlew -i --no-daemon clean >$LOGPATH/clean.log 2>&1

###########################
# Generate modules
###########################

modules=(
  "PrebidMobile"
  "PrebidMobile-core"
  "PrebidMobile-gamEventHandlers"
  "PrebidMobile-admobAdapters"
  "PrebidMobile-maxAdapters"
)

projectPaths=(
  "$BASEDIR/PrebidMobile"
  "$BASEDIR/PrebidMobile/PrebidMobile-core"
  "$BASEDIR/PrebidMobile/PrebidMobile-gamEventHandlers"
  "$BASEDIR/PrebidMobile/PrebidMobile-admobAdapters"
  "$BASEDIR/PrebidMobile/PrebidMobile-maxAdapters"
)

mkdir "$OUTDIR/aar"
for n in ${!modules[@]}; do

  echo -e "\n"
  echoX "Assembling ${modules[$n]}"
  cd $LIBDIR
  # clean existing build results, exclude test task, and assemble new release build
  (./gradlew -i --no-daemon ${modules[$n]}:assembleRelease >$LOGPATH/build.log 2>&1 || die "Build failed, check log in $LOGPATH/build.log")

  if [ "$1" != "-nojar" ]; then
    # Make folder generated/temp/output
    echoX "Packaging ${modules[$n]}"
    mkdir $TEMPDIR
    cd $TEMPDIR
    mkdir output

    AARPATH_ABSOLUTE="${projectPaths[$n]}/$AARPATH"

    cd $AARPATH_ABSOLUTE
    cp ${modules[$n]}-release.aar $OUTDIR/aar
    unzip -q -o ${modules[$n]}-release.aar
    cd $TEMPDIR/output

    # Extracting the Contents of a JAR File
    jar xf $AARPATH_ABSOLUTE/classes.jar
    rm $AARPATH_ABSOLUTE/classes.jar

    # Handle ProGuard rules from .aar into .jar
    # rename proguard.txt to proguard.pro
    mv $AARPATH_ABSOLUTE/proguard.{txt,pro}
    mkdir -p $AARPATH_ABSOLUTE/META-INF
    mkdir $AARPATH_ABSOLUTE/META-INF/proguard
    mv $AARPATH_ABSOLUTE/proguard.pro $AARPATH_ABSOLUTE/META-INF/proguard
    # move META-INF into a result direcotory
    mv $AARPATH_ABSOLUTE/META-INF $TEMPDIR/output

    rm -r $TEMPDIR/output/META-INF/com

    # Creating a JAR File
    if [ "${modules[$n]}" == "PrebidMobile-maxAdapters" ]; then
      jar cf ${modules[$n]}.jar org* com* META-INF*
    elif [ "${modules[$n]}" == "PrebidMobile" ]; then
      jar cf ${modules[$n]}.jar META-INF*
    else
      jar cf ${modules[$n]}.jar org* META-INF*
    fi

    # move jar into a result direcotory
    mv ${modules[$n]}.jar $OUTDIR

    cd $LIBDIR

    # Javadoc
    echoX "Preparing ${modules[$n]} Javadoc"
    ./gradlew -i --no-daemon ${modules[$n]}:javadocJar >$LOGPATH/javadoc.log 2>&1 || die "Build Javadoc failed, check log in $LOGPATH/javadoc.log"

    # Sources
    echoX "Preparing ${modules[$n]} Sources"
    ./gradlew -i --no-daemon ${modules[$n]}:sourcesJar >$LOGPATH/sources.log 2>&1 || die "Build Sources failed, check log in $LOGPATH/sources.log"

    # copy sources and javadoc into a result direcotory
    BUILD_LIBS_PATH_ABSOLUTE="${projectPaths[$n]}/$BUILD_LIBS_PATH"
    cp -a $BUILD_LIBS_PATH_ABSOLUTE/. $OUTDIR/
    # clean tmp dir
    rm -r $TEMPDIR
  fi
done


if [ "$1" != "-nojar" ]; then
  ### omsdk
  echo -e "\n"
  echoX "Assembling omsdk"

  mkdir $TEMPDIR
  cd $TEMPDIR
  mkdir output
  cd output
  cp -a "$BASEDIR/PrebidMobile/omsdk-android/omsdk-android-1.4.1.aar" "$TEMPDIR/output"
  unzip -q -o omsdk-android-1.4.1.aar
  # Delete all files instead classes.jar
  find . ! -name 'classes.jar' -type f -exec rm -f {} +
  unzip -q -o classes.jar
  rm classes.jar

  jar cf omsdk.jar com*
  mv omsdk.jar $OUTDIR
  cd $LIBDIR
  rm -r $TEMPDIR
fi

#######
# End
#######
echoX "Please find Prebid Mobile product in $OUTDIR"
echo -e "\n${GREEN}Done!${NC} \n"
