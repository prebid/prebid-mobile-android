/*
 *    Copyright 2020-2026 Prebid.org, Inc.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.eid.ExtendedId;
import org.prebid.mobile.api.eid.ExtendedIdProvider;
import org.prebid.mobile.api.plugin.PluginCapability;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.RecordingLogger;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class ExtendedIdRegistryTest {

    private ExtendedIdRegistry registry;
    private RecordingLogger logger;

    @Before
    public void setUp() {
        PluginCapability.EXTENDED_IDS.getRegistry().removeAll();
        registry = new ExtendedIdRegistry();
        logger = new RecordingLogger(LogUtil.WARN).install();
    }

    @After
    public void tearDown() {
        logger.uninstall();
        PluginCapability.EXTENDED_IDS.getRegistry().removeAll();
    }

    // --- Registration through the plugin registry ---

    @Test
    public void capabilityName() {
        assertEquals("extendedIds", PluginCapability.EXTENDED_IDS.getName());
    }

    @Test
    public void registeredProviderIdsAreIncluded() {
        ExternalUserId userId = createUserId("source1", "id1");

        PrebidMobile.registerPlugin(new FakeProvider("p1", userId), PluginCapability.EXTENDED_IDS);

        List<ExtendedId> extendedIds = registry.getAllExtendedIds(null);
        assertEquals(1, extendedIds.size());
        assertSame(userId, extendedIds.get(0));
    }

    @Test
    public void getAllExtendedIdsCallsProviderOnEveryRequest() {
        FakeProvider provider = new FakeProvider("p1", createUserId("source1", "id1"));
        PrebidMobile.registerPlugin(provider, PluginCapability.EXTENDED_IDS);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");
        provider.setIds(createUserId("source1", "id2"));
        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id2");
        assertEquals(2, provider.getExtendedIdsCount);
    }

    @Test
    public void registerCallsOnRegister() {
        FakeProvider provider = new FakeProvider("p1");

        PrebidMobile.registerPlugin(provider, PluginCapability.EXTENDED_IDS);

        assertEquals(1, provider.onRegisterCount);
        assertEquals(0, provider.onUnregisterCount);
        assertTrue(PrebidMobile.containsPlugin(provider, PluginCapability.EXTENDED_IDS));
    }

    @Test
    public void providerIsNotActiveUntilOnRegisterReturns() {
        AtomicReference<List<ExtendedId>> idsDuringOnRegister = new AtomicReference<>();
        AtomicReference<Boolean> containedDuringOnRegister = new AtomicReference<>();
        FakeProvider provider = new FakeProvider("p1", createUserId("source1", "id1"));
        provider.onRegisterAction = () -> {
            idsDuringOnRegister.set(registry.getAllExtendedIds(null));
            containedDuringOnRegister.set(PrebidMobile.containsPlugin(provider, PluginCapability.EXTENDED_IDS));
        };

        PrebidMobile.registerPlugin(provider, PluginCapability.EXTENDED_IDS);

        assertThat(idsDuringOnRegister.get()).isEmpty();
        assertEquals(Boolean.FALSE, containedDuringOnRegister.get());
        assertEquals(0, provider.getExtendedIdsCount);
        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");
        assertTrue(PrebidMobile.containsPlugin(provider, PluginCapability.EXTENDED_IDS));
    }

    @Test
    public void providerWhoseOnRegisterThrowsIsNotRegistered() {
        FakeProvider provider = new FakeProvider("p1", createUserId("source1", "id1"));
        provider.onRegisterAction = () -> {
            throw new IllegalStateException("boom");
        };

        PrebidMobile.registerPlugin(provider, PluginCapability.EXTENDED_IDS);

        assertFalse(PrebidMobile.containsPlugin(provider, PluginCapability.EXTENDED_IDS));
        assertThat(registry.getAllExtendedIds(null)).isEmpty();

        // The identity is free again, so a retry registers the provider.
        provider.onRegisterAction = null;
        PrebidMobile.registerPlugin(provider, PluginCapability.EXTENDED_IDS);
        assertTrue(PrebidMobile.containsPlugin(provider, PluginCapability.EXTENDED_IDS));
        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");
    }

    @Test
    public void duplicateProviderInfoIsIgnored() {
        FakeProvider provider1 = new FakeProvider("p1", createUserId("source1", "id1"));
        FakeProvider provider2 = new FakeProvider("p1", createUserId("source2", "id2"));

        PrebidMobile.registerPlugin(provider1, PluginCapability.EXTENDED_IDS);
        PrebidMobile.registerPlugin(provider2, PluginCapability.EXTENDED_IDS);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");
        assertEquals(0, provider2.onRegisterCount);
    }

    @Test
    public void providersWithSameNameAndDifferentVersionsAreDistinct() {
        PrebidMobile.registerPlugin(new FakeProvider("p1", "1.0", createUserId("source1", "id1")), PluginCapability.EXTENDED_IDS);
        PrebidMobile.registerPlugin(new FakeProvider("p1", "2.0", createUserId("source2", "id2")), PluginCapability.EXTENDED_IDS);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1", "id2");
    }

    @Test
    public void unregisterCallsOnUnregisterAndDropsItsIds() {
        FakeProvider provider = new FakeProvider("p1", createUserId("source1", "id1"));
        PrebidMobile.registerPlugin(provider, PluginCapability.EXTENDED_IDS);

        PrebidMobile.unregisterPlugin(provider, PluginCapability.EXTENDED_IDS);

        assertEquals(1, provider.onUnregisterCount);
        assertFalse(PrebidMobile.containsPlugin(provider, PluginCapability.EXTENDED_IDS));
        assertThat(registry.getAllExtendedIds(null)).isEmpty();
    }

    @Test
    public void unregisterUnknownProviderIsNoOp() {
        FakeProvider provider = new FakeProvider("p1");

        PrebidMobile.unregisterPlugin(provider, PluginCapability.EXTENDED_IDS);

        assertEquals(0, provider.onUnregisterCount);
    }

    @Test
    public void unregisterProviderWithEqualInfoRemovesRegisteredInstance() {
        FakeProvider registered = new FakeProvider("p1", createUserId("source1", "id1"));
        FakeProvider other = new FakeProvider("p1");
        PrebidMobile.registerPlugin(registered, PluginCapability.EXTENDED_IDS);

        PrebidMobile.unregisterPlugin(other, PluginCapability.EXTENDED_IDS);

        assertEquals(1, registered.onUnregisterCount);
        assertEquals(0, other.onUnregisterCount);
        assertFalse(PrebidMobile.containsPlugin(registered, PluginCapability.EXTENDED_IDS));
        assertThat(registry.getAllExtendedIds(null)).isEmpty();
    }

    @Test
    public void unregisterDoesNotAffectOtherProviders() {
        FakeProvider provider1 = new FakeProvider("p1", createUserId("source1", "id1"));
        PrebidMobile.registerPlugin(provider1, PluginCapability.EXTENDED_IDS);
        PrebidMobile.registerPlugin(new FakeProvider("p2", createUserId("source2", "id2")), PluginCapability.EXTENDED_IDS);

        PrebidMobile.unregisterPlugin(provider1, PluginCapability.EXTENDED_IDS);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id2");
    }

    @Test
    public void providerWithoutLifecycleMethods() {
        ExtendedIdProvider provider = new ExtendedIdProvider() {
            @NonNull
            @Override
            public Info getProviderInfo() {
                return new Info("minimal", "1.0");
            }

            @NonNull
            @Override
            public List<ExtendedId> getExtendedIds() {
                return Collections.singletonList(createUserId("source1", "id1"));
            }
        };

        PrebidMobile.registerPlugin(provider, PluginCapability.EXTENDED_IDS);
        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");

        PrebidMobile.unregisterPlugin(provider, PluginCapability.EXTENDED_IDS);
        assertThat(registry.getAllExtendedIds(null)).isEmpty();
    }

    // --- Misbehaving providers ---

    @Test
    public void providerThatThrowsIsSkipped() {
        FakeProvider throwing = new FakeProvider("throwing");
        throwing.throwOnGetExtendedIds = true;
        PrebidMobile.registerPlugin(throwing, PluginCapability.EXTENDED_IDS);
        PrebidMobile.registerPlugin(new FakeProvider("good", createUserId("good.com", "good")), PluginCapability.EXTENDED_IDS);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("good");
        assertTrue(logContains("Skipping the IDs of throwing/1.0: its getExtendedIds() threw"));
    }

    @Test
    public void nullListAndNullElementsAreSkipped() {
        FakeProvider nullList = new FakeProvider("nullList");
        nullList.ids = null;
        FakeProvider nullElement = new FakeProvider("nullElement");
        nullElement.ids = Arrays.asList(null, createUserId("source1", "id1"));
        PrebidMobile.registerPlugin(nullList, PluginCapability.EXTENDED_IDS);
        PrebidMobile.registerPlugin(nullElement, PluginCapability.EXTENDED_IDS);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");
    }

    @Test
    public void idWhoseGetSourceThrowsIsSkipped() {
        ExtendedId broken = new ExtendedId() {
            @NonNull
            @Override
            public String getSource() {
                throw new IllegalStateException("boom");
            }

            @Override
            public JSONObject getJson() {
                return new JSONObject();
            }
        };
        PrebidMobile.registerPlugin(new FakeProvider("p1", broken, createUserId("source1", "id1")), PluginCapability.EXTENDED_IDS);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");
        assertTrue(logContains("Skipping an ID provided by p1/1.0: its getSource() threw"));
    }

    // --- Order and sources ---

    @Test
    public void staticIdsComeFirstThenSharedIdThenProvidersInRegistrationOrder() {
        PrebidMobile.registerPlugin(new FakeProvider("p2", createUserId("p2.com", "p2")), PluginCapability.EXTENDED_IDS);
        PrebidMobile.registerPlugin(new FakeProvider("p1", createUserId("p1.com", "p1")), PluginCapability.EXTENDED_IDS);
        registry.staticProvider.addUserId(createUserId("static.com", "static"));

        List<ExtendedId> extendedIds = registry.getAllExtendedIds(createUserId("pubcid.org", "shared"));

        assertThat(sources(extendedIds)).containsExactly("static.com", "pubcid.org", "p2.com", "p1.com");
    }

    @Test
    public void providerIdWithSourceAlreadyPresentIsSkipped() {
        registry.staticProvider.addUserId(createUserId("static.com", "static"));
        PrebidMobile.registerPlugin(new FakeProvider(
                "first",
                createUserId("static.com", "first-static"),
                createUserId("vendor.com", "first-vendor"),
                createUserId("pubcid.org", "first-shared")
        ), PluginCapability.EXTENDED_IDS);
        PrebidMobile.registerPlugin(new FakeProvider(
                "second",
                createUserId("vendor.com", "second-vendor"),
                createUserId("second.com", "second")
        ), PluginCapability.EXTENDED_IDS);

        List<ExtendedId> extendedIds = registry.getAllExtendedIds(createUserId("pubcid.org", "shared"));

        assertThat(ids(extendedIds)).containsExactly("static", "shared", "first-vendor", "second");
        assertTrue(logContains("Skipping the ID from source static.com provided by first/1.0"));
        assertTrue(logContains("Skipping the ID from source pubcid.org provided by first/1.0"));
        assertTrue(logContains("Skipping the ID from source vendor.com provided by second/1.0"));
    }

    @Test
    public void providerIdWithSourceOfItsOwnEarlierIdIsSkipped() {
        PrebidMobile.registerPlugin(new FakeProvider(
                "p1",
                createUserId("source1", "id1"),
                createUserId("source1", "id2")
        ), PluginCapability.EXTENDED_IDS);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");
    }

    @Test
    public void staticIdAndSharedIdWithTheSameSourceAreBothKept() {
        registry.staticProvider.addUserId(createUserId("pubcid.org", "static"));

        List<ExtendedId> extendedIds = registry.getAllExtendedIds(createUserId("pubcid.org", "shared"));

        assertThat(ids(extendedIds)).containsExactly("static", "shared");
        assertThat(logger.getMessages()).isEmpty();
    }

    // --- Static IDs ---

    @Test
    public void addUserIdReplacesIdWithSameSource() {
        registry.staticProvider.addUserId(createUserId("source1", "id1"));
        registry.staticProvider.addUserId(createUserId("source2", "id2"));
        registry.staticProvider.addUserId(createUserId("source1", "id3"));

        assertThat(ids(registry.getAllExtendedIds(null))).containsOnly("id3", "id2");
    }

    @Test
    public void removeUserIdRemovesOnlyThatSource() {
        registry.staticProvider.addUserId(createUserId("source1", "id1"));
        registry.staticProvider.addUserId(createUserId("source2", "id2"));

        registry.staticProvider.removeUserId("source1");

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id2");
    }

    @Test
    public void setUserIdsReplacesAllAndSkipsNullElements() {
        registry.staticProvider.addUserId(createUserId("source1", "id1"));

        registry.staticProvider.setUserIds(Arrays.asList(createUserId("source2", "id2"), null, createUserId("source3", "id3")));

        assertThat(ids(registry.getAllExtendedIds(null))).containsOnly("id2", "id3");

        registry.staticProvider.setUserIds(null);
        assertThat(registry.getAllExtendedIds(null)).isEmpty();
    }

    @Test
    public void setUserIdsDoesNotAffectProviderIds() {
        PrebidMobile.registerPlugin(new FakeProvider("p1", createUserId("provider.com", "id1")), PluginCapability.EXTENDED_IDS);
        registry.staticProvider.setUserIds(Collections.singletonList(createUserId("static.com", "id2")));

        registry.staticProvider.setUserIds(null);

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("id1");
    }

    @Test
    public void unregisteringEveryProviderKeepsStaticIds() {
        registry.staticProvider.addUserId(createUserId("static.com", "static"));
        PrebidMobile.registerPlugin(new FakeProvider("p1", createUserId("provider.com", "provider")), PluginCapability.EXTENDED_IDS);

        PluginCapability.EXTENDED_IDS.getRegistry().removeAll();

        assertThat(ids(registry.getAllExtendedIds(null))).containsExactly("static");
    }

    // --- ExtendedIdProvider.Info ---

    @Test
    public void providerInfoEquality() {
        ExtendedIdProvider.Info info = new ExtendedIdProvider.Info("test", "1.0");

        assertEquals(new ExtendedIdProvider.Info("test", "1.0"), info);
        assertEquals(new ExtendedIdProvider.Info("test", "1.0").hashCode(), info.hashCode());
        assertNotEquals(new ExtendedIdProvider.Info("test", "2.0"), info);
        assertNotEquals(new ExtendedIdProvider.Info("other", "1.0"), info);
        assertEquals("test/1.0", info.toString());
    }

    // --- Helpers ---

    private boolean logContains(String text) {
        for (String message : logger.getMessages()) {
            if (message.contains(text)) {
                return true;
            }
        }
        return false;
    }

    private static ExternalUserId createUserId(String source, String id) {
        return new ExternalUserId(source, Collections.singletonList(new ExternalUserId.UniqueId(id, 1)));
    }

    private static List<String> ids(List<ExtendedId> extendedIds) {
        List<String> result = new ArrayList<>();
        for (ExtendedId extendedId : extendedIds) {
            result.add(((ExternalUserId) extendedId).getUniqueIds().get(0).getId());
        }
        return result;
    }

    private static List<String> sources(List<ExtendedId> extendedIds) {
        List<String> result = new ArrayList<>();
        for (ExtendedId extendedId : extendedIds) {
            result.add(extendedId.getSource());
        }
        return result;
    }

    private static class FakeProvider implements ExtendedIdProvider {

        private final Info info;
        List<ExtendedId> ids;
        Runnable onRegisterAction;
        boolean throwOnGetExtendedIds;
        int onRegisterCount;
        int onUnregisterCount;
        int getExtendedIdsCount;

        FakeProvider(String name, ExtendedId... ids) {
            this(name, "1.0", ids);
        }

        FakeProvider(String name, String version, ExtendedId... ids) {
            this.info = new Info(name, version);
            this.ids = Arrays.asList(ids);
        }

        void setIds(ExtendedId... ids) {
            this.ids = Arrays.asList(ids);
        }

        @NonNull
        @Override
        public Info getProviderInfo() {
            return info;
        }

        @NonNull
        @Override
        public List<ExtendedId> getExtendedIds() {
            getExtendedIdsCount++;
            if (throwOnGetExtendedIds) {
                throw new IllegalStateException("boom");
            }
            return ids;
        }

        @Override
        public void onRegister() {
            onRegisterCount++;
            if (onRegisterAction != null) {
                onRegisterAction.run();
            }
        }

        @Override
        public void onUnregister() {
            onUnregisterCount++;
        }
    }
}
