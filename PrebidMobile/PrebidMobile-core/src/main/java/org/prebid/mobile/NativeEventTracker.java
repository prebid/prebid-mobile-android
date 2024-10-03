package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Native event tracker for requesting ad.
 */
public class NativeEventTracker {

    /**
     * Event type.
     */
    public enum EVENT_TYPE {
        IMPRESSION(1),
        VIEWABLE_MRC50(2),
        VIEWABLE_MRC100(3),
        VIEWABLE_VIDEO50(4),
        CUSTOM(500);
        private int id;

        EVENT_TYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && !inExistingValue(id)) {
                this.id = id;
            }
        }

        private boolean inExistingValue(int id) {
            EVENT_TYPE[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (EVENT_TYPE value : possibleValues) {
                if (!value.equals(EVENT_TYPE.CUSTOM) && value.getID() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Event tracking method.
     */
    public enum EVENT_TRACKING_METHOD {
        IMAGE(1),
        JS(2),
        CUSTOM(500);
        private int id;

        EVENT_TRACKING_METHOD(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && !inExistingValue(id)) {
                this.id = id;
            }
        }

        private boolean inExistingValue(int id) {
            EVENT_TRACKING_METHOD[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (EVENT_TRACKING_METHOD value : possibleValues) {
                if (!value.equals(EVENT_TRACKING_METHOD.CUSTOM) && value.getID() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    EVENT_TYPE event;
    ArrayList<EVENT_TRACKING_METHOD> methods;
    Object extObject;

    public NativeEventTracker(EVENT_TYPE event, ArrayList<EVENT_TRACKING_METHOD> methods) {
        this.event = event;
        if (methods == null || methods.isEmpty()) {
            throw new NullPointerException("Methods are required");
        }
        this.methods = methods;
    }

    public void setExt(Object extObject) {
        if (extObject instanceof JSONObject || extObject instanceof JSONArray) {
            this.extObject = extObject;
        }
    }

    public EVENT_TYPE getEvent() {
        return event;
    }

    public ArrayList<EVENT_TRACKING_METHOD> getMethods() {
        return methods;
    }

    public Object getExtObject() {
        return extObject;
    }
}
