package org.prebid.mobile;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Video parameters for requesting ad.
 * Describes an <a href="https://www.iab.com/wp-content/uploads/2016/03/OpenRTB-API-Specification-Version-2-5-FINAL.pdf">OpenRTB</a> video object
 */
public class VideoParameters {

    /**
     * @param mimes - supported content MIME types (required parameter)
     *              "video/mp4"
     *              "video/x-ms-wmv"
     */
    public VideoParameters(List<String> mimes) {
        this.mimes = mimes;
    }

    /**
     * List of supported API frameworks for this impression. If an API is not explicitly listed, it is assumed not to be supported.
     */
    @Nullable
    private List<Signals.Api> api;

    /**
     * Maximum bit rate in Kbps.
     */
    @Nullable
    private Integer maxBitrate;

    /**
     * Maximum bit rate in Kbps.
     */
    @Nullable
    private Integer minBitrate;

    /**
     * Maximum video ad duration in seconds.
     */
    @Nullable
    private Integer maxDuration;

    /**
     * Minimum video ad duration in seconds.
     */
    @Nullable
    private Integer minDuration;

    /**
     * Content MIME types supported
     * <p>
     * # Example #
     * "video/mp4"
     * "video/x-ms-wmv"
     */
    private List<String> mimes;

    /**
     * Allowed playback methods. If none specified, assume all are allowed.
     */
    @Nullable
    private List<Signals.PlaybackMethod> playbackMethod;

    /**
     * Array of supported video bid response protocols.
     */
    @Nullable
    private List<Signals.Protocols> protocols;

    /**
     * Indicates the start delay in seconds for pre-roll, mid-roll, or post-roll ad placements.
     */
    @Nullable
    private Signals.StartDelay startDelay;

    /**
     * Placement type for the impression.
     */
    @Nullable
    private Signals.Placement placement;

    /**
     * Placement type for video content.
     */
    @Nullable
    private Signals.Plcmt plcmt;

    /**
     * Placement type for the impression.
     */
    @Nullable
    private Integer linearity;

    @Nullable
    private AdSize adSize;

    /**
     * Array of blocked creative attributes.
     */
    @Nullable
    private List<Signals.CreativeAttribute> battr;

    @Nullable
    private Boolean skippable;

    //Getters and setters
    @Nullable
    public List<Signals.Api> getApi() {
        return api;
    }

    public void setApi(@Nullable List<Signals.Api> api) {
        this.api = api;
    }

    @Nullable
    public Integer getMaxBitrate() {
        return maxBitrate;
    }

    public void setMaxBitrate(@Nullable Integer maxBitrate) {
        this.maxBitrate = maxBitrate;
    }

    @Nullable
    public Integer getMinBitrate() {
        return minBitrate;
    }

    public void setMinBitrate(@Nullable Integer minBitrate) {
        this.minBitrate = minBitrate;
    }

    @Nullable
    public Integer getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(@Nullable Integer maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Nullable
    public Integer getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(@Nullable Integer minDuration) {
        this.minDuration = minDuration;
    }

    @Nullable
    public List<String> getMimes() {
        return mimes;
    }

    @Nullable
    public List<Signals.PlaybackMethod> getPlaybackMethod() {
        return playbackMethod;
    }

    public void setPlaybackMethod(@Nullable List<Signals.PlaybackMethod> playbackMethod) {
        this.playbackMethod = playbackMethod;
    }

    @Nullable
    public List<Signals.Protocols> getProtocols() {
        return protocols;
    }

    public void setProtocols(@Nullable List<Signals.Protocols> protocols) {
        this.protocols = protocols;
    }

    @Nullable
    public Signals.StartDelay getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(@Nullable Signals.StartDelay startDelay) {
        this.startDelay = startDelay;
    }

    @Nullable
    public Signals.Placement getPlacement() {
        return placement;
    }

    public void setPlacement(@Nullable Signals.Placement placement) {
        this.placement = placement;
    }

    @Nullable
    public Integer getLinearity() {
        return linearity;
    }

    public void setLinearity(@Nullable Integer linearity) {
        this.linearity = linearity;
    }

    @Nullable
    public AdSize getAdSize() {
        return adSize;
    }

    public void setAdSize(@Nullable AdSize adSize) {
        this.adSize = adSize;
    }

    @Nullable
    public Signals.Plcmt getPlcmt() {
        return plcmt;
    }

    /**
     * Sets {@link org.prebid.mobile.Signals.Plcmt}.
     */
    public void setPlcmt(@Nullable Signals.Plcmt plcmt) {
        this.plcmt = plcmt;
    }

    @Nullable
    public List<Signals.CreativeAttribute> getBattr() {
        return battr;
    }

    /**
     * Sets {@link org.prebid.mobile.Signals.CreativeAttribute}.
     */
    public void setBattr(@Nullable List<Signals.CreativeAttribute> battr) {
        if (battr != null) {
            HashSet<Signals.CreativeAttribute> set = new HashSet<>(battr);
            this.battr = new ArrayList<>(set);
        } else {
            this.battr = null;
        }
    }

    @Nullable
    public Boolean getSkippable() {
        return skippable;
    }

    /**
     * Indicates if the player will allow the video to be skipped, where 0 = no, 1 = yes.
     */
    public void setSkippable(@Nullable Boolean skippable) {
        this.skippable = skippable;
    }
}