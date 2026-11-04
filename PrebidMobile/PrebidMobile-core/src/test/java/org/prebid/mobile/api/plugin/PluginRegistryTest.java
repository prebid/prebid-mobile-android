/*
 *    Copyright 2018-2026 Prebid.org, Inc.
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

package org.prebid.mobile.api.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PluginRegistryTest {

    private PluginRegistry<MockPlugin> registry;

    @Before
    public void setUp() {
        registry = makeRegistry(PluginRegistry.UNLIMITED);
    }

    // add

    @Test
    public void add_activatesPluginsInRegistrationOrder() {
        MockPlugin first = new MockPlugin("first");
        MockPlugin second = new MockPlugin("second");
        MockPlugin third = new MockPlugin("third");

        registry.add(first);
        registry.add(second);
        registry.add(third);

        assertEquals(Arrays.asList("first", "second", "third"), names(registry.getActive()));
        assertTrue(registry.contains(second));
    }

    @Test
    public void add_callsOnRegisterOnce() {
        MockPlugin plugin = new MockPlugin("plugin");

        registry.add(plugin);

        assertEquals(Collections.singletonList("onRegister"), plugin.getEvents());
    }

    @Test
    public void add_pluginIsNotActiveUntilOnRegisterReturns() {
        MockPlugin plugin = new MockPlugin("plugin");
        boolean[] isActiveInOnRegister = new boolean[1];
        boolean[] isContainedInOnRegister = new boolean[1];
        plugin.onRegisterAction = () -> {
            isActiveInOnRegister[0] = registry.getActive().contains(plugin);
            isContainedInOnRegister[0] = registry.contains(plugin);
        };

        registry.add(plugin);

        assertFalse(isActiveInOnRegister[0]);
        assertFalse(isContainedInOnRegister[0]);
        assertTrue(registry.contains(plugin));
    }

    @Test
    public void add_equalInfo_keepsFirstPlugin() {
        MockPlugin first = new MockPlugin("plugin", "1.0");
        MockPlugin duplicate = new MockPlugin("plugin", "1.0");

        registry.add(first);
        registry.add(duplicate);

        assertEquals(1, registry.getActive().size());
        assertSame(first, registry.getActive().get(0));
        assertTrue(duplicate.getEvents().isEmpty());
    }

    @Test
    public void add_sameNameOtherVersion_registersBoth() {
        registry.add(new MockPlugin("plugin", "1.0"));
        registry.add(new MockPlugin("plugin", "2.0"));

        List<String> versions = new ArrayList<>();
        for (MockPlugin plugin : registry.getActive()) {
            versions.add(plugin.info.getVersion());
        }
        assertEquals(Arrays.asList("1.0", "2.0"), versions);
    }

    @Test
    public void add_samePluginTwice_registersOnce() {
        MockPlugin plugin = new MockPlugin("plugin");

        registry.add(plugin);
        registry.add(plugin);

        assertEquals(1, registry.getActive().size());
        assertEquals(Collections.singletonList("onRegister"), plugin.getEvents());
    }

    @Test
    public void add_capacityReached_ignoresPlugin() {
        registry = makeRegistry(1);
        MockPlugin first = new MockPlugin("first");
        MockPlugin second = new MockPlugin("second");

        registry.add(first);
        registry.add(second);

        assertEquals(Collections.singletonList("first"), names(registry.getActive()));
        assertTrue(second.getEvents().isEmpty());

        registry.remove(first);
        registry.add(second);

        assertEquals(Collections.singletonList("second"), names(registry.getActive()));
    }

    @Test
    public void add_onRegisterMayCallBackIntoRegistry() {
        MockPlugin plugin = new MockPlugin("plugin");
        MockPlugin other = new MockPlugin("other");
        plugin.onRegisterAction = () -> registry.add(other);

        registry.add(plugin);

        assertEquals(new HashSet<>(Arrays.asList("plugin", "other")), new HashSet<>(names(registry.getActive())));
    }

    @Test
    public void add_onRegisterThrows_rollsBackRegistration() {
        MockPlugin plugin = new MockPlugin("plugin");
        plugin.onRegisterAction = () -> {
            throw new IllegalStateException("onRegister failed");
        };

        registry.add(plugin);

        assertFalse(registry.contains(plugin));
        assertTrue(registry.getActive().isEmpty());
        // Rolled back, not only inactive: the same identity can register again.
        plugin.onRegisterAction = null;
        registry.add(plugin);
        assertTrue(registry.contains(plugin));
        assertEquals(Collections.singletonList("onRegister"), plugin.getEvents());
    }

    @Test
    public void add_assignsIncreasingSequenceAcrossRegistries() {
        PluginRegistry<MockPlugin> otherRegistry = makeRegistry(PluginRegistry.UNLIMITED);

        registry.add(new MockPlugin("a"));
        otherRegistry.add(new MockPlugin("b"));
        registry.add(new MockPlugin("c"));

        long a = registry.getActiveEntries().get(0).getSequence();
        long b = otherRegistry.getActiveEntries().get(0).getSequence();
        long c = registry.getActiveEntries().get(1).getSequence();

        assertTrue(a < b);
        assertTrue(b < c);
    }

    @Test
    public void getActiveEntries_haveTheInfoAndPlugin() {
        MockPlugin plugin = new MockPlugin("plugin", "1.0");

        registry.add(plugin);

        PluginRegistry.Entry<MockPlugin> entry = registry.getActiveEntries().get(0);
        assertEquals(new PrebidPluginInfo("plugin", "1.0"), entry.getInfo());
        assertSame(plugin, entry.getPlugin());
    }

    @Test
    public void getActive_returnsSnapshot() {
        registry.add(new MockPlugin("plugin"));

        List<MockPlugin> active = registry.getActive();
        active.clear();

        assertEquals(1, registry.getActive().size());
    }

    // remove

    @Test
    public void remove_callsOnUnregisterAndDeactivates() {
        MockPlugin plugin = new MockPlugin("plugin");
        registry.add(plugin);

        registry.remove(plugin);

        assertFalse(registry.contains(plugin));
        assertTrue(registry.getActive().isEmpty());
        assertEquals(Arrays.asList("onRegister", "onUnregister"), plugin.getEvents());
    }

    @Test
    public void remove_equalInfo_removesRegisteredPlugin() {
        MockPlugin registered = new MockPlugin("plugin");
        MockPlugin other = new MockPlugin("plugin");
        registry.add(registered);

        registry.remove(other);

        assertTrue(registry.getActive().isEmpty());
        assertEquals(Arrays.asList("onRegister", "onUnregister"), registered.getEvents());
        assertTrue(other.getEvents().isEmpty());
    }

    @Test
    public void remove_unknownPlugin_isNoOp() {
        MockPlugin registered = new MockPlugin("registered");
        MockPlugin unknown = new MockPlugin("unknown");
        registry.add(registered);

        registry.remove(unknown);

        assertEquals(1, registry.getActive().size());
        assertTrue(unknown.getEvents().isEmpty());
    }

    @Test
    public void remove_thenAdd_registersAgain() {
        MockPlugin plugin = new MockPlugin("plugin");
        registry.add(plugin);
        registry.remove(plugin);

        registry.add(plugin);

        assertTrue(registry.contains(plugin));
        assertEquals(Arrays.asList("onRegister", "onUnregister", "onRegister"), plugin.getEvents());
    }

    @Test
    public void remove_onUnregisterThrows_pluginIsRemoved() {
        MockPlugin plugin = new MockPlugin("plugin");
        plugin.onUnregisterAction = () -> {
            throw new IllegalStateException("onUnregister failed");
        };
        registry.add(plugin);

        registry.remove(plugin);

        assertFalse(registry.contains(plugin));
        assertTrue(registry.getActive().isEmpty());
    }

    @Test
    public void removeAll_removesEveryPlugin_evenIfOneOnUnregisterThrows() {
        MockPlugin first = new MockPlugin("first");
        MockPlugin throwing = new MockPlugin("throwing");
        MockPlugin last = new MockPlugin("last");
        throwing.onUnregisterAction = () -> {
            throw new IllegalStateException("onUnregister failed");
        };
        registry.add(first);
        registry.add(throwing);
        registry.add(last);

        registry.removeAll();

        assertTrue(registry.getActive().isEmpty());
        assertEquals(Arrays.asList("onRegister", "onUnregister"), first.getEvents());
        assertEquals(Arrays.asList("onRegister", "onUnregister"), last.getEvents());
    }

    // Concurrency

    @Test
    public void concurrentAdd_registersEveryPluginOnce() throws InterruptedException {
        List<MockPlugin> plugins = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            plugins.add(new MockPlugin("plugin-" + (i % 100)));
        }

        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        for (MockPlugin plugin : plugins) {
            executor.execute(() -> {
                try {
                    start.await();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                registry.add(plugin);
            });
        }
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        List<PluginRegistry.Entry<MockPlugin>> entries = registry.getActiveEntries();
        assertEquals(100, entries.size());
        Set<String> uniqueNames = new HashSet<>();
        List<Long> sequences = new ArrayList<>();
        for (PluginRegistry.Entry<MockPlugin> entry : entries) {
            uniqueNames.add(entry.getInfo().getName());
            sequences.add(entry.getSequence());
        }
        assertEquals(100, uniqueNames.size());
        List<Long> sorted = new ArrayList<>(sequences);
        Collections.sort(sorted);
        assertEquals(sorted, sequences);
    }

    // Helpers

    private static PluginRegistry<MockPlugin> makeRegistry(int capacity) {
        return new PluginRegistry<>(
                "test",
                plugin -> plugin.info,
                capacity,
                plugin -> {
                    if (plugin.onRegisterAction != null) {
                        plugin.onRegisterAction.run();
                    }
                    plugin.record("onRegister");
                },
                plugin -> {
                    plugin.record("onUnregister");
                    if (plugin.onUnregisterAction != null) {
                        plugin.onUnregisterAction.run();
                    }
                }
        );
    }

    private static List<String> names(List<MockPlugin> plugins) {
        List<String> names = new ArrayList<>();
        for (MockPlugin plugin : plugins) {
            names.add(plugin.info.getName());
        }
        return names;
    }

    private static final class MockPlugin {

        @NonNull
        final PrebidPluginInfo info;

        /**
         * Called from {@code onRegister}, before the event is recorded.
         */
        @Nullable
        volatile Runnable onRegisterAction;
        /**
         * Called from {@code onUnregister}, after the event is recorded.
         */
        @Nullable
        volatile Runnable onUnregisterAction;

        private final List<String> events = new ArrayList<>();

        MockPlugin(String name) {
            this(name, "1.0");
        }

        MockPlugin(String name, String version) {
            info = new PrebidPluginInfo(name, version);
        }

        synchronized List<String> getEvents() {
            return new ArrayList<>(events);
        }

        synchronized void record(String event) {
            events.add(event);
        }
    }
}
