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

import java.util.regex.Pattern;

public enum VASTErrorCodes {
    XML_PARSE_ERROR(100, "XML parsing error."),
    VAST_SCHEMA_ERROR(101, "VAST schema validation error."),
    VAST_UNSUPPORTED_VERSION(102, "VAST version of response not supported."),
    TRAFFICK_ERROR(200, "Trafficking error. Video player received an Ad type that it was not expecting and/or cannot display."),
    LINEARITY_ERROR(201, "Video player expecting different linearity."),
    DURATION_ERROR(202, "Video player expecting different duration."),
    SIZE_ERROR(203, "Video player expecting different size."),
    GENERAL_WRAPPER_ERROR(300, "General Wrapper error."),

    VASTTAG_TIMEOUT_ERROR(301, "Timeout of VAST URI provided in Wrapper element, or of VAST URI provided in a subsequent Wrapper element. (URI was either unavailable or reached a timeout as defined by the video player.)"),
    WRAPPER_LIMIT_REACH_ERROR(302, "Wrapper limit reached, as defined by the video player. Too many Wrapper responses have been received with no InLine response."),
    NO_AD_IN_WRAPPER_ERROR(303, "No Ads VAST response after one or more Wrappers."),
    AD_DISPLAY_TIMEOUT_ERROR(304, "InLine response returned ad unit that failed to result in ad display within defined time limit."),
    GENERAL_LINEAR_ERROR(400, "General Linear error. Video player is unable to display the Linear Ad."),

    MEDIA_NOT_FOUND_ERROR(401, "File not found. Unable to find Linear/MediaFile from URI."),
    MEDIA_TIMEOUT_ERROR(402, "Timeout of MediaFile URI."),
    NO_SUPPORTED_MEDIA_ERROR(403, "Could not find MediaFile that is supported by this video player, based on the attributes of the MediaFile element."),
    MEDIA_DISPLAY_ERROR(405, "Problem displaying MediaFile. Video player found a MediaFile with supported type but couldn't display it. MediaFile may include: unsupported codecs, different MIME type than MediaFile@type, unsupported delivery method, etc."),

    //TODO: linear, vpaid & companion error codes
    UNDEFINED_ERROR(900, "Undefined Error.");

    private static final String ERROR_CODE_MACRO = "[ERRORCODE]";
    private static final Pattern ENCODED_ERROR_CODE_MACRO = Pattern.compile(
            Pattern.quote("%5BERRORCODE%5D"),
            Pattern.CASE_INSENSITIVE
    );

    private final int code;
    private final String message;

    VASTErrorCodes(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @return the VAST error code (VAST 4.3, section 2.3.6.3).
     */
    public int getCode() {
        return code;
    }

    /**
     * Replaces the {@code [ERRORCODE]} macro of a VAST {@code <Error>} URI with this code. The percent-encoded
     * form is replaced too, for servers that encode the whole URI. Other macros are left as they are.
     */
    @NonNull
    public String replaceErrorCodeMacro(@NonNull String uri) {
        String errorCode = String.valueOf(code);
        String replaced = uri.replace(ERROR_CODE_MACRO, errorCode);
        return ENCODED_ERROR_CODE_MACRO.matcher(replaced).replaceAll(errorCode);
    }

    @NonNull
    @Override
    public final String toString() {
        return message;
    }
}

