#! /bin/bash

# Merge Script
if [ -d "scripts" ]; then
cd scripts/
fi


set -e
echo $PWD
cd ..

function echoX {
echo -e "✅ PREBID TESTLOG: $@"
}

echoX "clean previous build"
./gradlew clean

echoX "start unit tests"
./gradlew -q PrebidMobile-core:testDebugUnitTest
./gradlew -q PrebidMobile-gamEventHandlers:testDebugUnitTest
./gradlew -q PrebidMobile-mopubAdapters:testDebugUnitTest
./gradlew -q PrebidMobile-admobAdapters:testDebugUnitTest
./gradlew -q PrebidMobile-maxAdapters:testDebugUnitTest