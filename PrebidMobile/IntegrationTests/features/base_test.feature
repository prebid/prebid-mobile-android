Feature: Base test

Scenario: Load DFP Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "Show Banner Example"
    Then I press "Refresh Banner"
    Then I wait for 5 seconds
    Then I should see AppNexus creative

Scenario: Load DFP Interstitial Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "Show Interstitial Example"
    Then I press "loadInterstitial"
    Then I wait for 5 seconds
    Then I should see AppNexus creative
    Then I press "Interstitial close button"

Scenario: Load MoPub Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "MoPub"
    Then I press "Show Banner Example"
    Then I press "Refresh Banner"
    Then I wait for 5 seconds
    Then I should see AppNexus creative in HTMLBannerWebView

Scenario: Load MoPub Interstitial Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "MoPub"
    Then I press "Show Interstitial Example"
    Then I press "loadInterstitial"
    Then I press "loadInterstitial"
    Then I wait for 5 seconds
    Then I should see AppNexus creative in MraidBridgeMraidWebView
    Then I press "Interstitial close button"
