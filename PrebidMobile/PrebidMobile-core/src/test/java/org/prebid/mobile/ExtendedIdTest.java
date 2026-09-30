package org.prebid.mobile;

import org.prebid.mobile.api.eid.ExtendedId;

import static org.assertj.core.api.Assertions.assertThat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class ExtendedIdTest {

    @Test
    public void wrap_validJson_backedByThatJson() throws Exception {
        JSONObject json = new JSONObject()
                .put("source", "id5-sync.com")
                .put("uids", new JSONArray().put(new JSONObject().put("id", "abc").put("atype", 1)));

        ExtendedId eid = ExtendedId.wrap(json);

        assertThat(eid.getSource()).isEqualTo("id5-sync.com");
        assertThat(eid.getJson()).isSameAs(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrap_missingSource_throws() {
        ExtendedId.wrap(new JSONObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrap_emptySource_throws() throws Exception {
        ExtendedId.wrap(new JSONObject().put("source", ""));
    }
}
