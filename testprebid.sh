#! /bin/bash
cd PrebidMobile
./gradlew clean test

./gradlew clean assembleDebug
mkdir -p IntegrationTests/apk && cp DemoApp/build/outputs/apk/DemoApp-debug.apk IntegrationTests/apk/DemoApp.apk

cd IntegrationTests
bundle install

bundle exec calabash-android resign apk/DemoApp.apk
bundle exec calabash-android build apk/DemoApp.apk
#bundle exec test-cloud submit apk/DemoApp.apk 435c130f3f6ff5256d19a790c21dd653 --devices 2ae0b5a0 --series "master" --locale "en_US" --app-name "DemoApp" --user nhedley@appnexus.com
bundle exec test-cloud submit apk/DemoApp.apk 435c130f3f6ff5256d19a790c21dd653 --devices b2a05af9 --series "master" --locale "en_US" --app-name "DemoApp" --user wzhang@appnexus.com
