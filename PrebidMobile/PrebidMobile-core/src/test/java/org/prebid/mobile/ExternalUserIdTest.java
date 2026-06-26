package org.prebid.mobile;

import static org.assertj.core.api.Assertions.assertThat;

import static java.util.Collections.singletonList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.users.Eid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExternalUserIdTest {

    @Test
    public void getJson_allFieldsSet_fullJsonReturned() throws Exception {
        // given
        ExternalUserId.UniqueId uid1 = new ExternalUserId.UniqueId("abc123", 3);
        Map<String, Object> uidExt = new HashMap<>();
        uidExt.put("stype", "ppuid");
        uid1.setExt(uidExt);

        ExternalUserId.UniqueId uid2 = new ExternalUserId.UniqueId("def456", 1);

        ExternalUserId userId = new ExternalUserId("id5-sync.com", Arrays.asList(uid1, uid2));
        userId.setInserter("prebid.org");
        userId.setMatcher("id5-sync.com");
        userId.setMm(1);

        Map<String, Object> ext = new HashMap<>();
        ext.put("rtiPartner", "TDID");
        userId.setExt(ext);

        // when
        JSONObject json = userId.getJson();

        // then
        assertThat(json).isNotNull();
        assertThat(json.getString("source")).isEqualTo("id5-sync.com");
        assertThat(json.getString("inserter")).isEqualTo("prebid.org");
        assertThat(json.getString("matcher")).isEqualTo("id5-sync.com");
        assertThat(json.getInt("mm")).isEqualTo(1);

        JSONObject jsonExt = json.getJSONObject("ext");
        assertThat(jsonExt).isNotNull();
        assertThat(jsonExt.getString("rtiPartner")).isEqualTo("TDID");

        JSONArray uids = json.getJSONArray("uids");
        assertThat(uids).isNotNull();
        assertThat(uids.length()).isEqualTo(2);

        JSONObject firstUid = uids.getJSONObject(0);
        assertThat(firstUid.getString("id")).isEqualTo("abc123");
        assertThat(firstUid.getInt("atype")).isEqualTo(3);

        JSONObject firstUidExt = firstUid.getJSONObject("ext");
        assertThat(firstUidExt).isNotNull();
        assertThat(firstUidExt.getString("stype")).isEqualTo("ppuid");

        JSONObject secondUid = uids.getJSONObject(1);
        assertThat(secondUid.getString("id")).isEqualTo("def456");
    }

    @Test
    public void getJson_mandatoryFieldsOnly_optionalFieldsAbsent() throws Exception {
        // given
        ExternalUserId.UniqueId uid = new ExternalUserId.UniqueId("user1", 1);
        ExternalUserId userId = new ExternalUserId("uid-provider", singletonList(uid));

        // when
        JSONObject json = userId.getJson();

        // then
        assertThat(json).isNotNull();
        assertThat(json.getString("source")).isEqualTo("uid-provider");

        JSONArray uids = json.getJSONArray("uids");
        assertThat(uids).isNotNull();
        assertThat(uids.length()).isEqualTo(1);

        JSONObject firstUid = uids.getJSONObject(0);
        assertThat(firstUid.getString("id")).isEqualTo("user1");
        assertThat(firstUid.getInt("atype")).isEqualTo(1);

        assertThat(json.has("inserter")).isFalse();
        assertThat(json.has("matcher")).isFalse();
        assertThat(json.has("mm")).isFalse();
        assertThat(json.has("ext")).isFalse();
    }

    @Test
    public void toEid_allFieldsSet_allFieldsMapped() throws Exception {
        // given
        ExternalUserId.UniqueId uid1 = new ExternalUserId.UniqueId("abc123", 3);
        Map<String, Object> uidExt = new HashMap<>();
        uidExt.put("stype", "ppuid");
        uid1.setExt(uidExt);

        ExternalUserId.UniqueId uid2 = new ExternalUserId.UniqueId("def456", 1);

        ExternalUserId userId = new ExternalUserId("id5-sync.com", Arrays.asList(uid1, uid2));
        userId.setInserter("prebid.org");
        userId.setMatcher("id5-sync.com");
        userId.setMm(1);

        Map<String, Object> ext = new HashMap<>();
        ext.put("rtiPartner", "TDID");
        userId.setExt(ext);

        // when
        Eid eid = userId.toEid();

        // then
        assertThat(eid).isNotNull();
        assertThat(eid.source).isEqualTo("id5-sync.com");
        assertThat(eid.inserter).isEqualTo("prebid.org");
        assertThat(eid.matcher).isEqualTo("id5-sync.com");
        assertThat(eid.mm).isEqualTo(1);

        assertThat(eid.ext).isNotNull();
        assertThat(eid.ext.getJsonObject().getString("rtiPartner")).isEqualTo("TDID");

        assertThat(eid.uids).isNotNull().hasSize(2);

        assertThat(eid.uids).first().satisfies(uid -> {
            assertThat(uid.id).isEqualTo("abc123");
            assertThat(uid.atype).isEqualTo(3);
            assertThat(uid.ext).isNotNull();
            assertThat(uid.ext.getJsonObject().getString("stype")).isEqualTo("ppuid");
        });

        assertThat(eid.uids).element(1).satisfies(uid -> {
            assertThat(uid.id).isEqualTo("def456");
            assertThat(uid.atype).isEqualTo(1);
        });
    }

    @Test
    public void toEid_mandatoryFieldsOnly_optionalFieldsNull() {
        // given
        ExternalUserId userId = new ExternalUserId(
                "uid-provider",
                singletonList(new ExternalUserId.UniqueId("user1", 1))
        );

        // when
        Eid eid = userId.toEid();

        // then
        assertThat(eid).isNotNull();
        assertThat(eid.source).isEqualTo("uid-provider");
        assertThat(eid.inserter).isNull();
        assertThat(eid.matcher).isNull();
        assertThat(eid.mm).isNull();
        assertThat(eid.ext).isNull();

        assertThat(eid.uids).isNotNull().hasSize(1);

        assertThat(eid.uids).first().satisfies(uid -> {
            assertThat(uid.id).isEqualTo("user1");
            assertThat(uid.atype).isEqualTo(1);
            assertThat(uid.ext).isNull();
        });
    }

}
