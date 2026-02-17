package org.prebid.mobile;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class PrebidNativeAdTest {

    @Test
    public void registerView_withAllTrackers() {
        PrebidNativeAd nativeAd = nativeAdFromFile("PrebidNativeAdTest/Full.json");

        assertEquals("https://prebid.qa.openx.net//event?t=win&b=5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff&a=b4eb1475-4e3d-4186-97b7-25b6a6cf8618&bidder=openx&ts=1643899069308", nativeAd.getWinEvent());
        assertEquals("https://prebid.qa.openx.net//event?t=imp&b=5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff&a=b4eb1475-4e3d-4186-97b7-25b6a6cf8618&bidder=openx&ts=1643899069308", nativeAd.getImpEvent());

        ArrayList<String> admImpressionTrackers = reflectAdmImpressionTrackers(nativeAd);
        assertNotNull(admImpressionTrackers);
        assertEquals(1, admImpressionTrackers.size());
        assertThat(admImpressionTrackers, hasItem("https://s3-us-west-2.amazonaws.com/omsdk-files/compliance-js/omid-validation-verification-script-v1.js"));


        nativeAd.registerView(createViewMock(), mock(List.class), mock(PrebidNativeAdEventListener.class));


        ArrayList<ImpressionTracker> trackerObjects = reflectImpressionTrackerObjects(nativeAd);
        assertEquals(2, trackerObjects.size());
        assertEquals("https://s3-us-west-2.amazonaws.com/omsdk-files/compliance-js/omid-validation-verification-script-v1.js", reflectImpressionTrackerUrl(trackerObjects.get(0)));
        assertEquals("https://prebid.qa.openx.net//event?t=imp&b=5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff&a=b4eb1475-4e3d-4186-97b7-25b6a6cf8618&bidder=openx&ts=1643899069308", reflectImpressionTrackerUrl(trackerObjects.get(1)));
    }

    @Test
    public void registerView_withoutTrackers() {
        PrebidNativeAd nativeAd = nativeAdFromFile("PrebidNativeAdTest/WithoutTrackers.json");

        assertNull(nativeAd.getWinEvent());
        assertNull(nativeAd.getImpEvent());
        assertNull(reflectAdmImpressionTrackers(nativeAd));


        nativeAd.registerView(createViewMock(), mock(List.class), mock(PrebidNativeAdEventListener.class));


        ArrayList<ImpressionTracker> trackerObjects = reflectImpressionTrackerObjects(nativeAd);
        assertEquals(0, trackerObjects.size());
    }

    @Test
    public void nativeAdParser() {
        PrebidNativeAd nativeAd = nativeAdFromFile("PrebidNativeAdTest/Full.json");

        assertNotNull(nativeAd);

        assertEquals("OpenX (Title)", nativeAd.getTitle());
        assertEquals("https://www.saashub.com/images/app/service_logos/5/1df363c9a850/large.png?1525414023", nativeAd.getIconUrl());
        assertEquals("https://ssl-i.cdn.openx.com/mobile/demo-creatives/mobile-demo-banner-640x100.png", nativeAd.getImageUrl());
        assertEquals("Click here to visit our site!", nativeAd.getCallToAction());
        assertEquals("Learn all about this awesome story of someone using out OpenX SDK.", nativeAd.getDescription());
        assertEquals("OpenX (Brand)", nativeAd.getSponsoredBy());
        assertEquals("https://www.openx.com/", nativeAd.getClickUrl());

        ArrayList<NativeData> dataList = nativeAd.getDataList();
        assertEquals(5, dataList.size());
        assertThat(dataList, hasItem(new NativeData(NativeData.Type.SPONSORED_BY, "OpenX (Brand)")));
        assertThat(dataList, hasItem(new NativeData(NativeData.Type.DESCRIPTION, "Learn all about this awesome story of someone using out OpenX SDK.")));
        assertThat(dataList, hasItem(new NativeData(NativeData.Type.CALL_TO_ACTION, "Click here to visit our site!")));
        assertThat(dataList, hasItem(new NativeData(500, "Sample value")));
        assertThat(dataList, hasItem(new NativeData(0, "Sample value 2")));

        ArrayList<NativeTitle> titlesList = nativeAd.getTitles();
        assertEquals(1, titlesList.size());
        assertThat(titlesList, hasItem(new NativeTitle("OpenX (Title)")));

        ArrayList<NativeImage> imagesList = nativeAd.getImages();
        assertEquals(4, imagesList.size());
        assertThat(imagesList, hasItem(new NativeImage(NativeImage.Type.ICON, "https://www.saashub.com/images/app/service_logos/5/1df363c9a850/large.png?1525414023")));
        assertThat(imagesList, hasItem(new NativeImage(NativeImage.Type.MAIN_IMAGE, "https://ssl-i.cdn.openx.com/mobile/demo-creatives/mobile-demo-banner-640x100.png")));
        assertThat(imagesList, hasItem(new NativeImage(500, "https://test.com/test.png")));
        assertThat(imagesList, hasItem(new NativeImage(0, "https://test2.com/test.png")));

        for (NativeImage image : imagesList) {
            if (image.getType() == NativeImage.Type.CUSTOM) {
                if (image.getUrl().equals("https://test.com/test.png")) {
                    assertEquals(500, image.getTypeNumber());
                } else if (image.getUrl().equals("https://test2.com/test.png")) {
                    assertEquals(0, image.getTypeNumber());
                }
            }
        }
    }

    @Test
    public void nativeAdWithWrapperParser() {
        PrebidNativeAd nativeAd = nativeAdFromFile("PrebidNativeAdTest/FullWithNativeWrapper.json");

        assertNotNull(nativeAd);

        assertEquals("OpenX (Title)", nativeAd.getTitle());
        assertEquals("https://www.saashub.com/images/app/service_logos/5/1df363c9a850/large.png?1525414023", nativeAd.getIconUrl());
        assertEquals("https://ssl-i.cdn.openx.com/mobile/demo-creatives/mobile-demo-banner-640x100.png", nativeAd.getImageUrl());
        assertEquals("Click here to visit our site!", nativeAd.getCallToAction());
        assertEquals("Learn all about this awesome story of someone using out OpenX SDK.", nativeAd.getDescription());
        assertEquals("OpenX (Brand)", nativeAd.getSponsoredBy());
        assertEquals("https://www.openx.com/", nativeAd.getClickUrl());

        ArrayList<NativeData> dataList = nativeAd.getDataList();
        assertEquals(5, dataList.size());
        assertThat(dataList, hasItem(new NativeData(NativeData.Type.SPONSORED_BY, "OpenX (Brand)")));
        assertThat(dataList, hasItem(new NativeData(NativeData.Type.DESCRIPTION, "Learn all about this awesome story of someone using out OpenX SDK.")));
        assertThat(dataList, hasItem(new NativeData(NativeData.Type.CALL_TO_ACTION, "Click here to visit our site!")));
        assertThat(dataList, hasItem(new NativeData(500, "Sample value")));
        assertThat(dataList, hasItem(new NativeData(0, "Sample value 2")));

        ArrayList<NativeTitle> titlesList = nativeAd.getTitles();
        assertEquals(1, titlesList.size());
        assertThat(titlesList, hasItem(new NativeTitle("OpenX (Title)")));

        ArrayList<NativeImage> imagesList = nativeAd.getImages();
        assertEquals(4, imagesList.size());
        assertThat(imagesList, hasItem(new NativeImage(NativeImage.Type.ICON, "https://www.saashub.com/images/app/service_logos/5/1df363c9a850/large.png?1525414023")));
        assertThat(imagesList, hasItem(new NativeImage(NativeImage.Type.MAIN_IMAGE, "https://ssl-i.cdn.openx.com/mobile/demo-creatives/mobile-demo-banner-640x100.png")));
        assertThat(imagesList, hasItem(new NativeImage(500, "https://test.com/test.png")));
        assertThat(imagesList, hasItem(new NativeImage(0, "https://test2.com/test.png")));

        for (NativeImage image : imagesList) {
            if (image.getType() == NativeImage.Type.CUSTOM) {
                if (image.getUrl().equals("https://test.com/test.png")) {
                    assertEquals(500, image.getTypeNumber());
                } else if (image.getUrl().equals("https://test2.com/test.png")) {
                    assertEquals(0, image.getTypeNumber());
                }
            }
        }
    }

    private PrebidNativeAd nativeAdFromFile(String path) {
        String resource = ResourceUtils.convertResourceToString(path);
        String cacheId = CacheManager.save(resource);
        return PrebidNativeAd.create(cacheId);
    }

    private View createViewMock() {
        Context contextMock = mock(Context.class);
        when(contextMock.getApplicationContext()).thenReturn(mock(Application.class));

        View mainMock = mock(View.class);
        when(mainMock.getContext()).thenReturn(contextMock);
        return mainMock;
    }

    private ArrayList<String> reflectAdmImpressionTrackers(PrebidNativeAd ad) {
        return Reflection.getFieldOf(ad, "imp_trackers");
    }

    private ArrayList<ImpressionTracker> reflectImpressionTrackerObjects(PrebidNativeAd ad) {
        return Reflection.getFieldOf(ad, "impressionTrackers");
    }

    private String reflectImpressionTrackerUrl(ImpressionTracker tracker) {
        return Reflection.getFieldOf(tracker, "url");
    }

}
