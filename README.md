[![Build Status](https://api.travis-ci.org/prebid/prebid-mobile-android.svg?branch=master)](https://travis-ci.org/prebid/prebid-mobile-android)

# Prebid Mobile Android SDK

Get started with Prebid Mobile by creating a Prebid Server account [here](http://prebid.org/prebid-mobile/prebid-mobile-pbs.html)

## Use Maven?

Easily include the Prebid Mobile SDK using Maven. Simply add this line to your gradle dependencies:

```
implementation 'org.prebid:prebid-mobile-sdk:[1,2)'
```
If you want explicit stable vesion, please use the following:
```
implementation 'org.prebid:prebid-mobile-sdk:1.1.2'
```


## Build from source

Build Prebid Mobile from source code. After cloning the repo, from the root directory run

```
./buildprebid.sh
```

to output the final lib jar and package you a demo app.


## Test Prebid Mobile

Run the test script to run unit tests and integration tests.

```
./testprebid.sh
```
