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

package org.prebid.mobile.rendering.utils.helpers;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class InsetsUtilsTest {

    @Test
    public void applyInsetsRepeatedly_DoesNotAccumulateMargins() {
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.topMargin = 11;
        params.rightMargin = 13;
        view.setLayoutParams(params);

        CustomInsets none = new CustomInsets(0, 0, 0, 0);
        CustomInsets portrait = new CustomInsets(10, 20, 30, 40);
        InsetsUtils.applyInsetsToMargins(view, none, portrait);
        InsetsUtils.applyInsetsToMargins(view, portrait, portrait);

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(21, result.topMargin);
        assertEquals(33, result.rightMargin);
        assertEquals(0, result.bottomMargin);
        assertEquals(0, result.leftMargin);
    }

    @Test
    public void applyInsetsAfterRotation_ReplacesPreviousInsets() {
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.topMargin = 21;
        params.rightMargin = 33;
        view.setLayoutParams(params);

        CustomInsets portrait = new CustomInsets(10, 20, 30, 40);
        CustomInsets landscape = new CustomInsets(4, 8, 12, 16);
        InsetsUtils.applyInsetsToMargins(view, portrait, landscape);

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(15, result.topMargin);
        assertEquals(21, result.rightMargin);
    }

    @Test
    public void applyInsetsWithUnsupportedParams_ReturnsFalse() {
        View view = new View(RuntimeEnvironment.getApplication());
        view.setLayoutParams(new android.view.ViewGroup.LayoutParams(100, 100));

        boolean applied = InsetsUtils.applyInsetsToMargins(
            view,
            new CustomInsets(0, 0, 0, 0),
            new CustomInsets(10, 20, 30, 40)
        );

        assertFalse(applied);
    }

    @Test
    public void applyInsetsWithEndGravityInRtl_UsesLeftInset() {
        View view = mock(View.class);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.END;
        when(view.getLayoutParams()).thenReturn(params);
        when(view.isLayoutDirectionResolved()).thenReturn(true);
        when(view.getLayoutDirection()).thenReturn(View.LAYOUT_DIRECTION_RTL);

        InsetsUtils.applyInsetsToMargins(
            view,
            new CustomInsets(0, 0, 0, 0),
            new CustomInsets(10, 20, 30, 40)
        );

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(40, result.leftMargin);
        assertEquals(0, result.rightMargin);
    }
}
