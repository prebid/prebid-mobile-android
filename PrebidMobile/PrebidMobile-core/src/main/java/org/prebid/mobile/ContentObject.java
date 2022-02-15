package org.prebid.mobile;

import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContentObject {

    /**
     * ID uniquely identifying the content.
     */
    @Nullable
    private String id;

    /**
     * Episode number.
     */
    @Nullable
    private Integer episode;

    /**
     * Content title.
     */
    @Nullable
    private String title;

    /**
     * Content series.
     */
    @Nullable
    private String series;

    /**
     * Content season.
     */
    @Nullable
    private String season;

    /**
     * Artist credited with the content.
     */
    @Nullable
    private String artist;

    /**
     * Genre that best describes the content.
     */
    @Nullable
    private String genre;

    /**
     * Album to which the content belongs; typically for audio.
     */
    @Nullable
    private String album;

    /**
     * International Standard Recording Code conforming to ISO- 3901.
     */
    @Nullable
    private String isrc;

    /**
     * URL of the content, for buy-side contextualization or review.
     */
    @Nullable
    private String url;

    /**
     * Array of IAB content categories that describe the content producer.
     */
    @Nullable
    private ArrayList<String> categories = new ArrayList<>();

    /**
     * Production quality.
     */
    @Nullable
    private Integer productionQuality;

    /**
     * Type of content (game, video, text, etc.).
     */
    @Nullable
    private Integer context;

    /**
     * Content rating (e.g., MPAA).
     */
    @Nullable
    private String contentRating;

    /**
     * User rating of the content (e.g., number of stars, likes, etc.).
     */
    @Nullable
    private String userRating;

    /**
     * Media rating per IQG guidelines.
     */
    @Nullable
    private Integer qaMediaRating;

    /**
     * Comma separated list of keywords describing the content.
     */
    @Nullable
    private String keywords;

    /**
     * Live stream. 0 = not live, 1 = content is live (e.g., stream, live blog).
     */
    @Nullable
    private Integer liveStream;

    /**
     * Source relationship. 0 = indirect, 1 = direct.
     */
    @Nullable
    private Integer sourceRelationship;

    /**
     * Length of content in seconds; appropriate for video or audio.
     */
    @Nullable
    private Integer length;

    /**
     * Content language using ISO-639-1-alpha-2.
     */
    @Nullable
    private String language;

    /**
     * Indicator of whether or not the content is embeddable (e.g., an embeddable video player), where 0 = no, 1 = yes.
     */
    @Nullable
    private Integer embeddable;

    /**
     * Additional content data.
     */
    @Nullable
    private ArrayList<ContentDataObject> contentDataObjects = new ArrayList<>();

    /**
     * This object defines the producer of the content in which the ad will be shown.
     */
    @Nullable
    private ContentProducerObject contentProducerObject;

    /**
     * @return JSONObject if at least one parameter was set; otherwise null.
     */
    public JSONObject getJsonObject() {
        JSONObject result = new JSONObject();

        try {
            result.putOpt("id", id);
            result.putOpt("episode", episode);
            result.putOpt("title", title);
            result.putOpt("series", series);
            result.putOpt("season", season);
            result.putOpt("artist", artist);
            result.putOpt("genre", genre);
            result.putOpt("album", album);
            result.putOpt("isrc", isrc);
            result.putOpt("url", url);
            result.putOpt("prodq", productionQuality);
            result.putOpt("context", context);
            result.putOpt("contentrating", contentRating);
            result.putOpt("userrating", userRating);
            result.putOpt("qagmediarating", qaMediaRating);
            result.putOpt("keywords", keywords);
            result.putOpt("livestream", liveStream);
            result.putOpt("sourcerelationship", sourceRelationship);
            result.putOpt("len", length);
            result.putOpt("language", language);
            result.putOpt("embeddable", embeddable);

            if (contentProducerObject != null) {
                result.putOpt("producer", contentProducerObject.getJsonObject());
            }

            if (categories != null && !categories.isEmpty()) {
                JSONArray jsonCategories = new JSONArray();
                for (String category : categories) {
                    jsonCategories.put(category);
                }
                result.putOpt("cat", jsonCategories);
            }

            if (contentDataObjects != null && !contentDataObjects.isEmpty()) {
                JSONArray dataJson = new JSONArray();
                for (ContentDataObject dataObject : contentDataObjects) {
                    dataJson.put(dataObject.getJsonObject());
                }
                result.put("data", dataJson);
            }

            if (result.length() == 0) {
                return null;
            }
        } catch (JSONException exception) {
            LogUtil.e("ContentObject", "Can't create json result object.");
        }

        return result;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public Integer getEpisode() {
        return episode;
    }

    public void setEpisode(@Nullable Integer episode) {
        this.episode = episode;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getSeries() {
        return series;
    }

    public void setSeries(@Nullable String series) {
        this.series = series;
    }

    @Nullable
    public String getSeason() {
        return season;
    }

    public void setSeason(@Nullable String season) {
        this.season = season;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    public void setArtist(@Nullable String artist) {
        this.artist = artist;
    }

    @Nullable
    public String getGenre() {
        return genre;
    }

    public void setGenre(@Nullable String genre) {
        this.genre = genre;
    }

    @Nullable
    public String getAlbum() {
        return album;
    }

    public void setAlbum(@Nullable String album) {
        this.album = album;
    }

    @Nullable
    public String getIsrc() {
        return isrc;
    }

    public void setIsrc(@Nullable String isrc) {
        this.isrc = isrc;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
    }

    public void addCategory(@Nullable String category) {
        if (categories != null && category != null) {
            categories.add(category);
        }
    }

    @Nullable
    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(@Nullable ArrayList<String> categories) {
        this.categories = categories;
    }

    @Nullable
    public Integer getProductionQuality() {
        return productionQuality;
    }

    public void setProductionQuality(@Nullable Integer productionQuality) {
        this.productionQuality = productionQuality;
    }

    @Nullable
    public Integer getContext() {
        return context;
    }

    public void setContext(@Nullable Integer context) {
        this.context = context;
    }

    @Nullable
    public String getContentRating() {
        return contentRating;
    }

    public void setContentRating(@Nullable String contentRating) {
        this.contentRating = contentRating;
    }

    @Nullable
    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(@Nullable String userRating) {
        this.userRating = userRating;
    }

    @Nullable
    public Integer getQaMediaRating() {
        return qaMediaRating;
    }

    public void setQaMediaRating(@Nullable Integer qaMediaRating) {
        this.qaMediaRating = qaMediaRating;
    }

    @Nullable
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(@Nullable String keywords) {
        this.keywords = keywords;
    }

    @Nullable
    public Integer getLiveStream() {
        return liveStream;
    }

    public void setLiveStream(@Nullable Integer liveStream) {
        this.liveStream = liveStream;
    }

    @Nullable
    public Integer getSourceRelationship() {
        return sourceRelationship;
    }

    public void setSourceRelationship(@Nullable Integer sourceRelationship) {
        this.sourceRelationship = sourceRelationship;
    }

    @Nullable
    public Integer getLength() {
        return length;
    }

    public void setLength(@Nullable Integer length) {
        this.length = length;
    }

    @Nullable
    public String getLanguage() {
        return language;
    }

    public void setLanguage(@Nullable String language) {
        this.language = language;
    }

    @Nullable
    public Integer getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(@Nullable Integer embeddable) {
        this.embeddable = embeddable;
    }

    public void addContentDataObject(@Nullable ContentDataObject contentDataObject) {
        if (contentDataObjects != null && contentDataObject != null) {
            contentDataObjects.add(contentDataObject);
        }
    }

    @Nullable
    public ArrayList<ContentDataObject> getContentDataObjects() {
        return contentDataObjects;
    }

    public void setContentDataObjects(@Nullable ArrayList<ContentDataObject> contentDataObjects) {
        this.contentDataObjects = contentDataObjects;
    }

    @Nullable
    public ContentProducerObject getContentProducerObject() {
        return contentProducerObject;
    }

    public void setContentProducerObject(@Nullable ContentProducerObject contentProducerObject) {
        this.contentProducerObject = contentProducerObject;
    }

    public static class ContentDataObject {

        /**
         * Exchange-specific ID for the data provider.
         */
        @Nullable
        private String id;

        /**
         * Exchange-specific name for the data provider.
         */
        @Nullable
        private String name;

        /**
         * Segment objects are essentially key-value pairs that convey specific units of data.
         */
        @Nullable
        private ArrayList<ContentSegmentObject> segments = new ArrayList<>();

        public JSONObject getJsonObject() {
            JSONObject result = new JSONObject();

            try {
                result.putOpt("id", id);
                result.putOpt("name", name);

                if (segments != null && !segments.isEmpty()) {
                    JSONArray segmentsJson = new JSONArray();
                    for (ContentSegmentObject segment : segments) {
                        segmentsJson.put(segment.getJsonObject());
                    }
                    result.put("segment", segmentsJson);
                }
            } catch (JSONException exception) {
                LogUtil.e("ContentObject", "Can't create json data content object.");
            }

            return result;
        }

        @Nullable
        public String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        @Nullable
        public String getName() {
            return name;
        }

        public void setName(@Nullable String name) {
            this.name = name;
        }

        public void addSegment(ContentSegmentObject contentSegmentObject) {
            if (segments != null) {
                segments.add(contentSegmentObject);
            }
        }

        @Nullable
        public ArrayList<ContentSegmentObject> getSegments() {
            return segments;
        }

        public void setSegments(@Nullable ArrayList<ContentSegmentObject> segments) {
            this.segments = segments;
        }
    }

    public static class ContentProducerObject {

        /**
         * Content producer or originator ID.
         */
        @Nullable
        private String id;

        /**
         * Content producer or originator name (e.g., “Warner Bros”).
         */
        @Nullable
        private String name;

        /**
         * Array of IAB content categories that describe the content producer.
         */
        @Nullable
        private List<String> categories;

        /**
         * Highest level domain of the content producer (e.g., “producer.com”).
         */
        @Nullable
        private String domain;

        @Nullable
        public JSONObject getJsonObject() {
            JSONObject result = new JSONObject();

            try {
                result.putOpt("id", id);
                result.putOpt("name", name);
                result.putOpt("domain", domain);

                if (categories != null && !categories.isEmpty()) {
                    JSONArray categoriesJson = new JSONArray();
                    for (String category : categories) {
                        categoriesJson.put(category);
                    }
                    result.put("cat", categoriesJson);
                }
            } catch (JSONException exception) {
                LogUtil.e("ContentObject", "Can't create json producer content object.");
            }

            return result;
        }


        @Nullable
        public String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        @Nullable
        public String getName() {
            return name;
        }

        public void setName(@Nullable String name) {
            this.name = name;
        }

        public void addCategory(@Nullable String category) {
            if (category != null && categories != null) {
                categories.add(category);
            }
        }

        @Nullable
        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(@Nullable List<String> categories) {
            this.categories = categories;
        }

        @Nullable
        public String getDomain() {
            return domain;
        }

        public void setDomain(@Nullable String domain) {
            this.domain = domain;
        }

    }

    public static class ContentSegmentObject {

        /**
         * ID of the data segment specific to the data provider.
         */
        @Nullable
        private String id;

        /**
         * Name of the data segment specific to the data provider.
         */
        @Nullable
        private String name;

        /**
         * String representation of the data segment value.
         */
        @Nullable
        private String value;

        @Nullable
        public JSONObject getJsonObject() {
            JSONObject result = new JSONObject();

            try {
                result.putOpt("id", id);
                result.putOpt("name", name);
                result.putOpt("value", value);
            } catch (JSONException exception) {
                LogUtil.e("ContentObject", "Can't create json segment content object.");
            }

            return result;
        }

        @Nullable
        public String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        @Nullable
        public String getName() {
            return name;
        }

        public void setName(@Nullable String name) {
            this.name = name;
        }

        @Nullable
        public String getValue() {
            return value;
        }

        public void setValue(@Nullable String value) {
            this.value = value;
        }
    }

}
