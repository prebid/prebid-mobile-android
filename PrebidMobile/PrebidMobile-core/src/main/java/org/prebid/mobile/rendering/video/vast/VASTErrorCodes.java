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

import androidx.annotation.NonNull;

public enum VASTErrorCodes {
    XML_PARSE_ERROR("XML parsing error."),
    VAST_SCHEMA_ERROR("VAST schema validation error."),
    VAST_UNSUPPORTED_VERSION("VAST version of response not supported."),
    TRAFFICK_ERROR("Trafficking error. Video player received an Ad type that it was not expecting and/or cannot display."),
    LINEARITY_ERROR("Video player expecting different linearity."),
    DURATION_ERROR("Video player expecting different duration."),
    SIZE_ERROR("Video player expecting different size."),
    GENERAL_WRAPPER_ERROR("General Wrapper error."),

    VASTTAG_TIMEOUT_ERROR("Timeout of VAST URI provided in Wrapper element, or of VAST URI provided in a subsequent Wrapper element. (URI was either unavailable or reached a timeout as defined by the video player.)"),
    WRAPPER_LIMIT_REACH_ERROR("Wrapper limit reached, as defined by the video player. Too many Wrapper responses have been received with no InLine response."),
    NO_AD_IN_WRAPPER_ERROR("No Ads VAST response after one or more Wrappers."),
    GENERAL_LINEAR_ERROR("General Linear error. Video player is unable to display the Linear Ad."),

    MEDIA_NOT_FOUND_ERROR("File not found. Unable to find Linear/MediaFile from URI."),
    MEDIA_TIMEOUT_ERROR("Timeout of MediaFile URI."),
    NO_SUPPORTED_MEDIA_ERROR("Could not find MediaFile that is supported by this video player, based on the attributes of the MediaFile element."),
    MEDIA_DISPLAY_ERROR("Problem displaying MediaFile. Video player found a MediaFile with supported type but couldn't display it. MediaFile may include: unsupported codecs, different MIME type than MediaFile@type, unsupported delivery method, etc."),

    //TODO: linear, vpaid & companion error codes
    UNDEFINED_ERROR("Undefined Error.");

    private final String message;

    VASTErrorCodes(String message) {
        this.message = message;
    }

    @NonNull
    @Override
    public final String toString() {
        return message;
    }
}

