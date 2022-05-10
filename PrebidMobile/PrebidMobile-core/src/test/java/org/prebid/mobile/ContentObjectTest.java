package org.prebid.mobile;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ContentObjectTest {

    @Test
    public void createContentObject_fillIt_rightJsonObject() {
        ContentObject content = new ContentObject();

        String id = "testId";
        content.setId(id);
        assertEquals(id, content.getId());

        Integer episode = 111;
        content.setEpisode(episode);
        assertEquals(episode, content.getEpisode());

        String title = "testTitle";
        content.setTitle(title);
        assertEquals(title, content.getTitle());

        String series = "testSeries";
        content.setSeries(series);
        assertEquals(series, content.getSeries());

        String season = "testSeason";
        content.setSeason(season);
        assertEquals(season, content.getSeason());

        String artist = "testArtist";
        content.setArtist(artist);
        assertEquals(artist, content.getArtist());

        String genre = "testGenre";
        content.setGenre(genre);
        assertEquals(genre, content.getGenre());

        String album = "testAlbum";
        content.setAlbum(album);
        assertEquals(album, content.getAlbum());

        String iSrc = "testIsrc";
        content.setIsrc(iSrc);
        assertEquals(iSrc, content.getIsrc());

        String url = "testUrl";
        content.setUrl(url);
        assertEquals(url, content.getUrl());

        ArrayList<String> categories = new ArrayList<>();
        categories.add("testCategory1");
        categories.add("testCategory2");
        content.setCategories(categories);
        content.addCategory("testCategory3");
        assertThat(content.getCategories(), hasItems(
                "testCategory1",
                "testCategory2",
                "testCategory3"
        ));

        Integer productionQuality = 112;
        content.setProductionQuality(productionQuality);
        assertEquals(productionQuality, content.getProductionQuality());

        Integer context = 113;
        content.setContext(context);
        assertEquals(context, content.getContext());

        String contentRating = "testContentRating";
        content.setContentRating(contentRating);
        assertEquals(contentRating, content.getContentRating());

        String userRating = "testUserRating";
        content.setUserRating(userRating);
        assertEquals(userRating, content.getUserRating());

        Integer qaMediaRating = 114;
        content.setQaMediaRating(qaMediaRating);
        assertEquals(qaMediaRating, content.getQaMediaRating());

        String keywords = "testKeywords";
        content.setKeywords(keywords);
        assertEquals(keywords, content.getKeywords());

        Integer liveStream = 115;
        content.setLiveStream(liveStream);
        assertEquals(liveStream, content.getLiveStream());

        Integer sourceRelationship = 116;
        content.setSourceRelationship(sourceRelationship);
        assertEquals(sourceRelationship, content.getSourceRelationship());

        Integer length = 117;
        content.setLength(length);
        assertEquals(length, content.getLength());

        String language = "testLanguage";
        content.setLanguage(language);
        assertEquals(language, content.getLanguage());

        Integer embeddable = 118;
        content.setEmbeddable(embeddable);
        assertEquals(embeddable, content.getEmbeddable());

        ArrayList<DataObject> dataObjects = new ArrayList<>();

        DataObject.SegmentObject segment1 = new DataObject.SegmentObject();
        segment1.setId("testSegmentId1");
        segment1.setName("testSegmentName1");
        segment1.setValue("testSegmentValue1");

        DataObject.SegmentObject segment2 = new DataObject.SegmentObject();
        segment2.setId("testSegmentId2");
        segment2.setName("testSegmentName2");
        segment2.setValue("testSegmentValue2");

        DataObject dataObject1 = new DataObject();
        dataObject1.setId("testId1");
        dataObject1.setName("testName1");
        dataObject1.setSegments(Lists.newArrayList(segment1));
        dataObject1.addSegment(segment2);
        dataObjects.add(dataObject1);

        DataObject dataObject2 = new DataObject();
        dataObject2.setId("testId2");
        dataObject2.setName("testName2");
        dataObjects.add(dataObject2);

        content.setDataList(dataObjects);

        ContentObject.ProducerObject producer = new ContentObject.ProducerObject();
        producer.setId("testProducerId1");
        producer.setName("testProducerName1");
        producer.setDomain("testProducerDomain1");
        producer.setCategories(Lists.newArrayList(
                "testProducerCategory1",
                "testProducerCategory2"
        ));
        producer.addCategory("testProducerCategory3");
        assertThat(producer.getCategories(), hasItems(
                "testProducerCategory1",
                "testProducerCategory2",
                "testProducerCategory3"
        ));

        content.setProducer(producer);

        String json = content.getJsonObject().toString();
        assertEquals(
                "{\"id\":\"testId\",\"episode\":111,\"title\":\"testTitle\",\"series\":\"testSeries\",\"season\":\"testSeason\",\"artist\":\"testArtist\",\"genre\":\"testGenre\",\"album\":\"testAlbum\",\"isrc\":\"testIsrc\",\"url\":\"testUrl\",\"prodq\":112,\"context\":113,\"contentrating\":\"testContentRating\",\"userrating\":\"testUserRating\",\"qagmediarating\":114,\"keywords\":\"testKeywords\",\"livestream\":115,\"sourcerelationship\":116,\"len\":117,\"language\":\"testLanguage\",\"embeddable\":118,\"producer\":{\"id\":\"testProducerId1\",\"name\":\"testProducerName1\",\"domain\":\"testProducerDomain1\",\"cat\":[\"testProducerCategory1\",\"testProducerCategory2\",\"testProducerCategory3\"]},\"cat\":[\"testCategory1\",\"testCategory2\",\"testCategory3\"],\"data\":[{\"id\":\"testId1\",\"name\":\"testName1\",\"segment\":[{\"id\":\"testSegmentId1\",\"name\":\"testSegmentName1\",\"value\":\"testSegmentValue1\"},{\"id\":\"testSegmentId2\",\"name\":\"testSegmentName2\",\"value\":\"testSegmentValue2\"}]},{\"id\":\"testId2\",\"name\":\"testName2\"}]}",
                json
        );
    }

}