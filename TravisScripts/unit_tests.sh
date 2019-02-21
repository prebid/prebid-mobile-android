#! /bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $BASEDIR/../PrebidMobile

echo "PREBID TESTLOG: start unit tests"
./gradlew API1.0:testDebugUnitTest