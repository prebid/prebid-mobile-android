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
./gradlew PrebidMobile:testDebugUnitTest
./gradlew PrebidMobile-rendering:testDebugUnitTest
./gradlew PrebidMobile-gamEventHandlers:testDebugUnitTest
./gradlew PrebidMobile-mopubAdapters:testDebugUnitTest
./gradlew PrebidMobile-admobAdapters:testDebugUnitTest