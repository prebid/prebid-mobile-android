[![Build Status](https://api.travis-ci.org/prebid/prebid-mobile-android.svg?branch=master)](https://travis-ci.org/prebid/prebid-mobile-android)

# Prebid Mobile Android SDK

To work with Prebid Mobile, you will need access to a Prebid Server.
See [this page](https://docs.prebid.org/prebid-server/overview/prebid-server-overview.html) for options.

## Use Maven?

Easily include the Prebid Mobile SDK using Maven. Simply add this line to your gradle dependencies:

```
implementation 'org.prebid:prebid-mobile-sdk:2.1.1'
```

## Build from source

Build Prebid Mobile from source code. After cloning the repo, from the root directory run

```
scripts/buildPrebidMobile.sh
```

to output the final lib jar and package you a demo app.


## Test Prebid Mobile

Run the test script to run unit tests and integration tests.

```
scripts/testPrebidMobile.sh
```
