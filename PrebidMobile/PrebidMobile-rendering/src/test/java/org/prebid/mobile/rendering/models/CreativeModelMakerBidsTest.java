package org.prebid.mobile.rendering.models;

import com.apollo.test.utils.ResourceUtils;
import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.loading.AdLoadListener;
import org.prebid.mobile.rendering.loading.VastParserExtractor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CreativeModelMakerBidsTest {

    private CreativeModelMakerBids mModelMakerBids;
    @Mock
    private AdLoadListener mMockLoadListener;
    @Mock
    private VastParserExtractor mMockExtractor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mModelMakerBids = new CreativeModelMakerBids(mMockLoadListener);

        WhiteBox.setInternalState(mModelMakerBids, "mParserExtractor", mMockExtractor);
    }

    @Test
    public void whenMakeModelsAndNoAdConfiguration_CallErrorListener() {
        mModelMakerBids.makeModels(null, mock(BidResponse.class));
        verify(mMockLoadListener).onFailedToLoadAd(any(AdException.class), anyString());
    }

    @Test
    public void whenMakeModelsAndNoBidResponse_CallErrorListener() {
        mModelMakerBids.makeModels(mock(AdConfiguration.class), null);
        verify(mMockLoadListener).onFailedToLoadAd(any(AdException.class), anyString());
    }

    @Test
    public void whenMakeModelsAndBidResponseWithError_CallErrorListener() {
        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.hasParseError()).thenReturn(true);
        mModelMakerBids.makeModels(null, mockResponse);
        verify(mMockLoadListener).onFailedToLoadAd(any(AdException.class), anyString());
    }

    @Test
    public void whenMakeModelsAndBidRequestContainsAcjAd_CreateAcjModel() throws IOException {
        AdConfiguration configuration = new AdConfiguration();
        configuration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);

        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json");
        BidResponse bidResponse = new BidResponse(responseString);

        ArgumentCaptor<CreativeModelsMaker.Result> resultArgumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);

        mModelMakerBids.makeModels(configuration, bidResponse);
        verify(mMockLoadListener).onCreativeModelReady(resultArgumentCaptor.capture());
        CreativeModel creativeModel = resultArgumentCaptor.getValue().creativeModels.get(0);
        Bid bid = bidResponse.getSeatbids().get(0).getBids().get(0);
        assertEquals("HTML", creativeModel.getName());
        assertEquals(bid.getAdm(), creativeModel.getHtml());
        assertEquals(bid.getWidth(), creativeModel.getWidth());
        assertEquals(bid.getHeight(), creativeModel.getHeight());
        assertFalse(creativeModel.isRequireImpressionUrl());
    }

    @Test
    public void makeVideoModels_ExecuteVastParserExtractor() {
        final AdConfiguration mockConfig = mock(AdConfiguration.class);
        final String vast = "1234";

        mModelMakerBids.makeVideoModels(mockConfig, vast);

        verify(mockConfig).setAdUnitIdentifierType(eq(AdConfiguration.AdUnitIdentifierType.VAST));
        verify(mMockExtractor).extract(eq(vast));
    }

    @Test
    public void cancel_CancelExtractor() {
        mModelMakerBids.cancel();

        verify(mMockExtractor).cancel();
    }
}