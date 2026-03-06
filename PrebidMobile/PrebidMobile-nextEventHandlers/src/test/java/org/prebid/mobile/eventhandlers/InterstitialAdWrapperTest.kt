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
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
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
import org.mockito.MockitoAnnotations
import org.prebid.mobile.eventhandlers.AdEvent.Displayed
import org.prebid.mobile.eventhandlers.global.Constants
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class InterstitialAdWrapperTest {
    private lateinit var interstitialAdWrapper: InterstitialAdWrapper

    @Mock
    internal lateinit var mockListener: NextAdEventListener
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Dispatchers.setMain(testDispatcher)
        val context = Robolectric.buildActivity(Activity::class.java).create().get()

        interstitialAdWrapper = InterstitialAdWrapper(context, "123", mockListener, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onAppEvent_WithValidNameAndExpectedAppEvent_NotifyAppEventListener() = runTest {
        interstitialAdWrapper.onAppEvent(Constants.APP_EVENT, "")
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(1))
            .onEvent(AdEvent.AppEvent())
    }

    @Test
    fun onAppEvent_WithInvalidNameAndExpectedAppEvent_DoNothing() = runTest {
        interstitialAdWrapper.onAppEvent("test", "")
        advanceUntilIdle()

        Mockito.verifyNoInteractions(mockListener)
    }

    @Test
    fun onNextAdClosed_NotifyEventCloseListener() = runTest {
        interstitialAdWrapper.onAdDismissedFullScreenContent()
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(1))
            .onEvent(AdEvent.Closed())
    }

    @Test
    fun onNextAdFailedToLoad_NotifyEventErrorListener() = runTest {
        val wantedNumberOfInvocations = 10

        for (i in 0..<wantedNumberOfInvocations) {
            val adError =
                FullScreenContentError(FullScreenContentError.ErrorCode.INTERNAL_ERROR, "", null)
            interstitialAdWrapper.onAdFailedToShowFullScreenContent(adError)
        }
        advanceUntilIdle()
        Mockito.verify(mockListener, Mockito.times(wantedNumberOfInvocations))
            .onEvent(AdEvent.Failed())
    }

    @Test
    fun onNextAdOpened_NotifyEventDisplayedListener() = runTest {
        interstitialAdWrapper.onAdShowedFullScreenContent()
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(1)).onEvent(Displayed())
    }

    @Test
    fun onNextAdLoadedAppEventExpected_NotifyLoadedListener() = runTest {
        val mock = Mockito.mock(InterstitialAd::class.java)
        interstitialAdWrapper.onAdLoaded(mock)
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(1))
            .onEvent(AdEvent.Loaded())
    }

    @Test
    fun isLoaded_adIsNull_ReturnFalse() {
        Assert.assertFalse(interstitialAdWrapper.isLoaded())
    }
}