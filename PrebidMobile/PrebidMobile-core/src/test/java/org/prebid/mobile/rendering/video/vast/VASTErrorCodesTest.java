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

package org.prebid.mobile.rendering.video.vast;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VASTErrorCodesTest {
    @Test
    public void testVastErrorCodes() {

        assertEquals("XML parsing error.", VASTErrorCodes.XML_PARSE_ERROR.toString());
        assertEquals("VAST schema validation error.", VASTErrorCodes.VAST_SCHEMA_ERROR.toString());
        assertEquals("VAST version of response not supported.", VASTErrorCodes.VAST_UNSUPPORTED_VERSION.toString());

        assertEquals("Trafficking error. Video player received an Ad type that it was not expecting and/or cannot display.", VASTErrorCodes.TRAFFICK_ERROR.toString());
        assertEquals("Video player expecting different linearity.", VASTErrorCodes.LINEARITY_ERROR.toString());
        assertEquals("Video player expecting different duration.", VASTErrorCodes.DURATION_ERROR.toString());
        assertEquals("Video player expecting different size.", VASTErrorCodes.SIZE_ERROR.toString());

        assertEquals("General Wrapper error.", VASTErrorCodes.GENERAL_WRAPPER_ERROR.toString());
        assertEquals("Timeout of VAST URI provided in Wrapper element, or of VAST URI provided in a subsequent Wrapper element. (URI was either unavailable or reached a timeout as defined by the video player.)", VASTErrorCodes.VASTTAG_TIMEOUT_ERROR.toString());
        assertEquals("Wrapper limit reached, as defined by the video player. Too many Wrapper responses have been received with no InLine response.", VASTErrorCodes.WRAPPER_LIMIT_REACH_ERROR.toString());
        assertEquals("No Ads VAST response after one or more Wrappers.", VASTErrorCodes.NO_AD_IN_WRAPPER_ERROR.toString());

        assertEquals("General Linear error. Video player is unable to display the Linear Ad.", VASTErrorCodes.GENERAL_LINEAR_ERROR.toString());
        assertEquals("File not found. Unable to find Linear/MediaFile from URI.", VASTErrorCodes.MEDIA_NOT_FOUND_ERROR.toString());
        assertEquals("Timeout of MediaFile URI.", VASTErrorCodes.MEDIA_TIMEOUT_ERROR.toString());
        assertEquals("Could not find MediaFile that is supported by this video player, based on the attributes of the MediaFile element.", VASTErrorCodes.NO_SUPPORTED_MEDIA_ERROR.toString());
        assertEquals("Problem displaying MediaFile. Video player found a MediaFile with supported type but couldn't display it. MediaFile may include: unsupported codecs, different MIME type than MediaFile@type, unsupported delivery method, etc.", VASTErrorCodes.MEDIA_DISPLAY_ERROR.toString());
    }
}