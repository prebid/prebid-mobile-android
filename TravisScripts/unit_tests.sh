#! /bin/bash
set -e

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $BASEDIR/../PrebidMobile

echoX "start unit tests"
./gradlew API1.0:testDebugUnitTest