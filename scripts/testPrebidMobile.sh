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
./gradlew -i PrebidMobile-core:testReleaseUnitTest
./gradlew -i PrebidMobile-gamEventHandlers:testReleaseUnitTest
./gradlew -i PrebidMobile-admobAdapters:testReleaseUnitTest
./gradlew -i PrebidMobile-maxAdapters:testReleaseUnitTest