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
import android.widget.RelativeLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class InsetsUtilsTest {

    private static final CustomInsets NONE = new CustomInsets(0, 0, 0, 0);
    private static final CustomInsets PORTRAIT = new CustomInsets(10, 20, 30, 40);
    private static final CustomInsets LANDSCAPE = new CustomInsets(4, 8, 12, 16);

    @Test
    public void applyInsetsToMargins_FrameLayoutTopRight_AddsInsetsOnTopOfBaseMargins() {
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        view.setLayoutParams(params);

        CustomInsets base = new CustomInsets(11, 13, 0, 0);
        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(21, result.topMargin);
        assertEquals(33, result.rightMargin);
        assertEquals(0, result.bottomMargin);
        assertEquals(0, result.leftMargin);
    }

    @Test
    public void applyInsetsToMargins_CalledRepeatedlyWithSameInsets_DoesNotAccumulate() {
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        view.setLayoutParams(params);

        CustomInsets base = new CustomInsets(11, 13, 0, 0);
        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);
        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);
        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(21, result.topMargin);
        assertEquals(33, result.rightMargin);
    }

    @Test
    public void applyInsetsToMargins_InsetsChangeBetweenCalls_ReplacesPreviousInsetsInsteadOfAdding() {
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        view.setLayoutParams(params);

        CustomInsets base = new CustomInsets(0, 0, 0, 0);
        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);
        InsetsUtils.applyInsetsToMargins(view, base, LANDSCAPE);

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(4, result.topMargin);
        assertEquals(8, result.rightMargin);
    }

    @Test
    public void applyInsetsToMargins_GravityChangesBetweenCalls_OldSideResetsToBaseMargin() {
        // Regression: previously, once an inset was added to a side, changing which side the
        // gravity resolves to (RTL flip, position change, re-added view with different params)
        // could leave the old side stuck with a stale inset instead of its original margin.
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        view.setLayoutParams(params);

        CustomInsets base = new CustomInsets(0, 0, 0, 0);
        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);

        FrameLayout.LayoutParams afterFirstCall = (FrameLayout.LayoutParams) view.getLayoutParams();
        afterFirstCall.gravity = Gravity.TOP | Gravity.LEFT;
        view.setLayoutParams(afterFirstCall);

        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(0, result.rightMargin);
        assertEquals(40, result.leftMargin);
    }

    @Test
    public void applyInsetsToMargins_WithUnsupportedParams_ReturnsFalse() {
        View view = new View(RuntimeEnvironment.getApplication());
        view.setLayoutParams(new android.view.ViewGroup.LayoutParams(100, 100));

        boolean applied = InsetsUtils.applyInsetsToMargins(view, NONE, PORTRAIT);

        assertFalse(applied);
    }

    @Test
    public void applyInsetsToMargins_FrameLayoutEndGravityInRtl_UsesLeftInset() {
        // A real, unattached View never resolves an RTL layout direction in Robolectric, so the
        // resolved direction is mocked directly (this is what ViewCompat.getLayoutDirection()
        // reads on API 17+). The assertion is tied to the captured setLayoutParams() argument,
        // not to re-reading the stubbed getLayoutParams(), so it can't pass for the wrong reason.
        View view = mock(View.class);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.END;
        when(view.getLayoutParams()).thenReturn(params);
        when(view.getLayoutDirection()).thenReturn(View.LAYOUT_DIRECTION_RTL);

        InsetsUtils.applyInsetsToMargins(view, NONE, PORTRAIT);

        ArgumentCaptor<FrameLayout.LayoutParams> captor = ArgumentCaptor.forClass(FrameLayout.LayoutParams.class);
        verify(view).setLayoutParams(captor.capture());
        assertEquals(40, captor.getValue().leftMargin);
        assertEquals(0, captor.getValue().rightMargin);
    }

    @Test
    public void applyInsetsToMargins_RelativeLayoutBottomLeft_AddsInsetsOnTopOfBaseMargins() {
        View view = new View(RuntimeEnvironment.getApplication());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        view.setLayoutParams(params);

        CustomInsets base = new CustomInsets(0, 0, 25, 25);
        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);

        RelativeLayout.LayoutParams result = (RelativeLayout.LayoutParams) view.getLayoutParams();
        assertEquals(55, result.bottomMargin);
        assertEquals(65, result.leftMargin);
        assertEquals(0, result.topMargin);
        assertEquals(0, result.rightMargin);
    }

    @Test
    public void applyInsetsToMargins_RelativeLayoutEndRuleInRtl_UsesLeftInset() {
        // Regression: ALIGN_PARENT_END must resolve to the left margin under RTL, not the
        // right margin (which is what ALIGN_PARENT_END means in LTR). See the note on the
        // FrameLayout RTL test above for why the resolved direction is mocked here.
        View view = mock(View.class);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        when(view.getLayoutParams()).thenReturn(params);
        when(view.getLayoutDirection()).thenReturn(View.LAYOUT_DIRECTION_RTL);

        InsetsUtils.applyInsetsToMargins(view, NONE, PORTRAIT);

        ArgumentCaptor<RelativeLayout.LayoutParams> captor = ArgumentCaptor.forClass(RelativeLayout.LayoutParams.class);
        verify(view).setLayoutParams(captor.capture());
        assertEquals(40, captor.getValue().leftMargin);
        assertEquals(0, captor.getValue().rightMargin);
    }

    @Test
    public void applyInsetsToMargins_RelativeLayoutStartRuleInRtl_UsesRightInset() {
        View view = mock(View.class);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        when(view.getLayoutParams()).thenReturn(params);
        when(view.getLayoutDirection()).thenReturn(View.LAYOUT_DIRECTION_RTL);

        InsetsUtils.applyInsetsToMargins(view, NONE, PORTRAIT);

        ArgumentCaptor<RelativeLayout.LayoutParams> captor = ArgumentCaptor.forClass(RelativeLayout.LayoutParams.class);
        verify(view).setLayoutParams(captor.capture());
        assertEquals(20, captor.getValue().rightMargin);
        assertEquals(0, captor.getValue().leftMargin);
    }

    @Test
    public void applyInsetsToMargins_SoundViewBottomMarginSurvivesTwoRotations() {
        // Utils.createSoundView() gives the sound button a 150px base bottom margin before
        // insets are ever applied. That base must survive repeated rotations unchanged.
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.bottomMargin = 150;
        view.setLayoutParams(params);

        CustomInsets base = new CustomInsets(0, 0, 150, 0);
        InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);
        assertEquals(180, ((FrameLayout.LayoutParams) view.getLayoutParams()).bottomMargin);

        InsetsUtils.applyInsetsToMargins(view, base, LANDSCAPE);
        assertEquals(162, ((FrameLayout.LayoutParams) view.getLayoutParams()).bottomMargin);

        InsetsUtils.applyInsetsToMargins(view, base, NONE);
        assertEquals(150, ((FrameLayout.LayoutParams) view.getLayoutParams()).bottomMargin);
    }

    @Test
    @Config(sdk = 19)
    public void applyInsetsToMargins_OnLegacyApi_DoesNotCrash() {
        // Regression: isLayoutDirectionResolved() on View requires API 19 and was previously
        // guarded at API 17, which crashed with NoSuchMethodError on API 17/18 (minSdk is 16).
        // Resolving gravity now goes through ViewCompat instead of calling that directly.
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.END;
        view.setLayoutParams(params);

        boolean applied = InsetsUtils.applyInsetsToMargins(view, NONE, PORTRAIT);

        assertEquals(true, applied);
        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(20, result.rightMargin);
    }

    @Test
    @Config(sdk = 19)
    public void applyInsetsToMargins_RelativeLayoutOnLegacyApi_DoesNotCrash() {
        // Regression: RelativeLayout.LayoutParams#getRule(int) requires API 23 and was
        // previously called directly, which crashed with NoSuchMethodError on API 16-22
        // (minSdk is 16). Rules are now read through getRules(), which is API 1.
        View view = new View(RuntimeEnvironment.getApplication());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        view.setLayoutParams(params);

        CustomInsets base = new CustomInsets(0, 0, 25, 25);
        boolean applied = InsetsUtils.applyInsetsToMargins(view, base, PORTRAIT);

        assertEquals(true, applied);
        RelativeLayout.LayoutParams result = (RelativeLayout.LayoutParams) view.getLayoutParams();
        assertEquals(55, result.bottomMargin);
        assertEquals(65, result.leftMargin);
    }

    @Test
    public void addCutoutAndNavigationInsets_CalledRepeatedly_CapturesBaseMarginsOnceAndIsIdempotent() {
        // End-to-end test of the public entry point: verifies the view-tag base-margin capture
        // wiring runs without error and stays stable across repeated calls on the same view,
        // even though Robolectric reports zero real window insets.
        View view = new View(RuntimeEnvironment.getApplication());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.topMargin = 7;
        params.rightMargin = 9;
        view.setLayoutParams(params);

        InsetsUtils.addCutoutAndNavigationInsets(view);
        InsetsUtils.addCutoutAndNavigationInsets(view);

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) view.getLayoutParams();
        assertEquals(7, result.topMargin);
        assertEquals(9, result.rightMargin);
    }
}
