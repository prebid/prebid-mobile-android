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
package org.prebid.mobile.eventhandlers

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.prebid.mobile.AdSize
import org.prebid.mobile.eventhandlers.global.Constants
import org.prebid.mobile.test.utils.WhiteBox
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class AdViewWrapperTest {
    private lateinit var adViewWrapper: AdViewWrapper

    @Mock
    internal lateinit var mockListener: NextAdEventListener

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Dispatchers.setMain(testDispatcher)
        val context: Context = Robolectric.buildActivity(Activity::class.java).create().get()

        adViewWrapper = AdViewWrapper.newInstance(
            context,
            "124",
            mockListener,
            listOf(AdSize(300, 250)),
            testDispatcher
        )!!
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onAppEvent_WithValidNameAndExpectedAppEvent_NotifyAppEventListener() = runTest {
        adViewWrapper.onAppEvent(Constants.APP_EVENT, "")
        advanceUntilIdle()
        verify(mockListener, times(1))
            .onEvent(AdEvent.AppEvent())
    }

    @Test
    fun onAppEvent_WithInvalidNameAndExpectedAppEvent_DoNothing() = runTest {
        adViewWrapper.onAppEvent("test", "")
        advanceUntilIdle()

        Mockito.verifyNoInteractions(mockListener)
    }

    @Test
    fun onNextAdClosed_NotifyBannerEventCloseListener() = runTest {
        adViewWrapper.onAdDismissedFullScreenContent()
        advanceUntilIdle()

        verify(mockListener, times(1))?.onEvent(AdEvent.Closed())
    }

    @Test
    fun onNextAdFailedToLoad_NotifyBannerEventErrorListener() = runTest {
        val wantedNumberOfInvocations = 10

        (0..<wantedNumberOfInvocations).forEach { i ->
            val loadAdError =
                LoadAdError(LoadAdError.ErrorCode.INTERNAL_ERROR, "", null)
            adViewWrapper.onAdFailedToLoad(loadAdError)
        }
        advanceUntilIdle()
        verify(mockListener, times(wantedNumberOfInvocations)).onEvent(AdEvent.Failed())
    }

    @Test
    fun onNextAdOpened_NotifyBannerEventClickedListener() = runTest {
        adViewWrapper.onAdClicked()
        advanceUntilIdle()

        verify(mockListener, times(1))
            .onEvent(AdEvent.Clicked())
    }

    @Test
    fun onNextAdLoadedAppEventExpected_NotifyLoadedListener() = runTest {
        val ad: BannerAd = Mockito.mock()
        adViewWrapper.onAdLoaded(ad)
        advanceUntilIdle()

        verify(mockListener, times(1))?.onEvent(AdEvent.Loaded())
    }

    @Test
    @Throws(IllegalAccessException::class)
    fun getView_ReturnNextView() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val publisherAdView = AdView(activity)

        WhiteBox.field(AdViewWrapper::class.java, "adView")
            .set(adViewWrapper, publisherAdView)

        val view = adViewWrapper.view

        Assert.assertEquals(publisherAdView, view)
    }
}