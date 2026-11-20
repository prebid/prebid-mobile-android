package org.prebid.mobile;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import android.os.Looper;
import android.view.View;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
public class PrebidNativeAdTest {

    @Before
    public void setUp() {
        CacheManager.clear();
    }

    @Test
    public void registerView_withAllTrackers() {
        PrebidNativeAd nativeAd = PrebidNativeAdTestHelper.nativeAdFromFile("PrebidNativeAdTest/Full.json");

        assertEquals("https://prebid.qa.openx.net//event?t=win&b=5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff&a=b4eb1475-4e3d-4186-97b7-25b6a6cf8618&bidder=openx&ts=1643899069308", nativeAd.getWinEvent());
        assertEquals("https://prebid.qa.openx.net//event?t=imp&b=5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff&a=b4eb1475-4e3d-4186-97b7-25b6a6cf8618&bidder=openx&ts=1643899069308", nativeAd.getImpEvent());

        // The fixture's only event tracker is the OM SDK verification script (method 2, JS,
        // custom event 555). It is not an impression pixel and must not be requested as one.
        assertNull(PrebidNativeAdTestHelper.reflectAdmImpressionTrackers(nativeAd));


        nativeAd.registerView(PrebidNativeAdTestHelper.createViewMock(), mock(List.class), mock(PrebidNativeAdEventListener.class));


        ArrayList<ImpressionTracker> trackerObjects = PrebidNativeAdTestHelper.reflectImpressionTrackerObjects(nativeAd);
        assertEquals(1, trackerObjects.size());
        assertEquals("https://prebid.qa.openx.net//event?t=imp&b=5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff&a=b4eb1475-4e3d-4186-97b7-25b6a6cf8618&bidder=openx&ts=1643899069308", PrebidNativeAdTestHelper.reflectImpressionTrackerUrl(trackerObjects.get(0)));
    }

    @Test
    public void parseImpressionTrackers_firesOnlyImagePixelsForMeasuredEvents() throws Exception {
        JSONObject nativeObj = new JSONObject("{\"eventtrackers\":["
                + "{\"event\":1,\"method\":1,\"url\":\"https://t.example/imp\"},"
                + "{\"event\":2,\"method\":1,\"url\":\"https://t.example/mrc50\"},"
                + "{\"event\":3,\"method\":1,\"url\":\"https://t.example/mrc100\"},"
                + "{\"event\":4,\"method\":1,\"url\":\"https://t.example/video50\"},"
                + "{\"event\":555,\"method\":1,\"url\":\"https://t.example/custom\"},"
                + "{\"event\":1,\"method\":2,\"url\":\"https://t.example/trk.js\"}"
                + "]}");

        ArrayList<String> trackers = PrebidNativeAd.parseImpressionTrackers(nativeObj, null);

        assertEquals(2, trackers.size());
        assertEquals("https://t.example/imp", trackers.get(0));
        assertEquals("https://t.example/mrc50", trackers.get(1));
    }

    @Test
    public void parseImpressionTrackers_firesLegacyImptrackers() throws Exception {
        // Native 1.1 bidders still send imptrackers; they were previously ignored entirely.
        JSONObject nativeObj = new JSONObject("{\"imptrackers\":[\"https://t.example/legacy?p={AUCTION_PRICE}\"]}");

        ArrayList<String> trackers = PrebidNativeAd.parseImpressionTrackers(nativeObj, "1.25");

        assertEquals(1, trackers.size());
        assertEquals("https://t.example/legacy?p=1.25", trackers.get(0));
    }

    @Test
    public void parseImpressionTrackers_sameUrlInBothFieldsFiresOnce() throws Exception {
        // Native 1.2 recommends sending both fields while buyers migrate.
        JSONObject nativeObj = new JSONObject("{"
                + "\"eventtrackers\":[{\"event\":1,\"method\":1,\"url\":\"https://t.example/imp\"}],"
                + "\"imptrackers\":[\"https://t.example/imp\",\"https://t.example/other\"]}");

        ArrayList<String> trackers = PrebidNativeAd.parseImpressionTrackers(nativeObj, null);

        assertEquals(2, trackers.size());
        assertEquals("https://t.example/imp", trackers.get(0));
        assertEquals("https://t.example/other", trackers.get(1));
    }

    @Test
    public void parseImpressionTrackers_missingEventOrMethodIsTreatedAsAnImpressionPixel() throws Exception {
        JSONObject nativeObj = new JSONObject("{\"eventtrackers\":["
                + "{\"url\":\"https://t.example/no-fields\"},"
                + "{\"event\":1,\"url\":\"https://t.example/no-method\"},"
                + "{\"method\":1,\"url\":\"https://t.example/no-event\"},"
                + "{\"event\":1,\"method\":1}"
                + "]}");

        ArrayList<String> trackers = PrebidNativeAd.parseImpressionTrackers(nativeObj, null);

        assertEquals(3, trackers.size());
    }

    @Test
    public void parseImpressionTrackers_noneReturnsNull() throws Exception {
        assertNull(PrebidNativeAd.parseImpressionTrackers(new JSONObject("{}"), null));
        assertNull(PrebidNativeAd.parseImpressionTrackers(new JSONObject(
                "{\"eventtrackers\":[{\"event\":1,\"method\":2,\"url\":\"https://t.example/trk.js\"}]}"), null));
    }

    @Test
    public void registerView_withoutTrackers() {
        PrebidNativeAd nativeAd = PrebidNativeAdTestHelper.nativeAdFromFile("PrebidNativeAdTest/WithoutTrackers.json");

        assertNull(nativeAd.getWinEvent());
        assertNull(nativeAd.getImpEvent());
        assertNull(PrebidNativeAdTestHelper.reflectAdmImpressionTrackers(nativeAd));


        nativeAd.registerView(PrebidNativeAdTestHelper.createViewMock(), mock(List.class), mock(PrebidNativeAdEventListener.class));


        ArrayList<ImpressionTracker> trackerObjects = PrebidNativeAdTestHelper.reflectImpressionTrackerObjects(nativeAd);
        assertEquals(0, trackerObjects.size());
    }

    @Test
    public void nativeAdParser() {
        PrebidNativeAd nativeAd = PrebidNativeAdTestHelper.nativeAdFromFile("PrebidNativeAdTest/Full.json");

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
        PrebidNativeAd nativeAd = PrebidNativeAdTestHelper.nativeAdFromFile("PrebidNativeAdTest/FullWithNativeWrapper.json");

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
    public void cacheManagerSaveWithExpireInterval_InvalidatesCache() {
        String cacheId = CacheManager.save("content", 1L);

        assertTrue(CacheManager.isValid(cacheId));

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(CacheManager.isValid(cacheId));
    }

    @Test
    public void cacheExpirationForNativeAdWithoutRegisteredView_NotifiesListener() {
        String resource = ResourceUtils.convertResourceToString("PrebidNativeAdTest/Full.json");
        String cacheId = CacheManager.save(resource, 1L);
        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);
        AtomicBoolean expired = new AtomicBoolean(false);

        nativeAd.registerPrebidNativeAdEventListener(new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdImpression() {
            }

            @Override
            public void onAdExpired() {
                expired.set(true);
            }
        });

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertTrue(expired.get());
    }

    @Test
    public void cacheExpirationForNativeAdWithRegisteredView_DoesNotNotifyListener() {
        String resource = ResourceUtils.convertResourceToString("PrebidNativeAdTest/Full.json");
        String cacheId = CacheManager.save(resource, 1L);
        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);
        AtomicBoolean expired = new AtomicBoolean(false);
        PrebidNativeAdEventListener listener = new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdImpression() {
            }

            @Override
            public void onAdExpired() {
                expired.set(true);
            }
        };
        View view = PrebidNativeAdTestHelper.createViewMock();
        ArrayList<View> clickableViews = new ArrayList<>();
        clickableViews.add(view);

        nativeAd.registerView(view, clickableViews, listener);

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(expired.get());
    }

    @Test
    public void cacheExpirationForNativeAd_RegisterViewAfterExpiryFails() {
        String resource = ResourceUtils.convertResourceToString("PrebidNativeAdTest/Full.json");
        String cacheId = CacheManager.save(resource, 1L);
        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);
        AtomicBoolean expired = new AtomicBoolean(false);

        nativeAd.registerPrebidNativeAdEventListener(new PrebidNativeAdTestHelper.TestNativeAdEventListener(expired::set));

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        View view = PrebidNativeAdTestHelper.createViewMock();
        ArrayList<View> clickableViews = new ArrayList<>();
        clickableViews.add(view);
        assertTrue(expired.get());
        assertFalse(nativeAd.registerView(view, clickableViews, mock(PrebidNativeAdEventListener.class)));
    }

    @Test
    public void cacheExpirationForNativeAd_DelegateCalledExactlyOnce() {
        String resource = ResourceUtils.convertResourceToString("PrebidNativeAdTest/Full.json");
        String cacheId = CacheManager.save(resource, 1L);
        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);
        AtomicInteger expireCallCount = new AtomicInteger(0);

        nativeAd.registerPrebidNativeAdEventListener(new PrebidNativeAdTestHelper.TestNativeAdEventListener(expired -> expireCallCount.incrementAndGet()));

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(2, TimeUnit.SECONDS);

        assertEquals(1, expireCallCount.get());
    }

    @Test
    public void cacheExpirationForNativeAdWithNullListener_DoesNotCrashAndPreventsRegistration() {
        String resource = ResourceUtils.convertResourceToString("PrebidNativeAdTest/Full.json");
        String cacheId = CacheManager.save(resource, 1L);
        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);

        nativeAd.registerPrebidNativeAdEventListener(null);

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertTrue(PrebidNativeAdTestHelper.isExpired(nativeAd));

        View view = PrebidNativeAdTestHelper.createViewMock();
        ArrayList<View> clickableViews = new ArrayList<>();
        clickableViews.add(view);
        assertFalse(nativeAd.registerView(view, clickableViews, null));
    }

}
