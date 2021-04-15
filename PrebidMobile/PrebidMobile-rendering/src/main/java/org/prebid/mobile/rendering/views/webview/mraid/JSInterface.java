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

package org.prebid.mobile.rendering.views.webview.mraid;

import android.webkit.JavascriptInterface;

public interface JSInterface {
	
    String ACTION_GET_MAX_SIZE = "getMaxSize";
    String ACTION_GET_SCREEN_SIZE = "getScreenSize";
    String ACTION_GET_DEFAULT_POSITION = "getDefaultPosition";
    String ACTION_GET_CURRENT_POSITION = "getCurrentPosition";
    String ACTION_GET_PLACEMENT_TYPE = "getPlacementType";
    String ACTION_CLOSE = "close";
    String ACTION_RESIZE = "resize";
    String ACTION_EXPAND = "expand";
    String ACTION_ORIENTATION_CHANGE= "orientationchange";
    String ACTION_OPEN = "open";
    String ACTION_CREATE_CALENDAR_EVENT = "createCalendarEvent";
    String ACTION_STORE_PICTURE = "storePicture";
    String ACTION_PLAY_VIDEO = "playVideo";
    String ACTION_UNLOAD = "unload";

    String STATE_LOADING = "loading";
    String STATE_DEFAULT = "default";
    String STATE_EXPANDED = "expanded";
    String STATE_RESIZED = "resized";
    String STATE_HIDDEN = "hidden";

    String JSON_METHOD = "method";
    String JSON_VALUE = "value";
	
    String JSON_WIDTH = "width";
    String JSON_HEIGHT = "height";
    String JSON_IS_MODAL = "isModal";
    String JSON_X = "x";
    String JSON_Y = "y";

    String LOCATION_ERROR = "-1";
    String LOCATION_LAT = "lat";
    String LOCATION_LON = "lon";
    String LOCATION_ACCURACY = "accuracy";
    String LOCATION_TYPE = "type";
    String LOCATION_LASTFIX = "lastfix";

    String DEVICE_ORIENTATION = "orientation";
    String DEVICE_ORIENTATION_LOCKED = "locked";
	
    @JavascriptInterface
    String getMaxSize();

    @JavascriptInterface
    String getScreenSize();

    @JavascriptInterface
    String getDefaultPosition();

    @JavascriptInterface
    String getCurrentPosition();
    
    @JavascriptInterface
    void onOrientationPropertiesChanged(String properties);

    @JavascriptInterface
    String getPlacementType();

    @JavascriptInterface
    void close();

    @JavascriptInterface
    void resize();

    @JavascriptInterface
    void expand();

    @JavascriptInterface
    void expand(String url);

    @JavascriptInterface
    void open(String url);

    @JavascriptInterface
    void javaScriptCallback(String handlerHash, String method, String value);

    @JavascriptInterface
    void createCalendarEvent(String parameters);

    @JavascriptInterface
    void storePicture(String url);

    @JavascriptInterface
    boolean supports(String feature);

    @JavascriptInterface
    void playVideo(String url);

    @Deprecated
    @JavascriptInterface
    void shouldUseCustomClose(String useCustomClose);

    @JavascriptInterface
    String getLocation();

    @JavascriptInterface
    String getCurrentAppOrientation();

    @JavascriptInterface
    void unload();

}
