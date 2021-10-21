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

package org.prebid.mobile.rendering.loading;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.VideoCreative;
import org.prebid.mobile.rendering.video.VideoCreativeModel;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.rendering.models.CreativeModelsMakerVast.HTML_CREATIVE_TAG;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CreativeFactoryTest {
    private Context mMockContext;
    private CreativeModel mMockModel;
    private CreativeFactory.Listener mMockListener;
    private OmAdSessionManager mMockOmAdSessionManager;
    private InterstitialManager mMockInterstitialManager;

    @Before
    public void setUp() throws Exception {
        mMockContext = Robolectric.buildActivity(Activity.class).create().get();
        mMockModel = mock(CreativeModel.class);
        mMockListener = mock(CreativeFactory.Listener.class);
        mMockOmAdSessionManager = mock(OmAdSessionManager.class);
        mMockInterstitialManager = mock(InterstitialManager.class);
    }

    @Test
    public void testCreativeFactory() throws Exception {
        boolean hasException;

        // Valid
        new CreativeFactory(mMockContext, mMockModel, mMockListener, mMockOmAdSessionManager, mMockInterstitialManager);

        // Null context
        hasException = false;
        try {
            new CreativeFactory(null, mMockModel, mMockListener, mMockOmAdSessionManager, mMockInterstitialManager);
        }
        catch (AdException e) {
            hasException = true;
        }
        assertTrue(hasException);

        // Null creative model
        hasException = false;
        try {
            new CreativeFactory(mMockContext, null, mMockListener, mMockOmAdSessionManager, mMockInterstitialManager);
        }
        catch (AdException e) {
            hasException = true;
        }
        assertTrue(hasException);

        // Null listener
        hasException = false;
        try {
            new CreativeFactory(mMockContext, mMockModel, null, mMockOmAdSessionManager, mMockInterstitialManager);
        }
        catch (AdException e) {
            hasException = true;
        }
        assertTrue(hasException);
    }

    @Test
    public void testAttemptAuidCreative() throws Exception {
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        Handler mockHandler = mock(Handler.class);
        when(mMockModel.getAdConfiguration()).thenReturn(adConfiguration);
        when(mMockModel.getName()).thenReturn(HTML_CREATIVE_TAG);
        when(mMockModel.getImpressionUrl()).thenReturn("impressionUrl");
        when(mMockModel.getClickUrl()).thenReturn("clickUrl");

        //Run the creativeFactory
        CreativeFactory creativeFactory = new CreativeFactory(mMockContext, mMockModel, mMockListener, mMockOmAdSessionManager, mMockInterstitialManager);
        WhiteBox.field(CreativeFactory.class, "mTimeoutHandler").set(creativeFactory, mockHandler);
        creativeFactory.start();

        AbstractCreative creative = creativeFactory.getCreative();
        assertNotNull(creative);
        assertTrue(creative instanceof HTMLCreative);
        verify(mockHandler).postDelayed(any(Runnable.class), eq(6_000L));
    }

    @Test
    public void testAttemptVastCreative() throws Exception {
        VideoCreativeModel mockVideoModel = mock(VideoCreativeModel.class);
        AdConfiguration adConfiguration = new AdConfiguration();
        Handler mockHandler = mock(Handler.class);
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);
        HashMap<VideoAdEvent.Event, ArrayList<String>> videoEventsUrls = new HashMap<>();
        videoEventsUrls.put(VideoAdEvent.Event.AD_EXPAND,
                            new ArrayList<>(Arrays.asList("AD_EXPAND")));
        when(mockVideoModel.getVideoEventUrls()).thenReturn(videoEventsUrls);
        when(mockVideoModel.getAdConfiguration()).thenReturn(adConfiguration);
        CreativeFactory creativeFactory;

        // Blank media URL
        when(mockVideoModel.getMediaUrl()).thenReturn("");
        creativeFactory = new CreativeFactory(mMockContext, mockVideoModel, mMockListener, mMockOmAdSessionManager, mMockInterstitialManager);
        creativeFactory.start();
        assertNull(WhiteBox.getInternalState(creativeFactory, "mCreative"));

        // Valid
        when(mockVideoModel.getMediaUrl()).thenReturn("mediaUrl");
        creativeFactory = new CreativeFactory(mMockContext, mockVideoModel, mMockListener, mMockOmAdSessionManager, mMockInterstitialManager);
        WhiteBox.field(CreativeFactory.class, "mTimeoutHandler").set(creativeFactory, mockHandler);
        creativeFactory.start();

        AbstractCreative creative = creativeFactory.getCreative();
        assertNotNull(creative);
        assertTrue(creative instanceof VideoCreative);
        verify(mockHandler).postDelayed(any(Runnable.class), eq(30_000L));
    }

    @Test
    public void testCreativeFactoryCreativeResolutionListener() throws Exception {
        CreativeFactory mockCreativeFactory = mock(CreativeFactory.class);
        CreativeFactory.Listener mockCreativeFactoryListener = mock(CreativeFactory.Listener.class);
        CreativeFactory.CreativeFactoryCreativeResolutionListener creativeResolutionListener = new CreativeFactory.CreativeFactoryCreativeResolutionListener(mockCreativeFactory);

        WhiteBox.field(CreativeFactory.class, "mListener").set(mockCreativeFactory, mockCreativeFactoryListener);
        WhiteBox.field(CreativeFactory.class, "mTimeoutHandler").set(mockCreativeFactory, mock(Handler.class));

        // Success
        creativeResolutionListener.creativeReady(mock(AbstractCreative.class));
        verify(mockCreativeFactoryListener).onSuccess();

        // Failure
        AdException adException = new AdException(AdException.INTERNAL_ERROR, "msg");
        creativeResolutionListener.creativeFailed(adException);

        verify(mockCreativeFactoryListener).onFailure(adException);
    }

    @Test
    public void creativeReadyWithExpiredTimeoutStatus_FactoryListenerNotCalled()
    throws IllegalAccessException {
        CreativeFactory mockCreativeFactory = mock(CreativeFactory.class);
        CreativeFactory.Listener mockCreativeFactoryListener = mock(CreativeFactory.Listener.class);
        CreativeFactory.TimeoutState expired = CreativeFactory.TimeoutState.EXPIRED;
        CreativeFactory.CreativeFactoryCreativeResolutionListener creativeResolutionListener = new CreativeFactory.CreativeFactoryCreativeResolutionListener(mockCreativeFactory);

        WhiteBox.field(CreativeFactory.class, "mListener").set(mockCreativeFactory, mockCreativeFactoryListener);
        WhiteBox.field(CreativeFactory.class, "mTimeoutState").set(mockCreativeFactory, expired);

        creativeResolutionListener.creativeReady(mock(AbstractCreative.class));

        verify(mockCreativeFactoryListener, never()).onSuccess();
    }

    @Test
    public void destroyCalled_removeCallbacksCalled()
    throws IllegalAccessException, AdException {
        CreativeFactory creativeFactory = new CreativeFactory(mMockContext, mMockModel, mMockListener, mMockOmAdSessionManager, mMockInterstitialManager);
        Handler mockHandler = mock(Handler.class);

        WhiteBox.field(CreativeFactory.class, "mTimeoutHandler").set(creativeFactory, mockHandler);

        creativeFactory.destroy();

        verify(mockHandler).removeCallbacks(null);
    }
}
