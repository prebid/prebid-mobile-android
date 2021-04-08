package org.prebid.mobile.rendering.bidding.data.ntv;

import com.apollo.test.utils.ResourceUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

import static org.junit.Assert.assertEquals;

public class NativeAdParserTest {
    private static final Integer DEFAULT_INTEGER = null;
    private static final Ext DEFAULT_EXT = null;
    private static final String DEFAULT_STRING = "";
    private static final List EMPTY_LIST = Collections.emptyList();

    private NativeAdParser mNativeAdParser;

    @Before
    public void setup() {
        mNativeAdParser = new NativeAdParser();
    }

    @Test
    public void parse_WithSingleTitle_ReturnNativeAdWithTitle() {
        List<NativeAdTitle> expectedAdTitleList = new ArrayList<>();
        NativeAdTitle expectedNativeAdTitle = new NativeAdTitle("text to test", 10, DEFAULT_EXT);
        expectedAdTitleList.add(expectedNativeAdTitle);

        NativeAd expected = new NativeAd(DEFAULT_STRING, null, DEFAULT_EXT, expectedAdTitleList,
                                         EMPTY_LIST, EMPTY_LIST,
                                         EMPTY_LIST, EMPTY_LIST);

        String adm = "{\"assets\":[{\"required\":1,\"title\":{\"text\":\"text to test\", \"len\":10}}]}";
        NativeAd actual = mNativeAdParser.parse(adm);

        assertEquals(expected, actual);
    }

    // Example response from https://www.iab.com/wp-content/uploads/2018/03/OpenRTB-Native-Ads-Specification-Final-1.2.pdf (page 28)
    @Test
    public void parse_ExampleFromIabDoc_MatchExpectedNativeAd() throws IOException {
        NativeAdTitle title = new NativeAdTitle("Learn about this awesome thing", DEFAULT_INTEGER, null);
        title.setNativeAdLink(new NativeAdLink("http://titlelink.com", EMPTY_LIST, "", null));

        NativeAdImage image = new NativeAdImage(null, "http://www.myads.com/thumbnail1.png",
                                                DEFAULT_INTEGER, DEFAULT_INTEGER, DEFAULT_EXT);

        NativeAdImage secondImage = new NativeAdImage(null, "http://www.myads.com/largethumb1.png",
                                                      DEFAULT_INTEGER, DEFAULT_INTEGER, DEFAULT_EXT);
        secondImage.setNativeAdLink(new NativeAdLink("imglink.com", EMPTY_LIST, "", null));
        NativeAdData data = new NativeAdData(NativeAssetData.DataType.DESC, "My Brand", DEFAULT_INTEGER, null);
        data.setNativeAdLink(new NativeAdLink("http://datalink.com", EMPTY_LIST, "", null));
        NativeAdData secondData = new NativeAdData(null,
                                                   "Learn all about this awesome story of someone using my product.", DEFAULT_INTEGER, DEFAULT_EXT);

        NativeAdEventTracker firstEventTracker = new NativeAdEventTracker(NativeEventTracker.EventType.IMPRESSION,
                                                                          NativeEventTracker.EventTrackingMethod.JS,
                                                                          "http://www.mytracker.com/tracker.js", DEFAULT_STRING, DEFAULT_EXT);
        NativeAdEventTracker secondEventTracker = new NativeAdEventTracker(NativeEventTracker.EventType.VIEWABLE_MRC50,
                                                                           NativeEventTracker.EventTrackingMethod.IMAGE,
                                                                           "http://www.mytracker.com/tracker.php", DEFAULT_STRING, DEFAULT_EXT);

        List<NativeAdTitle> nativeAdTitleList = new ArrayList<>();
        List<NativeAdImage> nativeAdImageList = new ArrayList<>();
        List<NativeAdData> nativeAdDataList = new ArrayList<>();
        List<NativeAdEventTracker> nativeAdEventTrackerList = new ArrayList<>();

        nativeAdTitleList.add(title);
        nativeAdImageList.add(image);
        nativeAdImageList.add(secondImage);
        nativeAdDataList.add(data);
        nativeAdDataList.add(secondData);
        nativeAdEventTrackerList.add(firstEventTracker);
        nativeAdEventTrackerList.add(secondEventTracker);

        NativeAdLink nativeAdLink = new NativeAdLink("http: //i.am.a/URL", EMPTY_LIST, "", DEFAULT_EXT);
        NativeAd expected = new NativeAd(DEFAULT_STRING, nativeAdLink, DEFAULT_EXT,
                                         nativeAdTitleList,
                                         nativeAdImageList,
                                         nativeAdDataList,
                                         EMPTY_LIST,
                                         nativeAdEventTrackerList);

        String adm = ResourceUtils.convertResourceToString("native_bid_response_example.json");
        NativeAd actual = mNativeAdParser.parse(adm);

        assertEquals(expected, actual);
    }

    @Test
    public void parse_FullAssetExample_NativeAdAndSingleAssetsAreEqualToExpected()
    throws Exception {

        String adm = ResourceUtils.convertResourceToString("native_bid_response_all_assets.json");
        NativeAd actual = mNativeAdParser.parse(adm);
        Ext expectedImgExt = createExt("extKeyImg");
        Ext expectedDataExt = createExt("extKeyData");
        Ext expectedTitleExt = createExt("extKeyTitle");

        NativeAdImage expectedIcon = new NativeAdImage(NativeAssetImage.ImageType.ICON,
                                                       "http://www.myads.com/largethumb1.png",
                                                       100,
                                                       100,
                                                       expectedImgExt);

        NativeAdImage expectedMainImage = new NativeAdImage(NativeAssetImage.ImageType.MAIN,
                                                            "http://www.myads.com/thumbnail1.png",
                                                            50,
                                                            150,
                                                            expectedImgExt);

        NativeAdVideo expectedNativeVideo = new NativeAdVideo(new MediaData("<vasttag/>"));

        assert actual != null;

        NativeAdVideo actualNativeVideoAd = actual.getNativeVideoAd();
        NativeAdTitle actualNativeAdTitle = actual.getNativeAdTitleList().get(0);
        NativeAdData firstActualNativeAdData = actual.getNativeAdDataList().get(0);
        NativeAdImage actualIcon = actual.getNativeAdImageList(NativeAssetImage.ImageType.ICON).get(0);
        NativeAdImage actualMain = actual.getNativeAdImageList(NativeAssetImage.ImageType.MAIN).get(0);

        int actualTitleLen = actualNativeAdTitle.getLen();

        // validate object content
        assertEquals(50, actualTitleLen);
        assertEquals(6, firstActualNativeAdData.getLen().intValue());
        assertEquals("1.2", actual.getVersion());
        assertEquals("Learn about this awesome thing", actual.getTitle());
        assertEquals("My Brand", actual.getCallToAction());
        assertEquals("Learn all about this awesome story of someone using my product.", actual.getText());
        assertEquals("http://www.myads.com/largethumb1.png", actual.getIconUrl());
        assertEquals("http://www.myads.com/thumbnail1.png", actual.getImageUrl());
        assertEquals(expectedNativeVideo.getMediaData(), actualNativeVideoAd.getMediaData());

        assertEquals(expectedIcon.getW(), actualIcon.getW());
        assertEquals(expectedIcon.getH(), actualIcon.getH());
        assertEquals(expectedMainImage.getW(), actualMain.getW());
        assertEquals(expectedMainImage.getH(), actualMain.getH());

        // validate ext
        assertEquals(expectedImgExt, actualIcon.getExt());
        assertEquals(expectedImgExt, actualMain.getExt());
        assertEquals(expectedDataExt, firstActualNativeAdData.getExt());
        assertEquals(expectedTitleExt, actualNativeAdTitle.getExt());

        // validate objects
        assertEquals(expectedIcon, actualIcon);
        assertEquals(expectedMainImage, actualMain);
        assertEquals(expectedNativeVideo, actualNativeVideoAd);

        // validate asset list size
        assertEquals(2, actual.getNativeAdDataList().size());
        assertEquals(2, actual.getNativeAdImageList().size());
        assertEquals(1, actual.getNativeAdTitleList().size());
        assertEquals(1, actual.getNativeAdVideoList().size());
    }

    private Ext createExt(String key) throws JSONException {
        Ext ext = new Ext();
        ext.put(new JSONObject("{\"" + key + "\":\"extValue\"}"));
        return ext;
    }
}