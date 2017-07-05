Feature: Base test

Scenario: Load DFP Interstitial Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "Show Interstitial Example"
    Then I press "loadInterstitial"
    Then I wait for 5 seconds
    Then I should see AppNexus creative
    Then I press "Interstitial close button"

Scenario: Load DFP Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "Show Banner Example"
    Then I press "Refresh Banner"
    Then I wait for 5 seconds
    Then I should see AppNexus creative in PublisherAdView number 0
    Then I should see AppNexus creative in PublisherAdView number 1
