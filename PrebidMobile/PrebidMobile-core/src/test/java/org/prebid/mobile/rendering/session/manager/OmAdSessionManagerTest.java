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
    // private OmAdSessionManager omAdSessionManager;
    // @Mock
    // AdSession mockAdSession;
    // @Mock
    // JSLibraryManager mockJsLibraryManager;
    //
    // @Before
    // public void setUp() throws IllegalAccessException {
    //     MockitoAnnotations.initMocks(this);
    //
    //     omAdSessionManager = OmAdSessionManager.createNewInstance(mockJsLibraryManager);
    //     WhiteBox.field(OmAdSessionManager.class, "adSession").set(omAdSessionManager, mockAdSession);
    // }
    //
    // @Test
    // public void createInstanceOmNotActive_ReturnNull() {
    //     when(Omid.isActive()).thenReturn(false);
    //     OmAdSessionManager omAdSessionManager = OmAdSessionManager.createNewInstance(mockJsLibraryManager);
    //
    //     assertNull(omAdSessionManager);
    // }
    //
    // @Test
    // public void createInstanceOmActive_ReturnInstance() {
    //     when(Omid.isActive()).thenReturn(true);
    //     OmAdSessionManager omAdSessionManager = OmAdSessionManager.createNewInstance(mockJsLibraryManager);
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
    //     omAdSessionManager.addObstruction(friendlyObstruction);
    //     verify(mockAdSession).addFriendlyObstruction(null, FriendlyObstructionPurpose.VIDEO_CONTROLS, "content");
    // }
    //
    // @Test
    // public void trackVideoAdEventsTest() throws Exception {
    //
    //     MediaEvents mockVideoAdEvent = mock(MediaEvents.class);
    //     WhiteBox.field(OmAdSessionManager.class, "mediaEvents")
    //                   .set(omAdSessionManager, mockVideoAdEvent);
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_PAUSE);
    //     verify(mockVideoAdEvent).pause();
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_RESUME);
    //     verify(mockVideoAdEvent).resume();
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_SKIP);
    //     verify(mockVideoAdEvent).skipped();
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_COMPLETE);
    //     verify(mockVideoAdEvent).complete();
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
    //     verify(mockVideoAdEvent).firstQuartile();
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_MIDPOINT);
    //     verify(mockVideoAdEvent).midpoint();
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    //     verify(mockVideoAdEvent).thirdQuartile();
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_FULLSCREEN);
    //     verify(mockVideoAdEvent).playerStateChange(PlayerState.FULLSCREEN);
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_EXITFULLSCREEN);
    //     verify(mockVideoAdEvent).playerStateChange(PlayerState.NORMAL);
    //
    //     omAdSessionManager.trackAdVideoEvent(VideoAdEvent.Event.AD_CLICK);
    //     verify(mockVideoAdEvent).adUserInteraction(InteractionType.CLICK);
    //
    //     omAdSessionManager.videoAdStarted(0, 0);
    //     verify(mockVideoAdEvent).start(0, 0);
    //
    //     omAdSessionManager.trackVolumeChange(1);
    //     verify(mockVideoAdEvent).volumeChange(1);
    // }
    //
    // @Test
    // public void nonSkippableVideoAdLoadedTest() throws IllegalAccessException {
    //     AdEvents mockAdEvents = PowerMockito.mock(AdEvents.class);
    //     WhiteBox.field(OmAdSessionManager.class, "adEvents")
    //                   .set(omAdSessionManager, mockAdEvents);
    //
    //     omAdSessionManager.nonSkippableStandaloneVideoAdLoaded(false);
    //     VastProperties vastProperties = VastProperties.createVastPropertiesForNonSkippableMedia(false, Position.STANDALONE);
    //     mockAdEvents.loaded(vastProperties);
    // }
    //
    // @Test
    // public void registerAdViewTest() {
    //     View adView = any(View.class);
    //     omAdSessionManager.registerAdView(adView);
    //     verify(mockAdSession).registerAdView(adView);
    // }
    //
    // @Test
    // public void stopAdSessionTest() {
    //     omAdSessionManager.stopAdSession();
    //     verify(mockAdSession).finish();
    // }
    //
    // @Test
    // public void startAdSessionTest() throws IllegalAccessException {
    //     omAdSessionManager.startAdSession();
    //     verify(mockAdSession).start();
    // }
}
