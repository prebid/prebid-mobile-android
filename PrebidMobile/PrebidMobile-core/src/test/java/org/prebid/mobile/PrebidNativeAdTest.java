package org.prebid.mobile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class PrebidNativeAdTest {

    @Test
    public void testNativeAdParser() {
        String cacheId = CacheManager.save(getResponse());
        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);

        assertNotNull(nativeAd);

        assertEquals("OpenX (Title)", nativeAd.getTitle());
        assertEquals("https://www.saashub.com/images/app/service_logos/5/1df363c9a850/large.png?1525414023", nativeAd.getIconUrl());
        assertEquals("https://ssl-i.cdn.openx.com/mobile/demo-creatives/mobile-demo-banner-640x100.png", nativeAd.getImageUrl());
        assertEquals("Click here to visit our site!", nativeAd.getCallToAction());
        assertEquals("Learn all about this awesome story of someone using out OpenX SDK.", nativeAd.getDescription());
        assertEquals("OpenX (Brand)", nativeAd.getSponsoredBy());

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

    private String getResponse() {
        return "{\n" +
                "  \"id\": \"5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff\",\n" +
                "  \"impid\": \"PrebidMobile\",\n" +
                "  \"price\": 0.11259999999999999,\n" +
                "  \"adm\": \"{\\\"assets\\\":[{\\\"required\\\":1,\\\"data\\\":{\\\"value\\\":\\\"Sample value 2\\\"}},{\\\"required\\\":1,\\\"data\\\":{\\\"type\\\":500,\\\"value\\\":\\\"Sample value\\\"}},{\\\"required\\\":1,\\\"img\\\":{\\\"type\\\":500,\\\"url\\\":\\\"https://test.com/test.png\\\"}},{\\\"required\\\":1,\\\"img\\\":{\\\"url\\\":\\\"https://test2.com/test.png\\\"}},\n{\\\"required\\\":1,\\\"title\\\":{\\\"text\\\":\\\"OpenX (Title)\\\"}},{\\\"required\\\":1,\\\"img\\\":{\\\"type\\\":1,\\\"url\\\":\\\"https:\\/\\/www.saashub.com\\/images\\/app\\/service_logos\\/5\\/1df363c9a850\\/large.png?1525414023\\\"}},{\\\"required\\\":1,\\\"img\\\":{\\\"type\\\":3,\\\"url\\\":\\\"https:\\/\\/ssl-i.cdn.openx.com\\/mobile\\/demo-creatives\\/mobile-demo-banner-640x100.png\\\"}},{\\\"required\\\":1,\\\"data\\\":{\\\"type\\\":1,\\\"value\\\":\\\"OpenX (Brand)\\\"}},{\\\"required\\\":1,\\\"data\\\":{\\\"type\\\":2,\\\"value\\\":\\\"Learn all about this awesome story of someone using out OpenX SDK.\\\"}},{\\\"required\\\":1,\\\"data\\\":{\\\"type\\\":12,\\\"value\\\":\\\"Click here to visit our site!\\\"}}],\\\"link\\\":{\\\"url\\\":\\\"https:\\/\\/www.openx.com\\/\\\"},\\\"eventtrackers\\\":[{\\\"event\\\":555,\\\"method\\\":2,\\\"url\\\":\\\"https:\\/\\/s3-us-west-2.amazonaws.com\\/omsdk-files\\/compliance-js\\/omid-validation-verification-script-v1.js\\\",\\\"ext\\\":{\\\"vendorKey\\\":\\\"iabtechlab.com-omid\\\",\\\"verification_parameters\\\":\\\"iabtechlab-Openx\\\"}}]}\",\n" +
                "  \"adid\": \"test-ad-id-12345\",\n" +
                "  \"adomain\": [\n" +
                "    \"openx.com\"\n" +
                "  ],\n" +
                "  \"crid\": \"test-creative-id-1\",\n" +
                "  \"w\": 300,\n" +
                "  \"h\": 250,\n" +
                "  \"ext\": {\n" +
                "    \"ad_ox_cats\": [\n" +
                "      2\n" +
                "    ],\n" +
                "    \"agency_id\": \"agency_10\",\n" +
                "    \"brand_id\": \"brand_10\",\n" +
                "    \"buyer_id\": \"buyer_10\",\n" +
                "    \"matching_ad_id\": {\n" +
                "      \"campaign_id\": 1,\n" +
                "      \"creative_id\": 3,\n" +
                "      \"placement_id\": 2\n" +
                "    },\n" +
                "    \"next_highest_bid_price\": 0.099,\n" +
                "    \"prebid\": {\n" +
                "      \"cache\": {\n" +
                "        \"key\": \"\",\n" +
                "        \"url\": \"\",\n" +
                "        \"bids\": {\n" +
                "          \"url\": \"prebid.qa.openx.net\\/cache?uuid=feb0b9c0-7064-4dd4-8607-bef8a41f7a2c\",\n" +
                "          \"cacheId\": \"feb0b9c0-7064-4dd4-8607-bef8a41f7a2c\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"targeting\": {\n" +
                "        \"hb_bidder\": \"openx\",\n" +
                "        \"hb_bidder_openx\": \"openx\",\n" +
                "        \"hb_cache_host\": \"prebid.qa.openx.net\",\n" +
                "        \"hb_cache_host_openx\": \"prebid.qa.openx.net\",\n" +
                "        \"hb_cache_id\": \"feb0b9c0-7064-4dd4-8607-bef8a41f7a2c\",\n" +
                "        \"hb_cache_id_openx\": \"feb0b9c0-7064-4dd4-8607-bef8a41f7a2c\",\n" +
                "        \"hb_cache_path\": \"\\/cache\",\n" +
                "        \"hb_cache_path_openx\": \"\\/cache\",\n" +
                "        \"hb_env\": \"mobile-app\",\n" +
                "        \"hb_env_openx\": \"mobile-app\",\n" +
                "        \"hb_pb\": \"0.10\",\n" +
                "        \"hb_pb_openx\": \"0.10\",\n" +
                "        \"hb_size\": \"300x250\",\n" +
                "        \"hb_size_openx\": \"300x250\"\n" +
                "      },\n" +
                "      \"type\": \"banner\",\n" +
                "      \"video\": {\n" +
                "        \"duration\": 0,\n" +
                "        \"primary_category\": \"\"\n" +
                "      },\n" +
                "      \"events\": {\n" +
                "        \"win\": \"https:\\/\\/prebid.qa.openx.net\\/\\/event?t=win&b=5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff&a=b4eb1475-4e3d-4186-97b7-25b6a6cf8618&bidder=openx&ts=1643899069308\",\n" +
                "        \"imp\": \"https:\\/\\/prebid.qa.openx.net\\/\\/event?t=imp&b=5f6bec03-a3ae-4084-b2ae-dedfb0ac01ff&a=b4eb1475-4e3d-4186-97b7-25b6a6cf8618&bidder=openx&ts=1643899069308\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

}
