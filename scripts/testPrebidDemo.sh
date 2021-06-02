#! /bin/bash

# Merge Script
if [ -d "scripts" ]; then
cd scripts/
fi

set -e

cd ..

function echoX {
echo -e "✅ PREBID TESTLOG: $@"
}

echoX "clean previous build"
./gradlew clean

#building
echoX "start building UI tests"
./gradlew assembleSourceCodeDebugAndroidTest

# testing
echoX "start UI tests"
# ./gradlew PrebidDemoJava:connectedSourceCodeDebugAndroidTest
