package org.prebid.mobile.rendering.mraid.methods;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidScreenMetricsTest {

    private MraidScreenMetrics mMraidScreenMetrics;

    @Before
    public void setUp() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mMraidScreenMetrics = new MraidScreenMetrics(context, 1f);
    }

    @Test
    public void getDensityTest() {
        assertEquals(1f, mMraidScreenMetrics.getDensity(), 0);
    }

    @Test
    public void setScreenSizeTest() {
        Rect expected = new Rect(0, 0, 100, 200);
        assertNotEquals(expected, mMraidScreenMetrics.getScreenRect());

        mMraidScreenMetrics.setScreenSize(100, 200);
        assertEquals(expected, mMraidScreenMetrics.getScreenRect());
    }

    @Test
    public void getScreenRectTest() {
        assertNotNull(mMraidScreenMetrics.getScreenRect());
    }

    @Test
    public void getScreenRectDipsTest() {
        assertNotNull(mMraidScreenMetrics.getScreenRectDips());
    }

    @Test
    public void setRootViewPositionTest() {
        Rect expected = new Rect(1, 2, 101, 202);
        assertNotEquals(expected, mMraidScreenMetrics.getRootViewRect());

        mMraidScreenMetrics.setRootViewPosition(1, 2, 100, 200);
        assertEquals(expected, mMraidScreenMetrics.getRootViewRect());
    }

    @Test
    public void getRootViewRectTest() {
        assertNotNull(mMraidScreenMetrics.getRootViewRect());
    }

    @Test
    public void getRootViewRectDipsTest() {
        assertNotNull(mMraidScreenMetrics.getRootViewRectDips());
    }

    @Test
    public void setCurrentAdPositionTest() {
        Rect expected = new Rect(1, 2, 101, 202);
        assertNotEquals(expected, mMraidScreenMetrics.getCurrentAdRect());

        mMraidScreenMetrics.setCurrentAdPosition(1, 2, 100, 200);
        assertEquals(expected, mMraidScreenMetrics.getCurrentAdRect());
    }

    @Test
    public void getCurrentAdRectTest() {
        assertNotNull(mMraidScreenMetrics.getCurrentAdRect());
    }

    @Test
    public void getCurrentAdRectDipsTest() {
        assertNotNull(mMraidScreenMetrics.getCurrentAdRectDips());
    }

    @Test
    public void setDefaultAdPositionTest() {
        Rect expected = new Rect(1, 2, 101, 202);
        assertNotEquals(expected, mMraidScreenMetrics.getDefaultAdRect());

        mMraidScreenMetrics.setDefaultAdPosition(1, 2, 100, 200);
        assertEquals(expected, mMraidScreenMetrics.getDefaultAdRect());
    }

    @Test
    public void getDefaultAdRectTest() {
        assertNotNull(mMraidScreenMetrics.getDefaultAdRect());
    }

    @Test
    public void getDefaultAdRectDipsTest() {
        assertNotNull(mMraidScreenMetrics.getDefaultAdRectDips());
    }
}