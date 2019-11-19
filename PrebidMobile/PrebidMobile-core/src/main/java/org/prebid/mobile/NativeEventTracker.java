package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NativeEventTracker {
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

        public void setID(int id) throws Exception {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            } else {
                throw new Exception("Invalid input, should only set value on CUSTOM, should only use 500 above.");
            }
        }
    }

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

        public void setID(int id) throws Exception {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            } else {
                throw new Exception("Invalid input, should only set value on CUSTOM, should only use 500 above.");
            }
        }
    }

    EVENT_TYPE event;
    ArrayList<EVENT_TRACKING_METHOD> methods;
    Object extObject;

    public NativeEventTracker(EVENT_TYPE event, ArrayList<EVENT_TRACKING_METHOD> methods) throws Exception {
        this.event = event;
        if (methods == null || methods.isEmpty()) {
            throw new Exception("Methods are required");
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
