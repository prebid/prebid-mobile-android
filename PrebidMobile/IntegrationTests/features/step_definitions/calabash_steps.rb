require 'calabash-android/calabash_steps'

Then(/^I should see AppNexus creative$/) do
    webview_class = backdoor "getDFPWebViewName"
    names = webview_class.split('.')
    len = names.length
    short_name = names[len-1]
    @query_results = query(short_name + " css:'body'")
    unless @query_results[1]['html'].include?('pbm.js')
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
    contains_header_bidding_ad = false
    len = @query_results.length
    for counter in 0..len-1
        contains_header_bidding_ad = contains_header_bidding_ad || @query_results[counter]['html'].include?("A Header Bidding Ad.")
    end
    unless contains_header_bidding_ad
        raise "Prebid creative was not served, acutal content is : " + @query_results.to_s
    end
end

Then(/^I should see AppNexus creative in MraidBridgeMraidWebView$/) do
    @query_results = query("MraidWebView css:'*'")
    unless @query_results[0]['html'].include?('pbm.js')
        raise "Pbm.js not found, prebid creative was not served"
    end
end

