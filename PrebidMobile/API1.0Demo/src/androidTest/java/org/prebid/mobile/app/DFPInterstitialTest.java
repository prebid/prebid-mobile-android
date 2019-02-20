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
import org.prebid.mobile.ResultCode;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
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
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DFPInterstitialTest {
    @Rule
    public ActivityTestRule<MainActivity> m = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testDFPInterstitialWithoutAutoRefresh() throws Exception {
        onView(withId(R.id.adTypeSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Interstitial"))).perform(click());
        onView(withId(R.id.autoRefreshInput)).perform(typeText("0"));
        onView(withId(R.id.showAd)).perform(click());
        Thread.sleep(10000);
        String activityName = TestUtil.getCurrentActivity().getClass().getName();
        if ("com.google.android.gms.ads.AdActivity".equals(activityName)) {
            onWebView().check(webMatches(getCurrentUrl(), containsString("pubads.g.doubleclick.net/gampad/ads")));
            onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
            Espresso.pressBack();
            Thread.sleep(30000);
            assertEquals(1, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
        } else if ("org.prebid.mobile.app.DemoActivity".equals(activityName)) {
            assertEquals(ResultCode.SUCCESS, ((DemoActivity) TestUtil.getCurrentActivity()).resultCode);
            fail("Prebid Demand was fetched successfully, but DFP interstitial not loaded and show successfully");
        } else {
            fail("Demo app not working properly.");
        }
    }

    @Test
    public void testDFPInterstitialWithAutoRefresh() throws Exception {
        onView(withId(R.id.adTypeSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Interstitial"))).perform(click());
        onView(withId(R.id.autoRefreshInput)).perform(typeText("30000"));
        onView(withId(R.id.showAd)).perform(click());
        Thread.sleep(10000);
        String activityName = TestUtil.getCurrentActivity().getClass().getName();
        if ("com.google.android.gms.ads.AdActivity".equals(activityName)) {
            onWebView().check(webMatches(getCurrentUrl(), containsString("pubads.g.doubleclick.net/gampad/ads")));
            onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
            Espresso.pressBack();
            assertEquals(1, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
            Thread.sleep(30000);
            activityName = TestUtil.getCurrentActivity().getClass().getName();
            if ("com.google.android.gms.ads.AdActivity".equals(activityName)) {
                onWebView().check(webMatches(getCurrentUrl(), containsString("pubads.g.doubleclick.net/gampad/ads")));
                onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
                Espresso.pressBack();
                assertEquals("Auto refresh not happening", 2, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
                ((DemoActivity) TestUtil.getCurrentActivity()).stopAutoRefresh();
                Thread.sleep(30000);
                assertEquals("Auto refresh didn't stop", 2, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
            } else if ("org.prebid.mobile.app.DemoActivity".equals(activityName)) {
                assertEquals("Auto refresh not happening", 2, ((DemoActivity) TestUtil.getCurrentActivity()).refreshCount);
                assertEquals(ResultCode.SUCCESS, ((DemoActivity) TestUtil.getCurrentActivity()).resultCode);
                fail("Prebid Demand was fetched successfully, but DFP interstitial not loaded and show successfully");
            } else {
                fail("Demo app not working properly.");
            }

        } else if ("org.prebid.mobile.app.DemoActivity".equals(activityName)) {
            assertEquals(ResultCode.SUCCESS, ((DemoActivity) TestUtil.getCurrentActivity()).resultCode);
            fail("Prebid Demand was fetched successfully, but DFP interstitial not loaded and show successfully");
        } else {
            fail("Demo app not working properly.");
        }


    }
}
