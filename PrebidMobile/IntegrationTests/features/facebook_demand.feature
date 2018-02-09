Feature: Facebook Demand Test

Scenario: Load Facebook Banner Ad in DFP
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "Facebook Banner 300x250"
    Then I press "load"
    Then I wait for 10 seconds
    Then I should see Facebook creative

Scenario: Load Facebook Interstitial Ad in DFP
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "Facebook Interstitial"
    Then I press "load"
    Then I wait for the "AudienceNetworkActivity" screen to appear

Scenario: Load Facebook Banner Ad in MoPub
    Given I wait for the "MainActivity" screen to appear
    Then I press "MoPub"
    Then I press "Facebook Banner 300x250"
    Then I press "load"
    Then I wait for 10 seconds
    Then I should see Facebook creative

Scenario: Load Facebook Interstitial Ad in MoPub
    Given I wait for the "MainActivity" screen to appear
    Then I press "MoPub"
    Then I press "Facebook Interstitial"
    Then I press "load"
    Then I wait for the "AudienceNetworkActivity" screen to appear