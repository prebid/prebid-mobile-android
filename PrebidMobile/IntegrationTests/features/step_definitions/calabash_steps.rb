require 'calabash-android/calabash_steps'

Then(/^I should see AppNexus creative$/) do
    webview_class = backdoor "getDFPWebViewName"
    @query_results = query(webview_class + " css:'*'")
    puts @query_results[0]['html']
    unless @query_results[0]['html'].include?('pbm.js')
        raise "Pbm.js not found, prebid creative was not served"
    end
end

Then(/^I should see AppNexus creative in PublisherAdView number (\d+)$/) do |arg1|
    webview_class = backdoor "getDFPWebViewName"
    @query_results = query(webview_class + " index:#{arg1} css:'*'")
    unless @query_results[0]['html'].include?('pbm.js')
        raise "Pbm.js not found, prebid creative was not served in number #{arg1} PublisherAdView"
    end
end

Then(/^I should see AppNexus creative in HTMLBannerWebView$/) do
    @query_results = query("HTMLBannerWebView css:'*'")
    unless @query_results[0]['html'].include?('pbm.js')
        raise "Pbm.js not found, prebid creative was not served"
    end
end

Then(/^I should see AppNexus creative in MraidBridgeMraidWebView$/) do
    @query_results = query("MraidWebView css:'*'")
    unless @query_results[0]['html'].include?('pbm.js')
        raise "Pbm.js not found, prebid creative was not served"
    end
end

