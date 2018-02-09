Feature: AppNexus Demand test

Scenario: Load DFP Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "DFP"
    Then I press "AppNexus Banner 300x250"
    Then I press "load"
    Then I wait for 10 seconds
    Then I should see AppNexus creative

Scenario: Load MoPub Banner Ad
    Given I wait for the "MainActivity" screen to appear
    Then I press "MoPub"
    Then I press "AppNexus Banner 300x250"
    Then I press "load"
    Then I wait for 10 seconds
    Then I should see AppNexus creative in HTMLBannerWebView
