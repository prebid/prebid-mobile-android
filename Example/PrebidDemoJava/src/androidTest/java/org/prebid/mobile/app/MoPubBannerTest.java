/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.app;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.matcher.DomMatchers.containingTextInBody;
import static androidx.test.espresso.web.model.Atoms.getCurrentUrl;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MoPubBannerTest {
    @Rule
    public ActivityTestRule<MainActivity> m = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testMoPubWithoutAutoRefreshAndSize300x250() throws Exception {
        onView(withId(R.id.adServerSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("MoPub"))).perform(click());
        onView(withId(R.id.autoRefreshInput)).perform(typeText("0"));
        onView(withId(R.id.showAd)).perform(click());
        Thread.sleep(10000);
        assertEquals(ResultCode.SUCCESS, ((DemoActivity) TestUtil.getCurrentActivity()).resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
        assertEquals(1, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
        Thread.sleep(120000);
        assertEquals(1, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
    }

    @Test
    public void testRubiconMoPubWithoutAutoRefreshAndSize300x250() throws Exception {
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_RUBICON);
        Constants.PBS_CONFIG_ID_300x250 = Constants.PBS_CONFIG_ID_300x250_RUBICON;
        Constants.DFP_BANNER_ADUNIT_ID_300x250 = Constants.MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON;

        onView(withId(R.id.adServerSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("MoPub"))).perform(click());
        onView(withId(R.id.autoRefreshInput)).perform(typeText("0"));
        onView(withId(R.id.showAd)).perform(click());
        Thread.sleep(10000);
        assertEquals(ResultCode.SUCCESS, ((DemoActivity) TestUtil.getCurrentActivity()).resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
        assertEquals(1, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
    }

    @Test
    public void testMoPubWithoutAutoRefreshAndSize320x50() throws Exception {
        onView(withId(R.id.adServerSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("MoPub"))).perform(click());
        onView(withId(R.id.adSizeSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("320x50"))).perform(click());
        onView(withId(R.id.autoRefreshInput)).perform(typeText("15000"));
        onView(withId(R.id.showAd)).perform(click());
        Thread.sleep(10000);
        assertEquals(ResultCode.SUCCESS, ((DemoActivity) TestUtil.getCurrentActivity()).resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("pbm.showAdFromCacheId")));
        assertEquals(1, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
        Thread.sleep(120000);
        assertEquals(1, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
    }

    @Test
    public void testMoPubWithAutoRefreshAndSize300x250() throws Exception {
        onView(withId(R.id.adServerSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("MoPub"))).perform(click());
        onView(withId(R.id.autoRefreshInput)).perform(typeText("30000"));
        onView(withId(R.id.showAd)).perform(click());
        Thread.sleep(10000);
        assertEquals(ResultCode.SUCCESS, ((DemoActivity) TestUtil.getCurrentActivity()).resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
        assertEquals(1, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
        Thread.sleep(120000);
        assertEquals(5, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
        ((DemoActivity) TestUtil.getCurrentActivity()).stopAutoRefresh();
        Thread.sleep(120000);
        assertEquals(5, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
    }
}
