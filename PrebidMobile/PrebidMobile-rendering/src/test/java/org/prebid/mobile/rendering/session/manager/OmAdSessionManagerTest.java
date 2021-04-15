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

package org.prebid.mobile.rendering.session.manager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class OmAdSessionManagerTest {
    // TODO: 03.12.2020 How to test without Powermock static mock
    @Test
    public void test() {}
    // private OmAdSessionManager mOmAdSessionManager;
    // @Mock
    // AdSession mMockAdSession;
    // @Mock
    // JSLibraryManager mMockJsLibraryManager;
    //
    // @Before
    // public void setUp() throws IllegalAccessException {
    //     MockitoAnnotations.initMocks(this);
    //
    //     mOmAdSessionManager = OmAdSessionManager.createNewInstance(mMockJsLibraryManager);
    //     WhiteBox.field(OmAdSessionManager.class, "mAdSession").set(mOmAdSessionManager, mMockAdSession);
    // }
    //
    // @Test
    // public void createInstanceOmNotActive_ReturnNull() {
    //     when(Omid.isActive()).thenReturn(false);
    //     OmAdSessionManager omAdSessionManager = OmAdSessionManager.createNewInstance(mMockJsLibraryManager);
    //
    //     assertNull(omAdSessionManager);
    // }
    //
    // @Test
    // public void createInstanceOmActive_ReturnInstance() {
    //     when(Omid.isActive()).thenReturn(true);
    //     OmAdSessionManager omAdSessionManager = OmAdSessionManager.createNewInstance(mMockJsLibraryManager);
    //
    //     assertNotNull(omAdSessionManager);
    // }
    //
    // @Test
    // public void activateOmsdkFails_ThrowExceptionAndReturnFalse() {
    //     PowerMockito.doThrow(new ClassNotFoundException("Class not fould")).when(Omid.class);
    //     boolean result = OmAdSessionManager.activateOmSdk(mock(Context.class));
    //
    //     assertFalse(result);
    // }
    //
    // @Test
    // public void initObstructionsTest() {
    //     InternalFriendlyObstruction friendlyObstruction = new InternalFriendlyObstruction(null,
    //                                                                                       InternalFriendlyObstruction.Purpose.VIDEO_CONTROLS,
    //                                                                                       "content");
    //     mOmAdSessionManager.addObstruction(friendlyObstruction);
    //     verify(mMockAdSession).addFriendlyObstruction(null, FriendlyObstructionPurpose.VIDEO_CONTROLS, "content");
    // }
    //
    // @Test
    // public void trackVideoAdEventsTest() throws Exception {
    //
    //     MediaEvents mockVideoAdEvent = mock(MediaEvents.class);
    //     WhiteBox.field(OmAdSessionManager.class, "mMediaEvents")
    //                   .set(mOmAdSessionManager, mockVideoAdEvent);
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_PAUSE);
    //     verify(mockVideoAdEvent).pause();
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_RESUME);
    //     verify(mockVideoAdEvent).resume();
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_SKIP);
    //     verify(mockVideoAdEvent).skipped();
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_COMPLETE);
    //     verify(mockVideoAdEvent).complete();
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
    //     verify(mockVideoAdEvent).firstQuartile();
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_MIDPOINT);
    //     verify(mockVideoAdEvent).midpoint();
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    //     verify(mockVideoAdEvent).thirdQuartile();
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_FULLSCREEN);
    //     verify(mockVideoAdEvent).playerStateChange(PlayerState.FULLSCREEN);
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_EXITFULLSCREEN);
    //     verify(mockVideoAdEvent).playerStateChange(PlayerState.NORMAL);
    //
    //     mOmAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_CLICK);
    //     verify(mockVideoAdEvent).adUserInteraction(InteractionType.CLICK);
    //
    //     mOmAdSessionManager.videoAdStarted(0, 0);
    //     verify(mockVideoAdEvent).start(0, 0);
    //
    //     mOmAdSessionManager.trackVolumeChange(1);
    //     verify(mockVideoAdEvent).volumeChange(1);
    // }
    //
    // @Test
    // public void nonSkippableVideoAdLoadedTest() throws IllegalAccessException {
    //     AdEvents mockAdEvents = PowerMockito.mock(AdEvents.class);
    //     WhiteBox.field(OmAdSessionManager.class, "mAdEvents")
    //                   .set(mOmAdSessionManager, mockAdEvents);
    //
    //     mOmAdSessionManager.nonSkippableStandaloneVideoAdLoaded(false);
    //     VastProperties vastProperties = VastProperties.createVastPropertiesForNonSkippableMedia(false, Position.STANDALONE);
    //     mockAdEvents.loaded(vastProperties);
    // }
    //
    // @Test
    // public void registerAdViewTest() {
    //     View adView = any(View.class);
    //     mOmAdSessionManager.registerAdView(adView);
    //     verify(mMockAdSession).registerAdView(adView);
    // }
    //
    // @Test
    // public void stopAdSessionTest() {
    //     mOmAdSessionManager.stopAdSession();
    //     verify(mMockAdSession).finish();
    // }
    //
    // @Test
    // public void startAdSessionTest() throws IllegalAccessException {
    //     mOmAdSessionManager.startAdSession();
    //     verify(mMockAdSession).start();
    // }
}
