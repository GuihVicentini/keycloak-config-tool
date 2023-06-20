package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.RequiredActionsConfigMapperImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.when;


public class RequiredActionExportServiceTest {

    private final RequiredActionsConfigMapperImpl mapper = new RequiredActionsConfigMapperImpl();

    private RequiredActionExportService target;

    private AuthenticationManagementResourceAdapter adapter;

    @BeforeEach
    public void setup() {
        adapter = Mockito.mock(AuthenticationManagementResourceAdapter.class);
        var resource1 = new RequiredActionProviderRepresentation();
        resource1.setAlias("Alias1");
        resource1.setName("Name1");
        resource1.setProviderId("ProviderId1");
        resource1.setEnabled(true);
        resource1.setDefaultAction(true);
        resource1.setPriority(1);
        resource1.setConfig(Map.of("key1", "value1"));
        var resource2 = new RequiredActionProviderRepresentation();
        resource2.setAlias("Alias2");
        resource2.setName("Name2");
        resource2.setProviderId("ProviderId2");
        resource2.setEnabled(false);
        resource2.setDefaultAction(false);
        resource2.setPriority(2);
        resource2.setConfig(Map.of("key2", "value2"));

        when(adapter.getRequiredActions(Mockito.any())).thenReturn(
                Arrays.asList(resource1, resource2)
        );
        target = new RequiredActionExportService(mapper, adapter);
    }

    @Test
    public void getAll_whenAdapterReturnsNull_thenResultContainsZeroItems(){
        when(adapter.getRequiredActions(Mockito.any())).thenReturn(null);

        var result = target.getAll("test");

        Assertions.assertEquals(0, result.size());
    }


    @Test
    public void getAll_whenAdapterReturnsZeroItems_thenResultContainsZeroItems(){
        when(adapter.getRequiredActions(Mockito.any())).thenReturn(new ArrayList<>());

        var result = target.getAll("test");

        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void getAll_whenAdapterReturnsTwoItems_thenResultContainsTwoItems(){
        var result = target.getAll("test");

        Assertions.assertEquals("Alias1", result.get(0).identifier());
        Assertions.assertEquals("Alias1", result.get(0).getAlias());
        Assertions.assertEquals("Name1", result.get(0).getName());
        Assertions.assertEquals("ProviderId1", result.get(0).getProviderId());
        Assertions.assertEquals(true, result.get(0).isEnabled());
        Assertions.assertEquals(true, result.get(0).isDefaultAction());
        Assertions.assertEquals(1, result.get(0).getPriority());
        Assertions.assertEquals(Map.of("key1", "value1"),  result.get(0).getConfig());

        Assertions.assertEquals("Alias2", result.get(1).identifier());
        Assertions.assertEquals("Alias2", result.get(1).getAlias());
        Assertions.assertEquals("Name2", result.get(1).getName());
        Assertions.assertEquals("ProviderId2", result.get(1).getProviderId());
        Assertions.assertEquals(false, result.get(1).isEnabled());
        Assertions.assertEquals(false, result.get(1).isDefaultAction());
        Assertions.assertEquals(2, result.get(1).getPriority());
        Assertions.assertEquals(Map.of("key2", "value2"),  result.get(1).getConfig());
    }


}
