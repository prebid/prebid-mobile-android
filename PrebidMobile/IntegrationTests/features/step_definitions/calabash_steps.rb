require 'calabash-android/calabash_steps'

Then(/^I should see AppNexus creative$/) do
    @query_results = query("o css:'*'")
    unless @query_results[0]['html'].include?('mbpb.js')
      raise "Mbpb.js not found, prebid creative was not served"
    end
end

Then(/^I should see AppNexus creative in PublisherAdView number (\d+)$/) do |arg1|
  @query_results = query("o index:#{arg1} css:'*'")
  unless @query_results[0]['html'].include?('mbpb.js')
    raise "Mbpb.js not found, prebid creative was not served in number #{arg1} PublisherAdView"
  end
end

