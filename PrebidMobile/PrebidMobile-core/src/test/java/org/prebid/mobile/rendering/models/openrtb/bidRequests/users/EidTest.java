package org.prebid.mobile.rendering.models.openrtb.bidRequests.users;

import static org.assertj.core.api.Assertions.assertThat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public class EidTest {

    @Test
    public void getJsonObjectWithAllFields() throws Exception {
        // given
        Eid eid = new Eid();
        eid.source = "id5-sync.com";
        eid.inserter = "prebid.org";
        eid.matcher = "id5-sync.com";
        eid.mm = 1;

        Uid uid1 = new Uid();
        uid1.id = "abc123";
        uid1.atype = 3;

        Uid uid2 = new Uid();
        uid2.id = "def456";
        uid2.atype = 1;

        eid.uids.add(uid1);
        eid.uids.add(uid2);

        Ext ext = new Ext();
        ext.put("rtiPartner", "TDID");
        eid.ext = ext;

        // when
        JSONObject json = eid.getJsonObject();

        // then
        assertThat(json.getString("source")).isEqualTo("id5-sync.com");
        assertThat(json.getString("inserter")).isEqualTo("prebid.org");
        assertThat(json.getString("matcher")).isEqualTo("id5-sync.com");
        assertThat(json.getInt("mm")).isEqualTo(1);

        JSONArray uids = json.getJSONArray("uids");
        assertThat(uids).isNotNull();
        assertThat(uids.length()).isEqualTo(2);

        JSONObject firstUid = uids.getJSONObject(0);
        assertThat(firstUid.getString("id")).isEqualTo("abc123");

        JSONObject secondUid = uids.getJSONObject(1);
        assertThat(secondUid.getString("id")).isEqualTo("def456");

        JSONObject jsonExt = json.getJSONObject("ext");
        assertThat(jsonExt).isNotNull();
        assertThat(jsonExt.getString("rtiPartner")).isEqualTo("TDID");
    }

    @Test
    public void getJsonObjectWithMandatoryFieldsOnly() throws Exception {
        // given
        Eid eid = new Eid();
        eid.source = "uid-provider";

        Uid uid = new Uid();
        uid.id = "user1";
        uid.atype = 1;
        eid.uids.add(uid);

        // when
        JSONObject json = eid.getJsonObject();

        // then
        assertThat(json.getString("source")).isEqualTo("uid-provider");

        JSONArray uids = json.getJSONArray("uids");
        assertThat(uids).isNotNull();
        assertThat(uids.length()).isEqualTo(1);

        JSONObject firstUid = uids.getJSONObject(0);
        assertThat(firstUid.getString("id")).isEqualTo("user1");

        assertThat(json.has("inserter")).isFalse();
        assertThat(json.has("matcher")).isFalse();
        assertThat(json.has("mm")).isFalse();
        assertThat(json.has("ext")).isFalse();
    }

}
