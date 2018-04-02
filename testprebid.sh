#! /bin/bash
function echoX {
echo -e "PREBID TESTLOG: $@"
}

echoX "start unit tests"
cd PrebidMobile
./gradlew clean test

echoX "assemble debug apk"
./gradlew clean assembleDebug
if [ ! -e DemoApp/build/outputs/apk/debug/DemoApp-debug.apk ];then
	echoX "apk creation unsuccessful"
fi


echoX "copy debug apk to destination path"
mkdir -p IntegrationTests/apk && cp DemoApp/build/outputs/apk/debug/DemoApp-debug.apk IntegrationTests/apk/DemoApp.apk
if [ ! -e IntegrationTests/apk/DemoApp.apk ]; then
	echoX "file copy unsuccessful"
fi

# Commenting Integration tests out, running it locally only for now since it requires login
# echoX "start integration tests"
# cd IntegrationTests
# bundle install
# npm install -g appcenter-cli
# gem install xamarin-test-cloud

# bundle exec calabash-android resign apk/DemoApp.apk
# bundle exec calabash-android build apk/DemoApp.apk
# #bundle exec test-cloud submit apk/DemoApp.apk 435c130f3f6ff5256d19a790c21dd653 --devices 2ae0b5a0 --series "master" --locale "en_US" --app-name "DemoApp" --user nhedley@appnexus.com
# # bundle exec test-cloud submit apk/DemoApp.apk 435c130f3f6ff5256d19a790c21dd653 --devices b2a05af9 --series "master" --locale "en_US" --app-name "DemoApp" --user wzhang@appnexus.com
# appcenter login
# appcenter test run calabash --app "xtc-AppNexus/DemoApp" --devices "xtc-AppNexus/test-demo-app" --app-path apk/DemoApp.apk --test-series "master" --locale "en_US" --project-dir IntegrationTests/features