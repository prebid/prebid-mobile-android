/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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