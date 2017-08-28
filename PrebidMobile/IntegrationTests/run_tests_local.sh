#! /bin/bash 
function echoX {
echo -e "CALABASH BUILDLOG: $@"
}

# Build the apk
echoX "Build the apk."
cd ..
./gradlew clean build
cp DemoApp/build/outputs/apk/DemoApp-debug.apk IntegrationTests/apk/DemoApp.apk
cd IntegrationTests

echoX "Run the tests."
calabash-android resign apk/DemoApp.apk
calabash-android run apk/DemoApp.apk

# to debug your test if it fails, run calabash-android console apk/DemoApp.apk
# for more details see https://github.com/calabash/calabash-android/blob/master/documentation/ruby_api.md
