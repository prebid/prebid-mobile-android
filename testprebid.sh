#! /bin/bash
set -e

function echoX {
echo -e "PREBID TESTLOG: $@"
}

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $BASEDIR/PrebidMobile

echoX "clean previous build"
./gradlew clean

echoX "start unit tests"
./gradlew API1.0:testDebugUnitTest

echoX "start UI tests"
echoX "run sanity test first"
./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest --stacktrace -Pandroid.testInstrumentationRunnerArguments.class=org.prebid.mobile.app.SanityTest
echoX "run all tests"
./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest --stacktrace