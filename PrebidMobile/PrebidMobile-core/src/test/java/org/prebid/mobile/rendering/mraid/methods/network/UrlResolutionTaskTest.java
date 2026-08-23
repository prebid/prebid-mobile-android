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

package org.prebid.mobile.rendering.mraid.methods.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 25)
@LooperMode(LEGACY)
public class UrlResolutionTaskTest {

    private MockWebServer server;
    private UrlResolutionTask task;

    private final UrlResolutionTask.UrlResolutionListener noOpListener =
            new UrlResolutionTask.UrlResolutionListener() {
                @Override
                public void onSuccess(@NonNull String resolvedUrl) {
                }

                @Override
                public void onFailure(@NonNull String message, @Nullable Throwable throwable) {
                }
            };

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        task = new UrlResolutionTask(noOpListener);
        // Keep the stalled-host cases fast; this also exercises the configured
        // timeout rather than the SOCKET_TIMEOUT default.
        PrebidMobile.setTimeoutMillis(300);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
        // setTimeoutMillis also flips isTimeoutModified, and Robolectric does not reset
        // app statics between test classes sharing a sandbox. Left alone, every later
        // BaseNetworkTask read timeout would become 300ms instead of SOCKET_TIMEOUT.
        WhiteBox.setStaticVariableTo(PrebidMobile.class, "timeoutMillis", 2_000);
        WhiteBox.setStaticVariableTo(PrebidMobile.class, "isTimeoutModified", false);
    }

    @Test
    public void whenRedirectChainCompletes_resolvesToFinalUrl() {
        String finalUrl = server.url("/final").toString();
        server.enqueue(new MockResponse().setResponseCode(302).setHeader("location", finalUrl));
        server.enqueue(new MockResponse().setResponseCode(200));

        assertEquals(finalUrl, task.doInBackground(server.url("/click").toString()));
    }

    @Test
    public void whenRedirectHostStalls_returnsLastKnownUrlInsteadOfDroppingClick() {
        // A host that accepts the connection and then never answers is exactly the
        // slow-landing-page case: without a read timeout this blocks forever, and
        // without the fallback the tap resolves to null and nothing opens.
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        String clickUrl = server.url("/click").toString();

        assertEquals(clickUrl, task.doInBackground(clickUrl));
    }

    @Test
    public void whenLaterHopStalls_returnsTheHopAlreadyResolved() {
        // The more interesting half of the fallback: one redirect succeeded before the
        // stall, so the URL handed back is a resolved hop rather than the original.
        String secondHop = server.url("/second").toString();
        server.enqueue(new MockResponse().setResponseCode(302).setHeader("location", secondHop));
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        assertEquals(secondHop, task.doInBackground(server.url("/click").toString()));
    }

    @Test
    public void whenHostFailsHard_staysCancelled() {
        // Only timeouts fall back. A refused or dropped connection means the
        // destination is unreachable, so the click stays cancelled and no
        // click-tracking fires.
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        assertNull(task.doInBackground(server.url("/click").toString()));
    }

    @Test
    public void whenUrlIsNotHttp_returnsDeepLinkUnchanged() {
        String deepLink = "myapp://product/1";

        assertEquals(deepLink, task.doInBackground(deepLink));
    }

    @Test
    public void whenNoUrlSupplied_returnsNull() {
        assertNull(task.doInBackground());
    }
}
