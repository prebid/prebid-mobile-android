package com.applovin.mediation.adapters;

import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.Util;

import static org.junit.Assert.assertEquals;

public class PrebidMaxMediationAdapterTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void compareAdapterResponseKeyWithUtilsKey() {
        assertEquals(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, Util.APPLOVIN_MAX_RESPONSE_ID_KEY);
    }

    @Test
    public void compareAdapterKeywordsKeyWithUtilsKey() {
        assertEquals(PrebidMaxMediationAdapter.EXTRA_KEYWORDS_ID, Util.APPLOVIN_MAX_KEYWORDS_KEY);
    }

}