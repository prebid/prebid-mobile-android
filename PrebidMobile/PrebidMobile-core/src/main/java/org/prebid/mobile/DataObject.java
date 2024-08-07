package org.prebid.mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

import java.util.ArrayList;

/**
 * Data content object for additional targeting.
 */
public class DataObject {

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
    @NonNull
    private ArrayList<SegmentObject> segments = new ArrayList<>();

    @Nullable
    private Ext ext;

    public JSONObject getJsonObject() {
        JSONObject result = new JSONObject();

        try {
            result.putOpt("id", id);
            result.putOpt("name", name);

            if (!segments.isEmpty()) {
                JSONArray segmentsJson = new JSONArray();
                for (SegmentObject segment : segments) {
                    segmentsJson.put(segment.getJsonObject());
                }
                result.put("segment", segmentsJson);
            }

            if (ext != null) {
                result.putOpt("ext", ext.getJsonObject());
            }
        } catch (JSONException exception) {
            LogUtil.error("DataObject", "Can't create json data content object.");
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

    public void addSegment(@NonNull SegmentObject segmentObject) {
        segments.add(segmentObject);
    }

    @NonNull
    public ArrayList<SegmentObject> getSegments() {
        return segments;
    }

    public void setSegments(@NonNull ArrayList<SegmentObject> segments) {
        this.segments = segments;
    }

    public void setExt(Ext ext) {
        this.ext = ext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataObject that = (DataObject) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return segments.equals(that.segments);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + segments.hashCode();
        return result;
    }

    /**
     * Segment object.
     */
    public static class SegmentObject {

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
                LogUtil.error("SegmentObject", "Can't create json segment object.");
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SegmentObject that = (SegmentObject) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            return value != null ? value.equals(that.value) : that.value == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

}