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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.models.internal.VastExtractorResult;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.modelcontrollers.AsyncVastLoader;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;
import org.prebid.mobile.rendering.parser.AdResponseParserVast;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.api.exceptions.AdException.INTERNAL_ERROR;
import static org.prebid.mobile.rendering.video.vast.VASTErrorCodes.WRAPPER_LIMIT_REACH_ERROR;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VastParserExtractorTest {

    private VastParserExtractor vastParserExtractor;
    @Mock private VastParserExtractor.Listener mockListener;
    @Mock private AsyncVastLoader mockAsyncVastLoader;
    @Mock private AdResponseParserVast mockResponseParserVast;

    private String defaultResponseString;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockResponseParserVast.getVastUrl()).thenReturn("url");

        vastParserExtractor = new VastParserExtractor(mockListener);
        WhiteBox.field(VastParserExtractor.class, "asyncVastLoader").set(vastParserExtractor, mockAsyncVastLoader);
        defaultResponseString = ResourceUtils.convertResourceToString("vast.xml");
    }

    @Test
    public void whenFirstExtract_AssignRootParserAndMakeRequest()
    throws IllegalAccessException, IOException {
        String responseString = ResourceUtils.convertResourceToString("vast_wrapper_linear_nonlinear.xml");
        vastParserExtractor.extract(responseString);
        assertNotNull(WhiteBox.field(VastParserExtractor.class, "rootVastParser").get(vastParserExtractor));
        verify(mockAsyncVastLoader).loadVast(anyString(), any());
    }

    @Test
    public void extractAndRootParserNotNull_SetWrapperToLatestParser()
    throws IllegalAccessException {
        AdResponseParserVast mockParser = mock(AdResponseParserVast.class);
        WhiteBox.field(VastParserExtractor.class, "rootVastParser").set(vastParserExtractor, mockResponseParserVast);
        WhiteBox.field(VastParserExtractor.class, "latestVastWrapperParser").set(vastParserExtractor, mockParser);
        vastParserExtractor.extract(defaultResponseString);
        verify(mockParser).setWrapper(any(AdResponseParserVast.class));
    }

    @Test
    public void extractAndVastUrlIsEmpty_NotifyListener() {
        when(mockResponseParserVast.getVastUrl()).thenReturn("");
        vastParserExtractor.extract(defaultResponseString);
        verify(mockListener).onResult(any(VastExtractorResult.class));
    }

    @Test
    public void extractAndWrapperLimitReached_CallOnFailedToLoad()
    throws IllegalAccessException, IOException {
        String responseString = ResourceUtils.convertResourceToString("vast_wrapper_linear_nonlinear.xml");
        WhiteBox.field(VastParserExtractor.class, "vastWrapperCount").set(vastParserExtractor, 5);
        final AdException exception = new AdException(INTERNAL_ERROR, WRAPPER_LIMIT_REACH_ERROR.toString());

        vastParserExtractor.extract(responseString);

        ArgumentCaptor<VastExtractorResult> argument = ArgumentCaptor.forClass(VastExtractorResult.class);

        verify(mockListener).onResult(argument.capture());
        final VastExtractorResult value = argument.getValue();
        assertTrue(value.hasException());
        assertEquals(exception.getMessage(), value.getAdException().getMessage());
    }

    // The [ERRORCODE] macro in the Error URI of a no-ad response is replaced with 303 before the request.
    @Test
    public void extractNoAdsResponse_FireErrorUrlWithErrorCodeAndNotifyFailure() {
        try (MockedStatic<ServerConnection> serverConnection = mockStatic(ServerConnection.class)) {
            vastParserExtractor.extract(noAdsVast("http://response1/noAds?code=[ERRORCODE]"));

            serverConnection.verify(() -> ServerConnection.fireAndForget("http://response1/noAds?code=303"));
            serverConnection.verifyNoMoreInteractions();
        }

        assertNoAdsFailure();
    }

    // Every Error URI up the wrapper chain gets 303, including the percent-encoded form of the macro.
    @Test
    public void extractNoAdsResponseAfterWrapper_FireErrorUrlsOfAllResponses() {
        String wrapperVast = "<VAST version=\"3.0\">"
                             + "<Error><![CDATA[http://response1/noAds?code=[ERRORCODE]]]></Error>"
                             + "<Ad id=\"1\"><Wrapper><AdSystem>test</AdSystem>"
                             + "<VASTAdTagURI><![CDATA[http://wrapper/vast]]></VASTAdTagURI>"
                             + "</Wrapper></Ad>"
                             + "</VAST>";
        BaseNetworkTask.GetUrlResult wrappedResponse = new BaseNetworkTask.GetUrlResult();
        wrappedResponse.responseString = noAdsVast("http://response2/noAds?code=%5bERRORCODE%5d&again=[ERRORCODE]");

        try (MockedStatic<ServerConnection> serverConnection = mockStatic(ServerConnection.class)) {
            vastParserExtractor.extract(wrapperVast);

            serverConnection.verifyNoInteractions();
            verify(mockListener, never()).onResult(any());
            ArgumentCaptor<ResponseHandler> responseHandler = ArgumentCaptor.forClass(ResponseHandler.class);
            verify(mockAsyncVastLoader).loadVast(eq("http://wrapper/vast"), responseHandler.capture());

            responseHandler.getValue().onResponse(wrappedResponse);

            serverConnection.verify(() -> ServerConnection.fireAndForget("http://response1/noAds?code=303"));
            serverConnection.verify(() -> ServerConnection.fireAndForget("http://response2/noAds?code=303&again=303"));
            serverConnection.verifyNoMoreInteractions();
        }

        assertNoAdsFailure();
    }

    // An Error URI without the macro is requested unchanged.
    @Test
    public void extractNoAdsResponseWithoutErrorCodeMacro_FireErrorUrlUnchanged() {
        try (MockedStatic<ServerConnection> serverConnection = mockStatic(ServerConnection.class)) {
            vastParserExtractor.extract(noAdsVast("http://response1/noAds?code=900&cb=12345"));

            serverConnection.verify(() -> ServerConnection.fireAndForget("http://response1/noAds?code=900&cb=12345"));
            serverConnection.verifyNoMoreInteractions();
        }

        assertNoAdsFailure();
    }

    @Test
    public void extractNoAdsResponseWithoutErrorUrl_NotifyFailure() throws IOException {
        try (MockedStatic<ServerConnection> serverConnection = mockStatic(ServerConnection.class)) {
            vastParserExtractor.extract(ResourceUtils.convertResourceToString("vast_noad.xml"));

            serverConnection.verifyNoInteractions();
        }

        assertNoAdsFailure();
    }

    @Test
    public void extractNoAdsAfterNestedWrappers_FireAllWrapperErrorsAndRootError() {
        try (MockedStatic<ServerConnection> serverConnection = mockStatic(ServerConnection.class)) {
            vastParserExtractor.extract(wrapperVast("http://wrapper/one",
                    "http://error/one?code=[ERRORCODE]", "http://error/two?code=%5berrorcode%5d", "  "));
            respondToWrapper("http://wrapper/one", wrapperVast("http://wrapper/two", "http://error/three?code=[ERRORCODE]"));
            serverConnection.verifyNoInteractions();
            verify(mockListener, never()).onResult(any());

            respondToWrapper("http://wrapper/two", noAdsVast("http://error/empty?code=[ERRORCODE]"));

            serverConnection.verify(() -> ServerConnection.fireAndForget("http://error/one?code=303"));
            serverConnection.verify(() -> ServerConnection.fireAndForget("http://error/two?code=303"));
            serverConnection.verify(() -> ServerConnection.fireAndForget("http://error/three?code=303"));
            serverConnection.verify(() -> ServerConnection.fireAndForget("http://error/empty?code=303"));
            serverConnection.verifyNoMoreInteractions();
        }
        assertNoAdsFailure();
    }

    @Test
    public void extractNoAdsWithoutRootError_FireWrapperError() {
        try (MockedStatic<ServerConnection> serverConnection = mockStatic(ServerConnection.class)) {
            vastParserExtractor.extract(wrapperVast("http://wrapper/vast", "http://error/wrapper?code=[ERRORCODE]"));
            respondToWrapper("http://wrapper/vast", "<VAST version=\"4.3\"/>");

            serverConnection.verify(() -> ServerConnection.fireAndForget("http://error/wrapper?code=303"));
            serverConnection.verifyNoMoreInteractions();
        }
        assertNoAdsFailure();
    }

    @Test
    public void extractSuccessfulWrappedResponse_DoesNotFireWrapperErrors() {
        try (MockedStatic<ServerConnection> serverConnection = mockStatic(ServerConnection.class)) {
            vastParserExtractor.extract(wrapperVast("http://wrapper/vast", "http://error/wrapper?code=[ERRORCODE]"));
            respondToWrapper("http://wrapper/vast", defaultResponseString);

            serverConnection.verifyNoInteractions();
        }
        ArgumentCaptor<VastExtractorResult> result = ArgumentCaptor.forClass(VastExtractorResult.class);
        verify(mockListener).onResult(result.capture());
        assertFalse(result.getValue().hasException());
    }

    @Test
    public void extractNoAds_FireOnlyFollowedWrapperErrors() {
        String vast = "<VAST version=\"4.3\">"
                + wrapperAd("http://wrapper/followed", "http://error/followed?code=[ERRORCODE]")
                + wrapperAd("http://wrapper/ignored", "http://error/ignored?code=[ERRORCODE]")
                + "</VAST>";
        try (MockedStatic<ServerConnection> serverConnection = mockStatic(ServerConnection.class)) {
            vastParserExtractor.extract(vast);
            respondToWrapper("http://wrapper/followed", "<VAST version=\"4.3\"/>");

            serverConnection.verify(() -> ServerConnection.fireAndForget("http://error/followed?code=303"));
            serverConnection.verifyNoMoreInteractions();
        }
        verify(mockAsyncVastLoader, never()).loadVast(eq("http://wrapper/ignored"), any());
        assertNoAdsFailure();
    }

    private void respondToWrapper(String url, String xml) {
        ArgumentCaptor<ResponseHandler> responseHandler = ArgumentCaptor.forClass(ResponseHandler.class);
        verify(mockAsyncVastLoader).loadVast(eq(url), responseHandler.capture());
        BaseNetworkTask.GetUrlResult response = new BaseNetworkTask.GetUrlResult();
        response.responseString = xml;
        responseHandler.getValue().onResponse(response);
    }

    private static String wrapperVast(String url, String... errorUrls) {
        return "<VAST version=\"4.3\">" + wrapperAd(url, errorUrls) + "</VAST>";
    }

    private static String wrapperAd(String url, String... errorUrls) {
        StringBuilder xml = new StringBuilder("<Ad id=\"1\"><Wrapper><AdSystem>test</AdSystem>");
        for (String errorUrl : errorUrls) {
            xml.append("<Error><![CDATA[").append(errorUrl).append("]]></Error>");
        }
        return xml.append("<VASTAdTagURI><![CDATA[").append(url)
                .append("]]></VASTAdTagURI></Wrapper></Ad>").toString();
    }

    @Test
    public void cancel_CancelRunningTask() {
        vastParserExtractor.cancel();

        verify(mockAsyncVastLoader).cancelTask();
    }

    @Test
    public void replaceErrorCodeMacro() {
        assertEquals("http://e?code=303", VastParserExtractor.replaceErrorCodeMacro("http://e?code=[ERRORCODE]", "303"));
        assertEquals("http://e?a=303&b=303", VastParserExtractor.replaceErrorCodeMacro("http://e?a=%5BERRORCODE%5D&b=%5berrorcode%5d", "303"));
        assertEquals("http://e?code=[errorcode]", VastParserExtractor.replaceErrorCodeMacro("http://e?code=[errorcode]", "303"));
        assertEquals("http://e?ts=[TIMESTAMP]", VastParserExtractor.replaceErrorCodeMacro("http://e?ts=[TIMESTAMP]", "303"));
    }

    private static String noAdsVast(String errorUrl) {
        return "<VAST version=\"3.0\"><Error><![CDATA[" + errorUrl + "]]></Error></VAST>";
    }

    private void assertNoAdsFailure() {
        ArgumentCaptor<VastExtractorResult> result = ArgumentCaptor.forClass(VastExtractorResult.class);
        verify(mockListener).onResult(result.capture());
        assertTrue(result.getValue().hasException());
        assertEquals(INTERNAL_ERROR + ": " + VastParserExtractor.NO_ADS_MESSAGE, result.getValue().getAdException().getMessage());
    }
}