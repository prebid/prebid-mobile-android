package org.prebid.mobile.rendering.models.openrtb.bidRequests.users;

import static org.assertj.core.api.Assertions.assertThat;

import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public class UidTest {

    @Test
    public void getJsonObjectWithAllFields() throws Exception {
        // given
        Uid uid = new Uid();
        uid.id = "abc123";
        uid.atype = 3;

        Ext ext = new Ext();
        ext.put("stype", "ppuid");
        uid.ext = ext;

        // when
        JSONObject json = uid.getJsonObject();

        // then
        assertThat(json.getString("id")).isEqualTo("abc123");
        assertThat(json.getInt("atype")).isEqualTo(3);
        assertThat(json.getJSONObject("ext").getString("stype")).isEqualTo("ppuid");
    }

    @Test
    public void getJsonObjectWithMandatoryFieldsOnly() throws Exception {
        // given
        Uid uid = new Uid();
        uid.id = "user1";
        uid.atype = 1;

        // when
        JSONObject json = uid.getJsonObject();

        // then
        assertThat(json.getString("id")).isEqualTo("user1");
        assertThat(json.getInt("atype")).isEqualTo(1);
        assertThat(json.has("ext")).isFalse();
    }

}
