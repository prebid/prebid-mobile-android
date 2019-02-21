#! /bin/bash
set -e
function echoX {
echo -e "PREBID TESTLOG: $@"
}

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $BASEDIR/PrebidMobile

echoX "clean previous build"
./gradlew clean

echoX "start unit tests"
./gradlew API1.0:testDebugUnitTest

echoX "start UI tests"
echoX "run sanity test first"
./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.prebid.mobile.app.SanityTest
# echoX "run MoPub banner tests"
# ./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.prebid.mobile.app.MoPubBannerTest
# echoX "run MoPub interstitial tests"
# ./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.prebid.mobile.app.MoPubInterstitialTest
# echoX "run DFP banner tests"
# ./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.prebid.mobile.app.DFPBannerTest
# echoX "run DFP interstitial tests"
# ./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.prebid.mobile.app.DFPInterstitialTest
# echoX "run extra tests"
# ./gradlew API1.0Demo:connectedSourceCodeDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.prebid.mobile.app.ExtraTests
