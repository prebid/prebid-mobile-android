#! /bin/bash
set -e

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $BASEDIR/../PrebidMobile

echoX "run extra tests"
./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.prebid.mobile.app.ExtraTests
