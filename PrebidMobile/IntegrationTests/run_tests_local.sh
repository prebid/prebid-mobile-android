#! /bin/bash 
function echoX {
echo -e "CALABASH BUILDLOG: $@"
}

# Build the apk
echoX "Build the apk."
cd ..
./gradlew clean build
cp DemoApp/build/outputs/apk/DemoApp-release-unsigned.apk IntegrationTests/apk/DemoApp.apk
cd IntegrationTests

echoX "Run the tests."
calabash-android resign apk/DemoApp.apk
calabash-android run apk/DemoApp.apk
