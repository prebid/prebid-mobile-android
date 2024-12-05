/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.interstitial;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardManager;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedClosingRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedCompletionRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedExt;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
@LooperMode(LEGACY)
public class AdInterstitialDialogTest {

    private AdInterstitialDialog adInterstitialDialog;

    private Context mockContext;
    private WebViewBase mockWebViewBase;
    private BaseJSInterface mockBaseJSInterface;
    private FrameLayout mockAdContainer;
    private InterstitialManager mockInterstitialManager;

    @Before
    public void setUp() throws Exception {
        mockContext = Robolectric.buildActivity(Activity.class).create().get();
        mockWebViewBase = mock(WebViewBase.class);
        mockAdContainer = mock(FrameLayout.class);
        mockBaseJSInterface = mock(BaseJSInterface.class);
        mockInterstitialManager = mock(InterstitialManager.class);

        when(mockWebViewBase.getMRAIDInterface()).thenReturn(mockBaseJSInterface);
        when(mockBaseJSInterface.getJsExecutor()).thenReturn(mock(JsExecutor.class));

        adInterstitialDialog = spy(new AdInterstitialDialog(mockContext,
                mockWebViewBase,
                mockAdContainer,
                mockInterstitialManager
        ));
    }

    @Test
    public void handleCloseClick() throws IllegalAccessException {
        InterstitialManager interstitialManager = mock(InterstitialManager.class);
        Field interstitialManagerField = WhiteBox.field(AdInterstitialDialog.class, "interstitialManager");
        interstitialManagerField.set(adInterstitialDialog, interstitialManager);

        adInterstitialDialog.handleCloseClick();
        verify(interstitialManager).interstitialClosed(mockWebViewBase);
    }

    @Test
    public void nullifyDialog() {
        adInterstitialDialog.nullifyDialog();

        verify(adInterstitialDialog, atLeastOnce()).cancel();
        verify(adInterstitialDialog).cleanup();
    }

    @Test
    public void cancelTest() {
        when(mockWebViewBase.isMRAID()).thenReturn(true);

        adInterstitialDialog.cancel();
        verify(mockBaseJSInterface).onStateChange(JSInterface.STATE_DEFAULT);
        verify(mockWebViewBase).detachFromParent();
    }


    @Test
    public void setUpCloseButton_noConfig() {
        AdInterstitialDialog spySubject = adInterstitialDialog;

        spySubject.setUpCloseButtonTask();

        verify(spySubject, never()).changeCloseViewVisibility(View.GONE);
        verify(spySubject, never()).scheduleCloseButtonDisplaying(anyInt(), anyBoolean());
        verify(spySubject, never()).scheduleRewardListener(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    public void setUpCloseButton_userAlreadyRewarded() {
        AdInterstitialDialog spySubject = adInterstitialDialog;

        InterstitialDisplayPropertiesInternal mockProperties = mock(InterstitialDisplayPropertiesInternal.class);
        when(mockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(mockProperties);

        RewardManager mockRewardManager = mock(RewardManager.class);
        when(mockRewardManager.getUserRewardedAlready()).thenReturn(true);

        AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);
        when(mockConfig.isRewarded()).thenReturn(true);
        when(mockConfig.getRewardManager()).thenReturn(mockRewardManager);
        mockProperties.config = mockConfig;

        spySubject.setUpCloseButtonTask();

        verify(spySubject, never()).changeCloseViewVisibility(View.GONE);
        verify(spySubject, never()).scheduleCloseButtonDisplaying(anyInt(), anyBoolean());
        verify(spySubject, never()).scheduleRewardListener(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    public void setUpCloseButton_default() {
        AdInterstitialDialog spySubject = adInterstitialDialog;
        InterstitialDisplayPropertiesInternal mockProperties = mock(InterstitialDisplayPropertiesInternal.class);

        AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);
        when(mockConfig.isRewarded()).thenReturn(true);
        RewardedCompletionRules completionRules = new RewardedCompletionRules();
        RewardedClosingRules closingRules = new RewardedClosingRules();
        RewardedExt rewardedExt = new RewardedExt(null, completionRules, closingRules);
        mockProperties.config = mockConfig;

        RewardManager mockRewardManager = mock(RewardManager.class);
        when(mockRewardManager.getRewardedExt()).thenReturn(rewardedExt);
        when(mockConfig.getRewardManager()).thenReturn(mockRewardManager);

        when(mockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(mockProperties);

        spySubject.setUpCloseButtonTask();

        verify(spySubject).changeCloseViewVisibility(View.GONE);
        verify(spySubject).scheduleRewardListener(RewardedCompletionRules.DEFAULT_BANNER_TIME_MS, 0, false);
    }

    @Test
    public void setUpCloseButton_rewardEventUrl() {
        AdInterstitialDialog spySubject = adInterstitialDialog;
        InterstitialDisplayPropertiesInternal mockProperties = mock(InterstitialDisplayPropertiesInternal.class);

        AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);
        when(mockConfig.isRewarded()).thenReturn(true);
        RewardedCompletionRules completionRules = new RewardedCompletionRules(null, null, null, "rwdd://yes", null, null);
        RewardedClosingRules closingRules = new RewardedClosingRules();
        RewardedExt rewardedExt = new RewardedExt(null, completionRules, closingRules);
        mockProperties.config = mockConfig;

        RewardManager mockRewardManager = mock(RewardManager.class);
        when(mockRewardManager.getRewardedExt()).thenReturn(rewardedExt);
        when(mockConfig.getRewardManager()).thenReturn(mockRewardManager);

        when(mockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(mockProperties);

        spySubject.setUpCloseButtonTask();

        verify(spySubject).changeCloseViewVisibility(View.GONE);
        verify(spySubject).scheduleRewardListener(RewardedCompletionRules.DEFAULT_BANNER_TIME_MS, 0, false);
        verify(mockRewardManager).setAfterRewardListener(any());
    }

    @Test
    public void setUpCloseButton_noRewardEventUrl() {
        AdInterstitialDialog spySubject = adInterstitialDialog;
        InterstitialDisplayPropertiesInternal mockProperties = mock(InterstitialDisplayPropertiesInternal.class);

        AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);
        when(mockConfig.isRewarded()).thenReturn(true);
        RewardedCompletionRules completionRules = new RewardedCompletionRules(15, null, null, null, null, null);
        RewardedClosingRules closingRules = new RewardedClosingRules();
        RewardedExt rewardedExt = new RewardedExt(null, completionRules, closingRules);
        mockProperties.config = mockConfig;

        RewardManager mockRewardManager = mock(RewardManager.class);
        when(mockRewardManager.getRewardedExt()).thenReturn(rewardedExt);
        when(mockConfig.getRewardManager()).thenReturn(mockRewardManager);

        when(mockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(mockProperties);

        spySubject.setUpCloseButtonTask();

        verify(spySubject).changeCloseViewVisibility(View.GONE);
        verify(spySubject).scheduleRewardListener(15_000, 0, false);
    }

}