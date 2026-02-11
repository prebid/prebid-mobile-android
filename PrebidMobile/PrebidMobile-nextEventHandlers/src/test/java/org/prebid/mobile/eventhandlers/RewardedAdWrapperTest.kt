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

import android.os.Bundle
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.prebid.mobile.eventhandlers.AdEvent.Displayed
import org.prebid.mobile.eventhandlers.global.Constants.APP_EVENT
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class RewardedAdWrapperTest {
    private lateinit var rewardedAdWrapper: RewardedAdWrapper

    @Mock
    internal lateinit var mockListener: NextAdEventListener

    @Rule
    @JvmField
    var rule: MockitoRule = MockitoJUnit.rule()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Dispatchers.setMain(testDispatcher)
        rewardedAdWrapper = RewardedAdWrapper("123", mockListener, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onAdMetadataChanged_WithMetadataContainsAdEvent_NotifyAppEventListener() = runTest {
        val mockRewardedAd = Mockito.mock(RewardedAd::class.java)
        val bundle = Bundle()
        bundle.putString(RewardedAdWrapper.KEY_METADATA, APP_EVENT)
        Mockito.`when`(mockRewardedAd.getAdMetadata()).thenReturn(bundle)

        rewardedAdWrapper.onAdLoaded(mockRewardedAd)
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(1))
            .onEvent(AdEvent.AppEvent())
    }

    @Test
    fun onAdMetadataChangedMetadata_ContainsNoAdEvent_DoNothing() = runTest {
        val mockRewardedAd = Mockito.mock(RewardedAd::class.java)
        val bundle = Bundle()
        Mockito.`when`(mockRewardedAd.getAdMetadata()).thenReturn(bundle)

        rewardedAdWrapper.onAdLoaded(mockRewardedAd)
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(0))
            .onEvent(AdEvent.AppEvent())
    }

    @Test
    fun onGamAdClosed_NotifyEventCloseListener() = runTest {
        rewardedAdWrapper.onAdDismissedFullScreenContent()
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(1))
            .onEvent(AdEvent.Closed())
    }

    @Test
    fun onGamAdFailedToLoad_NotifyEventErrorListener() = runTest {
        val wantedNumberOfInvocations = 10

        for (i in 0..<wantedNumberOfInvocations) {
            val adError =
                FullScreenContentError(FullScreenContentError.ErrorCode.INTERNAL_ERROR, "", null)
            rewardedAdWrapper.onAdFailedToShowFullScreenContent(adError)
        }
        advanceUntilIdle()
        Mockito.verify(mockListener, Mockito.times(wantedNumberOfInvocations))
            .onEvent(AdEvent.Failed())
    }

    @Test
    fun onGamAdOpened_NotifyBannerEventDisplayListener() = runTest {
        rewardedAdWrapper.onAdShowedFullScreenContent()
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(1)).onEvent(Displayed())
    }

    @Test
    fun onGamAdLoadedAppEventExpected_ScheduleAppEventHandler() = runTest {
        rewardedAdWrapper.onAdLoaded(Mockito.mock(RewardedAd::class.java))
        advanceUntilIdle()

        Mockito.verify(mockListener, Mockito.times(1))
            .onEvent(AdEvent.Loaded())
    }

    @Test
    fun isLoaded_adIsNull_ReturnFalse() {
        Assert.assertFalse(rewardedAdWrapper.isLoaded())
    }

    @Test
    fun isLoaded_adIsNonNull_ReturnTrue() {
        rewardedAdWrapper.onAdLoaded(Mockito.mock(RewardedAd::class.java))

        Assert.assertTrue(rewardedAdWrapper.isLoaded())
    }
}