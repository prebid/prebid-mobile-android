/**
 * Extended ID providers for OpenRTB auction requests.
 * <p>
 * Implement {@link org.prebid.mobile.api.eid.ExtendedIdProvider} for any EID source, synchronous
 * or asynchronous, and register it with
 * {@link org.prebid.mobile.PrebidMobile#registerPlugin(Object, org.prebid.mobile.api.plugin.PluginCapability)}
 * as {@link org.prebid.mobile.api.plugin.PluginCapability#EXTENDED_IDS}.
 */
package org.prebid.mobile.api.eid;
