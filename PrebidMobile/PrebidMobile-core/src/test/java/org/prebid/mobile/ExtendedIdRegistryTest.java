package org.prebid.mobile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.eid.ExtendedIdProvider;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class ExtendedIdRegistryTest {

    private ExtendedIdRegistry registry;

    @Before
    public void setUp() {
        registry = ExtendedIdRegistry.getInstance();
        registry.clearProviders();
    }

    @After
    public void tearDown() {
        registry.clearProviders();
    }

    // --- Provider management ---

    @Test
    public void addProvider_includedInGetAllExtendedIds() {
        ExternalUserId userId = createUserId("source1", "id1");

        registry.addProvider(mockProvider("test", userId));

        assertThat(registry.getAllExtendedIds()).containsExactly(userId);
    }

    @Test
    public void getAllExtendedIds_callsGetExtendedIdsOnDemand() {
        ExternalUserId id1 = createUserId("source1", "id1");
        ExternalUserId id2 = createUserId("source1", "id2");
        ExtendedIdProvider provider = mockProvider("test");
        when(provider.getExtendedIds())
                .thenReturn(List.of(id1))
                .thenReturn(List.of(id2));
        registry.addProvider(provider);

        assertThat(registry.getAllExtendedIds()).containsExactly(id1);
        assertThat(registry.getAllExtendedIds()).containsExactly(id2);
    }

    @Test
    public void addProvider_duplicateSameInfoIgnored() {
        ExtendedIdProvider provider1 = mockProvider("test", createUserId("s1", "id1"));
        ExtendedIdProvider provider2 = mockProvider("test", createUserId("s2", "id2"));

        registry.addProvider(provider1);
        registry.addProvider(provider2);

        assertThat(registry.getAllExtendedIds()).hasSize(1);
    }

    @Test
    public void addProvider_callsOnRegister() {
        ExtendedIdProvider provider = mockProvider("test");

        registry.addProvider(provider);

        verify(provider).onRegister();
    }

    @Test
    public void removeProvider_callsOnUnregister() {
        ExtendedIdProvider provider = mockProvider("test");
        registry.addProvider(provider);

        registry.removeProvider(provider);

        verify(provider).onUnregister();
    }

    @Test
    public void removeProvider_unknownProviderNoOp() {
        ExtendedIdProvider provider = mockProvider("test");

        registry.removeProvider(provider);

        verify(provider, never()).onUnregister();
    }

    @Test
    public void removeProvider_doesNotAffectOtherProviders() {
        ExternalUserId userId2 = createUserId("source2", "id2");
        ExtendedIdProvider provider1 = mockProvider("p1", createUserId("source1", "id1"));
        registry.addProvider(provider1);
        registry.addProvider(mockProvider("p2", userId2));

        registry.removeProvider(provider1);

        assertThat(registry.getAllExtendedIds()).containsExactly(userId2);
    }

    @Test
    public void multipleProviders_aggregateEids() {
        ExternalUserId id1 = createUserId("source1", "id1");
        ExternalUserId id2 = createUserId("source2", "id2");
        registry.addProvider(mockProvider("p1", id1));
        registry.addProvider(mockProvider("p2", id2));

        assertThat(registry.getAllExtendedIds()).containsOnly(id1, id2);
    }

    @Test
    public void getAllExtendedIds_providerThrows_isSkipped() {
        ExternalUserId good = createUserId("good", "id");
        ExtendedIdProvider throwing = mock(ExtendedIdProvider.class);
        when(throwing.getProviderInfo()).thenReturn(new ExtendedIdProvider.Info("throwing", "1.0"));
        when(throwing.getExtendedIds()).thenThrow(new RuntimeException("boom"));

        registry.addProvider(throwing);
        registry.addProvider(mockProvider("good", good));

        assertThat(registry.getAllExtendedIds()).containsExactly(good);
    }

    @Test
    public void getAllExtendedIds_providerReturnsNull_isSkipped() {
        ExtendedIdProvider nullProvider = mock(ExtendedIdProvider.class);
        when(nullProvider.getProviderInfo()).thenReturn(new ExtendedIdProvider.Info("nullp", "1.0"));
        when(nullProvider.getExtendedIds()).thenReturn(null);

        registry.addProvider(nullProvider);

        assertThat(registry.getAllExtendedIds()).isEmpty();
    }

    @Test
    public void clearProviders_callsOnUnregisterOnAll() {
        ExtendedIdProvider p1 = mockProvider("p1");
        ExtendedIdProvider p2 = mockProvider("p2");
        registry.addProvider(p1);
        registry.addProvider(p2);

        registry.clearProviders();

        verify(p1).onUnregister();
        verify(p2).onUnregister();
    }

    @Test
    public void clearProviders_removesAllEids() {
        registry.addProvider(mockProvider("p1", createUserId("source1", "id1")));
        registry.addProvider(mockProvider("p2", createUserId("source2", "id2")));

        registry.clearProviders();

        assertThat(registry.getAllExtendedIds()).isEmpty();
    }

    // --- Static EIDs ---

    @Test
    public void setStaticExternalUserIds_addsToRegistry() {
        ExternalUserId userId = createUserId("source1", "id1");

        registry.setStaticExternalUserIds(Collections.singletonList(userId));

        assertThat(registry.getAllExtendedIds()).containsExactly(userId);
    }

    @Test
    public void setStaticExternalUserIds_multipleIds() {
        ExternalUserId userId1 = createUserId("source1", "id1");
        ExternalUserId userId2 = createUserId("source2", "id2");

        registry.setStaticExternalUserIds(Arrays.asList(userId1, userId2));

        assertThat(registry.getAllExtendedIds()).containsOnly(userId1, userId2);
    }

    @Test
    public void setStaticExternalUserIds_replacesExisting() {
        ExternalUserId userId2 = createUserId("source2", "id2");
        registry.setStaticExternalUserIds(Collections.singletonList(createUserId("source1", "id1")));

        registry.setStaticExternalUserIds(Collections.singletonList(userId2));

        assertThat(registry.getAllExtendedIds()).containsExactly(userId2);
    }

    @Test
    public void setStaticExternalUserIds_nullClearsIds() {
        registry.setStaticExternalUserIds(Collections.singletonList(createUserId("source1", "id1")));

        registry.setStaticExternalUserIds(null);

        assertThat(registry.getAllExtendedIds()).isEmpty();
    }

    @Test
    public void setStaticExternalUserIds_skipsNullElements() {
        ExternalUserId userId1 = createUserId("source1", "id1");
        ExternalUserId userId2 = createUserId("source2", "id2");

        registry.setStaticExternalUserIds(Arrays.asList(userId1, null, userId2));

        assertThat(registry.getAllExtendedIds()).containsOnly(userId1, userId2);
    }

    @Test
    public void clearProviders_clearsStaticIds() {
        registry.setStaticExternalUserIds(Collections.singletonList(createUserId("source1", "id1")));

        registry.clearProviders();

        assertThat(registry.getAllExtendedIds()).isEmpty();
    }

    @Test
    public void staticIds_coexistWithProviderIds() {
        ExternalUserId staticId = createUserId("static-source", "id1");
        ExternalUserId providerId = createUserId("provider-source", "id2");
        registry.setStaticExternalUserIds(Collections.singletonList(staticId));
        registry.addProvider(mockProvider("test", providerId));

        assertThat(registry.getAllExtendedIds()).containsOnly(staticId, providerId);
    }

    @Test
    public void setStaticExternalUserIds_doesNotAffectProviderIds() {
        ExternalUserId providerId = createUserId("provider-source", "id1");
        registry.addProvider(mockProvider("test", providerId));
        registry.setStaticExternalUserIds(Collections.singletonList(createUserId("static-source", "id2")));

        registry.setStaticExternalUserIds(null);

        assertThat(registry.getAllExtendedIds()).containsExactly(providerId);
    }

    // --- Helpers ---

    private ExternalUserId createUserId(String source, String id) {
        return new ExternalUserId(
                source,
                Collections.singletonList(new ExternalUserId.UniqueId(id, 1))
        );
    }

    private ExtendedIdProvider mockProvider(String name) {
        return mockProvider(name, null);
    }

    private ExtendedIdProvider mockProvider(String name, ExternalUserId userId) {
        ExtendedIdProvider provider = mock(ExtendedIdProvider.class);
        when(provider.getProviderInfo()).thenReturn(new ExtendedIdProvider.Info(name, "1.0"));
        when(provider.getExtendedIds()).thenReturn(
                userId != null ? List.of(userId) : Collections.emptyList()
        );
        return provider;
    }
}
