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

package org.prebid.mobile.renderingtestapp.utils

import android.content.Context
import android.os.Bundle
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.data.DemoItem
import org.prebid.mobile.renderingtestapp.data.Tag
import org.prebid.mobile.renderingtestapp.plugplay.bidding.admob.AdMobInterstitialFragment
import org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.rendering.GamNativeFragment
import org.prebid.mobile.renderingtestapp.plugplay.bidding.max.MaxInterstitialFragment

class DemoItemProvider private constructor() {

    companion object {
        private var context: Context? = null
        private val demoList = mutableListOf<DemoItem>()

        private const val MIN_WIDTH_PERC = 30
        private const val MIN_HEIGHT_PERC = 30

        private const val ppmBannerAction = R.id.action_header_bidding_to_in_app_banner
        private const val ppmInterstitialAction = R.id.action_header_bidding_to_in_app_interstitial
        private const val ppmInterstitialMultiformatAction =
            R.id.action_header_bidding_to_in_app_interstitial_multiformat
        private const val ppmInterstitialCloseButtonAreaAction =
            R.id.action_header_bidding_to_in_app_interstitial_close_button_area
        private const val ppmInterstitialSoundButtonAction =
            R.id.action_header_bidding_to_in_app_interstitial_sound_button
        private const val ppmInterstitialSkipButtonAction =
            R.id.action_header_bidding_to_in_app_interstitial_skip_button
        private const val ppmRewardedAction = R.id.action_header_bidding_to_in_app_video_rewarded

        private const val gamBannerAction = R.id.action_header_bidding_to_gam_banner
        private const val gamBannerOriginalAction =
            R.id.action_header_bidding_to_gam_original_banner
        private const val gamInterstitialAction = R.id.action_header_bidding_to_gam_interstitial
        private const val gamInterstitialMultiformatAction =
            R.id.action_header_bidding_to_gam_interstitial_multiformat
        private const val gamRewardedAction = R.id.action_header_bidding_to_gam_video_rewarded

        private const val adMobBannerAction = R.id.action_header_bidding_to_admob_banner
        private const val adMobRandomBannerAction =
            R.id.action_header_bidding_to_admob_random_banner
        private const val adMobFlexibleBannerAction =
            R.id.action_header_bidding_to_admob_flexible_banner
        private const val adMobInterstitialAction = R.id.action_header_bidding_to_admob_interstitial
        private const val adMobInterstitialMultiformatAction =
            R.id.action_header_bidding_to_admob_interstitial_multiformat
        private const val adMobInterstitialRandomAction =
            R.id.action_header_bidding_to_admob_interstitial_random
        private const val adMobRewardedAction = R.id.action_header_bidding_to_admob_rewarded
        private const val adMobRewardedRandomAction =
            R.id.action_header_bidding_to_admob_rewarded_random
        private const val adMobNativeAction = R.id.action_header_bidding_to_admob_native

        fun init(context: Context) {
            if (demoList.isNotEmpty()) {
                return
            }
            Companion.context = context

            formPbsDemoList()

            Companion.context = null
        }

        fun getDemoList() = demoList

        private fun getString(resId: Int): String {
            return context!!.getString(resId)
        }

        private fun formPbsDemoList() {
            addGamOriginalExamples()
            addInAppPbsExamples()
            addGamPbsExamples()
            addAdMobPbsExamples()
            addApplovinMaxPbsExamples()
        }

        private fun addGamOriginalExamples() {
            val gamBannerTagList = listOf(Tag.ALL, Tag.ORIGINAL, Tag.BANNER, Tag.REMOTE)
            val gamInterstitialTagList = listOf(Tag.ALL, Tag.ORIGINAL, Tag.INTERSTITIAL, Tag.REMOTE)
            val gamVideoTagList = listOf(Tag.ALL, Tag.ORIGINAL, Tag.VIDEO, Tag.REMOTE)
            val gamNativeTagList = listOf(Tag.ALL, Tag.ORIGINAL, Tag.NATIVE, Tag.REMOTE)

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_320_50_original),
                    gamBannerOriginalAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.adunit_gam_banner_320_50_original,
                        320,
                        50
                    )
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_300_250_original),
                    gamBannerOriginalAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_300x250,
                        R.string.adunit_gam_banner_300_250_original,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_728_90_original),
                    gamBannerOriginalAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_728x90,
                        R.string.adunit_gam_banner_728_90_original,
                        728,
                        90
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_multisize_original),
                    gamBannerOriginalAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_multisize,
                        R.string.adunit_gam_banner_multisize_original,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_320_480_original),
                    R.id.action_header_bidding_to_gam_original_interstitial,
                    gamInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_interstitial_320_480,
                        R.string.adunit_gam_interstitial_320_480_original,
                        320,
                        480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_oustream_original),
                    R.id.action_header_bidding_to_gam_video_outstream_original,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_outstream_original_api,
                        R.string.adunit_gam_video_300_250_original,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_instream_original),
                    R.id.action_header_bidding_to_gam_original_instream,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_instream,
                        R.string.adunit_gam_video_instream,
                        640,
                        480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_video_320_480_original),
                    R.id.action_header_bidding_to_gam_original_interstitial,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_original_api,
                        R.string.adunit_gam_interstitial_video_320_480_original,
                        320,
                        480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_original),
                    R.id.action_header_bidding_to_gam_original_rewarded_video,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480_original_api,
                        R.string.adunit_gam_interstitial_video_320_480_original,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_in_app_original),
                    R.id.action_header_bidding_to_gam_original_native_in_app,
                    gamNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        adUnitIdRes = R.string.adunit_gam_native_custom_template
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_banner_original),
                    R.id.action_header_bidding_to_gam_original_native_banner,
                    gamNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        adUnitIdRes = R.string.adunit_gam_native_styles
                    )
                )
            )
        }

        private fun addInAppPbsExamples() {
            val ppmBannerTagList = listOf(Tag.ALL, Tag.IN_APP, Tag.BANNER, Tag.REMOTE)
            val ppmInterstitialTagList = listOf(Tag.ALL, Tag.IN_APP, Tag.INTERSTITIAL, Tag.REMOTE)
            val ppmMraidTagList = listOf(Tag.ALL, Tag.IN_APP, Tag.MRAID, Tag.REMOTE)
            val ppmVideoTagList = listOf(Tag.ALL, Tag.IN_APP, Tag.VIDEO, Tag.REMOTE)
            val ppmNativeTagList = listOf(Tag.ALL, Tag.IN_APP, Tag.NATIVE, Tag.REMOTE)

            // In-App Banner
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_320_50),
                    ppmBannerAction,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_320_50_imp_prebid_random_bid),
                    ppmBannerAction,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_320_50_no_bids),
                    ppmBannerAction,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_320_50_events),
                    ppmBannerAction,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        null,
                        320,
                        50
                    ).apply {
                        putString(
                            AdFragment.ARGUMENT_ACCOUNT_ID,
                            getString(R.string.prebid_account_id_prod_enabled_events)
                        )
                    }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_300_250),
                    ppmBannerAction,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_300x250,
                        null,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_728_90),
                    ppmBannerAction,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_728x90,
                        null,
                        728,
                        90
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_320_50_vast),
                    ppmBannerAction,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50_vast,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_320_50_scrollable),
                    R.id.action_header_bidding_to_in_app_banner_scrollable,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_320_50_deeplink),
                    ppmBannerAction,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50_deeplink,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_layout),
                    R.id.action_header_bidding_to_in_app_banner_in_layout,
                    ppmBannerTagList,
                    createBannerBundle(null, null, 320, 50)
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_multisize),
                    R.id.action_header_bidding_to_in_app_multisize_banner,
                    ppmBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_multisize,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banners_and_interstitial),
                    R.id.action_header_bidding_to_in_app_banners_and_interstitial,
                    ppmBannerTagList,
                    createBannerBundle(null, null, 0, 0)
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_expand),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_expand,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_expand_2),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_expand_two_part,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_resize),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_resize,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_resize_with_errors),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_resize_with_errors,
                        null,
                        300,
                        100
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_fullscreen),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_fullscreen,
                        null,
                        320,
                        480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_fullscreen_video),
                    ppmInterstitialAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_video_interstitial,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_viewability_compliance),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_3_viewability_compliance,
                        null,
                        320,
                        480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_resize_negative),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_3_resize_negative,
                        null,
                        320,
                        480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_load_and_events),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_3_load_and_events,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_test_properties),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_ox_test_properties,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_test_methods),
                    ppmBannerAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_ox_test_methods,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_resize_scroll),
                    R.id.action_header_bidding_to_in_app_banner_scrollable,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_resize,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_mraid_resize_expandable),
                    R.id.action_header_bidding_to_in_app_banner,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_ox_resize_expandable,
                        null,
                        320,
                        50
                    )
                )
            )

            // In-App Interstitial
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_320_480),
                    ppmInterstitialAction,
                    ppmInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_interstitial_320_480,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_320_480_no_bids),
                    ppmInterstitialAction,
                    ppmInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )

            /// In-App Video
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_no_bids),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_skipoffset),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_skipoffset,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_deeplink),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_deeplink,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_end_card),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_with_end_card,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_close_button_area),
                    ppmInterstitialCloseButtonAreaAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_end_card_sound_button),
                    ppmInterstitialSoundButtonAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_with_end_card,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_end_card_skip_button),
                    ppmInterstitialSkipButtonAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_with_end_card,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_mraid_end_card),
                    ppmInterstitialAction,
                    ppmMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_mraid_end_card,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_with_ad_configuration),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_with_ad_configuration,
                        null,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_320_480_end_card_with_ad_configuration),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_with_end_card_with_ad_configuration,
                        null,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_vertical_end_card),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_vertical,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_video_landscape_end_card),
                    ppmInterstitialAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_vertical,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_interstitial_320_480_multiformat),
                    ppmInterstitialMultiformatAction,
                    ppmInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_dynamic,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_video_rewarded_320_480),
                    ppmRewardedAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_320_480,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_video_rewarded_end_card_320_480_no_bids),
                    ppmRewardedAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_video_rewarded_end_card_320_480),
                    ppmRewardedAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_video_rewarded_320_480_with_ad_configuration),
                    ppmRewardedAction,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480_with_ad_configuration,
                        null,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )


            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_video_outstream),
                    R.id.action_header_bidding_to_in_app_banner_video,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_outstream,
                        null,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_video_outstream_no_bids),
                    R.id.action_header_bidding_to_in_app_banner_video,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        null,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_video_outstream_feed),
                    R.id.action_header_bidding_to_in_app_banner_video_feed,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_outstream,
                        null,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_banner_video_outstream_end_card),
                    R.id.action_header_bidding_to_in_app_banner_video,
                    ppmVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_outstream_end_card,
                        null,
                        300,
                        250
                    )
                )
            )

            // Native
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_native),
                    R.id.action_header_bidding_to_in_app_native,
                    ppmNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_native_feed),
                    R.id.action_header_bidding_to_in_app_native_feed,
                    ppmNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_native_links),
                    R.id.action_header_bidding_to_in_app_native_links,
                    ppmNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_links
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_in_app_native_events),
                    R.id.action_header_bidding_to_in_app_native,
                    ppmNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles
                    ).apply {
                        putString(
                            AdFragment.ARGUMENT_ACCOUNT_ID,
                            getString(R.string.prebid_account_id_prod_enabled_events)
                        )
                    }
                )
            )
        }

        private fun addGamPbsExamples() {
            val gamBannerTagList = listOf(Tag.ALL, Tag.GAM, Tag.BANNER, Tag.REMOTE)
            val gamInterstitialTagList = listOf(Tag.ALL, Tag.GAM, Tag.INTERSTITIAL, Tag.REMOTE)
            val gamMraidTagList = listOf(Tag.ALL, Tag.GAM, Tag.MRAID, Tag.REMOTE)
            val gamVideoTagList = listOf(Tag.ALL, Tag.GAM, Tag.VIDEO, Tag.REMOTE)
            val gamNativeTagList = listOf(Tag.ALL, Tag.GAM, Tag.NATIVE, Tag.REMOTE)

            /// GAM Banner
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_320_50_app_event),
                    gamBannerAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.adunit_gam_banner_320_50_app_event,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_320_50_no_bids),
                    gamBannerAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        null,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_320_50_gam_ad),
                    gamBannerAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.adunit_gam_banner_320_50_gam_ad,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_320_50_random),
                    gamBannerAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.adunit_gam_banner_320_50_random,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_320_50_app_event_with_events_url),
                    gamBannerAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.adunit_gam_banner_320_50_app_event,
                        320,
                        50
                    ).apply {
                        putString(
                            AdFragment.ARGUMENT_ACCOUNT_ID,
                            getString(R.string.prebid_account_id_prod_enabled_events)
                        )
                    }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_300_250),
                    gamBannerAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_300x250,
                        R.string.adunit_gam_banner_300_250,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_728_90),
                    gamBannerAction,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_728x90,
                        R.string.adunit_gam_banner_728_90,
                        728,
                        90
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banner_multisize),
                    R.id.action_header_bidding_to_gam_multisize_banner,
                    gamBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_multisize,
                        R.string.adunit_gam_banner_multisize,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_banners_and_interstitial),
                    R.id.action_header_bidding_to_gam_banners_and_interstitial,
                    gamBannerTagList,
                    createBannerBundle(null, null, 0, 0)
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_mraid_expand),
                    gamBannerAction,
                    gamMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_expand,
                        R.string.adunit_gam_banner_320_50_app_event,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_mraid_resize),
                    gamBannerAction,
                    gamMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_resize,
                        R.string.adunit_gam_banner_320_50_app_event,
                        320,
                        50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_mraid_fullscreen_video),
                    gamInterstitialAction,
                    gamMraidTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_mraid_video_interstitial,
                        R.string.adunit_gam_interstitial_320_480_app_event,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )

            // GAM Interstitial
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_320_480_app_event),
                    gamInterstitialAction,
                    gamInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_interstitial_320_480,
                        R.string.adunit_gam_interstitial_320_480_app_event,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_320_480_random),
                    gamInterstitialAction,
                    gamInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_interstitial_320_480,
                        R.string.adunit_gam_interstitial_320_480_random,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_320_480_no_bids),
                    gamInterstitialAction,
                    gamInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.adunit_gam_interstitial_320_480_no_bids,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )

            ///GAM Video
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_video_320_480_app_event),
                    gamInterstitialAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.adunit_gam_interstitial_video_320_480_app_event,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_video_320_480_random),
                    gamInterstitialAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.adunit_gam_interstitial_video_320_480_random,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_video_320_480_no_bids),
                    gamInterstitialAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.adunit_gam_interstitial_video_320_480_no_bids,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_video_320_480_app_event_configuration),
                    gamInterstitialAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.adunit_gam_interstitial_video_320_480_app_event,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_video_320_480_app_event_endcard_configuration),
                    gamInterstitialAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_with_end_card_with_ad_configuration,
                        R.string.adunit_gam_interstitial_video_320_480_app_event,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_interstitial_320_480_multiformat),
                    gamInterstitialMultiformatAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_dynamic,
                        R.string.adunit_gam_interstitial_video_320_480_app_event,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_rewarded_320_480_metadata),
                    gamRewardedAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_320_480,
                        R.string.adunit_gam_video_rewarded_320_480_metadata,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_metadata),
                    gamRewardedAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480,
                        R.string.adunit_gam_video_rewarded_320_480_metadata,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_no_bids),
                    gamRewardedAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.adunit_gam_video_rewarded_320_480,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_random),
                    gamRewardedAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480,
                        R.string.adunit_gam_video_rewarded_320_480_random,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_metadata_configuration),
                    gamRewardedAction,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480_with_ad_configuration,
                        R.string.adunit_gam_video_rewarded_320_480_metadata,
                        MIN_WIDTH_PERC,
                        MIN_HEIGHT_PERC
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_oustream_app_event),
                    R.id.action_header_bidding_to_gam_banner_video,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_outstream,
                        R.string.adunit_gam_banner_300_250,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_outstream_no_bids),
                    R.id.action_header_bidding_to_gam_banner_video,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.adunit_gam_video_300_250,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_outstream_random),
                    R.id.action_header_bidding_to_gam_banner_video,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_outstream,
                        R.string.adunit_gam_video_300_250_random,
                        300,
                        250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_video_outstream_feed),
                    R.id.action_header_bidding_to_gam_banner_video_feed,
                    gamVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_outstream,
                        R.string.adunit_gam_video_300_250_random,
                        300,
                        250
                    )
                )
            )

            // Native
            var gamNativeBundle = createBannerBundle(
                R.string.imp_prebid_id_native_styles,
                R.string.adunit_gam_native_custom_template
            )
            gamNativeBundle.putString(GamNativeFragment.ARG_CUSTOM_FORMAT_ID, "11934135")
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_custom_templates),
                    R.id.action_header_bidding_to_gam_native,
                    gamNativeTagList,
                    gamNativeBundle
                )
            )

            gamNativeBundle = createBannerBundle(
                R.string.imp_prebid_id_native_styles,
                R.string.adunit_gam_native_custom_template
            )
            gamNativeBundle.putString(GamNativeFragment.ARG_CUSTOM_FORMAT_ID, "11982639")
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_custom_templates_prebid_ok),
                    R.id.action_header_bidding_to_gam_native,
                    gamNativeTagList,
                    gamNativeBundle
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_custom_templates_events),
                    R.id.action_header_bidding_to_gam_native,
                    gamNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        R.string.adunit_gam_native_custom_template
                    ).apply {
                        putString(GamNativeFragment.ARG_CUSTOM_FORMAT_ID, "11934135")
                        putString(
                            AdFragment.ARGUMENT_ACCOUNT_ID,
                            getString(R.string.prebid_account_id_prod_enabled_events)
                        )
                    }
                )
            )

            gamNativeBundle = createBannerBundle(
                R.string.imp_prebid_id_no_bids,
                R.string.adunit_gam_native_custom_template
            )
            gamNativeBundle.putString(GamNativeFragment.ARG_CUSTOM_FORMAT_ID, "11982639")
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_custom_templates_no_bids),
                    R.id.action_header_bidding_to_gam_native,
                    gamNativeTagList,
                    gamNativeBundle
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_unified_ads),
                    R.id.action_header_bidding_to_gam_native,
                    gamNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        R.string.adunit_gam_native_unified
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_unified_ads_prebid_ok),
                    R.id.action_header_bidding_to_gam_native,
                    gamNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        R.string.adunit_gam_native_unified_static
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_unified_ads_no_bids),
                    R.id.action_header_bidding_to_gam_native,
                    gamNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.adunit_gam_native_unified_static
                    )
                )
            )

            gamNativeBundle = createBannerBundle(
                R.string.imp_prebid_id_native_styles,
                R.string.adunit_gam_native_custom_template
            )
            gamNativeBundle.putString(GamNativeFragment.ARG_CUSTOM_FORMAT_ID, "11934135")
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_gam_native_feed),
                    R.id.action_header_bidding_to_gam_native_feed,
                    gamNativeTagList, gamNativeBundle
                )
            )
        }

        private fun addAdMobPbsExamples() {
            val adMobBannerTagList = listOf(Tag.ALL, Tag.ADMOB, Tag.BANNER, Tag.REMOTE)
            val adMobInterstitialTagList = listOf(Tag.ALL, Tag.ADMOB, Tag.INTERSTITIAL, Tag.REMOTE)
            val adMobVideoTagList = listOf(Tag.ALL, Tag.ADMOB, Tag.VIDEO, Tag.REMOTE)
            val adMobNativeTagList = listOf(Tag.ALL, Tag.ADMOB, Tag.NATIVE, Tag.REMOTE)

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_banner_320_50_adapter),
                    adMobBannerAction,
                    adMobBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.admob_banner_bidding_ad_unit_id_adapter,
                        320, 50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_banner_320_50_ok_random),
                    adMobBannerAction,
                    adMobBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.admob_banner_bidding_ad_unit_id_adapter,
                        320, 50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_banner_320_50_no_bids),
                    adMobBannerAction,
                    adMobBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.admob_banner_bidding_ad_unit_id_adapter,
                        320, 50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_banner_320_50_random_respectively),
                    adMobRandomBannerAction,
                    adMobBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.admob_banner_bidding_ad_unit_id_adapter,
                        320, 50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_banner_320_50_adapter_with_events),
                    adMobBannerAction,
                    adMobBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.admob_banner_bidding_ad_unit_id_adapter,
                        320, 50
                    ).apply {
                        putString(
                            AdFragment.ARGUMENT_ACCOUNT_ID,
                            getString(R.string.prebid_account_id_prod_enabled_events)
                        )
                    }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_banner_300_250_adapter),
                    adMobBannerAction,
                    adMobBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_300x250,
                        R.string.admob_banner_bidding_ad_unit_id_adapter,
                        300, 250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_banner_300_250_ok_random),
                    adMobBannerAction,
                    adMobBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_300x250,
                        R.string.admob_banner_bidding_ad_unit_id_adapter,
                        300, 250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_flexible_banner_ok_random),
                    adMobFlexibleBannerAction,
                    adMobBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_multisize,
                        R.string.admob_banner_bidding_ad_unit_id_adapter,
                        320, 50
                    )
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_adapter),
                    adMobInterstitialAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    ).apply { putBoolean(AdMobInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_admob),
                    adMobInterstitialAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    ).apply { putBoolean(AdMobInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_random),
                    adMobInterstitialRandomAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    ).apply { putBoolean(AdMobInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_adapter_configuration),
                    adMobInterstitialAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    ).apply { putBoolean(AdMobInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_adapter_endcard_configuration),
                    adMobInterstitialAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_with_end_card_with_ad_configuration,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    ).apply { putBoolean(AdMobInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_multiformat),
                    adMobInterstitialMultiformatAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_dynamic,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_rewarded_adapter),
                    adMobRewardedAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480,
                        R.string.admob_rewarded_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_rewarded_admob),
                    adMobRewardedAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.admob_rewarded_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_rewarded_adapter_no_end_card),
                    adMobRewardedAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_320_480,
                        R.string.admob_rewarded_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_rewarded_random),
                    adMobRewardedRandomAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480,
                        R.string.admob_rewarded_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_rewarded_adapter_configuration),
                    adMobRewardedAction,
                    adMobVideoTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480,
                        R.string.admob_rewarded_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_display_adapter),
                    adMobInterstitialAction,
                    adMobInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_interstitial_320_480,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_display_admob),
                    adMobInterstitialAction,
                    adMobInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_interstitial_display_random),
                    adMobInterstitialRandomAction,
                    adMobInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_interstitial_320_480,
                        R.string.admob_interstitial_bidding_ad_unit_id_adapter,
                        320, 480
                    )
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_native_adapter),
                    adMobNativeAction,
                    adMobNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        R.string.admob_native_bidding_ad_unit_id_adapter
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_native_no_bids),
                    adMobNativeAction,
                    adMobNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.admob_native_bidding_ad_unit_id_adapter
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_admob_native_adapter_events),
                    adMobNativeAction,
                    adMobNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        R.string.admob_native_bidding_ad_unit_id_adapter
                    ).apply {
                        putString(
                            AdFragment.ARGUMENT_ACCOUNT_ID,
                            getString(R.string.prebid_account_id_prod_enabled_events)
                        )
                    }
                )
            )
        }

        private fun addApplovinMaxPbsExamples() {
            val maxBannerTagList = listOf(Tag.ALL, Tag.MAX, Tag.BANNER, Tag.REMOTE)
            val maxBannerAction = R.id.action_header_bidding_to_max_banner
            val maxBannerRandomAction = R.id.action_header_bidding_to_max_banner_random
            val maxBannerAdaptiveAction = R.id.action_header_bidding_to_max_banner_adaptive

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_banner_320_50_adapter),
                    maxBannerAction,
                    maxBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.max_banner_ad_unit_id,
                        320, 50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_banner_320_50_no_bids),
                    maxBannerAction,
                    maxBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.max_banner_ad_unit_id,
                        320, 50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_banner_320_50_random),
                    maxBannerRandomAction,
                    maxBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.max_banner_ad_unit_id,
                        320, 50
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_banner_320_50_adapter_with_events),
                    maxBannerAction,
                    maxBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.max_banner_ad_unit_id,
                        320, 50
                    ).apply {
                        putString(
                            AdFragment.ARGUMENT_ACCOUNT_ID,
                            getString(R.string.prebid_account_id_prod_enabled_events)
                        )
                    }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_banner_300_250_adapter),
                    maxBannerAction,
                    maxBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_300x250,
                        R.string.max_mrec_ad_unit_id,
                        300, 250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_banner_300_250_no_bids),
                    maxBannerAction,
                    maxBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.max_mrec_ad_unit_id,
                        300, 250
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_banner_adaptive),
                    maxBannerAdaptiveAction,
                    maxBannerTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_banner_320x50,
                        R.string.max_banner_ad_unit_id,
                        320, 50
                    )
                )
            )

            val maxInterstitialTagList = listOf(Tag.ALL, Tag.MAX, Tag.INTERSTITIAL, Tag.REMOTE)
            val maxVideoInterstitialTagList = listOf(Tag.ALL, Tag.MAX, Tag.VIDEO, Tag.REMOTE)
            val maxInterstitialAction = R.id.action_header_bidding_to_max_interstitial
            val maxMultiformatInterstitialAction =
                R.id.action_header_bidding_to_max_interstitial_multiformat
            val maxRandomInterstitialAction = R.id.action_header_bidding_to_max_interstitial_random


            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_interstitial_adapter),
                    maxInterstitialAction,
                    maxInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_interstitial_320_480,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_interstitial_no_bids),
                    maxInterstitialAction,
                    maxInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_interstitial_random),
                    maxRandomInterstitialAction,
                    maxInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_interstitial_320_480,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    )
                )
            )

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_video_interstitial_adapter),
                    maxInterstitialAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    ).apply { putBoolean(MaxInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_video_interstitial_no_bids),
                    maxInterstitialAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    ).apply { putBoolean(MaxInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_video_interstitial_random),
                    maxRandomInterstitialAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    ).apply { putBoolean(MaxInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_video_interstitial_adapter_configuration),
                    maxInterstitialAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    ).apply { putBoolean(MaxInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_video_interstitial_adapter_endcard_configuration),
                    maxInterstitialAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_interstitial_320_480_with_end_card_with_ad_configuration,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    ).apply { putBoolean(MaxInterstitialFragment.ARG_IS_VIDEO, true) }
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_interstitial_multiformat_adapter),
                    maxMultiformatInterstitialAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_dynamic,
                        R.string.max_interstitial_ad_unit_id,
                        320, 480
                    )
                )
            )

            val maxRewardedAction = R.id.action_header_bidding_to_max_rewarded
            val maxRewardedRandomAction = R.id.action_header_bidding_to_max_rewarded_random

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_rewarded_adapter),
                    maxRewardedAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_320_480,
                        R.string.max_rewarded_ad_unit_id,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_rewarded_no_bids),
                    maxRewardedAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.max_rewarded_ad_unit_id,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_rewarded_random),
                    maxRewardedRandomAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_320_480,
                        R.string.max_rewarded_ad_unit_id,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_rewarded_adapter_no_end_card),
                    maxRewardedAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480,
                        R.string.max_rewarded_ad_unit_id,
                        320, 480
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_rewarded_adapter_endcard_configuration),
                    maxRewardedAction,
                    maxVideoInterstitialTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_video_rewarded_end_card_320_480_with_ad_configuration,
                        R.string.max_rewarded_ad_unit_id,
                        320, 480
                    )
                )
            )


            val maxNativeAction = R.id.action_header_bidding_to_max_native
            val maxNativeTagList = listOf(Tag.ALL, Tag.MAX, Tag.NATIVE, Tag.REMOTE)

            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_native_adapter),
                    maxNativeAction,
                    maxNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        R.string.max_native_ad_unit_id
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_native_no_bids),
                    maxNativeAction,
                    maxNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_no_bids,
                        R.string.max_native_ad_unit_id
                    )
                )
            )
            demoList.add(
                DemoItem(
                    getString(R.string.demo_bidding_max_native_adapter_events),
                    maxNativeAction,
                    maxNativeTagList,
                    createBannerBundle(
                        R.string.imp_prebid_id_native_styles,
                        R.string.max_native_ad_unit_id
                    ).apply {
                        putString(
                            AdFragment.ARGUMENT_ACCOUNT_ID,
                            getString(R.string.prebid_account_id_prod_enabled_events)
                        )
                    }
                )
            )
        }

        private fun createBannerBundle(
            configIdRes: Int?,
            adUnitIdRes: Int? = null,
            width: Int = 0,
            height: Int = 0
        ): Bundle {
            return Bundle().apply {
                if (configIdRes != null) {
                    putString(getString(R.string.key_bid_config_id), getString(configIdRes))
                }
                if (adUnitIdRes != null) {
                    putString(getString(R.string.key_ad_unit), getString(adUnitIdRes))
                }
                putInt(getString(R.string.key_width), width)
                putInt(getString(R.string.key_height), height)
            }
        }
    }

}