#! /bin/bash
set -e
function echoX {
echo -e "PREBID TESTLOG: $@"
}

export -f echoX

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $BASEDIR/../PrebidMobile

echoX "clean project"
./gradlew clean