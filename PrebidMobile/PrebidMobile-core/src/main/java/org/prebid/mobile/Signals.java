/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

public class Signals {

    static class SingleContainerInt {

        final int value;
        private SingleContainerInt(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SingleContainerInt that = (SingleContainerInt) o;

            return value == that.value;
        }

        @Override
        public int hashCode() {
            return value;
        }

        public int getValue() {
            return value;
        }

    }

    /**
     * OpenRTB - API Frameworks
     * <pre>
     | Value | Description |
     |-------|-------------|
     | 1     | VPAID 1.0   |
     | 2     | VPAID 2.0   |
     | 3     | MRAID-1     |
    | 4     | ORMMA       |
    | 5     | MRAID-2     |
    | 6     | MRAID-3     |
    | 7     | OMID-1      |
     *</pre>
     */
    public static class Api extends SingleContainerInt {

        /** VPAID 1.0 */
        public static Api VPAID_1 = new Api(1);

        /** VPAID 2.0 */
        public static Api VPAID_2 = new Api(2);

        /** MRAID-1 */
        public static Api MRAID_1 = new Api(3);

        /** ORMMA */
        public static Api ORMMA = new Api(4);

        /** MRAID-2 */
        public static Api MRAID_2 = new Api(5);

        /** MRAID-3 */
        public static Api MRAID_3 = new Api(6);

        /** OMID-1 */
        public static Api OMID_1 = new Api(7);

        public Api(int value) {
            super(value);
        }
    }

    /**
     OpenRTB - Playback Methods
     * <pre>
     | Value | Description                                              |
     |-------|----------------------------------------------------------|
     | 1     | Initiates on Page Load with Sound On                     |
     | 2     | Initiates on Page Load with Sound Off by Default         |
     | 3     | Initiates on Click with Sound On                         |
     | 4     | Initiates on Mouse-Over with Sound On                    |
     | 5     | Initiates on Entering Viewport with Sound On             |
     | 6     | Initiates on Entering Viewport with Sound Off by Default |
     *</pre>
     */
    public static class PlaybackMethod extends SingleContainerInt {

        /** Initiates on Page Load with Sound On */
        public static PlaybackMethod AutoPlaySoundOn = new PlaybackMethod(1);

        /** Initiates on Page Load with Sound Off by Default  */
        public static PlaybackMethod AutoPlaySoundOff = new PlaybackMethod(2);

        /** Initiates on Click with Sound On */
        public static PlaybackMethod ClickToPlay = new PlaybackMethod(3);

        /** Initiates on Mouse-Over with Sound On */
        public static PlaybackMethod MouseOver = new PlaybackMethod(4);

        /** Initiates on Entering Viewport with Sound On */
        public static PlaybackMethod EnterSoundOn = new PlaybackMethod(5);

        /** Initiates on Entering Viewport with Sound Off by Default */
        public static PlaybackMethod EnterSoundOff = new PlaybackMethod(6);

        public PlaybackMethod(int value) {
            super(value);
        }
    }

    /**
     OpenRTB - Protocols
     * <pre>
     | Value | Description       |
     |-------|-------------------|
     | 1     | VAST 1.0          |
     | 2     | VAST 2.0          |
     | 3     | VAST 3.0          |
     | 4     | VAST 1.0 Wrapper  |
     | 5     | VAST 2.0 Wrapper  |
     | 6     | VAST 3.0 Wrapper  |
     | 7     | VAST 4.0          |
     | 8     | VAST 4.0 Wrapper  |
     | 9     | DAAST 1.0         |
     | 10    | DAAST 1.0 Wrapper |
     *</pre>
     */
    public static class Protocols extends SingleContainerInt {

        /** VAST 1.0 */
        public static Protocols VAST_1_0 = new Protocols(1);

        /** VAST 2.0 */
        public static Protocols VAST_2_0 = new Protocols(2);

        /** VAST 3.0 */
        public static Protocols VAST_3_0 = new Protocols(3);

        /** VAST 1.0 Wrapper */
        public static Protocols VAST_1_0_Wrapper = new Protocols(4);

        /** VAST 2.0 Wrapper */
        public static Protocols VAST_2_0_Wrapper = new Protocols(5);

        /** VAST 3.0 Wrapper */
        public static Protocols VAST_3_0_Wrapper = new Protocols(6);

        /** VAST 4.0 */
        public static Protocols VAST_4_0 = new Protocols(7);

        /** VAST 4.0 Wrapper */
        public static Protocols VAST_4_0_Wrapper = new Protocols(8);

        /** DAAST 1.0 */
        public static Protocols DAAST_1_0 = new Protocols(9);

        /** DAAST 1.0 Wrapper */
        public static Protocols DAAST_1_0_WRAPPER = new Protocols(10);

        public Protocols(int value) {
            super(value);
        }
    }

    /**
     OpenRTB - Start Delay
     * <pre>
     | Value | Description                                      |
     |-------|--------------------------------------------------|
     | > 0   | Mid-Roll (value indicates start delay in second) |
     | 0     | Pre-Roll                                         |
     | -1    | Generic Mid-Roll                                 |
     | -2    | Generic Post-Roll                                |
     *</pre>
     */
    public static class StartDelay extends SingleContainerInt {

        /** Pre-Roll */
        public static StartDelay PreRoll = new StartDelay(0);

        /** Generic Mid-Roll */
        public static StartDelay GenericMidRoll = new StartDelay(-1);

        /** Generic Post-Roll */
        public static StartDelay GenericPostRoll = new StartDelay(-2);

        public StartDelay(int value) {
            super(value);
        }
    }

    /**
     OpenRTB - Video Placement Types
     * <pre>
     | Value | Description                  |
     |-------|------------------------------|
     | 1     | In-Stream                    |
     | 2     | In-Banner                    |
     | 3     | In-Article                   |
     | 4     | In-Feed                      |
     | 5     | Interstitial/Slider/Floating |
     *</pre>
     */
    public static class Placement extends SingleContainerInt {

        /** In-Stream */
        public static Placement InStream = new Placement(1);

        /** In-Banner */
        public static Placement InBanner = new Placement(2);

        /** In-Article */
        public static Placement InArticle = new Placement(3);

        /** In-Feed */
        public static Placement InFeed = new Placement(4);

        /** Interstitial */
        public static Placement Interstitial = new Placement(5);

        /** Slider */
        public static Placement Slider = new Placement(5);

        /** Floating */
        public static Placement Floating = new Placement(5);

        public Placement(int value) {
            super(value);
        }
    }

}
