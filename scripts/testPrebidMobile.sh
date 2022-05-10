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
./gradlew -q PrebidMobile-core:testReleaseUnitTest
./gradlew -q PrebidMobile-gamEventHandlers:testReleaseUnitTest
./gradlew -q PrebidMobile-admobAdapters:testReleaseUnitTest
./gradlew -q PrebidMobile-maxAdapters:testReleaseUnitTest