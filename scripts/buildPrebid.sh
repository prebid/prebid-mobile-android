#! /bin/bash
# This script helps to build the Prebid products in the following steps:
# It will ask you the version you're releasing
# Check if it's the same as the one in the project's build.gradle
# If not, ask you to pick one, and update build.gradle if necessary
# Run unit tests
# Stop releasing if unit tests doesn't pass
# Package releases
# End

######################
# Helper Methods
######################
set -e

function echoX {
echo -e "PREBID BUILDLOG: $@"
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
# how to use?
# ( command ) & spinner $!
# Example: (sleep 2) & spinner $!

bold=$(tput bold)
normal=$(tput sgr0)

die() { echoX "$@" 1>&2 ; echoX "End Script"; exit 1;  }

######################
# Build Settings
######################
# exit script if a command line fails
#set -e

# file paths
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
OUTDIR=$BASEDIR/out
LOGPATH=$OUTDIR/logs
AARPATH=build/outputs/aar
TEMPDIR=$BASEDIR/temp
LIBDIR=$BASEDIR/PrebidMobile
PREBIDCORE=API1.0

# set the default release version to what's in the project's build.gradle file
RELEASE_VERSION=""
regex="prebidVersionName.*=.*\"(.*)\""
while read -r line;
do
if [[ $line =~ $regex ]];
then
RELEASE_VERSION=${BASH_REMATCH[1]};
fi
done < $LIBDIR/build.gradle

echoX "Start building Prebid Mobile version $RELEASE_VERSION"

###########################
# Prepare
###########################
echoX "Clean directories"
rm -rf $OUTDIR
mkdir $OUTDIR
mkdir $LOGPATH
rm -rf $TEMPDIR
mkdir $TEMPDIR
cd $LIBDIR
./gradlew -i --no-daemon clean >$LOGPATH/clean.log 2>&1

###########################
# Test and Build
###########################
echoX "Run unit tests"
cd $LIBDIR
(./gradlew -i --no-daemon API1.0:test > $LOGPATH/testResults.log 2>&1) || (die "Unit tests failed, check log in $LOGPATH/testResults.log") &
PID=$!
spinner $PID &
wait $PID

echoX "Assemble builds"
cd $LIBDIR
# clean existing build results, exclude test task, and assemble new release build
(./gradlew -i --no-daemon API1.0:assembleRelease > $LOGPATH/build.log 2>&1 || die "Build failed, check log in $LOGPATH/build.log" ) &
PID=$!
spinner $PID &
wait $PID

echoX "Start packaging product"
cd $TEMPDIR
mkdir output
echoX "Move library core to output"
cd $LIBDIR/$PREBIDCORE/$AARPATH
unzip -q -o $PREBIDCORE-release.aar
cd $TEMPDIR/output
jar xf $LIBDIR/$PREBIDCORE/$AARPATH/classes.jar
rm $LIBDIR/$PREBIDCORE/$AARPATH/classes.jar
cd $TEMPDIR/output
jar cf PrebidMobile.jar org*

mv PrebidMobile.jar $OUTDIR
# clean tmp dir
rm -r $TEMPDIR

# javadoc
echoX "Prepare Javedoc"

# class paths
CORE_API_PATH="API1.0/src/main/java/org/prebid/mobile"
CORE_CLASSES=()
CORE_CLASSES+=("AdType.java")
CORE_CLASSES+=("AdUnit.java")
CORE_CLASSES+=("BannerAdUnit.java")
CORE_CLASSES+=("DemandAdapter.java")
CORE_CLASSES+=("Host.java")
CORE_CLASSES+=("ResultCode.java")
CORE_CLASSES+=("InterstitialAdUnit.java")
CORE_CLASSES+=("LogUtil.java")
CORE_CLASSES+=("OnCompleteListener.java")
CORE_CLASSES+=("PrebidMobile.java")
CORE_CLASSES+=("TargetingParams.java")

FINAL_CLASSES=""
for classes in "${CORE_CLASSES[@]}"; do
    FINAL_CLASSES="$FINAL_CLASSES $LIBDIR/$CORE_API_PATH/$classes"
done

cd $OUTDIR
# disable Javadoc for illegal pacakge name error
javadoc -d Javadoc -protected $FINAL_CLASSES>$LOGPATH/javadoc.log 2>&1 || die "Build Javadoc failed, check log in $LOGPATH/javadoc.log"

#######
# End
#######
echoX "Please find Prebid Mobile product in $OUTDIR"
echoX "Build finished."
