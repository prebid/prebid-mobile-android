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
import android.os.Handler
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.eventhandlers.AdEvent.Displayed
import org.prebid.mobile.rendering.bidding.data.bid.Bid
import org.prebid.mobile.rendering.bidding.data.bid.Prebid
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener
import org.prebid.mobile.test.utils.WhiteBox
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class NextInterstitialEventHandlerTest {
    private lateinit var eventHandler: NextInterstitialEventHandler

    @Mock
    private lateinit var mockEventListener: InterstitialEventListener

    @Mock
    private lateinit var mockAppEventHandler: Handler

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        val activity = Robolectric.buildActivity(Activity::class.java).get()

        eventHandler = NextInterstitialEventHandler(activity, GAM_AD_UNIT_ID)
        eventHandler.setInterstitialEventListener(mockEventListener)
    }

    @Test
    fun onAppEventWithValidNameAndExpectedAppEvent_HandleAppEvent() {
        changeExpectingAppEventStatus(true)

        eventHandler.onEvent(AdEvent.AppEvent())

        Mockito.verify(mockEventListener, Mockito.times(1)).onPrebidSdkWin()
        Assert.assertFalse(this.expectingAppEventStatus)
    }

    @Test
    fun onNextAdClosed_NotifyEventCloseListener() {
        eventHandler.onEvent(AdEvent.Closed())

        Mockito.verify(mockEventListener, Mockito.times(1)).onAdClosed()
    }

    @Test
    fun onNextAdFailedToLoad_NotifyEventErrorListener() {
        val wantedNumberOfInvocations = 10

        for (i in 0..<wantedNumberOfInvocations) {
            val adEvent: AdEvent = AdEvent.Failed(i)
            eventHandler.onEvent(adEvent)
        }
        Mockito.verify(
            mockEventListener, Mockito.times(wantedNumberOfInvocations)
        ).onAdFailed(ArgumentMatchers.any(AdException::class.java))
    }

    @Test
    fun onNextAdOpened_NotifyBannerEventDisplayListener() {
        eventHandler.onEvent(Displayed())

        Mockito.verify(mockEventListener, Mockito.times(1)).onAdDisplayed()
    }

    @Test
    fun onNextAdLoadedAppEventExpected_ScheduleAppEventHandler() {
        changeExpectingAppEventStatus(true)

        eventHandler.onEvent(AdEvent.Loaded())

        Assert.assertNotNull(WhiteBox.getInternalState(eventHandler, "appEventHandler"))
    }

    @Test
    @Throws(Exception::class)
    fun onNextAdLoadedAppEventNotExpectedAndRequestInterstitialNotNull_NotifyEventListenerOnAdServerWin() {
        val publisherInterstitialAd = Mockito.mock(InterstitialAdWrapper::class.java)
        WhiteBox.field(NextInterstitialEventHandler::class.java, "requestInterstitial")
            .set(eventHandler, publisherInterstitialAd)

        eventHandler.onEvent(AdEvent.Loaded())

        Mockito.verify(mockEventListener, Mockito.times(1)).onAdServerWin()
    }

    @Test
    @Throws(Exception::class)
    fun onAppEventTimeout_NotifyBannerEventOnAdServerWin() {
        WhiteBox.method(NextInterstitialEventHandler::class.java, "handleAppEventTimeout")
            .invoke(eventHandler)

        Mockito.verify(mockEventListener, Mockito.times(1)).onAdServerWin()
    }

    @Test
    @Throws(IllegalAccessException::class)
    fun destroy_CancelTimer() {
        // by default apEventHandler is null if not scheduled
        WhiteBox.field(NextInterstitialEventHandler::class.java, "appEventHandler")
            .set(eventHandler, mockAppEventHandler)

        eventHandler.destroy()

        Mockito.verify(mockAppEventHandler, Mockito.times(1)).removeCallbacksAndMessages(null)
    }

    @Test
    fun requestAdWithDifferentBids_VerifyAdStatus() {
        val mockBid = Mockito.mock(Bid::class.java)
        val mockPrebid = Mockito.mock(Prebid::class.java)
        Mockito.`when`(mockPrebid.targeting).thenReturn(HashMap<String?, String?>())

        Mockito.`when`(mockBid.getPrebid()).thenReturn(mockPrebid)
        Mockito.`when`(mockBid.price).thenReturn(0.2)
        eventHandler.requestAdWithBid(mockBid)
        Assert.assertTrue(this.expectingAppEventStatus)

        Mockito.`when`(mockBid.price).thenReturn(0.0)
        eventHandler.requestAdWithBid(mockBid)
        Assert.assertFalse(this.expectingAppEventStatus)

        eventHandler.requestAdWithBid(null)
        Assert.assertFalse(this.expectingAppEventStatus)
    }

    @Test
    fun showWhenEmbeddedInterstitialIsNull_NotifyEventErrorListener() {
        eventHandler.show()

        Mockito.verify(mockEventListener).onAdFailed(ArgumentMatchers.any(AdException::class.java))
    }

    private fun changeExpectingAppEventStatus(status: Boolean) {
        WhiteBox.setInternalState(eventHandler, "isExpectingAppEvent", status)
    }

    private val expectingAppEventStatus: Boolean
        get() = WhiteBox.getInternalState(
            eventHandler, "isExpectingAppEvent"
        )

    companion object {
        private const val GAM_AD_UNIT_ID = "12345678"
    }
}
