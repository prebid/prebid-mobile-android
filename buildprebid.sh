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
PREBIDCORE=("PrebidMobileCore")
DEMANDSOURCES=()
DEMANDSOURCES+=("PrebidServerAdapter")

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

###########################
# Test and Build
###########################
echoX "Run unit tests"
cd $LIBDIR
(./gradlew -i --no-daemon clean test > $LOGPATH/testResults.log 2>&1) || (die "Unit tests failed, check log in $LOGPATH/testResults.log") &
PID=$!
spinner $PID &
wait $PID

echoX "Assemble builds"
cd $LIBDIR
# clean existing build results, exclude test task, and assemble new release build
(./gradlew -i --no-daemon -x test build > $LOGPATH/build.log 2>&1 || die "Build failed, check log in $LOGPATH/build.log" ) &
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
# combine demand sources with the prebid core
echoX "Move demand library to output"
for i in "${DEMANDSOURCES[@]}";
do
cd $LIBDIR/$i/$AARPATH
unzip -q -o $i-release.aar
cd $TEMPDIR/output
jar xf $LIBDIR/$i/$AARPATH/classes.jar
rm $LIBDIR/$i/$AARPATH/classes.jar
done
cd $TEMPDIR/output
jar cf PrebidMobile.jar org*

mv PrebidMobile.jar $OUTDIR
# clean tmp dir
rm -r $TEMPDIR

echoX "Prepare Demo App"
APPNAME="DemoApp"
cp -r $LIBDIR/$APPNAME $OUTDIR/$APPNAME
# update build.gradle
cd $OUTDIR/$APPNAME
rm build.gradle
cat > build.gradle << EOL
buildscript {
repositories {
jcenter()
}
dependencies {
classpath 'com.android.tools.build:gradle:2.3.0'

// NOTE: Do not place your application dependencies here; they belong
// in the individual module build.gradle files
}
}

apply plugin: 'com.android.application'

repositories {
mavenCentral()
jcenter()
flatDir {
dirs 'libs'
}
}

android {
compileSdkVersion 25
buildToolsVersion '25.0.0'

defaultConfig {
applicationId 'org.prebid.mobile.demoapp'
minSdkVersion 16
targetSdkVersion 25
versionCode 1
versionName '1.0'
}
buildTypes {
release {
minifyEnabled true
proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
}
debug {
minifyEnabled false
proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
}
}
}

dependencies {
compile 'com.android.support:appcompat-v7:23.1.1'
// For aaid
compile 'com.google.android.gms:play-services-ads:9.2.1'

// From MoPub Android SDK docs
compile('com.mopub:mopub-sdk:4.15.0@aar') {
transitive = true
}

// Build from compiled libs
compile fileTree(dir: 'libs', include: ['*.jar'])
compile 'org.apache.commons:commons-text:1.1'
}

EOL

if [ ! -d libs ]; then
mkdir libs
fi

# add libs
cp ../PrebidMobile.jar libs/
# move gradlew here
cp -r $LIBDIR/gradle .
cp -r $LIBDIR/gradlew .
cp -r $LIBDIR/gradlew.bat .
cp -r $LIBDIR/gradle.properties .
chmod +x gradlew
./gradlew clean >/dev/null
# TODO maybe launch the app with an emulator for developer to test directly

# javadoc
echoX "Prepare Javedoc"

# class paths
CORE_API_PATH="PrebidMobileCore/src/main/java/org/prebid/mobile/core"
CORE_CLASSES=()
CORE_CLASSES+=("AdSize.java")
CORE_CLASSES+=("AdType.java")
CORE_CLASSES+=("AdUnit.java")
CORE_CLASSES+=("BannerAdUnit.java")
CORE_CLASSES+=("BidManager.java")
CORE_CLASSES+=("BidResponse.java")
CORE_CLASSES+=("DemandAdapter.java")
CORE_CLASSES+=("ErrorCode.java")
CORE_CLASSES+=("InterstitialAdUnit.java")
CORE_CLASSES+=("LogUtil.java")
CORE_CLASSES+=("Prebid.java")
CORE_CLASSES+=("PrebidException.java")
CORE_CLASSES+=("TargetingParams.java")
PREBID_SERVER_PATH="PrebidServerAdapter/src/main/java/org/prebid/mobile/prebidserver"
PREBID_SERVER_CLASSES=()
PREBID_SERVER_CLASSES+=("PrebidServerAdapter.java")

FINAL_CLASSES=""
for classes in "${CORE_CLASSES[@]}"; do
    FINAL_CLASSES="$FINAL_CLASSES $LIBDIR/$CORE_API_PATH/$classes"
done
for classes in "${PREBID_SERVER_CLASSES[@]}"; do
    FINAL_CLASSES="$FINAL_CLASSES $LIBDIR/$PREBID_SERVER_PATH/$classes"
done

cd $OUTDIR
# disable Javadoc for illegal pacakge name error
javadoc -d Javadoc -protected $FINAL_CLASSES>$LOGPATH/javadoc.log 2>&1 || die "Build Javadoc failed, check log in $LOGPATH/javadoc.log"

#######
# End
#######
echoX "Please find Prebid Mobile product in $OUTDIR"
echoX "Build finished."
