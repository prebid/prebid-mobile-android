#!/usr/bin/env bash
set -o errexit

# Command line argument with file path
FILE=$1
HOST="jenkins-mobile@mbp-op-320.corp.openx.com:~/Documents/Appium_builds"

if [ ! -f $FILE  ]; then
    echo "File for upload not found!"
    exit -1
fi

readonly BASENAME=$(basename $FILE)

echo "Uploading $BASENAME to $HOST"
scp $FILE $HOST
echo "$BASENAME successfully uploaded!"
