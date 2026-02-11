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
import android.os.Handler
import android.view.View
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.eventhandlers.AdEvent.Clicked
import org.prebid.mobile.rendering.bidding.data.bid.Bid
import org.prebid.mobile.rendering.bidding.data.bid.Prebid
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener
import org.prebid.mobile.test.utils.WhiteBox
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class NextBannerEventHandlerTest {
    private lateinit var bannerEventHandler: NextBannerEventHandler

    @Mock
    private val mockBannerEventListener: BannerEventListener? = null

    @Mock
    private val mockAppEventHandler: Handler? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        val context: Context = Robolectric.buildActivity<Activity>(Activity::class.java).get()

        bannerEventHandler = NextBannerEventHandler(context, GAM_AD_UNIT_ID, AD_SIZE)
        bannerEventHandler.setBannerEventListener(mockBannerEventListener!!)
    }

    @Test
    fun onAppEventWithValidNameAndExpectedAppEvent_HandleAppEvent() {
        changeExpectingAppEventStatus(true)

        bannerEventHandler.onEvent(AdEvent.AppEvent())

        Mockito.verify<BannerEventListener>(mockBannerEventListener, Mockito.times(1))
            .onPrebidSdkWin()
        Assert.assertFalse(this.expectingAppEventStatus)
    }

    @Test
    fun onNextAdClosed_NotifyBannerEventCloseListener() {
        bannerEventHandler.onEvent(AdEvent.Closed())

        Mockito.verify<BannerEventListener>(mockBannerEventListener, Mockito.times(1)).onAdClosed()
    }

    @Test
    fun onNextAdFailedToLoad_NotifyBannerEventErrorListener() {
        val wantedNumberOfInvocations = 10

        for (i in 0..<wantedNumberOfInvocations) {
            val adEvent: AdEvent = AdEvent.Failed(i)
            bannerEventHandler.onEvent(adEvent)
        }
        Mockito.verify<BannerEventListener>(
            mockBannerEventListener,
            Mockito.times(wantedNumberOfInvocations)
        ).onAdFailed(ArgumentMatchers.any<AdException?>(AdException::class.java))
    }

    @Test
    fun onNextAdOpened_NotifyBannerEventClickedListener() {
        bannerEventHandler.onEvent(Clicked())

        Mockito.verify<BannerEventListener>(mockBannerEventListener, Mockito.times(1))
            .onAdClicked()
    }

    @Test
    fun onNextAdLoadedAppEventExpected_ScheduleAppEventHandler() {
        changeExpectingAppEventStatus(true)

        bannerEventHandler.onEvent(AdEvent.Loaded())

        Assert.assertNotNull(WhiteBox.getInternalState(bannerEventHandler, "appEventHandler"))
    }

    @Test
    @Throws(Exception::class)
    fun onNextAdLoadedAppEventNotExpectedRequestBannerNotNull_NotifyBannerEventOnAdServerWin() {
        // can't mock a final view class
        val mockPublisherAdView = Mockito.mock(AdViewWrapper::class.java)
        val mockView = Mockito.mock(View::class.java)
        Mockito.`when`(mockPublisherAdView.view).thenReturn(mockView)

        WhiteBox.field(NextBannerEventHandler::class.java, "requestBanner")
            .set(bannerEventHandler, mockPublisherAdView)

        bannerEventHandler.onEvent(AdEvent.Loaded())

        Mockito.verify<BannerEventListener>(mockBannerEventListener, Mockito.times(1))
            .onAdServerWin(ArgumentMatchers.eq(mockView))
    }

    @Test
    @Throws(Exception::class)
    fun onAppEventTimeout_NotifyBannerEventOnAdServerWin() {
        WhiteBox.method(NextBannerEventHandler::class.java, "handleAppEventTimeout")
            .invoke(bannerEventHandler)

        Mockito.verify<BannerEventListener>(mockBannerEventListener, Mockito.times(1))
            .onAdServerWin(ArgumentMatchers.any())
    }

    @Test
    @Throws(IllegalAccessException::class)
    fun destroy_CancelTimer() {
        // by default apEventHandler is null if not scheduled
        WhiteBox.field(NextBannerEventHandler::class.java, "appEventHandler")
            .set(bannerEventHandler, mockAppEventHandler)

        bannerEventHandler.destroy()

        Mockito.verify<Handler>(mockAppEventHandler, Mockito.times(1))
            .removeCallbacksAndMessages(null)
    }

    @Test
    fun requestAdWithDifferentBids_VerifyAdStatus() {
        val mockBid = Mockito.mock(Bid::class.java)
        val mockPrebid = Mockito.mock(Prebid::class.java)
        Mockito.`when`(mockPrebid.targeting).thenReturn(HashMap<String?, String?>())

        Mockito.`when`(mockBid.prebid).thenReturn(mockPrebid)
        Mockito.`when`(mockBid.price).thenReturn(0.2)
        bannerEventHandler.requestAdWithBid(mockBid)
        Assert.assertTrue(expectingAppEventStatus)

        Mockito.`when`(mockBid.getPrice()).thenReturn(0.0)
        bannerEventHandler.requestAdWithBid(mockBid)
        Assert.assertFalse(expectingAppEventStatus)

        bannerEventHandler.requestAdWithBid(null)
        Assert.assertFalse(expectingAppEventStatus)
    }

    private fun changeExpectingAppEventStatus(status: Boolean) {
        WhiteBox.setInternalState(bannerEventHandler, "isExpectingAppEvent", status)
    }

    private val expectingAppEventStatus: Boolean
        get() = WhiteBox.getInternalState(
            bannerEventHandler,
            "isExpectingAppEvent"
        )

    companion object {
        private const val GAM_AD_UNIT_ID = "12345678"
        private val AD_SIZE = AdSize(350, 50)
    }
}