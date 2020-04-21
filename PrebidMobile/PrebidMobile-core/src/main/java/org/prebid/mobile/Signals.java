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

    }
    /**
     * OpenRTB
     * <pre>
    | Value | Description |
    |-------|-------------|
    | 1     | VPAID 1.0   |
    | 2     | VPAID 2.0   |
    | 3     | MRAID-1     |
    | 4     | ORMMA       |
    | 5     | MRAID-2     |
    | 6     | MRAID-3     |
     *</pre>
     */
    public static class Api extends SingleContainerInt {

        public static Api VPAID_1 = new Api(1);
        public static Api VPAID_2 = new Api(2);
        public static Api MRAID_1 = new Api(3);
        public static Api ORMMA = new Api(4);
        public static Api MARAID_2 = new Api(5);
        public static Api MARAID_3 = new Api(6);

        public Api(int value) {
            super(value);
        }
    }

    /**
     OpenRTB
     * <pre>
     | Value | Description         |
     |-------|---------------------|
     | 1     | Auto-Play Sound On  |
     | 2     | Auto-Play Sound Off |
     | 3     | Click-to-Play       |
     | 4     | Mouse-Over          |
     *</pre>
     */
    public static class PlaybackMethod extends SingleContainerInt {

        public static PlaybackMethod AutoPlaySoundOn = new PlaybackMethod(1);
        public static PlaybackMethod AutoPlaySoundOff = new PlaybackMethod(2);
        public static PlaybackMethod ClickToPlay = new PlaybackMethod(3);
        public static PlaybackMethod MouseOver = new PlaybackMethod(4);

        public PlaybackMethod(int value) {
            super(value);
        }
    }

    /**
     OpenRTB
     * <pre>
     | Value | Description      |
     |-------|------------------|
     | 1     | VAST 1.0         |
     | 2     | VAST 2.0         |
     | 3     | VAST 3.0         |
     | 4     | VAST 1.0 Wrapper |
     | 5     | VAST 2.0 Wrapper |
     | 6     | VAST 3.0 Wrapper |
     *</pre>
     */
    public static class Protocols extends SingleContainerInt {

        public static Protocols VAST_1_0 = new Protocols(1);
        public static Protocols VAST_2_0 = new Protocols(2);
        public static Protocols VAST_3_0 = new Protocols(3);
        public static Protocols VAST_1_0_Wrapper = new Protocols(4);
        public static Protocols VAST_2_0_Wrapper = new Protocols(5);
        public static Protocols VAST_3_0_Wrapper = new Protocols(6);

        public Protocols(int value) {
            super(value);
        }
    }

    /**
     OpenRTB
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

        public static StartDelay PreRoll = new StartDelay(0);
        public static StartDelay GenericMidRoll = new StartDelay(-1);
        public static StartDelay GenericPostRoll = new StartDelay(-2);

        public StartDelay(int value) {
            super(value);
        }
    }
}
