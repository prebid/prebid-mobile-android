Feature: Base test

@Facebook_For_DFP_Banner
Scenario: Load DFP Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "Show Facebook Demand"
    Then I press "Load Banner"
    Then I wait for 10 seconds
    Then I should see Facebook creative

@Facebook_For_DFP_Interstitial
Scenario: Load DFP Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "Show Facebook Demand"
    Then I press "Load Interstitial"
    Then I wait for the "AudienceNetworkActivity" screen to appear
    Then I wait for 10 seconds

@Facebook_For_MoPub_Banner
Scenario: Load MoPub Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "MoPub"
    Then I press "Show Facebook Demand"
    Then I press "Load Banner"
    Then I wait for 10 seconds
    Then I should see Facebook creative

@Facebook_For_MoPub_Interstitial
Scenario: Load MoPub Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "MoPub"
    Then I press "Show Facebook Demand"
    Then I press "Load Interstitial"
    Then I wait for the "AudienceNetworkActivity" screen to appear
    Then I wait for 10 seconds