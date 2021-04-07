fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew install fastlane`

# Available Actions
## Android
### android build_apps
```
fastlane android build_apps
```

### android unit_tests
```
fastlane android unit_tests
```
Running Unit tests for PrebidMobile-rendering
### android ui_tests_ppm
```
fastlane android ui_tests_ppm
```
Running UI tests for Prebid SDK PPM examples
### android ui_tests_gam
```
fastlane android ui_tests_gam
```
Running UI tests for Prebid SDK GAM examples
### android ui_tests_mopub
```
fastlane android ui_tests_mopub
```
Running UI tests for Prebid SDK MoPub examples
### android send_slack_message
```
fastlane android send_slack_message
```
Sends a notification to the Slack channel

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
