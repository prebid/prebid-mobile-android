#! /bin/bash

cd PrebidMobile
./gradlew clean test

./gradlew clean build
mkdir -p IntegrationTests/apk && cp DemoApp/build/outputs/apk/DemoApp-release-unsigned.apk IntegrationTests/apk/DemoApp.apk

gem install xamarin-test-cloud
gem install calabash-cucumber
gem install rspec
cd IntegrationTests
test-cloud submit apk/DemoApp.apk 435c130f3f6ff5256d19a790c21dd653 --devices 2ae0b5a0 --series "master" --locale "en_US" --app-name "DemoApp" --user nhedley@appnexus.com
