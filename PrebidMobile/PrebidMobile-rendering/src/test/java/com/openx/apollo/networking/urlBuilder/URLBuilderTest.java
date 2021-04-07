package com.openx.apollo.networking.urlBuilder;

import android.app.Activity;
import android.content.Context;

import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.networking.parameters.AdRequestInput;
import com.openx.apollo.networking.parameters.ParameterBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class URLBuilderTest {

    private AdConfiguration mMockConfig;
    private Context mContext;
    private final boolean mBrowserActivityAvailable = true;

    @Before
    public void setUp() throws Exception {
        mMockConfig = mock(AdConfiguration.class);
        mContext = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void testBuildURLObject() {
        URLPathBuilder mockPathBuilder = mock(URLPathBuilder.class);
        ArrayList<ParameterBuilder> parameterBuilders = new ArrayList<>();
        URLBuilder urlBuilder = new URLBuilder(mockPathBuilder, parameterBuilders, new AdRequestInput());
        assertNotNull(urlBuilder);
    }

    @Test
    public void testBuildParametersNull() throws Exception {
        AdRequestInput adRequestInput = URLBuilder.buildParameters(null, null);
        assertEquals("{}", adRequestInput.getBidRequest().getJsonObject().toString());
    }
}