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

echoX "start UI tests"

#building
./gradlew assembleSourceCodeDebugAndroidTest

# testing
# ./gradlew PrebidDemoJava:connectedSourceCodeDebugAndroidTest
