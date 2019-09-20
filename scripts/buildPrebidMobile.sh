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

function echoX {
echo -e "PREBID BUILDLOG: $@"
}

die() { echoX "$@" 1>&2 ; echoX "End Script"; exit 1;  }

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
cd $LIBDIR
./gradlew -i --no-daemon clean >$LOGPATH/clean.log 2>&1

###########################
# Test and Build
###########################

echoX "Run unit tests"
cd $LIBDIR
(./gradlew -i --no-daemon PrebidMobile:test > $LOGPATH/testResults.log 2>&1) || (die "Unit tests failed, check log in $LOGPATH/testResults.log") 

modules=("PrebidMobile" "PrebidMobile-core")
projectPaths=("$BASEDIR/PrebidMobile" "$BASEDIR/PrebidMobile/PrebidMobile-core")

for n in ${!modules[@]}; do

	echo -e "\n"
	echoX "Assembling ${modules[$n]}"
	cd $LIBDIR
	# clean existing build results, exclude test task, and assemble new release build
	(./gradlew -i --no-daemon ${modules[$n]}:assembleRelease > $LOGPATH/build.log 2>&1 || die "Build failed, check log in $LOGPATH/build.log" )

	echoX "packaging ${modules[$n]}"
	mkdir $TEMPDIR
	cd $TEMPDIR
	mkdir output
	
	AARPATH_ABSOLUTE="${projectPaths[$n]}/$AARPATH"

	cd $AARPATH_ABSOLUTE
	unzip -q -o ${modules[$n]}-release.aar
	cd $TEMPDIR/output
	jar xf $AARPATH_ABSOLUTE/classes.jar
	rm $AARPATH_ABSOLUTE/classes.jar
	cd $TEMPDIR/output
	jar cf ${modules[$n]}.jar org*

	mv ${modules[$n]}.jar $OUTDIR

	cd $LIBDIR

	# Javadoc
	echoX "Preparing ${modules[$n]} Javadoc"
	./gradlew -i --no-daemon ${modules[$n]}:javadocJar>$LOGPATH/javadoc.log 2>&1 || die "Build Javadoc failed, check log in $LOGPATH/javadoc.log"

	# Sources
	echoX "Preparing ${modules[$n]} Sources"
	./gradlew -i --no-daemon ${modules[$n]}:sourcesJar>$LOGPATH/sources.log 2>&1 || die "Build Sources failed, check log in $LOGPATH/sources.log"

	# copy the results
	BUILD_LIBS_PATH_ABSOLUTE="${projectPaths[$n]}/$BUILD_LIBS_PATH"
	cp -a $BUILD_LIBS_PATH_ABSOLUTE/. $OUTDIR/

	# clean tmp dir
	rm -r $TEMPDIR
done

# Prepare fat PrebidDemo library
echo -e "\n"
echoX "Preparing fat PrebidDemo library"
cd $OUTDIR
mkdir $TEMPDIR
cd $TEMPDIR; unzip -uo $OUTDIR/PrebidMobile-core.jar
cd $TEMPDIR; unzip -uo $OUTDIR/PrebidMobile.jar

rm $TEMPDIR/org/prebid/mobile/core/BuildConfig.class
jar -cvf PrebidMobile.jar -C $TEMPDIR .

mkdir $FAT_PATH
mv PrebidMobile.jar $FAT_PATH

rm -r $TEMPDIR

#######
# End
#######
echoX "Please find Prebid Mobile product in $OUTDIR"
echo -e "\n${GREEN}Done!${NC} \n"
