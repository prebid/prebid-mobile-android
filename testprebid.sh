#! /bin/bash

# run unit tests
#cd PrebidMobile
#./gradlew clean test

# build Espresso test apk
#./gradlew clean :DemoApp:assembleDebug :DemoApp:assembleDebugAndroidTest

# download xtc command line tool
uname
cd ..
curl -O 'http://calabash-ci.macminicolo.net:8080/view/Uploader/job/Uploader%20master/lastSuccessfulBuild/artifact/publish/Release/xtc.osx.10.10-x64.tar.gz'
tar -xvzf xtc.osx.10.10-x64.tar.gz
cd xtc
export PATH=$PATH:`pwd`
xtc help

# upload the test apk to xamarin test cloud
#cd ../PrebidMobile
#xtc test DemoApp/build/outputs/apk/DemoApp-debug.apk 435c130f3f6ff5256d19a790c21dd653 --devices f2572021 --series "master" --app-name "DemoApp" --user wzhang@appnexus.com --workspace DemoApp/build/outputs/apk