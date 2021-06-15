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

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Dips {
    public static float pixelsToFloatDips(final float pixels, final Context context) {
        return pixels / getDensity(context);
    }

    public static int pixelsToIntDips(final float pixels, final Context context) {
        return (int) (pixelsToFloatDips(pixels, context) + 0.5f);
    }

    public static float dipsToFloatPixels(final float dips, final Context context) {
        return dips * getDensity(context);
    }

    public static int dipsToIntPixels(final float dips, final Context context) {
        return (int) asFloatPixels(dips, context);
    }

    private static float getDensity(final Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static float asFloatPixels(float dips, Context context) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, displayMetrics);
    }

    public static int asIntPixels(float dips, Context context) {
        return (int) (asFloatPixels(dips, context) + 0.5f);
    }
}
