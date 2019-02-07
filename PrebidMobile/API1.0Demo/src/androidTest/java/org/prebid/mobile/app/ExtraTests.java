package org.prebid.mobile.app;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.ResultCode;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.matcher.DomMatchers.containingTextInBody;
import static androidx.test.espresso.web.model.Atoms.getCurrentUrl;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExtraTests {

    @Rule
    public ActivityTestRule<TestActivity> m = new ActivityTestRule<>(TestActivity.class);

    @Test
    public void testRubiconDemand() throws Exception {
        m.getActivity().setUpRunbiconDemandTest();
        Thread.sleep(10000);
        assertEquals(ResultCode.SUCCESS, m.getActivity().resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
    }
}
