#!/bin/bash

ADB_CMD="/Users/$USER/Library/Android/sdk/platform-tools/adb"

function checkParameters {
	if [[ -z ${ANDROID_HOME} ]]; then
	echo '$ANDROID_SDK is not set.'
	exit -1
	fi
}

checkParameters

echo "Killing all running emulators..."
for ((PORT=5554; PORT<=5584; PORT+=2)); do
    echo killing emulator-$PORT...
    adb -s emulator-$PORT emu kill
done
echo 'All emulators killed'
